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

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import promoter.BuildInfo.Location;
import promoter.util.IO;
import promoter.util.XMLOutput;

/**
 * @author Eike Stepper
 */
public class Repository
{
  public static final boolean COMPRESS = false;

  private static final String PARENT_DIRECTORY = "../";

  private String name;

  private String path;

  private File folder;

  private List<String> children = new ArrayList<>();

  public Repository(String name, File relativePath)
  {
    this.name = name;
    path = relativePath.getPath().replace('\\', '/');
    folder = new File(PromoterConfig.INSTANCE.getCompositionTempArea(), path);

    System.out.println();
    System.out.println("Generating repository " + name + ":");
    System.out.println(getURL(false));
  }

  public final String getName()
  {
    return name;
  }

  public final String getPath()
  {
    return path;
  }

  public final File getFolder()
  {
    return folder;
  }

  public final String getURL(boolean mirror)
  {
    return PromoterConfig.INSTANCE.formatUpdateURL(path, mirror);
  }

  public final List<String> getChildren()
  {
    return Collections.unmodifiableList(children);
  }

  public void addChild(String child)
  {
    child = child.replace('\\', '/');
    System.out.println("   Adding child location: " + child);
    children.add(child);
  }

  public void addDrop(BuildInfo buildInfo)
  {
    File dropFolder = buildInfo.getDrop();
    if (IO.isRepository(dropFolder))
    {
      String child = null;
      if (buildInfo.getLocation() == Location.ARCHIVE)
      {
        child = buildInfo.getDropURL(null, false);
      }
      else
      {
        child = folder.toPath().relativize(dropFolder.toPath()).toString();
      }

      if (child != null)
      {
        addChild(child);
        addChild(child + "/categories");
      }
    }
  }

  @Override
  public String toString()
  {
    return path;
  }

  public void generate(XMLOutput xml)
  {
    folder.mkdirs();

    IO.writeFile(new File(folder, "composition.properties"), out -> {
      PrintStream stream = new PrintStream(out);
      stream.println("composite.name=" + name);
      stream.flush();
    });

    // IO.writeFile(new File(folder, "p2.index"), new IO.OutputHandler()
    // {
    // public void handleOutput(OutputStream out) throws IOException
    // {
    // PrintStream stream = new PrintStream(out);
    // stream.println("version = 1");
    // stream.println("metadata.repository.factory.order = compositeContent.xml,\\!");
    // stream.println("artifact.repository.factory.order = compositeArtifacts.xml,\\!");
    // stream.flush();
    // }
    // });

    generateHTML(folder);

    generateXML(xml, folder, "compositeArtifacts", "compositeArtifactRepository",
        "org.eclipse.equinox.internal.p2.artifact.repository.CompositeArtifactRepository");
    generateXML(xml, folder, "compositeContent", "compositeMetadataRepository",
        "org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository");
  }

