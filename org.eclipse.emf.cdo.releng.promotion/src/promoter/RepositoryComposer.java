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

import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import promoter.Repository.Drops;
import promoter.util.Config;
import promoter.util.IO;
import promoter.util.XMLOutput;

/**
 * @author Eike Stepper
 */
public class RepositoryComposer extends PromoterComponent
{
  public RepositoryComposer()
  {
  }

  public WebNode composeRepositories(XMLOutput xml, List<BuildInfo> buildInfos, File baseFolder, File folder) throws SAXException
  {
    if (!folder.isDirectory())
    {
      return null;
    }

    if (IO.isExcluded(folder.getName()))
    {
      return null;
    }

    File relativePath = IO.makeRelative(folder, baseFolder);
    WebNode webNode = new WebNode(relativePath);

    Repository repository = createRepository(baseFolder, relativePath, buildInfos);
    if (repository == Repository.DISABLED)
    {
      return null;
    }

    if (repository != null)
    {
      repository.generate(xml);
      webNode.setRepository(repository);
    }

    File[] children = folder.listFiles();
    for (File child : children)
    {
      WebNode childWebNode = composeRepositories(xml, buildInfos, baseFolder, child);
      if (childWebNode != null)
      {
        webNode.getChildren().add(childWebNode);
      }
    }

    Collections.sort(webNode.getChildren());

    createSymLink(xml, webNode);
    return webNode;
  }

  protected Repository createRepository(File baseFolder, File relativePath, List<BuildInfo> buildInfos)
  {
    String path = relativePath.getPath();
    File configFolder = new File(baseFolder, path);
    Properties compositionProperties = Config.loadProperties(new File(configFolder, "composition.properties"), false);

    boolean disabled = Config.isDisabled(compositionProperties);
    if (disabled)
    {
      return Repository.DISABLED;
    }

    String name = compositionProperties.getProperty("composite.name");
    if (name == null)
    {
      return null;
    }

    Repository repository;

    File temp = PromoterConfig.INSTANCE.getCompositionTempArea();

    // This method is only called for 2. level repositories.
    // Strip off "composites/" from the beginning of the path.
    int pos = path.indexOf("/");
    if (pos == -1)
    {
      pos = path.indexOf("\\");
    }

    path = path.substring(pos + 1);

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

    repository.setProperties(compositionProperties);
    return repository;
  }

  protected void createSymLink(XMLOutput xml, WebNode webNode) throws SAXException
  {
    Repository repository = webNode.getRepository();
    if (repository == null)
    {
      return;
    }

    List<BuildInfo> drops = new ArrayList<>();
    collectAllDrops(webNode, drops);
    if (drops.isEmpty())
    {
      return;
    }

    BuildInfo latest = null;
    for (BuildInfo drop : drops)
    {
      if (drop.isLaterThan(latest))
      {
        latest = drop;
      }
    }

    if (latest != null)
    {
      String path = repository.getPath();
      File folder = new File(PromoterConfig.INSTANCE.getCompositionTempArea(), path);

      // File link = new File(folder, "latest");
      // File drop = new File(PromoterConfig.INSTANCE.getDropsArea(), latest.getQualifier());
      // String relativeDrop = IO.makeRelative(drop.getAbsolutePath(), new
      // File(PromoterConfig.INSTANCE.getCompositionArea(), path).getAbsolutePath());
      // xml.element("symlink");
      // xml.attribute("link", link.getAbsolutePath());
      // xml.attribute("resource", relativeDrop);

      File latestUrl = new File(folder, "latest.qualifier");
      appendFile(latestUrl, latest.getQualifier());

      File latestUrls = new File(PromoterConfig.INSTANCE.getCompositionTempArea(), "latest.qualifiers");
      String append = path.replace('/', '_').replace('.', '_') + " = " + latest.getQualifier() + "\n";
      appendFile(latestUrls, append);
    }
  }

  protected void appendFile(File file, String line)
  {
    OutputStream out = null;

    try
    {
      out = new FileOutputStream(file, true);
      PrintStream stream = new PrintStream(out);

      stream.print(line);
      stream.flush();
    }
    catch (IOException ex)
    {
      throw new RuntimeException(ex);
    }
    finally
    {
      IO.close(out);
    }
  }

  protected void collectAllDrops(WebNode webNode, List<BuildInfo> drops)
  {
    Repository repository = webNode.getRepository();
    if (repository instanceof Drops)
    {
      Drops dropsRepository = (Drops)repository;
      drops.addAll(dropsRepository.getBuildInfos());
    }

    for (WebNode childNode : webNode.getChildren())
    {
      collectAllDrops(childNode, drops);
    }
  }
}
