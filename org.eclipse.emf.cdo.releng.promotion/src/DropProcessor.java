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
import org.xml.sax.SAXException;

import util.Config;
import util.IO;
import util.XMLOutput;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Eike Stepper
 */
public class DropProcessor
{
  public static final String MARKER_MIRRORED = ".mirrored";

  public static final String MARKER_PROMOTED = ".promoted";

  public static final String MARKER_INVISIBLE = ".invisible";

  public DropProcessor()
  {
  }

  public List<BuildInfo> processDrops(XMLOutput xml) throws Exception
  {
    List<BuildInfo> buildInfos = new ArrayList<BuildInfo>();
    for (File drop : PromoterConfig.INSTANCE.getDropsArea().listFiles())
    {
      if (drop.isDirectory())
      {
        processDrop(xml, drop, buildInfos);
      }
    }

    return buildInfos;
  }

  protected void processDrop(XMLOutput xml, File drop, List<BuildInfo> buildInfos) throws SAXException
  {
    // Add p2.mirrorsURL
    File markerFile = new File(drop, DropProcessor.MARKER_MIRRORED);
    if (!markerFile.exists())
    {
      addMirroring(xml, drop);

      xml.element("touch");
      xml.attribute("file", markerFile);
    }

    File buildInfoFile = new File(drop, "build-info.xml");
    if (buildInfoFile.isFile())
    {
      BuildInfo buildInfo = BuildInfo.read(buildInfoFile);
      buildInfos.add(buildInfo);

      Properties promotionProperties = Config.loadProperties(new File(drop, DropProcessor.MARKER_PROMOTED), false);
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
  }

  protected void addMirroring(XMLOutput xml, File drop) throws SAXException
  {
    addMirroring(xml, drop, null, "artifacts");
    addMirroring(xml, drop, null, "content");

    File categories = new File(drop, "categories");
    if (categories.isDirectory())
    {
      addMirroring(xml, drop, "categories", "content");
    }
  }

  protected void addMirroring(XMLOutput xml, File drop, String pathInDrop, String name) throws SAXException
  {
    File path = pathInDrop == null ? drop : new File(drop, pathInDrop);

    String match = "<property name=.p2\\.compressed. value=.true./>";
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
