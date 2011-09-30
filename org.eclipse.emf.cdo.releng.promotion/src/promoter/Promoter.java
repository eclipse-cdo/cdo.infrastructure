/*
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

import promoter.util.Ant;
import promoter.util.IO;
import promoter.util.XMLOutput;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Eike Stepper
 */
public class Promoter
{
  private static boolean force;

  public static void main(String[] args) throws Exception
  {
    if (args != null && args.length != 0)
    {
      if ("--force".equals(args[0]))
      {
        force = true;
      }
    }

    Promoter main = new Promoter();
    main.run();
  }

  public void run()
  {
    BuildCopier buildCopier = createBuildCopier();
    List<BuildInfo> builds = buildCopier.copyBuilds();

    List<Task> tasks = performTasks(builds);

    if (builds.isEmpty() && tasks.isEmpty())
    {
      System.out.println();
      System.out.print("No new builds or tasks have been found.");

      if (!force)
      {
        System.out.println(" Exiting...");
        System.out.println();
        return;
      }

      System.out.println();
      System.out.println();
    }

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

  public List<Task> performTasks(List<BuildInfo> builds)
  {
    List<Task> tasks = new ArrayList<Task>();
    File publicFolder = new File(PromoterConfig.INSTANCE.getWorkingArea(), "public");
    File taskFolder = new File(publicFolder, "tasks.inprogress");
    if (taskFolder.isDirectory())
    {
      try
      {
        for (File file : taskFolder.listFiles())
        {
          if (file.isFile() && file.getName().endsWith(".task"))
          {
            String content = IO.readTextFile(file);
            List<String> args = new ArrayList<String>(Arrays.asList(content.split("\n")));
            String type = args.remove(0);

            Task task = createComponent("promoter.tasks." + type + "Task");

            System.out.println();
            System.out.println("Performing " + task.getClass().getName());

            if (task.execute(args))
            {
              System.out.println("   Ordering recomposition...");
              tasks.add(task);
            }
          }
        }
      }
      catch (RuntimeException ex)
      {
        throw ex;
      }
      catch (Exception ex)
      {
        throw new RuntimeException(ex);
      }
      finally
      {
        IO.delete(taskFolder);
        IO.delete(new File(publicFolder, "tasks.tmp")); // Can be left by PHP
      }
    }

    return tasks;
  }

  public SourceCodeManager createSourceCodeManager()
  {
    return createComponent(SourceCodeManager.class);
  }

  public IssueManager createIssueManager()
  {
    return createComponent(IssueManager.class);
  }

  public BuildCopier createBuildCopier()
  {
    return createComponent(BuildCopier.class);
  }

  public DropProcessor createDropProcessor()
  {
    return createComponent(DropProcessor.class);
  }

  public ReleaseNotesGenerator createReleaseNotesGenerator()
  {
    return createComponent(ReleaseNotesGenerator.class);
  }

  public RepositoryComposer createRepositoryComposer()
  {
    return createComponent(RepositoryComposer.class);
  }

  public WebGenerator createWebGenerator()
  {
    return createComponent(WebGenerator.class);
  }

  public Ant<Map.Entry<List<BuildInfo>, WebNode>> createAnt()
  {
    File script = new File(PromoterConfig.INSTANCE.getWorkingArea(), "promoter.ant");
    File basedir = PromoterConfig.INSTANCE.getDownloadsArea();
    return new DefaultAnt(script, basedir);
  }

  public <T> T createComponent(Class<T> type)
  {
    String name = PromoterConfig.INSTANCE.getProperty("class" + type.getSimpleName(), type.getName());
    return createComponent(name);
  }

  public <T> T createComponent(String name)
  {
    try
    {
      @SuppressWarnings("unchecked")
      Class<T> c = (Class<T>)getClass().getClassLoader().loadClass(name);

      T component = c.newInstance();
      if (component instanceof PromoterComponent)
      {
        PromoterComponent promoterComponent = (PromoterComponent)component;
        promoterComponent.setPromoter(this);
      }

      return component;
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
      DropProcessor dropProcessor = createDropProcessor();
      final List<BuildInfo> buildInfos = dropProcessor.processDrops(xml);

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
