/*
 * Copyright (c) 2004 - 2012 Eike Stepper (Loehne, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package promoter;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import promoter.util.Config;
import promoter.util.IO;
import promoter.util.XML;

/**
 * @author Eike Stepper
 */
public class BuildCopier extends PromoterComponent
{
  public BuildCopier()
  {
  }

  public List<PromotionCandidate> collectPromotionCandidates()
  {
    List<PromotionCandidate> candidates = new ArrayList<>();
    File configFolder = new File(PromoterConfig.INSTANCE.getConfigDirectory(), "jobs");

    for (File jobDir : configFolder.listFiles())
    {
      if (!jobDir.isDirectory())
      {
        continue;
      }

      String jobName = jobDir.getName();
      if (IO.isExcluded(jobName))
      {
        continue;
      }

      Properties jobProperties = Config.loadProperties(new File(jobDir, "job.properties"), false);

      boolean disabled = Config.isDisabled(jobProperties);
      if (disabled)
      {
        continue;
      }

      collectPromotionCandidates(jobName, jobProperties, candidates);
    }

    return candidates;
  }

  protected void collectPromotionCandidates(String jobName, Properties jobProperties, List<PromotionCandidate> candidates)
  {
    String jobPath = jobProperties.getProperty("path", jobName);
    String jobURL = PromoterConfig.INSTANCE.getJobsURL() + "/" + jobPath;

    System.out.println();
    System.out.println("Checking builds of " + jobURL);

    Set<Integer> excludedBuilds = new HashSet<>();
    StringTokenizer tokenizer = new StringTokenizer(jobProperties.getProperty("excluded.builds", ""), ",;: \t\n\r\f");
    while (tokenizer.hasMoreTokens())
    {
      excludedBuilds.add(Integer.parseInt(tokenizer.nextToken()));
    }

    List<Integer> buildNumbers = getBuildNumbers(jobURL);
    for (Integer buildNumber : buildNumbers)
    {
      if (excludedBuilds.contains(buildNumber))
      {
        System.out.println("Build " + buildNumber + " is excluded");
        continue;
      }

      String buildURL = jobURL + "/" + buildNumber;
      String buildResult = getBuildResult(buildURL);

      if ("SUCCESS".equalsIgnoreCase(buildResult) || "UNSTABLE".equalsIgnoreCase(buildResult))
      {
        try (InputStream xml = Jenkins.openInputStream(buildURL + "/artifact/build-info.xml"))
        {
          BuildInfo buildInfo = BuildInfo.read(xml);
          addPromotionCandidate(jobProperties, buildURL, buildInfo, candidates);
        }
        catch (FileNotFoundException ex)
        {
          System.out.println("Build " + buildNumber + " is missing build infos");
          continue;
        }
        catch (IOException ex)
        {
          throw new RuntimeException(ex);
        }
      }
      else if ("FAILURE".equalsIgnoreCase(buildResult))
      {
        System.out.println("Build " + buildNumber + " is failed");
      }
      else if ("ABORTED".equalsIgnoreCase(buildResult))
      {
        System.out.println("Build " + buildNumber + " is aborted");
      }
      else
      {
        System.out.println("Build " + buildNumber + " is in progress");
      }
    }
  }

  protected void addPromotionCandidate(Properties jobProperties, String buildURL, BuildInfo buildInfo, List<PromotionCandidate> candidates)
  {
    String buildType = buildInfo.getType();
    String autoPromote = jobProperties.getProperty("auto.promote", "IMSR");
    String autoVisible = jobProperties.getProperty("auto.visible", "");
    String message = "Build " + buildInfo.getNumber() + " (" + buildType + ")";

    if (autoPromote.contains(buildType))
    {
      File dropsDir = PromoterConfig.INSTANCE.getDropsArea();
      File drop = new File(dropsDir, buildInfo.getQualifier());
      if (!drop.exists())
      {
        for (PromotionCandidate candidate : candidates)
        {
          if (buildInfo.getQualifier().equals(candidate.getBuildInfo().getQualifier()))
          {
            System.out.println(message + " is already selected for promotion");
            return;
          }
        }

        boolean isVisible = autoVisible.contains(buildType);
        candidates.add(new PromotionCandidate(jobProperties, buildURL, buildInfo, isVisible));
        return;
      }

      System.out.println(message + " is already promoted");
    }
    else
    {
      System.out.println(message + " is not configured for promotion");
    }
  }

