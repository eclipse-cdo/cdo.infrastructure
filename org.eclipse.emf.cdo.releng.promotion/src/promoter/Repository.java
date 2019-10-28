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
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import promoter.util.IO;
import promoter.util.XMLOutput;

/**
 * @author Eike Stepper
 */
public class Repository
{
  public static final Repository DISABLED = new Repository();

  public static final boolean COMPRESS = false;

  private static final String PARENT_DIRECTORY = "../";

  private RepositoryComposer composer;

  private File base;

  private String name;

  private String path;

  private int pathLevel;

  private List<String> children = new ArrayList<String>();

  private String childRetention;

  private String targetInfo;

  private String targetVersions;

  private String apiBaselineURL;

  private String apiBaselineSize;

  private String webLabel;

  private int webPriority;

  private boolean webCollapsed;

  private Repository()
  {
  }

  public Repository(RepositoryComposer composer, File base, String name, String path)
  {
    System.out.println();
    System.out.println("Generating repository " + name + ": " + new File(base, path).getAbsolutePath());

    this.composer = composer;
    this.base = base;
    this.name = name;
    this.path = path;

    StringTokenizer tokenizer = new StringTokenizer(path, "/");
    while (tokenizer.hasMoreTokens())
    {
      tokenizer.nextToken();
      ++pathLevel;
    }
  }

  public final RepositoryComposer getComposer()
  {
    return composer;
  }

  public final File getBase()
  {
    return base;
  }

  public final String getName()
  {
    return name;
  }

  public final String getPath()
  {
    return path;
  }

  public final int getPathLevel()
  {
    return pathLevel;
  }

  public final List<String> getChildren()
  {
    return Collections.unmodifiableList(children);
  }

  public void addChild(String child)
  {
    try
    {
      File composite = new File(base, path);
      File folder = new File(composite, child).getCanonicalFile();

      System.out.println("   Adding child location: " + folder);
      children.add(child);
    }
    catch (IOException ex)
    {
      throw new RuntimeException(ex);
    }
  }

  public String getChildRetention()
  {
    return childRetention;
  }

  public String getTargetInfo()
  {
    return targetInfo;
  }

  public String getTargetVersions()
  {
    return targetVersions;
  }

  public String getApiBaselineURL()
  {
    return apiBaselineURL;
  }

  public String getApiBaselineSize()
  {
    return apiBaselineSize;
  }

  public String getWebLabel()
  {
    return webLabel;
  }

  public int getWebPriority()
  {
    return webPriority;
  }

  public boolean isWebCollapsed()
  {
    return webCollapsed;
  }

  public String getAnchorName()
  {
    StringBuilder builder = new StringBuilder();
    for (char c : path.toCharArray())
    {
      if (Character.isJavaIdentifierPart(c))
      {
        builder.append(c);
      }
      else
      {
        builder.append('_');
      }
    }

    return builder.toString();
  }

  public String getDownloadsURL(String... paths)
  {
    StringBuilder builder = new StringBuilder(path);
    for (String p : paths)
    {
      builder.append("/");
      builder.append(p);
    }

    return PromoterConfig.INSTANCE.formatUpdateURL(builder.toString());
  }

  public void generate(XMLOutput xml)
  {
    File folder = new File(base, path);
    folder.mkdirs();

    IO.writeFile(new File(folder, "composition.properties"), new IO.OutputHandler()
    {
      public void handleOutput(OutputStream out) throws IOException
      {
        PrintStream stream = new PrintStream(out);
        stream.println("composite.name=" + name);
        stream.flush();
      }
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
      out.println("         <a href=\"http://help.eclipse.org/helios/index.jsp?topic=/org.eclipse.platform.doc.user/tasks/tasks-124.htm\">");
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
    IO.writeFile(xmlFile, new IO.OutputHandler()
    {
      public void handleOutput(OutputStream out) throws IOException
      {
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
          repoXML.attribute("value", PromoterConfig.INSTANCE.formatUpdateURL(path) + "&amp;format=xml");
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
      }
    });
  }

  void setProperties(Properties properties)
  {
    childRetention = properties.getProperty("child.retention");
    targetInfo = properties.getProperty("target.info");
    targetVersions = properties.getProperty("target.versions", "");
    apiBaselineURL = properties.getProperty("api.baseline.url");
    apiBaselineSize = properties.getProperty("api.baseline.size", "");
    webLabel = properties.getProperty("web.label", name);
    webPriority = Integer.parseInt(properties.getProperty("web.priority", "500"));
    webCollapsed = Boolean.parseBoolean(properties.getProperty("web.collapsed", "false"));
  }

  /**
   * @author Eike Stepper
   */
  public static class Drops extends Repository
  {
    private String job;

    private String stream;

    private String types;

    private List<BuildInfo> buildInfos = new ArrayList<BuildInfo>();

    public Drops(RepositoryComposer composer, File base, String name, String path, String job, String stream, String types, List<BuildInfo> buildInfos)
    {
      super(composer, base, name, path);
      this.job = job;
      this.stream = stream;
      this.types = types;

      for (BuildInfo buildInfo : buildInfos)
      {
        if (job != null && !buildInfo.getJob().equals(job))
        {
          continue;
        }

        if (stream != null && !buildInfo.getStream().equals(stream))
        {
          continue;
        }

        if (types != null && !types.contains(buildInfo.getType()))
        {
          continue;
        }

        File drop = new File(PromoterConfig.INSTANCE.getDropsArea(), buildInfo.getQualifier());
        if (new File(drop, DropProcessor.MARKER_INVISIBLE).isFile())
        {
          continue;
        }

        String child = PARENT_DIRECTORY;
        for (int i = 0; i < getPathLevel(); i++)
        {
          child += PARENT_DIRECTORY;
        }

        child += "drops/" + buildInfo.getQualifier();
        addChild(child);
        addChild(child + "/categories");

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
