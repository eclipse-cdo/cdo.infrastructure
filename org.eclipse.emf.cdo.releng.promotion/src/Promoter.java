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
import util.Ant;
import util.XMLOutput;

import java.io.File;
import java.util.List;

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

    Ant<WebNode> ant = createAnt();
    WebNode webNode = ant.run();

    if (webNode != null)
    {
      DocumentGenerator documentGenerator = createDocumentGenerator();
      documentGenerator.generateDocument(webNode);
    }

    System.out.println();
  }

  protected BuildCopier createBuildCopier()
  {
    return createReflective(BuildCopier.class);
  }

  protected DropProcessor createDropProcessor()
  {
    return createReflective(DropProcessor.class);
  }

  protected RepositoryComposer createRepositoryComposer()
  {
    return createReflective(RepositoryComposer.class);
  }

  protected DocumentGenerator createDocumentGenerator()
  {
    return createReflective(DocumentGenerator.class);
  }

  protected Ant<WebNode> createAnt()
  {
    File script = new File(PromoterConfig.INSTANCE.getWorkingArea(), "promoter.ant");
    File basedir = PromoterConfig.INSTANCE.getDownloadsArea();
    return new DefaultAnt(script, basedir);
  }

  private <T> T createReflective(Class<T> type)
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
  protected class DefaultAnt extends Ant<WebNode>
  {
    public DefaultAnt(File script, File basedir)
    {
      super(PromoterConfig.INSTANCE.getDirectory("ANT_HOME"), script, basedir);
    }

    @Override
    protected WebNode create(XMLOutput xml) throws Exception
    {
      DropProcessor dropProcessor = createDropProcessor();
      List<BuildInfo> buildInfos = dropProcessor.processDrops(xml);

      RepositoryComposer repositoryComposer = createRepositoryComposer();
      return repositoryComposer.composeRepositories(xml, buildInfos, new File("composites"));
    }
  }
}
