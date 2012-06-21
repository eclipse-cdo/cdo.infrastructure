/*
 * Copyright (c) 2004 - 2012 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package promoter;

import promoter.util.IO;
import promoter.util.XMLOutput;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * @author Eike Stepper
 */
public class Repository
{
  private static final boolean COMPRESS = false;

  private File base;

  private String name;

  private String path;

  private int pathLevel;

  private List<String> children = new ArrayList<String>();

  private List<Repository> childRepositories = new ArrayList<Repository>();

  private String childRetention;

  private String targetInfo;

  private String targetVersions;

  private String apiBaselineURL;

  private String apiBaselineSize;

  private String webLabel;

  private int webPriority;

  private boolean webCollapsed;

  public Repository(File base, String name, String path)
  {
    System.out.println();
    System.out.println("Generating repository " + name + ": " + new File(base, path).getAbsolutePath());

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

  public final List<Repository> getChildRepositories()
  {
    return childRepositories;
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

    generateXML(xml, folder, "compositeArtifacts", "compositeArtifactRepository",
        "org.eclipse.equinox.internal.p2.artifact.repository.CompositeArtifactRepository");
    generateXML(xml, folder, "compositeContent", "compositeMetadataRepository",
        "org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository");
  }

  private void generateXML(final XMLOutput xml, final File folder, final String xmlName, final String entityName,
      final String typeName)
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

    public Drops(File base, String name, String path, String job, String stream, String types,
        List<BuildInfo> buildInfos)
    {
      super(base, name, path);
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

        String child = "../";
        for (int i = 0; i < getPathLevel(); i++)
        {
          child += "../";
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
