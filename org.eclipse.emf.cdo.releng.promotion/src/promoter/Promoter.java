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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import promoter.util.Ant;
import promoter.util.IO;
import promoter.util.XMLOutput;

/**
 * @author Eike Stepper
 */
public class Promoter
{
  private boolean force;

  public Promoter(boolean force)
  {
    this.force = force;
  }

  public boolean isForce()
  {
    return force;
  }

  public void run()
  {
    if (true)
    {
      if (false)
      {
        for (Entry<Object, Object> entry : System.getProperties().entrySet())
        {
          System.out.println(entry.getKey() + " = " + entry.getValue());
        }

        System.out.println();
        System.out.println();
      }

      System.out.println("----------------------------------------------------------------------------------------");
      System.out.println("DownloadsPath       = " + PromoterConfig.INSTANCE.getDownloadsPath());
      System.out.println("DownloadsHome       = " + PromoterConfig.INSTANCE.getDownloadsHome());
      System.out.println("DownloadsArea       = " + PromoterConfig.INSTANCE.getDownloadsArea());
      System.out.println("DropsArea           = " + PromoterConfig.INSTANCE.getDropsArea());
      System.out.println("CompositionArea     = " + PromoterConfig.INSTANCE.getCompositionArea());
      System.out.println("CompositionTempArea = " + PromoterConfig.INSTANCE.getCompositionTempArea());
      System.out.println("WorkingArea         = " + PromoterConfig.INSTANCE.getWorkingArea());
      System.out.println("ProjectRelengArea   = " + PromoterConfig.INSTANCE.getProjectRelengArea());
      System.out.println("AntHome             = " + PromoterConfig.INSTANCE.getAntHome());
      System.out.println("UserDirectory       = " + PromoterConfig.INSTANCE.getUserDirectory());
      System.out.println("JobsURL             = " + PromoterConfig.INSTANCE.getJobsURL());
      System.out.println("----------------------------------------------------------------------------------------");
      return;
    }

    BuildCopier buildCopier = createBuildCopier();
    List<BuildInfo> builds = new ArrayList<BuildInfo>();

    try
    {
      builds = buildCopier.copyBuilds();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      System.out.println();
    }

    List<Task> tasks = performTasks(builds);

    if (builds.isEmpty() && tasks.isEmpty())
    {
      if (force)
      {
        System.out.println();
        System.out.print("No new builds or tasks have been found.");
      }
      else
      {
        return;
      }

      System.out.println();
      System.out.println();
    }

    Ant<AntResult> ant = createAnt();
    AntResult result = ant.run();

    try
    {
      ReleaseNotesGenerator releaseNotesGenerator = createReleaseNotesGenerator();
      releaseNotesGenerator.generateReleaseNotes(result.getBuildInfos());
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      System.out.println();
    }

    WebNode rootNode = result.getRootNode();
    if (rootNode != null)
    {
      WebGenerator webGenerator = createWebGenerator();
      webGenerator.generateWeb(rootNode);
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

            if (task.execute(args, builds))
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

  public Ant<AntResult> createAnt()
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

  public static void main(String[] args) throws Exception
  {
    boolean force = false;
    if (args != null)
    {
      for (String arg : args)
      {
        if ("--force".equals(arg))
        {
          force = true;
        }
      }
    }

    Promoter main = new Promoter(force);
    main.run();
  }

  /**
   * @author Eike Stepper
   */
  public class DefaultAnt extends Ant<AntResult>
  {
    public DefaultAnt(File script, File basedir)
    {
      super(PromoterConfig.INSTANCE.getDirectory("ANT_HOME"), script, basedir);
    }

    @Override
    protected AntResult create(XMLOutput xml) throws Exception
    {
      DropProcessor dropProcessor = createDropProcessor();
      final List<BuildInfo> buildInfos = dropProcessor.processDrops(xml);

      RepositoryComposer repositoryComposer = createRepositoryComposer();
      final WebNode webNode = repositoryComposer.composeRepositories(xml, buildInfos, new File("composites"));

      return new AntResult(buildInfos, webNode);
    }
  }

  /**
   * @author Eike Stepper
   */
  public static class AntResult
  {
    private List<BuildInfo> buildInfos;

    private WebNode rootNode;

    public AntResult(List<BuildInfo> buildInfos, WebNode rootNode)
    {
      this.buildInfos = buildInfos;
      this.rootNode = rootNode;
    }

    public List<BuildInfo> getBuildInfos()
    {
      return buildInfos;
    }

    public WebNode getRootNode()
    {
      return rootNode;
    }
  }
}
