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

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import promoter.util.Config;
import promoter.util.IO;

/**
 * @author Eike Stepper
 */
public class DropProcessor extends PromoterComponent
{
  public static final String MARKER_MIRRORED = ".mirrored";

  public static final String MARKER_PROMOTED = ".promoted";

  public static final String MARKER_INVISIBLE = ".invisible";

  public DropProcessor()
  {
  }

  public List<BuildInfo> loadBuildInfos() throws Exception
  {
    List<BuildInfo> buildInfos = new ArrayList<>();
    processDrops(true, buildInfos, PromoterConfig.INSTANCE.getArchiveDropsArea());
    processDrops(true, buildInfos, PromoterConfig.INSTANCE.getDropsArea());
    return buildInfos;
  }

  public List<BuildInfo> processDrops() throws Exception
  {
    List<BuildInfo> buildInfos = new ArrayList<>();
    processDrops(true, buildInfos, PromoterConfig.INSTANCE.getArchiveDropsArea());
    processDrops(false, buildInfos, PromoterConfig.INSTANCE.getDropsArea());
    return buildInfos;
  }

  protected void processDrops(boolean loadInfoOnly, List<BuildInfo> buildInfos, File dropsArea) throws Exception
  {
    File[] drops = dropsArea.listFiles();
    if (drops != null)
    {
      for (File drop : drops)
      {
        if (drop.isDirectory())
        {
          processDrop(drop, loadInfoOnly, buildInfos);
        }
      }
    }
  }

  protected void processDrop(File drop, boolean loadInfoOnly, List<BuildInfo> buildInfos) throws Exception
  {
    BuildInfo buildInfo = null;

    File buildInfoFile = new File(drop, "build-info.xml");
    if (buildInfoFile.isFile())
    {
      buildInfo = BuildInfo.read(buildInfoFile);
      buildInfos.add(buildInfo);
    }

    if (loadInfoOnly)
    {
      return;
    }

    if (IO.isRepository(drop))
    {
      generateCategories(drop);

      // Add p2.mirrorsURL
      File markerFile = new File(drop, DropProcessor.MARKER_MIRRORED);
      if (!markerFile.exists())
      {
        addMirroring(drop, null, "artifacts");
        addMirroring(drop, null, "content");

        File categories = new File(drop, "categories");
        if (categories.isDirectory())
        {
          addMirroring(drop, "categories", "content");
        }

        markerFile.createNewFile();
      }
    }

    if (buildInfo != null)
    {
      Properties promotionProperties = Config.loadProperties(new File(drop, DropProcessor.MARKER_PROMOTED), false);

      String generateZipSite = promotionProperties.getProperty("generate.zip.site");
      if (generateZipSite != null)
      {
        File zips = new File(drop, "zips");
        generateZipSite = buildInfo.substitute(generateZipSite);

        File zipSite = new File(zips, generateZipSite);
        if (!zipSite.exists())
        {
          if (!IO.isContained(drop, zipSite))
          {
            throw new IllegalStateException("Refusing to create site outside drop: " + zipSite);
          }

          generateZipSite(drop, zipSite);
        }
      }

      TPMacroSetup.insertDropRepository(buildInfo);
    }

    File help = new File(drop, "help");
    if (help.isDirectory())
    {
      unpackHelp(help);
    }
  }

  protected File generateCategories(File drop) throws Exception
  {
    File categories = new File(drop, "categories");
    if (categories.isDirectory())
    {
      return null;
    }

    File contentJAR = new File(drop, "content.jar");
    File contentXML = new File(drop, "content.xml");

    unzip(contentJAR, drop, name -> name.equals(contentXML.getName()));

    File categoriesJAR = new File(categories, "content.jar");
    File categoriesXML = new File(categories, "content.xml");

    File categoriesJAR2 = new File(categories, "artifacts.jar");
    File categoriesXML2 = new File(categories, "artifacts.xml");

    categories.mkdirs();

    TransformerFactory.newInstance().newTransformer(new StreamSource(new File(PromoterConfig.INSTANCE.getXSLDirectory(), "content2categories.xsl")))
        .transform(new StreamSource(contentXML), new StreamResult(categoriesXML));

    replaceRegex(categoriesXML, Pattern.compile("BUILD_QUALIFIER"), drop.getName());

    long requiredSize;
    try (Stream<String> lines = Files.lines(categoriesXML.toPath(), StandardCharsets.UTF_8))
    {
      requiredSize = lines.filter(line -> line.contains("required namespace")).count();
    }

    replaceRegex(categoriesXML, Pattern.compile("REQUIRES_SIZE"), Long.toString(requiredSize));

    IO.copyFile(categoriesXML, categoriesXML2);
    replaceRegex(categoriesXML2, Pattern.compile("org\\.eclipse\\.equinox\\.internal\\.p2\\.metadata\\.repository\\.LocalMetadataRepository"),
        "org.eclipse.equinox.p2.artifact.repository.simpleRepository");

    zipSingle(categories, categoriesJAR, categoriesXML);
    categoriesXML.delete();

    zipSingle(categories, categoriesJAR2, categoriesXML2);
    categoriesXML2.delete();

    return contentXML;
  }

