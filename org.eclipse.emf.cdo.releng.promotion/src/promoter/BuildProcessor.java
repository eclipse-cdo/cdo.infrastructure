/*
 * Copyright (c) 2004 - 2011 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package promoter;

import org.xml.sax.SAXException;

import promoter.util.Config;
import promoter.util.IO;
import promoter.util.XMLOutput;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * @author Eike Stepper
 */
public class BuildProcessor extends PromoterComponent
{
  public static final String MARKER_MIRRORED = ".mirrored";

  public static final String MARKER_PROMOTED = ".promoted";

  public static final String MARKER_INVISIBLE = ".invisible";

  public BuildProcessor()
  {
  }

  public List<BuildInfo> processBuilds(XMLOutput xml) throws Exception
  {
    List<BuildInfo> buildInfos = new ArrayList<BuildInfo>();
    for (File drop : PromoterConfig.INSTANCE.getDropsArea().listFiles())
    {
      if (drop.isDirectory())
      {
        processBuild(xml, drop, buildInfos);
      }
    }

    return buildInfos;
  }

  protected void processBuild(XMLOutput xml, File drop, List<BuildInfo> buildInfos) throws Exception
  {
    generateCategories(xml, drop);

    // Add p2.mirrorsURL
    File markerFile = new File(drop, BuildProcessor.MARKER_MIRRORED);
    if (!markerFile.exists())
    {
      addMirroring(xml, drop, null, "artifacts");
      addMirroring(xml, drop, null, "content");

      File categories = new File(drop, "categories");
      if (categories.isDirectory())
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

      Properties promotionProperties = Config.loadProperties(new File(drop, BuildProcessor.MARKER_PROMOTED), false);
      File zips = new File(drop, "zips");

      String generateZipSite = promotionProperties.getProperty("generate.zip.site");
      if (generateZipSite != null)
      {
        File zipSite = new File(zips, buildInfo.substitute(generateZipSite));
        if (!zipSite.exists())
        {
          generateZipSite(xml, drop, zipSite);
        }
      }

      String generateZipAll = promotionProperties.getProperty("generate.zip.all");
      if (generateZipAll != null)
      {
        File dropinsZip = new File(zips, "dropins.zip");
        if (dropinsZip.isFile())
        {
          File zipAll = new File(zips, buildInfo.substitute(generateZipAll));
          renameZipAll(xml, dropinsZip, zipAll);
        }
      }
    }

    File help = new File(drop, "help");
    if (help.isDirectory())
    {
      unpackHelp(xml, help);
    }
  }

  protected File generateCategories(XMLOutput xml, File drop) throws SAXException
  {
    File categories = new File(drop, "categories");
    if (categories.isDirectory())
    {
      return null;
    }

    File categoriesJAR = new File(categories, "content.jar");
    File categoriesXML = new File(categories, "content.xml");

    File contentJAR = new File(drop, "content.jar");
    File contentXML = new File(drop, "content.xml");

    unzip(xml, drop, contentJAR, contentXML);

    // Transform
    xml.element("xslt");
    xml.attribute("style", new File(PromoterConfig.INSTANCE.getInstallArea(), "xsl/content2categories.xsl"));
    xml.attribute("in", contentXML);
    xml.attribute("out", categoriesXML);

    xml.element("replaceregexp");
    xml.attribute("file", categoriesXML);
    xml.attribute("match", "BUILD_QUALIFIER");
    xml.attribute("replace", drop.getName());
    xml.attribute("byline", true);

    // Find number of categories
    String sizeProperty = "requires.size." + drop.getName();

    xml.element("resourcecount");
    xml.attribute("property", sizeProperty);
    xml.push();
    xml.element("tokens");
    xml.push();
    xml.element("concat");
    xml.push();
    xml.element("filterchain");
    xml.push();
    xml.element("tokenfilter");
    xml.push();
    xml.element("containsregex");
    xml.attribute("pattern", "required namespace");
    xml.element("linetokenizer");
    xml.pop();
    xml.pop();
    xml.element("fileset");
    xml.attribute("file", categoriesXML);
    xml.pop();
    xml.pop();
    xml.pop();

    xml.element("replaceregexp");
    xml.attribute("file", categoriesXML);
    xml.attribute("match", "REQUIRES_SIZE");
    xml.attribute("replace", "${" + sizeProperty + "}");
    xml.attribute("byline", true);

    zip(xml, categories, categoriesJAR, categoriesXML);

    xml.element("delete");
    xml.attribute("file", categoriesXML);

    return contentXML;
  }

