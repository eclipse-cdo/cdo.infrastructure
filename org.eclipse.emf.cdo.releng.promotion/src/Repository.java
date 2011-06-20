import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
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

  public void generate(XMLOutput xml)
  {
    File folder = new File(base, path);
    folder.mkdirs();

    System.out.println("Generating repository " + name + ": " + folder.getAbsolutePath());
    for (String child : children)
    {
      System.out.println("   Adding child location: " + child);
    }

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
        // stream.println("<?xml version='1.0' encoding='UTF-8'?>");

        try
        {
          XMLOutput repoXML = new XMLOutput(out);
          // PrintStream stream = new PrintStream(out);
          // stream.println("<?" + entityName + " version='1.0.0'?>");
          // stream.flush();

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
          repoXML.attribute("value", "false");

          repoXML.element("property");
          repoXML.attribute("name", "p2.mirrorsURL");
          repoXML.attribute("value", "http://www.eclipse.org/downloads/download.php?file=/"
              + Config.getProperties().getProperty("projectMirrorsPrefix") + "/updates/" + path
              + "&amp;protocol=http&amp;format=xml");
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

          String match = "&lt;\\?xml version=\"1.0\" encoding=\"UTF-8\"\\?>";

          xml.element("replaceregexp");
          xml.attribute("file", xmlFile);
          xml.attribute("match", match);
          xml.attribute("replace", match + "\n&lt;?" + entityName + " version=\"1.0.0\"?>");

          xml.element("zip");
          xml.attribute("destfile", new File(folder, xmlName + ".jar"));
          xml.attribute("update", "false");
          xml.push();
          xml.element("fileset");
          xml.attribute("dir", folder);
          xml.push();
          xml.element("include");
          xml.attribute("name", xmlFile.getName());
          xml.pop();
          xml.pop();
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
