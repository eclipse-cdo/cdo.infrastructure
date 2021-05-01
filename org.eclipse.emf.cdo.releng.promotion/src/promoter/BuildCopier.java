/*
 * Copyright (c) 2004 - 2012 Eike Stepper (Loehne, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
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

  public List<BuildInfo> copyBuilds()
  {
    List<BuildInfo> buildInfos = new ArrayList<>();
    File configFolder = new File(PromoterConfig.INSTANCE.getConfigDirectory(), "jobs");

    File logFile = new File(PromoterConfig.INSTANCE.getWorkingArea(), "copied-builds.txt");
    logFile.delete(); // Intentionally on best effort.

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

      Properties jobProperties = Config.loadProperties(new File(jobDir, "promotion.properties"), false);

      boolean disabled = Config.isDisabled(jobProperties);
      if (disabled)
      {
        continue;
      }

      copyBuilds(jobName, jobProperties, buildInfos);
    }

    if (!buildInfos.isEmpty())
    {
      IO.writeFile(logFile, out -> {
        PrintStream stream = new PrintStream(out);

        for (BuildInfo buildInfo : buildInfos)
        {
          stream.println(buildInfo.getQualifier());
        }

        stream.flush();
      });
    }

    return buildInfos;
  }

  protected void copyBuilds(String jobName, Properties jobProperties, List<BuildInfo> buildInfos)
  {
    String jobURL = PromoterConfig.INSTANCE.getJobsURL() + "/" + jobName;

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
        try
        {
          BuildInfo buildInfo = BuildInfo.read(new URL(buildURL + "/artifact/build-info.xml"));
          if (copyBuild(jobProperties, buildURL, buildInfo))
          {
            buildInfos.add(buildInfo);
          }
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

  protected boolean copyBuild(Properties jobProperties, String buildURL, BuildInfo buildInfo)
  {
    String buildType = buildInfo.getType();
    String autoPromote = jobProperties.getProperty("auto.promote", "IMSR");
    String autoVisible = jobProperties.getProperty("auto.visible", "");
    String message = "Build " + buildInfo.getNumber() + " (" + buildType + ")";

    if (autoPromote.contains(buildType))
    {
      File dropsDir = PromoterConfig.INSTANCE.getDropsArea();
      dropsDir.mkdirs();

      File drop = new File(dropsDir, buildInfo.getQualifier());
      if (!drop.exists())
      {
        boolean isVisible = autoVisible.contains(buildType);
        System.out.println("Build " + buildInfo.getNumber() + " is being copied to " + drop + (isVisible ? " (visible)" : " (invisible)"));

        try
        {
          IO.unzip(new URL(buildURL + "/artifact/*zip*/archive.zip"), drop, "archive/");
        }
        catch (MalformedURLException ex)
        {
          throw new RuntimeException(ex);
        }

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
        DropProcessor.storeMarkers(drop, jobProperties, isVisible);
        return true;
      }

      System.out.println(message + " is already promoted");
    }
    else
    {
      System.out.println(message + " is not configured for promotion");
    }

    return false;
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

    try
    {
      XML.parseXML(new URL(jobURL + "/api/xml"), new DefaultHandler()
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

    try
    {
      XML.parseXML(new URL(buildURL + "/api/xml"), new DefaultHandler()
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
