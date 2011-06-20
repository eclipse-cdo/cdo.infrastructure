import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
public class Repository
{
  private File base;

  private String name;

  private String path;

  private int pathLevel;

  private List<String> children = new ArrayList<String>();

  public Repository(File base, String name, String path)
  {
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
    children.add(child);
    File folder = new File(base, child);
    if (new File(folder, "categories").isDirectory())
    {
      children.add(child + "/categories");
    }
  }

  public void generate()
  {
    File folder = new File(base, path);
    folder.mkdirs();

    generateXML(folder, "compositeArtifacts.xml", "compositeArtifactRepository",
        "org.eclipse.equinox.internal.p2.artifact.repository.CompositeArtifactRepository");
    generateXML(folder, "compositeContent.xml", "compositeMetadataRepository",
        "org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository");
  }

  private void generateXML(final File folder, String xmlName, final String entityName, final String typeName)
  {
    IO.writeFile(new File(folder, xmlName), new IO.OutputHandler()
    {
      public void handleOutput(OutputStream out) throws IOException
      {
        PrintStream stream = new PrintStream(out);
        stream.println("<?xml version='1.0' encoding='UTF-8'?>");
        stream.println("<?" + entityName + " version='1.0.0'?>");
        stream.flush();

        try
        {
          XMLOutput xml = new XMLOutput(out);
          stream.println("<?" + typeName + " version='1.0.0'?>");

          xml.element("repository");
          xml.attribute("name", name);
          xml.attribute("type", typeName);
          xml.attribute("version", "1.0.0");

          xml.element("properties");
          xml.attribute("size", "3");
          xml.push();

          xml.element("property");
          xml.attribute("name", "p2.timestamp");
          xml.attribute("value", System.currentTimeMillis());

          xml.element("property");
          xml.attribute("name", "p2.compressed");
          xml.attribute("value", "false");

          xml.element("property");
          xml.attribute("name", "p2.mirrorsURL");
          xml.attribute("value", "http://www.eclipse.org/downloads/download.php?file=/"
              + Config.getProperties().getProperty("projectMirrorsPrefix") + "/updates/" + path
              + "&amp;protocol=http&amp;format=xml");
          xml.pop();

          xml.element("children");
          xml.attribute("size", children.size());
          xml.push();

          for (String child : children)
          {
            xml.element("child");
            xml.attribute("location", child);
          }

          xml.pop();
          xml.done();
        }
        catch (Exception ex)
        {
          throw new RuntimeException(ex);
        }
      }
    });
  }

  /**
   * @author Eike Stepper
   */
  public static class Filtered extends Repository
  {
    private String stream;

    private String types;

    public Filtered(File base, String name, String path, String stream, String types, List<BuildInfo> buildInfos)
    {
      super(base, name, path);
      this.stream = stream;
      this.types = types;

      for (BuildInfo buildInfo : buildInfos)
      {
        if (stream != null && buildInfo.getStream() != stream)
        {
          continue;
        }

        if (types != null && !types.contains(buildInfo.getType()))
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
      }
    }

    public final String getStream()
    {
      return stream;
    }

    public final String getTypes()
    {
      return types;
    }
  }
}
