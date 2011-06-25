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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * @author Eike Stepper
 */
public class Main
{
  public static final String MARKER_PROMOTED = ".promoted";

  public static final String MARKER_MIRRORED = ".mirrored";

  public static final String MARKER_INVISIBLE = ".invisible";

  public static void main(String[] args) throws Exception
  {
    copyBuilds();
    // performTasks();

    final String downloadsPath = PromoterConfig.INSTANCE.getDownloadsArea().getAbsolutePath();
    OutputStream out = null;

    try
    {
      out = new FileOutputStream(new File(PromoterConfig.INSTANCE.getWorkingArea(), "promoter.ant"));
      XMLOutput xml = new XMLOutput(out)
      {
        @Override
        public XMLOutput attribute(String name, File file) throws SAXException
        {
          String path = file.getAbsolutePath();
          if (path.startsWith(downloadsPath))
          {
            path = path.substring(downloadsPath.length() + 1);
          }

          return super.attribute(name, path);
        }
      };

      xml.element("project");
      xml.attribute("name", "promoter");
      xml.attribute("default", "main");
      xml.attribute("basedir", downloadsPath);
      xml.push();

      xml.element("target");
      xml.attribute("name", "main");
      xml.push();

      List<BuildInfo> buildInfos = postProcessDrops(xml);
      WebNode webNode = generateRepositories(xml, buildInfos);
      generateDocuments(xml, webNode);

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
          copyBuilds(new File(PromoterConfig.INSTANCE.getJobsHome(), jobName), jobProperties);
        }
      }
    }
  }

  private static void copyBuilds(File jobDir, Properties jobProperties)
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

        storeMarkers(drop, jobProperties, isVisible);
      }
      else
      {
        System.out.println("Build " + buildInfo.getNumber() + " is already promoted");
      }
    }
  }

  private static void storeMarkers(File target, Properties properties, boolean visible)
  {
    OutputStream out = null;

    try
    {
      out = new FileOutputStream(new File(target, MARKER_PROMOTED));
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

    if (!visible)
    {
      IO.writeFile(new File(target, MARKER_INVISIBLE), IO.OutputHandler.EMPTY);
    }
  }

  private static void storeNextBuildNumber(String jobName, final int nextBuildNumber)
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

  private static List<BuildInfo> postProcessDrops(XMLOutput xml) throws SAXException
  {
    File dropsDir = PromoterConfig.INSTANCE.getDropsArea();

    List<BuildInfo> buildInfos = new ArrayList<BuildInfo>();
    for (File drop : dropsDir.listFiles())
    {
      if (drop.isDirectory())
      {
        // Add p2.mirrorsURL
        File markerFile = new File(drop, MARKER_MIRRORED);
        if (!markerFile.exists())
        {
          addMirroring(xml, drop, null, "artifacts");
          addMirroring(xml, drop, null, "content");

          File categories = new File(drop, "categories");
          if (!categories.exists())
          {
            addMirroring(xml, drop, "categories", "content");
          }
          xml.element("touch");
          xml.attribute("file", markerFile);
        }

        File buildInfoFile = new File(drop, "build-info.xml");
        if (buildInfoFile.isFile())
        {
          BuildInfo buildInfo = BuildInfo.read(buildInfoFile);
          buildInfos.add(buildInfo);

          Properties promotionProperties = Config.loadProperties(new File(drop, MARKER_PROMOTED), false);
          File zips = new File(drop, "zips");

          String generateZipSite = promotionProperties.getProperty("generate.zip.site");
          if (generateZipSite != null)
          {
            xml.element("zip");
            xml.attribute("destfile", new File(zips, buildInfo.substitute(generateZipSite)));
            xml.push();
            xml.element("fileset");
            xml.attribute("dir", drop);
            xml.push();
            xml.element("include");
            xml.attribute("name", "artifacts.jar");
            xml.element("include");
            xml.attribute("name", "content.jar");
            xml.element("include");
            xml.attribute("name", "binary/**");
            xml.element("include");
            xml.attribute("name", "features/**");
            xml.element("include");
            xml.attribute("name", "plugins/**");
            xml.element("include");
            xml.pop();
            xml.pop();
          }

          String generateZipAll = promotionProperties.getProperty("generate.zip.all");
          if (generateZipAll != null)
          {
            File dropinsZip = new File(zips, "dropins.zip");
            if (dropinsZip.isFile())
            {
              xml.element("move");
              xml.attribute("file", dropinsZip);
              xml.attribute("tofile", new File(zips, buildInfo.substitute(generateZipAll)));
            }
          }
        }
      }
    }

    return buildInfos;
  }

  private static void addMirroring(XMLOutput xml, File drop, String pathInDrop, String name) throws SAXException
  {
    File path = new File(drop, pathInDrop);

    String match = "<property name='p2\\.compressed' value='true'/>";
    String replace = "<property name='p2.compressed' value='true'/>\n    " + "<property name='p2.mirrorsURL' value='"
        + PromoterConfig.INSTANCE.formatDropURL(drop.getName()) + (pathInDrop == null ? "" : "/" + pathInDrop)
        + "&amp;format=xml'/>";

    File xmlFile = new File(path, name + ".xml");
    File jarFile = new File(path, name + ".jar");

    xml.element("unzip");
    xml.attribute("dest", path);
    xml.attribute("src", jarFile);
    xml.push();
    xml.element("patternset");
    xml.attribute("includes", xmlFile.getName());
    xml.pop();

    xml.element("replaceregexp");
    xml.attribute("file", xmlFile);
    xml.attribute("match", match);
    xml.attribute("replace", replace);

    xml.element("zip");
    xml.attribute("destfile", jarFile);
    xml.attribute("update", false);
    xml.push();
    xml.element("fileset");
    xml.attribute("dir", path);
    xml.push();
    xml.element("include");
    xml.attribute("name", xmlFile.getName());
    xml.pop();
    xml.pop();

    xml.element("delete");
    xml.attribute("file", xmlFile);
  }

  private static WebNode generateRepositories(XMLOutput xml, List<BuildInfo> buildInfos) throws SAXException
  {
    File compositesDir = new File("composites");
    WebNode webNode = generateRepositories(xml, buildInfos, compositesDir);

    File temp = PromoterConfig.INSTANCE.getCompositionTempArea();
    File updates = PromoterConfig.INSTANCE.getCompositionArea();
    File updatesTmp = new File(updates.getParentFile(), updates.getName() + ".tmp");

    xml.element("move");
    xml.attribute("file", updates);
    xml.attribute("tofile", updatesTmp);

    xml.element("move");
    xml.attribute("file", temp);
    xml.attribute("tofile", updates);

    xml.element("delete");
    xml.attribute("includeemptydirs", true);
    xml.push();
    xml.element("fileset");
    xml.attribute("dir", ".");
    xml.push();
    xml.element("include");
    xml.attribute("name", updatesTmp.getName() + "/**");
    xml.pop();
    xml.pop();

    return webNode;
  }

  private static WebNode generateRepositories(XMLOutput xml, List<BuildInfo> buildInfos, File folder)
      throws SAXException
  {
    if (folder.isDirectory())
    {
      String compositeName = folder.getName();
      if (!isExcluded(compositeName))
      {
        WebNode webNode = new WebNode(folder);

        Repository repository = getRepository(folder, buildInfos);
        if (repository != null)
        {
          repository.generate(xml);
          webNode.setRepository(repository);
        }

        for (File child : folder.listFiles())
        {
          WebNode childWebNode = generateRepositories(xml, buildInfos, child);
          if (childWebNode != null)
          {
            webNode.getChildren().add(childWebNode);
          }
        }

        Collections.sort(webNode.getChildren());
        return webNode;
      }
    }

    return null;
  }

  private static Repository getRepository(File compositeDir, List<BuildInfo> buildInfos)
  {
    Properties compositionProperties = Config.loadProperties(new File(compositeDir, "composition.properties"), false);
    String name = compositionProperties.getProperty("composite.name");
    if (name == null)
    {
      return null;
    }

    Repository repository;

    File temp = PromoterConfig.INSTANCE.getCompositionTempArea();
    String path = compositeDir.getPath();
    path = path.substring(path.indexOf("/") + 1);

    String childLocations = compositionProperties.getProperty("child.locations");
    if (childLocations != null)
    {
      repository = new Repository(temp, name, path);

      StringTokenizer tokenizer = new StringTokenizer(childLocations, ",");
      while (tokenizer.hasMoreTokens())
      {
        String childLocation = tokenizer.nextToken().trim();
        repository.addChild(childLocation);
      }

    }
    else
    {
      String childJob = compositionProperties.getProperty("child.job");
      String childStream = compositionProperties.getProperty("child.stream");
      String childTypes = compositionProperties.getProperty("child.types");
      repository = new Repository.Drops(temp, name, path, childJob, childStream, childTypes, buildInfos);
    }

    String webLabel = compositionProperties.getProperty("web.label", repository.getName());
    repository.setWebLabel(webLabel);

    int webPriority = Integer.parseInt(compositionProperties.getProperty("web.priority", "500"));
    repository.setWebPriority(webPriority);

    boolean webCollapsed = Boolean.parseBoolean(compositionProperties.getProperty("web.collapsed", "false"));
    repository.setWebCollapsed(webCollapsed);

    return repository;
  }

  private static void generateDocuments(XMLOutput xml, WebNode webNode)
  {
    System.out.println();
    PrintStream out = null;

    try
    {
      out = new PrintStream(new File(PromoterConfig.INSTANCE.getCompositionTempArea(), "index.html"));
      webNode.generate(out, 0);
      out.flush();
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
    finally
    {
      IO.close(out);
    }
  }
}
