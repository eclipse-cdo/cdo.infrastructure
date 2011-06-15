package main;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import tasks.TaskManager;
import tasks.hudson.HudsonTask;
import util.Config;
import util.XML;

import java.io.File;
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
  private static final File drops = new File(Config.getProjectDownloadsArea(), "drops");

  public static void main(String[] args)
  {
    boolean modifiedRepositories = false;
    modifiedRepositories |= copyBuilds();
    modifiedRepositories |= performTasks();

    if (modifiedRepositories)
    {
      generateRepositories();
    }

    generateDocuments();

    TaskManager taskManager = new TaskManager();
    taskManager.addTaskProvider(new HudsonTask.Provider(1));
    taskManager.run();
  }

  private static boolean copyBuilds()
  {
    boolean modifiedRepositories = false;
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
        modifiedRepositories |= copyBuilds(new File(Config.getHudsonJobsArea(), jobName), jobProperties);
      }
    }

    return modifiedRepositories;
  }

  private static boolean copyBuilds(File jobDir, Properties jobProperties)
  {
    boolean modifiedRepositories = false;
    File buildsDir = new File(jobDir, "builds");
    System.out.println("Checking " + buildsDir);

    for (File buildDir : buildsDir.listFiles())
    {
      if (buildDir.isDirectory())
      {
        String buildResult = getBuildResult(buildDir);
        System.out.println(buildResult);
      }
    }

    return modifiedRepositories;
  }

  private static String getBuildResult(File buildDir)
  {
    final StringBuilder builder = new StringBuilder();
    XML.parseXML(new File(buildDir, "build.xml"), new DefaultHandler()
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

  private static boolean performTasks()
  {
    return false;
  }

  private static void generateRepositories()
  {
  }

  private static void generateDocuments()
  {
  }
}
