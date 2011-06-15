package main;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import util.BuildInfo;
import util.Config;
import util.IO;
import util.XML;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
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

  private static final List<File> addedDrops = new ArrayList<File>();

  private static final List<File> removedDrops = new ArrayList<File>();

  public static void main(String[] args)
  {
    copyBuilds();
    // performTasks();

    if (!addedDrops.isEmpty() || !removedDrops.isEmpty())
    {
      generateRepositories();
      generateDocuments();
    }
  }

  private static void copyBuilds()
  {
    File jobsDir = new File("jobs");
    for (File jobDir : jobsDir.listFiles())
    {
      if (jobDir.isDirectory())
      {
        String jobName = jobDir.getName();
        if (".svn".equalsIgnoreCase(jobName) || "cvs".equalsIgnoreCase(jobName))
        {
          continue;
        }

        Properties jobProperties = Config.loadProperties(new File(jobDir, "promoter.properties"), false);
        copyBuilds(new File(Config.getHudsonJobsArea(), jobName), jobProperties);
      }
    }
  }

  private static void copyBuilds(File jobDir, Properties jobProperties)
  {
    File buildsDir = new File(jobDir, "builds");
    System.out.println("Checking " + buildsDir);

    for (File buildDir : buildsDir.listFiles())
    {
      String name = buildDir.getName();
      if (buildDir.isDirectory())
      {
        if (isNumber(name))
        {
          String buildResult = getBuildResult(buildDir);
          if ("SUCCESS".equalsIgnoreCase(buildResult))
          {
            File archiveDir = new File(buildDir, "archive");
            if (archiveDir.isDirectory() && archiveDir.isDirectory())
            {
              File buildInfoFile = new File(archiveDir, "build-info.xml");
              if (buildInfoFile.exists() && buildInfoFile.isFile())
              {
                BuildInfo buildInfo = XML.readBuildInfo(buildInfoFile);
                copyBuildIdNeeded(jobProperties, buildDir, buildInfo);
              }
            }
          }
        }
      }
    }
  }

  private static void copyBuildIdNeeded(Properties jobProperties, File buildDir, BuildInfo buildInfo)
  {
    String buildType = buildInfo.getType();

    String autoPromote = jobProperties.getProperty("auto.promote", "IMSR");
    if (autoPromote.contains(buildType))
    {
      dropsDir.mkdirs();
      File target = new File(dropsDir, buildInfo.getQualifier());
      if (!target.exists())
      {
        System.out.println("Copying build " + buildInfo.getNumber() + " to " + target);

        File archiveDir = new File(buildDir, "archive");
        IO.copyTree(archiveDir, target);

        String autoVisible = jobProperties.getProperty("auto.visible", "");
        if (autoVisible.contains(buildType))
        {
          File file = new File(target, "visible");
          OutputStream stream = null;

          try
          {
            stream = new FileOutputStream(file);
          }
          catch (Exception ex)
          {
            throw new RuntimeException(file.getAbsolutePath() + " could not be created", ex);
          }
          finally
          {
            IO.close(stream);
          }
        }

        addedDrops.add(target);
      }
    }
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