  protected void addMirroring(XMLOutput xml, File drop, String pathInDrop, String name) throws SAXException
  {
    File path = pathInDrop == null ? drop : new File(drop, pathInDrop);

    String match = "<property name=.p2\\.compressed. value=.true./>";
    String replace = "<property name='p2.compressed' value='true'/>\n    " + "<property name='p2.mirrorsURL' value='"
        + PromoterConfig.INSTANCE.formatDropURL(drop.getName()) + (pathInDrop == null ? "" : "/" + pathInDrop)
        + "&amp;format=xml'/>";

    File jarFile = new File(path, name + ".jar");
    File xmlFile = new File(path, name + ".xml");
    if (!xmlFile.isFile())
    {
      unzip(xml, path, jarFile, xmlFile);
    }

    xml.element("replaceregexp");
    xml.attribute("file", xmlFile);
    xml.attribute("match", match);
    xml.attribute("replace", replace);

    zip(xml, path, jarFile, xmlFile);

    xml.element("delete");
    xml.attribute("file", xmlFile);
  }

  protected void zip(XMLOutput xml, File path, File jarFile, File xmlFile) throws SAXException
  {
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
  }

  protected void unzip(XMLOutput xml, File path, File jarFile, File xmlFile) throws SAXException
  {
    xml.element("unzip");
    xml.attribute("dest", path);
    xml.attribute("src", jarFile);
    xml.push();
    xml.element("patternset");
    xml.attribute("includes", xmlFile.getName());
    xml.pop();
  }

  protected void generateZipSite(XMLOutput xml, File drop, File zipSite) throws SAXException
  {
    xml.element("zip");
    xml.attribute("destfile", zipSite);
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

  protected void renameZipAll(XMLOutput xml, File dropinsZip, File zipAll) throws SAXException
  {
    xml.element("move");
    xml.attribute("file", dropinsZip);
    xml.attribute("tofile", zipAll);
  }

  protected void unpackHelp(XMLOutput xml, File help) throws SAXException, IOException
  {
    File docsFile = new File(help, "docs.txt");
    if (docsFile.isFile())
    {
      // Cleanup possible former unpacks that have failed in the middle
      for (File file : help.listFiles())
      {
        if (file.isDirectory())
        {
          IO.delete(file);
        }
      }

      // Load names of doc plugins
      Set<String> docs = new HashSet<String>();
      BufferedReader reader = null;

      try
      {
        reader = new BufferedReader(new FileReader(docsFile));

        String line;
        while ((line = reader.readLine()) != null)
        {
          docs.add(line);
        }
      }
      finally
      {
        IO.close(reader);
      }

      // Unzip the doc plugins
      File plugins = new File(help, "plugins");
      for (String doc : docs)
      {
        // Unzip from dropins.zip
        xml.element("unzip");
        xml.attribute("dest", help);
        xml.push();
        xml.element("fileset");
        xml.attribute("dir", new File(help.getParentFile(), "zips"));
        xml.push();
        xml.element("include");
        xml.attribute("name", "*-Dropins.zip");
        xml.pop();
        xml.element("patternset");
        xml.attribute("includes", "plugins/" + doc + "_*.jar");
        xml.pop();

        xml.element("unzip");
        xml.attribute("dest", new File(help, doc));
        xml.push();
        xml.element("patternset");
        xml.push();
        xml.element("include");
        xml.attribute("name", "javadoc/**");
        xml.element("include");
        xml.attribute("name", "schemadoc/**");
        xml.element("include");
        xml.attribute("name", "html/**");
        xml.element("include");
        xml.attribute("name", "images/**");
        xml.element("include");
        xml.attribute("name", "about.html");
        xml.element("include");
        xml.attribute("name", "copyright.txt");
        xml.element("include");
        xml.attribute("name", "plugin.properties");
        xml.pop();
        xml.element("fileset");
        xml.attribute("dir", plugins);
        xml.attribute("includes", doc + "_*.jar");
        xml.pop();
      }

      // Remove the temp unpack folder
      xml.element("delete");
      xml.attribute("includeemptydirs", true);
      xml.attribute("failonerror", true);
      xml.push();
      xml.element("fileset");
      xml.attribute("dir", help);
      xml.push();
      xml.element("include");
      xml.attribute("name", "plugins/**");
      xml.element("include");
      xml.attribute("name", "plugins/**");
      xml.pop();
      xml.pop();

      // Uncomment breadcrumbs
      xml.element("replaceregexp");
      xml.attribute("match", "<!-- (<div class=\"help_breadcrumbs\">.*?) -->");
      xml.attribute("replace", "\\1");
      xml.attribute("flags", "s");
      xml.push();
      xml.element("fileset");
      xml.attribute("dir", help);
      xml.attribute("includes", "**/*.html");
      xml.pop();

      // Rename docs.txt
      xml.element("move");
      xml.attribute("file", docsFile);
      xml.attribute("tofile", new File(help, ".docs"));
    }
  }

  public static void storeMarkers(File drop, Properties properties, boolean visible)
  {
    OutputStream out = null;

    try
    {
      out = new FileOutputStream(new File(drop, MARKER_PROMOTED));
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
      IO.writeFile(new File(drop, MARKER_INVISIBLE), IO.OutputHandler.EMPTY);
    }
  }
}
