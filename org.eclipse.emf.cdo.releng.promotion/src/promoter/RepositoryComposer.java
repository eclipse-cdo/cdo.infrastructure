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
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

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

  public WebNode composeRepositories(XMLOutput xml, List<BuildInfo> buildInfos, File configFolder) throws SAXException
  {
    if (!configFolder.isDirectory())
    {
      return null;
    }

    if (IO.isExcluded(configFolder.getName()))
    {
      return null;
    }

    Properties compositionProperties = Config.loadProperties(new File(configFolder, "composition.properties"), false);
    if (Config.isDisabled(compositionProperties))
    {
      return null;
    }

    File relativePath = PromoterConfig.INSTANCE.getConfigCompositesDirectory().toPath().relativize(configFolder.toPath()).toFile();
    WebNode webNode = new WebNode(relativePath, compositionProperties);

    Repository repository = createRepository(relativePath, compositionProperties, buildInfos);
    if (repository != null)
    {
      repository.generate(xml);
      webNode.setRepository(repository);
    }

    File[] children = configFolder.listFiles();
    for (File child : children)
    {
      WebNode childWebNode = composeRepositories(xml, buildInfos, child);
      if (childWebNode != null)
      {
        webNode.getChildren().add(childWebNode);
      }
    }

    Collections.sort(webNode.getChildren());

    if (repository != null)
    {
      BuildInfo latestDrop = webNode.getLatestDrop(false);
      if (latestDrop != null)
      {
        String latestQualifier = latestDrop.getQualifier();
        String path = relativePath.getPath();
        File compositionFolder = new File(PromoterConfig.INSTANCE.getCompositionTempArea(), path);

        File latestUrl = new File(compositionFolder, "latest.qualifier");
        appendFile(latestUrl, latestQualifier);

        File latestUrls = new File(PromoterConfig.INSTANCE.getCompositionTempArea(), "latest.qualifiers");
        appendFile(latestUrls, path.replace('/', '_').replace('\\', '_').replace('.', '_') + " = " + latestQualifier + "\n");

        Repository latestRepository = createLatestRepository(new File(relativePath, "latest"), compositionProperties, latestDrop);
        if (latestRepository != null)
        {
          latestRepository.generate(xml);
          webNode.setLatestRepository(latestRepository);

          TPMacroSetup.copyToLatestRepository(latestDrop, latestRepository);
        }

        String helpPath = compositionProperties.getProperty("latest.help");
        if (helpPath != null && helpPath.length() > 0)
        {
          File help = new File(latestDrop.getDrop(), "help");
          if (help.isDirectory())
          {
            xml.element("copy");
            xml.attribute("todir", new File(compositionFolder, helpPath));
            xml.attribute("preservelastmodified", true);
            xml.attribute("failonerror", false);
            xml.push();
            xml.element("fileset");
            xml.attribute("dir", help);
            xml.push();
            xml.element("include");
            xml.attribute("name", "**/*");
            xml.pop();
            xml.pop();
          }
        }
      }
    }

    return webNode;
  }

  protected Repository createRepository(File relativePath, Properties compositionProperties, List<BuildInfo> buildInfos)
  {
    String name = compositionProperties.getProperty("composite.name");
    if (name == null)
    {
      return null;
    }

    String childLocations = compositionProperties.getProperty("child.locations");
    if (childLocations != null && !childLocations.isEmpty())
    {
      Repository repository = new Repository(name, relativePath);

      StringTokenizer tokenizer = new StringTokenizer(childLocations, ",");
      while (tokenizer.hasMoreTokens())
      {
        String childLocation = tokenizer.nextToken().trim();
        repository.addChild(childLocation);
      }

      return repository;
    }

    String childJob = compositionProperties.getProperty("child.job");
    String childStream = compositionProperties.getProperty("child.stream");
    String childTypes = compositionProperties.getProperty("child.types");
    int childMax = Integer.parseInt(compositionProperties.getProperty("child.max", Integer.toString(Integer.MAX_VALUE)));
    String childSurrogates = compositionProperties.getProperty("child.surrogates");

    return new Repository.Drops(name, relativePath, childJob, childStream, childTypes, childMax, childSurrogates, buildInfos);
  }

  protected Repository createLatestRepository(File relativePath, Properties compositionProperties, BuildInfo buildInfo)
  {
    String name = compositionProperties.getProperty("latest.name");
    if (name == null)
    {
      return null;
    }

    Repository repository = new Repository(name, relativePath);
    repository.addDrop(buildInfo);
    return repository;
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
}
