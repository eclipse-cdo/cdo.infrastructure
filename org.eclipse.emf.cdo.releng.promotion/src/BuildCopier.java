/**
 * Copyright (c) 2004 - 2011 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import util.Config;
import util.IO;
import util.XML;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * @author Eike Stepper
 */
public class BuildCopier
{
  public BuildCopier()
  {
  }

  public void copyBuilds()
  {
    File jobsDir = new File("jobs");
    for (File jobDir : jobsDir.listFiles())
    {
      if (jobDir.isDirectory())
      {
        String jobName = jobDir.getName();
        if (!IO.isExcluded(jobName))
        {
          Properties jobProperties = Config.loadProperties(new File(jobDir, "promotion.properties"), false);
          copyBuilds(new File(PromoterConfig.INSTANCE.getJobsHome(), jobName), jobProperties);
        }
      }
    }
  }

  protected void copyBuilds(File jobDir, Properties jobProperties)
  {
    File buildsDir = new File(jobDir, "builds");
    System.out.println();
    System.out.println("Checking " + buildsDir);

    Set<Integer> excludedBuilds = new HashSet<Integer>();
    StringTokenizer tokenizer = new StringTokenizer(jobProperties.getProperty("excluded.builds", ""), ",;: \t\n\r\f");
    while (tokenizer.hasMoreTokens())
    {
      excludedBuilds.add(Integer.parseInt(tokenizer.nextToken()));
    }

    final int NO_BUILD = -1;
    int nextBuildNumber = NO_BUILD;
    boolean buildInProgress = false;

    for (File buildDir : buildsDir.listFiles())
    {
      String name = buildDir.getName();
      if (buildDir.isDirectory() && isNumber(name))
      {
        int buildNumber = Integer.parseInt(name);
        if (excludedBuilds.contains(buildNumber))
        {
          System.out.println("Build " + buildNumber + " is excluded");
          continue;
        }

        String buildResult = getBuildResult(buildDir);
        File archiveDir = new File(buildDir, "archive");
        if ("SUCCESS".equalsIgnoreCase(buildResult) && archiveDir.isDirectory())
        {
          File buildInfoFile = new File(archiveDir, "build-info.xml");
          if (buildInfoFile.isFile())
          {
            BuildInfo buildInfo = BuildInfo.read(buildInfoFile);
            copyBuild(jobProperties, buildDir, buildInfo);
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
          buildInProgress = true;
        }

        if (!buildInProgress)
        {
          nextBuildNumber = buildNumber + 1;
        }
      }
    }

    if (nextBuildNumber != NO_BUILD)
    {
      storeNextBuildNumber(jobDir.getName(), nextBuildNumber);
    }
  }

  protected void copyBuild(Properties jobProperties, File buildDir, BuildInfo buildInfo)
  {
    String buildType = buildInfo.getType();
    String autoPromote = jobProperties.getProperty("auto.promote", "IMSR");
    String autoVisible = jobProperties.getProperty("auto.visible", "");

    if (autoPromote.contains(buildType))
    {
      File dropsDir = PromoterConfig.INSTANCE.getDropsArea();
      dropsDir.mkdirs();

      File drop = new File(dropsDir, buildInfo.getQualifier());
      if (!drop.exists())
      {
        boolean isVisible = autoVisible.contains(buildType);
        System.out.println("Build " + buildInfo.getNumber() + " is copied to " + drop
            + (isVisible ? " (visible)" : " (invisible)"));

        File archiveDir = new File(buildDir, "archive");
        IO.copyTree(archiveDir, drop);

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

        DropProcessor.storeMarkers(drop, jobProperties, isVisible);
      }
      else
      {
        System.out.println("Build " + buildInfo.getNumber() + " is already promoted");
      }
    }
  }

  protected void storeNextBuildNumber(String jobName, final int nextBuildNumber)
  {
    IO.writeFile(new File(PromoterConfig.INSTANCE.getWorkingArea(), jobName + ".nextBuildNumber"),
        new IO.OutputHandler()
        {
          public void handleOutput(OutputStream out) throws IOException
          {
            PrintStream stream = new PrintStream(out);
            stream.println(nextBuildNumber);
            stream.flush();
          }
        });
  }

  protected boolean isNumber(String str)
  {
    for (char c : str.toCharArray())
    {
      if (!Character.isDigit(c))
      {
        return false;
      }
    }

    return true;
  }

  protected String getBuildResult(File buildDir)
  {
    File file = new File(buildDir, "build.xml");
    if (!file.exists() || !file.isFile())
    {
      return null;
    }

    final StringBuilder builder = new StringBuilder();
    XML.parseXML(file, new DefaultHandler()
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

    return builder.toString();
  }
}