  protected void generateHTML(File folder)
  {
    File htmlFile = new File(folder, "index.html");
    PrintStream out = null;

    try
    {
      out = new PrintStream(htmlFile);

      String title = "CDO Composite Update Site (" + name + ")";

      out.println("<!DOCTYPE html>");
      out.println("<html>");
      out.println("<head>");
      out.println("  <title>" + title + "</title>");
      out.println("</head>");
      out.println();
      out.println("<body style=\"font-family:Arial; font-size:small;\">");
      out.println("<h1>" + title + "</h1>");
      out.println("<p>");
      out.println("<em>For information about CDO or Net4j, see their <a href=\"https://www.eclipse.org/cdo\">homepage</a> or <a");
      out.println("href=\"https://wiki.eclipse.org/CDO\">wiki</a>.");
      out.println("         <br> For information about installing or updating Eclipse software, see the");
      out.println("         <a href=\"" + PromoterConfig.INSTANCE.getHelpTopicURL() + "/org.eclipse.platform.doc.user/tasks/tasks-124.htm\">");
      out.println("           Eclipse Platform Help</a>.");
      out.println("         <br> Some plugins require third party software from p2 repositories listed in this ");
      out.println("         <a href=\"bookmarks.xml\">bookmarks.xml</a> file.</em>");
      out.println("</p>");

      if (!children.isEmpty())
      {
        out.println("<h3>Contents</h3>");
        out.println("<ul>");
        for (String child : children)
        {
          String label = child;
          while (label.startsWith(PARENT_DIRECTORY))
          {
            label = label.substring(PARENT_DIRECTORY.length());
          }

          out.println("<li><a href=\"" + child + "/index.html\">" + label + "</a></li>");
        }

        out.println("</ul>");
      }
      else
      {
        out.println("<p><em>Currently this composite update site is empty.<br/>");
        out.println("This may change in the future when new builds are promoted.</em></p>");
      }

      out.println("</body>");
      out.println("</html>");
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

  protected void generateXML(final XMLOutput xml, final File folder, final String xmlName, final String entityName, final String typeName)
  {
    final File xmlFile = new File(folder, xmlName + ".xml");
    IO.writeFile(xmlFile, out -> {
      try
      {
        XMLOutput repoXML = new XMLOutput(out);
        repoXML.processingInstruction(entityName, "version=\"1.0.0\"");

        repoXML.element("repository");
        repoXML.attribute("name", name);
        repoXML.attribute("type", typeName);
        repoXML.attribute("version", "1.0.0");
        repoXML.push();

        repoXML.element("properties");
        repoXML.attribute("size", "3");
        repoXML.push();

        repoXML.element("property");
        repoXML.attribute("name", "p2.timestamp");
        repoXML.attribute("value", System.currentTimeMillis());

        repoXML.element("property");
        repoXML.attribute("name", "p2.compressed");
        repoXML.attribute("value", COMPRESS);

        repoXML.element("property");
        repoXML.attribute("name", "p2.mirrorsURL");
        repoXML.attribute("value", getURL(true) + "&format=xml");
        repoXML.pop();

        repoXML.element("children");
        repoXML.attribute("size", children.size());
        repoXML.push();

        for (String child : children)
        {
          repoXML.element("child");
          repoXML.attribute("location", child);
        }

        repoXML.pop();
        repoXML.pop();
        repoXML.done();

        if (COMPRESS)
        {
          xml.element("zip");
          xml.attribute("destfile", new File(folder, xmlName + ".jar"));
          xml.attribute("update", false);
          xml.push();
          xml.element("fileset");
          xml.attribute("dir", folder);
          xml.push();
          xml.element("include");
          xml.attribute("name", xmlFile.getName());
          xml.pop();
          xml.pop();
        }
      }
      catch (Exception ex)
      {
        throw new RuntimeException(ex);
      }
    });
  }

  /**
   * @author Eike Stepper
   */
  public static class Drops extends Repository
  {
    private String job;

    private String stream;

    private String types;

    private String surrogates;

    private boolean containsSurrogateDrop;

    private List<BuildInfo> buildInfos;

    public Drops(String name, File relativePath, String job, String stream, String types, int max, String surrogates, List<BuildInfo> allBuildInfos)
    {
      super(name, relativePath);
      this.job = job;
      this.stream = stream;
      this.types = types;
      this.surrogates = surrogates;

      buildInfos = allBuildInfos.stream().filter(testJobStreamTypesVisible(job, stream, types)).collect(Collectors.toList());
      Collections.sort(buildInfos);

      while (buildInfos.size() > max)
      {
        buildInfos.remove(max);
      }

      for (BuildInfo buildInfo : buildInfos)
      {
        addDrop(buildInfo);
      }

      if (surrogates != null && buildInfos.isEmpty())
      {
        StringTokenizer tokenizer = new StringTokenizer(surrogates, ",");
        while (tokenizer.hasMoreTokens())
        {
          String surrogate = tokenizer.nextToken().trim();
          if (attemptSurrogate(job, stream, surrogate, allBuildInfos))
          {
            containsSurrogateDrop = true;
            break;
          }
        }
      }
    }

    public final String getJob()
    {
      return job;
    }

    public final String getStream()
    {
      return stream;
    }

    public final String getTypes()
    {
      return types;
    }

    public String getSurrogates()
    {
      return surrogates;
    }

    public boolean containsSurrogateDrop()
    {
      return containsSurrogateDrop;
    }

    public final List<BuildInfo> getBuildInfos()
    {
      return buildInfos;
    }

    private boolean attemptSurrogate(String job, String stream, String surrogate, List<BuildInfo> allBuildInfos)
    {
      String[] segments = surrogate.split("/");
      String types;

      if (segments.length == 3)
      {
        job = segments[0];
        stream = segments[1];
        types = segments[2];
      }
      else if (segments.length == 2)
      {
        stream = segments[0];
        types = segments[1];
      }
      else if (segments.length == 1)
      {
        types = segments[0];
      }
      else
      {
        throw new IllegalArgumentException("Surrogate description should be in the format '[[job/]stream/]types': " + surrogate);
      }

      Optional<BuildInfo> buildInfo = allBuildInfos.stream().filter(testJobStreamTypesVisible(job, stream, types)).sorted().findFirst();
      if (buildInfo.isPresent())
      {
        addDrop(buildInfo.get());
        buildInfos.add(buildInfo.get());
        return true;
      }

      return false;
    }

    private static Predicate<BuildInfo> testJobStreamTypesVisible(String job, String stream, String types)
    {
      return BuildInfo.testJob(job).and(BuildInfo.testStream(stream).and(BuildInfo.testTypes(types).and(BuildInfo.testVisible())));
    }
  }
}
