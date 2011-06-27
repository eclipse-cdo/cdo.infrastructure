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
package promoter;

import org.xml.sax.SAXException;

import promoter.util.Config;
import promoter.util.IO;
import promoter.util.XMLOutput;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * @author Eike Stepper
 */
public class RepositoryComposer
{
  public RepositoryComposer()
  {
  }

  public WebNode composeRepositories(XMLOutput xml, List<BuildInfo> buildInfos, File folder) throws SAXException
  {
    if (folder.isDirectory())
    {
      String compositeName = folder.getName();
      if (!IO.isExcluded(compositeName))
      {
        WebNode webNode = new WebNode(folder);

        Repository repository = getRepository(folder, buildInfos);
        if (repository != null)
        {
          repository.generate(xml);
          webNode.setRepository(repository);
        }

        for (File child : folder.listFiles())
        {
          WebNode childWebNode = composeRepositories(xml, buildInfos, child);
          if (childWebNode != null)
          {
            webNode.getChildren().add(childWebNode);
          }
        }

        Collections.sort(webNode.getChildren());
        return webNode;
      }
    }

    return null;
  }

  protected Repository getRepository(File compositeDir, List<BuildInfo> buildInfos)
  {
    Properties compositionProperties = Config.loadProperties(new File(compositeDir, "composition.properties"), false);
    String name = compositionProperties.getProperty("composite.name");
    if (name == null)
    {
      return null;
    }

    Repository repository;

    File temp = PromoterConfig.INSTANCE.getCompositionTempArea();
    String path = compositeDir.getPath();
    path = path.substring(path.indexOf("/") + 1);

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

    String targetInfo = compositionProperties.getProperty("target.info");
    repository.setTargetInfo(targetInfo);

    String apiBaselineURL = compositionProperties.getProperty("api.baseline.url");
    repository.setApiBaselineURL(apiBaselineURL);

    String apiBaselineSize = compositionProperties.getProperty("api.baseline.size", "");
    repository.setApiBaselineSize(apiBaselineSize);

    String webLabel = compositionProperties.getProperty("web.label", repository.getName());
    repository.setWebLabel(webLabel);

    int webPriority = Integer.parseInt(compositionProperties.getProperty("web.priority", "500"));
    repository.setWebPriority(webPriority);

    boolean webCollapsed = Boolean.parseBoolean(compositionProperties.getProperty("web.collapsed", "false"));
    repository.setWebCollapsed(webCollapsed);

    return repository;
  }
}
