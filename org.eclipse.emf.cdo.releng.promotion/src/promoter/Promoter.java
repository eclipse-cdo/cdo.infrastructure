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

import promoter.issues.IssueManager;
import promoter.scm.SCM;
import promoter.util.Ant;
import promoter.util.XMLOutput;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Eike Stepper
 */
public class Promoter
{
  public static void main(String[] args) throws Exception
  {
    Promoter main = new Promoter();
    main.run();
  }

  public void run()
  {
    BuildCopier buildCopier = createBuildCopier();
    buildCopier.copyBuilds();

    // performTasks();

    Ant<Entry<List<BuildInfo>, WebNode>> ant = createAnt();
    Entry<List<BuildInfo>, WebNode> result = ant.run();

    ReleaseNotesGenerator releaseNotesGenerator = createReleaseNotesGenerator();
    releaseNotesGenerator.generateReleaseNotes(result.getKey());

    WebNode webNode = result.getValue();
    if (webNode != null)
    {
      WebGenerator webGenerator = createWebGenerator();
      webGenerator.generateWeb(webNode);
    }

    System.out.println();
  }

  public SCM createSCM()
  {
    return create(SCM.class);
  }

  public IssueManager createIssueManager()
  {
    return create(IssueManager.class);
  }

  public BuildCopier createBuildCopier()
  {
    BuildCopier buildCopier = create(BuildCopier.class);
    buildCopier.setPromoter(this);
    return buildCopier;
  }

  public BuildProcessor createBuildProcessor()
  {
    return create(BuildProcessor.class);
  }

  public ReleaseNotesGenerator createReleaseNotesGenerator()
  {
    return create(ReleaseNotesGenerator.class);
  }

  public RepositoryComposer createRepositoryComposer()
  {
    return create(RepositoryComposer.class);
  }

  public WebGenerator createWebGenerator()
  {
    return create(WebGenerator.class);
  }

  public Ant<Map.Entry<List<BuildInfo>, WebNode>> createAnt()
  {
    File script = new File(PromoterConfig.INSTANCE.getWorkingArea(), "promoter.ant");
    File basedir = PromoterConfig.INSTANCE.getDownloadsArea();
    return new DefaultAnt(script, basedir);
  }

  public <T> T create(Class<T> type)
  {
    String name = PromoterConfig.INSTANCE.getProperty("class" + type.getSimpleName(), type.getName());

    try
    {
      @SuppressWarnings("unchecked")
      Class<T> c = (Class<T>)getClass().getClassLoader().loadClass(name);
      return c.newInstance();
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  /**
   * @author Eike Stepper
   */
  public class DefaultAnt extends Ant<Map.Entry<List<BuildInfo>, WebNode>>
  {
    public DefaultAnt(File script, File basedir)
    {
      super(PromoterConfig.INSTANCE.getDirectory("ANT_HOME"), script, basedir);
    }

    @Override
    protected Map.Entry<List<BuildInfo>, WebNode> create(XMLOutput xml) throws Exception
    {
      BuildProcessor buildProcessor = createBuildProcessor();
      final List<BuildInfo> buildInfos = buildProcessor.processBuilds(xml);

      RepositoryComposer repositoryComposer = createRepositoryComposer();
      final WebNode webNode = repositoryComposer.composeRepositories(xml, buildInfos, new File("composites"));

      return new Map.Entry<List<BuildInfo>, WebNode>()
      {
        public List<BuildInfo> getKey()
        {
          return buildInfos;
        }

        public WebNode getValue()
        {
          return webNode;
        }

        public WebNode setValue(WebNode value)
        {
          throw new UnsupportedOperationException();
        }
      };
    }
  }
}
