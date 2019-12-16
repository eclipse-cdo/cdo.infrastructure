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
    System.out.println("Generating repository " + name + ": " + path);
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
      out.println("href=\"http://wiki.eclipse.org/CDO\">wiki</a>.");
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

    private List<BuildInfo> buildInfos = new ArrayList<>();

    public Drops(String name, File relativePath, String job, String stream, String types, List<BuildInfo> buildInfos)
    {
      super(name, relativePath);
      this.job = job;
      this.stream = stream;
      this.types = types;

      for (BuildInfo buildInfo : buildInfos)
      {
        if (job != null && !job.equals(buildInfo.getJob()))
        {
          continue;
        }

        if (stream != null && !stream.equals(buildInfo.getStream()))
        {
          continue;
        }

        if (types != null && !types.contains(buildInfo.getType()))
        {
          continue;
        }

        File drop = buildInfo.getDrop();
        if (new File(drop, DropProcessor.MARKER_INVISIBLE).isFile())
        {
          continue;
        }

        addDrop(buildInfo);
        this.buildInfos.add(buildInfo);
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

    public final List<BuildInfo> getBuildInfos()
    {
      return buildInfos;
    }
  }
}