  public List<BuildInfo> copyBuilds(List<PromotionCandidate> candidates)
  {
    List<BuildInfo> buildInfos = new ArrayList<>();
    for (PromotionCandidate candidate : candidates)
    {
      if (copyBuild(candidate))
      {
        buildInfos.add(candidate.getBuildInfo());
      }
    }

    File logFile = new File(PromoterConfig.INSTANCE.getWorkingArea(), "copied-builds.txt");
    IO.writeFile(logFile, out -> {
      PrintStream stream = new PrintStream(out);

      for (BuildInfo buildInfo : buildInfos)
      {
        stream.println(buildInfo.getQualifier());
      }

      stream.flush();
    });

    return buildInfos;
  }

  protected boolean copyBuild(PromotionCandidate candidate)
  {
    Properties jobProperties = candidate.getJobProperties();
    String buildURL = candidate.getBuildURL();
    BuildInfo buildInfo = candidate.getBuildInfo();
    File dropsDir = PromoterConfig.INSTANCE.getDropsArea();
    File drop = new File(dropsDir, buildInfo.getQualifier());

    dropsDir.mkdirs();
    if (drop.exists())
    {
      System.out.println("Build " + buildInfo.getNumber() + " (" + buildInfo.getType() + ") is already promoted");
      return false;
    }

    drop.mkdirs();
    boolean visible = buildInfo.isVisible();
    DropProcessor.storeMarkers(drop, jobProperties, visible);
    System.out.println("Build " + buildInfo.getNumber() + " is being copied to " + drop + (visible ? " (visible)" : " (invisible)"));

    File zip = new File(drop, "build-results.zip");
    IO.copyFile(() -> Jenkins.openInputStream(buildURL + "/artifact/build-results.zip"), zip);
    IO.unzip(zip, drop, null);
    zip.delete();

    // Handle old build results layout
    File siteP2 = new File(drop, "site.p2");
    if (siteP2.isDirectory())
    {
      for (File file : siteP2.listFiles())
      {
        file.renameTo(new File(drop, file.getName()));
      }

      siteP2.delete();
    }

    setTag(buildInfo);
    return true;
  }

  protected void setTag(BuildInfo buildInfo)
  {
    System.out.println();
    SourceCodeManager scm = getPromoter().getSourceCodeManager();
    if (scm != null)
    {
      scm.setTag(buildInfo.getBranch(), buildInfo.getRevision(), buildInfo.getQualifier());
    }
  }

  protected List<Integer> getBuildNumbers(String jobURL)
  {
    final List<Integer> buildNumbers = new ArrayList<>();

    try (InputStream xml = Jenkins.openInputStream(jobURL + "/api/xml"))
    {
      XML.parseXML(xml, new DefaultHandler()
      {
        private int level;

        private boolean build;

        private boolean number;

        private StringBuilder builder = new StringBuilder();

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
        {
          ++level;
          if (level == 2 && "build".equalsIgnoreCase(qName))
          {
            build = true;
          }

          if (build && level == 3 && "number".equalsIgnoreCase(qName))
          {
            number = true;
          }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException
        {
          if (number)
          {
            buildNumbers.add(Integer.parseInt(builder.toString().trim()));
            builder = new StringBuilder();
          }

          --level;
          number = false;
          build = false;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException
        {
          if (number)
          {
            builder.append(ch, start, length);
          }
        }
      });
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      return null;
    }

    Collections.sort(buildNumbers);
    return buildNumbers;
  }

  protected String getBuildResult(String buildURL)
  {
    final StringBuilder builder = new StringBuilder();

    try (InputStream xml = Jenkins.openInputStream(buildURL + "/api/xml"))
    {
      XML.parseXML(xml, new DefaultHandler()
      {
        private int level;

        private boolean result;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
        {
          if (++level == 2)
          {
            if ("result".equalsIgnoreCase(qName))
            {
              result = true;
            }
          }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException
        {
          --level;
          result = false;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException
        {
          if (result)
          {
            builder.append(ch, start, length);
          }
        }
      });
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      return null;
    }

    return builder.toString();
  }
}
