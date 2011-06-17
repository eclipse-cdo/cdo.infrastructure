package main;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import util.BuildInfo;
import util.Config;
import util.IO;
import util.IO.OutputHandler;
import util.XML;
import util.XMLOutput;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

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
  public static final String DOWNLOADS_PATH = Config.getProjectDownloadsArea().getAbsolutePath();

  public static final File dropsDir = new File(Config.getProjectDownloadsArea(), "drops");

  private static final String MARKER_MIRRORED = ".mirrored";

  private static final String MARKER_VISIBLE = ".visible";

  public static void main(String[] args) throws Exception
  {
    copyBuilds();
    // performTasks();

    OutputStream out = null;

    try
    {
      out = new FileOutputStream(new File(Config.getProjectWorkingArea(), "promoter.ant"));
      XMLOutput xml = new XMLOutput(out)
      {
        @Override
        public XMLOutput attribute(String name, File file) throws SAXException
        {
          String path = file.getAbsolutePath();
          if (path.startsWith(DOWNLOADS_PATH))
          {
            path = path.substring(DOWNLOADS_PATH.length() + 1);
          }

          return super.attribute(name, path);
        }
      };

      xml.element("project");
      xml.attribute("name", "promoter");
      xml.attribute("default", "main");
      xml.attribute("basedir", DOWNLOADS_PATH);
      xml.push();

      xml.element("target");
      xml.attribute("name", "main");
      xml.push();

      postProcessDrops(xml);
      generateRepositories(xml);
      generateDocuments(xml);

      xml.pop();
      xml.pop();
      xml.done();
    }
    finally
    {
      IO.close(out);
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
            copyBuildIfNeeded(jobProperties, buildDir, buildInfo);
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

  private static void copyBuildIfNeeded(Properties jobProperties, File buildDir, BuildInfo buildInfo)
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
        System.out.println("Build " + buildInfo.getNumber() + " is copied to " + target
            + (isVisible ? " (visible)" : " (invisible)"));

        File archiveDir = new File(buildDir, "archive");
        IO.copyTree(archiveDir, target);

        storePromotionProperties(target, jobProperties);
        storeMarkers(target, isVisible);
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

  private static void storeMarkers(File target, boolean visible)
  {
    if (visible)
    {
      IO.writeFile(new File(target, MARKER_VISIBLE), OutputHandler.EMPTY);
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

  private static void postProcessDrops(XMLOutput xml) throws SAXException
  {
    String downloadsPrefix = Config.getProperties().getProperty("projectMirrorsPrefix");

    for (File drop : dropsDir.listFiles())
    {
      if (drop.isDirectory())
      {
        File siteP2 = new File(drop, "site.p2");
        if (siteP2.isDirectory())
        {
          if (downloadsPrefix != null)
          {
            File markerFile = new File(siteP2, MARKER_MIRRORED);
            if (!markerFile.exists())
            {
              addMirroring(xml, siteP2, "artifacts", downloadsPrefix);
              addMirroring(xml, siteP2, "content", downloadsPrefix);

              xml.element("touch");
              xml.attribute("file", markerFile);
            }
          }

          File buildInfoFile = new File(drop, "build-info.xml");
          if (buildInfoFile.isFile())
          {
            BuildInfo buildInfo = BuildInfo.read(buildInfoFile);
            Properties promotionProperties = Config.loadProperties(new File(drop, "promotion.properties"), false);
            File zips = new File(drop, "zips");

            String generateZipSite = promotionProperties.getProperty("generate.zip.site");
            if (generateZipSite != null)
            {
              xml.element("zip");
              xml.attribute("destfile", new File(zips, buildInfo.substitute(generateZipSite)));
              xml.push();
              xml.element("fileset");
              xml.attribute("dir", siteP2);
              xml.push();
              xml.element("includes");
              xml.attribute("name", "artifacts.jar");
              xml.element("includes");
              xml.attribute("name", "content.jar");
              xml.element("includes");
              xml.attribute("name", " binary/**");
              xml.element("includes");
              xml.attribute("name", " features/**");
              xml.element("includes");
              xml.attribute("name", " plugins/**");
              xml.element("includes");
              xml.pop();
              xml.pop();
            }

            String generateZipAll = promotionProperties.getProperty("generate.zip.all");
            if (generateZipAll != null)
            {
              File dropinsZip = new File(zips, "dropins.zip");
              if (dropinsZip.isFile())
              {
                xml.element("rename");
                xml.attribute("dest", new File(zips, buildInfo.substitute(generateZipAll)));
                xml.attribute("src", dropinsZip);
              }
            }
          }
        }
      }
    }
  }

  private static void addMirroring(XMLOutput xml, File siteP2, String name, String downloadsPrefix) throws SAXException
  {
    String qualifier = siteP2.getParentFile().getName();
    String match = "<property name='p2\\.compressed'";
    String url = "http://www.eclipse.org/downloads/download.php?file=/" + downloadsPrefix + "/drops/" + qualifier
        + "/site.p2&amp;protocol=http&amp;format=xml";
    String replace = match + "\n    " + "<property name='p2.mirrorsURL' value='" + url + "'/>'>";

    File jarFile = new File(siteP2, name + ".jar");
    File xmlFile = new File(siteP2, name + ".xml");

    xml.element("unjar");
    xml.attribute("dest", siteP2);
    xml.attribute("src", jarFile);
    xml.push();
    xml.element("patternset");
    xml.attribute("includes", xmlFile.getName());
    xml.pop();

    xml.element("replaceregexp");
    xml.attribute("file", xmlFile);
    xml.attribute("match", match);
    xml.attribute("replace", replace);

    xml.element("jar");
    xml.attribute("destfile", jarFile);
    xml.attribute("includesfile", xmlFile);
    xml.attribute("update", "false");

    xml.element("delete");
    xml.attribute("file", xmlFile);
  }

  private static void generateRepositories(XMLOutput xml)
  {
  }

  private static void generateDocuments(XMLOutput xml)
  {
  }
}