  protected void addMirroring(File drop, String pathInDrop, String name) throws Exception
  {
    File path = pathInDrop == null ? drop : new File(drop, pathInDrop);

    String match = "<property name=.p2\\.compressed. value=.true./>";
    String replace = "<property name='p2.compressed' value='true'/>\n    " + "<property name='p2.mirrorsURL' value='"
        + PromoterConfig.INSTANCE.formatDropURL(drop.getName() + (pathInDrop == null ? "" : "/" + pathInDrop), true).replace("&", "&amp;")
        + "&amp;format=xml'/>";

    File jarFile = new File(path, name + ".jar");
    File xmlFile = new File(path, name + ".xml");
    if (!xmlFile.isFile())
    {
      unzip(jarFile, path, entry -> entry.equals(xmlFile.getName()));
    }

    replaceRegex(xmlFile, Pattern.compile(match), replace);
    zipSingle(path, jarFile, xmlFile);
    xmlFile.delete();
  }

  protected void zipSingle(File path, File jarFile, File xmlFile)
  {
    zip(path, jarFile, name -> name.equals(xmlFile.getName()));
  }

  protected void unzip(File zipFile, File target, Predicate<String> selector)
  {
    try (InputStream input = IO.openInputStream(zipFile))
    {
      IO.unzip(input, target, name -> isSafeZipEntry(name) && selector.test(name) ? name : null);
    }
    catch (IOException ex)
    {
      throw new RuntimeException(ex);
    }
  }

  protected boolean isSafeZipEntry(String name)
  {
    String normalized = name.replace('\\', '/');
    return !normalized.startsWith("/") && !normalized.contains(":") && !normalized.startsWith("../") && !normalized.contains("/../")
        && !normalized.endsWith("/..") && !normalized.equals("..");
  }

  protected void zip(File source, File destination, Predicate<String> selector)
  {
    IO.mkdirs(destination.getParentFile());

    try (ZipOutputStream output = new ZipOutputStream(new FileOutputStream(destination)))
    {
      zip(source, source, selector, output);
    }
    catch (IOException ex)
    {
      throw new RuntimeException(ex);
    }
  }

  protected void zip(File root, File current, Predicate<String> selector, ZipOutputStream output) throws IOException
  {
    File[] files = current.listFiles();
    if (files == null)
    {
      return;
    }

    for (File file : files)
    {
      String name = root.toPath().relativize(file.toPath()).toString().replace(File.separatorChar, '/');
      if (file.isDirectory())
      {
        zip(root, file, selector, output);
      }
      else if (selector.test(name))
      {
        output.putNextEntry(new ZipEntry(name));

        try (InputStream input = fileInput(file))
        {
          IO.copy(input, output);
        }

        output.closeEntry();
      }
    }
  }

  protected InputStream fileInput(File file)
  {
    return IO.openInputStream(file);
  }

  protected void generateZipSite(File drop, File zipSite)
  {
    zip(drop, zipSite, name -> name.equals("artifacts.jar") //
        || name.equals("content.jar") //
        || name.startsWith("binary/") //
        || name.startsWith("features/") //
        || name.startsWith("plugins/"));
  }

  protected void unpackHelp(File help) throws IOException
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
      Set<String> docs = new LinkedHashSet<>();
      BufferedReader reader = null;

      try
      {
        reader = new BufferedReader(new FileReader(docsFile, StandardCharsets.UTF_8));

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
        File zips = new File(help.getParentFile(), "zips");
        File[] dropinZips = zips.listFiles(file -> file.isFile() && file.getName().endsWith("-Dropins.zip"));
        if (dropinZips != null)
        {
          for (File dropinZip : dropinZips)
          {
            unzip(dropinZip, help, name -> name.startsWith("plugins/" + doc + "_") && name.endsWith(".jar"));
          }
        }

        File plugin = new File(help, doc);
        if (!IO.isContained(help, plugin))
        {
          throw new IllegalStateException("Refusing to unpack documentation outside help directory: " + plugin);
        }

        File[] pluginJars = plugins.listFiles(file -> file.isFile() && file.getName().startsWith(doc + "_") && file.getName().endsWith(".jar"));
        if (pluginJars != null)
        {
          for (File pluginJar : pluginJars)
          {
            unzip(pluginJar, plugin,
                name -> name.startsWith("javadoc/") || name.startsWith("productdoc/") || name.startsWith("schemadoc/") || name.startsWith("html/")
                    || name.startsWith("images/") || name.equals("about.html") || name.equals("copyright.txt") || name.equals("plugin.properties"));
          }
        }
      }

      // Remove the temp unpack folder
      IO.delete(plugins);

      // Uncomment breadcrumbs
      Pattern breadcrumbs = Pattern.compile("<!-- (<div class=\\\"help_breadcrumbs\\\">.*?) -->", Pattern.DOTALL);
      replaceInTree(help, breadcrumbs, "$1");

      // Rename docs.txt
      Files.move(docsFile.toPath(), new File(help, ".docs").toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
  }

  protected void replaceRegex(File file, Pattern pattern, String replacement)
  {
    replaceRegexRaw(file, pattern, Matcher.quoteReplacement(replacement));
  }

  protected void replaceRegexRaw(File file, Pattern pattern, String replacement)
  {
    String contents = IO.readTextFile(file);
    String result = pattern.matcher(contents).replaceAll(replacement);
    if (!contents.equals(result))
    {
      IO.writeTextFile(file, result);
    }
  }

  protected void replaceInTree(File folder, Pattern pattern, String replacement)
  {
    File[] files = folder.listFiles();
    if (files == null)
    {
      return;
    }

    for (File file : files)
    {
      if (file.isDirectory())
      {
        replaceInTree(file, pattern, replacement);
      }
      else if (file.getName().endsWith(".html"))
      {
        replaceRegexRaw(file, pattern, replacement);
      }
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
