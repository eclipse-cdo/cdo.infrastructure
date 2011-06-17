package main;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import util.BuildInfo;
import util.Config;
import util.IO;
import util.IO.OutputHandler;
import util.XML;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;

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

/**
 * @author Eike Stepper
 */
public class Main
{
  private static final File dropsDir = new File(Config.getProjectDownloadsArea(), "drops");

  public static void main(String[] args)
  {
    copyBuilds();
    // performTasks();
    generateRepositories();
    generateDocuments();
  }

  private static void copyBuilds()
  {
    File jobsDir = new File("jobs");
    for (File jobDir : jobsDir.listFiles())
    {
      if (jobDir.isDirectory())
      {
        String jobName = jobDir.getName();
        if (!isExcluded(jobName))
        {
          Properties jobProperties = Config.loadProperties(new File(jobDir, "promotion.properties"), false);
          copyBuilds(new File(Config.getHudsonJobsArea(), jobName), jobProperties);
        }
      }
    }
  }

  private static void copyBuilds(File jobDir, Properties jobProperties)
  {
    File buildsDir = new File(jobDir, "builds");
    System.out.println("Checking " + buildsDir);

    final int NO_BUILD = -1;
    int nextBuildNumber = NO_BUILD;
    boolean buildInProgress = false;

    for (File buildDir : buildsDir.listFiles())
    {
      String name = buildDir.getName();
      if (buildDir.isDirectory() && isNumber(name))
      {
        String buildResult = getBuildResult(buildDir);
        File archiveDir = new File(buildDir, "archive");
        if ("SUCCESS".equalsIgnoreCase(buildResult) && archiveDir.isDirectory())
        {
          File buildInfoFile = new File(archiveDir, "build-info.xml");
          if (buildInfoFile.isFile())
          {
            BuildInfo buildInfo = XML.readBuildInfo(buildInfoFile);
            copyBuildIdNeeded(jobProperties, buildDir, buildInfo);
          }
        }
        else
        {
          buildInProgress = true;
        }

        if (!buildInProgress)
        {
          nextBuildNumber = Integer.parseInt(name) + 1;
        }
      }
    }

    if (nextBuildNumber != NO_BUILD)
    {
      storeNextBuildNumber(jobDir.getName(), nextBuildNumber);
    }
  }

  private static void copyBuildIdNeeded(Properties jobProperties, File buildDir, BuildInfo buildInfo)
  {
    String buildType = buildInfo.getType();
    String autoPromote = jobProperties.getProperty("auto.promote", "IMSR");
    String autoVisible = jobProperties.getProperty("auto.visible", "");

    if (autoPromote.contains(buildType))
    {
      dropsDir.mkdirs();
      File target = new File(dropsDir, buildInfo.getQualifier());
      if (!target.exists())
      {
        boolean isVisible = autoVisible.contains(buildType);
        System.out.println("Copying build " + buildInfo.getNumber() + " to " + target
            + (isVisible ? " (visible)" : " (invisible)"));

        File archiveDir = new File(buildDir, "archive");
        IO.copyTree(archiveDir, target);

        storePromotionProperties(target, jobProperties);
        storeVisibility(target, isVisible);
      }
    }
  }

  private static void storePromotionProperties(File target, Properties properties)
  {
    OutputStream out = null;

    try
    {
      out = new FileOutputStream(new File(target, "promotion.properties"));
      properties.store(out, "Promotion Properties");
    }
    catch (IOException ex)
    {
      throw new RuntimeException(ex);
    }
    finally
    {
      IO.close(out);
    }
  }

  private static void storeVisibility(File target, boolean visible)
  {
    if (visible)
    {
      IO.writeFile(new File(target, ".visible"), new OutputHandler()
      {
        public void handleOutput(OutputStream out) throws IOException
        {
          // Do nothing
        }
      });
    }
  }

  private static void storeNextBuildNumber(String jobName, final int nextBuildNumber)
  {
    IO.writeFile(new File(Config.getProjectWorkingArea(), jobName + ".nextBuildNumber"), new OutputHandler()
    {
      public void handleOutput(OutputStream out) throws IOException
      {
        PrintStream stream = new PrintStream(out);
        stream.println(nextBuildNumber);
        stream.flush();
      }
    });
  }

  private static boolean isNumber(String str)
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

  private static boolean isExcluded(String name)
  {
    if (".svn".equalsIgnoreCase(name))
    {
      return true;
    }

    if ("cvs".equalsIgnoreCase(name))
    {
      return true;
    }

    if (".git".equalsIgnoreCase(name))
    {
      return true;
    }

    if (".hg".equalsIgnoreCase(name))
    {
      return true;
    }

    if (".bzr".equalsIgnoreCase(name))
    {
      return true;
    }

    if ("SCCS".equalsIgnoreCase(name))
    {
      return true;
    }

    return false;
  }

  private static String getBuildResult(File buildDir)
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

  private static void generateRepositories()
  {
  }

  private static void generateDocuments()
  {
  }
}
