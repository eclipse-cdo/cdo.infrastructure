/*
 * Copyright (c) 2004 - 2012 Eike Stepper (Loehne, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package promoter;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import promoter.util.Ant;
import promoter.util.IO;
import promoter.util.XMLOutput;

/**
 * @author Eike Stepper
 */
public class Promoter extends ComponentFactory implements Runnable
{
  private final boolean force;

  private final boolean skipCopyBuilds;

  private final boolean skipGenerateReleaseNotes;

  private SourceCodeManager scm;

  public static void main(String[] args) throws Exception
  {
    boolean force = Boolean.getBoolean("forcedPromotion");
    boolean skipCopyBuilds = Boolean.getBoolean("skipCopyBuilds");
    boolean skipGenerateReleaseNotes = Boolean.getBoolean("skipGenerateReleaseNotes");

    if (args != null)
    {
      for (String arg : args)
      {
        if ("--force".equals(arg))
        {
          force = true;
        }
        else if ("--skipCopyBuilds".equals(arg))
        {
          skipCopyBuilds = true;
        }
        else if ("--skipGenerateReleaseNotes".equals(arg))
        {
          skipGenerateReleaseNotes = true;
        }
      }
    }

    Promoter promoter = new Promoter(force, skipCopyBuilds, skipGenerateReleaseNotes);
    promoter.run();
  }

  public Promoter(boolean force, boolean skipCopyBuilds, boolean skipGenerateReleaseNotes)
  {
    this.force = force;
    this.skipCopyBuilds = skipCopyBuilds;
    this.skipGenerateReleaseNotes = skipGenerateReleaseNotes;
  }

  public SourceCodeManager getSourceCodeManager()
  {
    if (scm == null)
    {
      scm = createSourceCodeManager();
    }

    return scm;
  }

  @Override
  public void run()
  {
    System.out.println("----------------------------------------------------------------------------------------------------------");
    System.out.println("ProjectName          = " + PromoterConfig.INSTANCE.getProjectName());
    System.out.println("ProjectPath          = " + PromoterConfig.INSTANCE.getProjectPath());
    System.out.println("DownloadsHome        = " + PromoterConfig.INSTANCE.getDownloadsHome());
    System.out.println("DownloadsArea        = " + PromoterConfig.INSTANCE.getDownloadsArea());
    System.out.println("DropsArea            = " + PromoterConfig.INSTANCE.getDropsArea());
    System.out.println("CompositionArea      = " + PromoterConfig.INSTANCE.getCompositionArea());
    System.out.println("CompositionTempArea  = " + PromoterConfig.INSTANCE.getCompositionTempArea());
    System.out.println("ArchiveHome          = " + PromoterConfig.INSTANCE.getArchiveHome());
    System.out.println("ArchiveArea          = " + PromoterConfig.INSTANCE.getArchiveArea());
    System.out.println("ArchiveDropsArea     = " + PromoterConfig.INSTANCE.getArchiveDropsArea());
    System.out.println("WorkingArea          = " + PromoterConfig.INSTANCE.getWorkingArea());
    System.out.println("ProjectCloneLocation = " + PromoterConfig.INSTANCE.getProjectCloneLocation());
    System.out.println("InstallDirectory     = " + PromoterConfig.INSTANCE.getInstallDirectory());
    System.out.println("ConfigDirectory      = " + PromoterConfig.INSTANCE.getConfigDirectory());
    System.out.println("GitExecutable        = " + PromoterConfig.INSTANCE.getGitExecutable());
    System.out.println("GitRepositoryURL     = " + PromoterConfig.INSTANCE.getGitRepositoryURL());
    System.out.println("----------------------------------------------------------------------------------------------------------");

    List<BuildInfo> copiedBuilds = new ArrayList<>();

    if (!skipCopyBuilds)
    {
      BuildCopier buildCopier = createBuildCopier();

      try
      {
        copiedBuilds = buildCopier.copyBuilds();
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
        System.out.println();
      }
    }

    if (copiedBuilds.isEmpty())
    {
      System.out.println();
      System.out.print("No new builds have been found.");

      if (!force)
      {
        return;
      }
    }

    System.out.println();
    System.out.println();

    Ant<AntResult> ant = createAnt();
    AntResult result = ant.run(); // Calls processDropsAndComposeRepositories().
    List<BuildInfo> buildInfos = result.getBuildInfos();

    if (!skipGenerateReleaseNotes)
    {
      try
      {
        ReleaseNotesGenerator releaseNotesGenerator = createReleaseNotesGenerator();
        releaseNotesGenerator.generateReleaseNotes(buildInfos);
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
        System.out.println();
      }
    }

    WebNode rootNode = result.getRootNode();
    if (rootNode != null)
    {
      WebGenerator webGenerator = createWebGenerator();
      webGenerator.generateWeb(rootNode);
    }

    IO.writeFile(new File(PromoterConfig.INSTANCE.getDropsArea(), "drops.txt"), out -> {
      PrintStream stream = new PrintStream(out);

      for (BuildInfo buildInfo : buildInfos)
      {
        stream.println(buildInfo.getQualifier());
      }

      stream.flush();
    });

    System.out.println();
  }

  public AntResult processDropsAndComposeRepositories(XMLOutput xml) throws Exception
  {
    DropProcessor dropProcessor = createDropProcessor();
    List<BuildInfo> buildInfos = dropProcessor.processDrops(xml);

    RepositoryComposer repositoryComposer = createRepositoryComposer();
    WebNode webNode = repositoryComposer.composeRepositories(xml, buildInfos, PromoterConfig.INSTANCE.getConfigCompositesDirectory());

    deleteOldDrops(webNode, buildInfos);
    return new AntResult(buildInfos, webNode);
  }

  public void deleteOldDrops(WebNode webNode, List<BuildInfo> allBuildInfos)
  {
    Set<BuildInfo> composedDrops = new HashSet<>(webNode.getDrops(true));

    for (Iterator<BuildInfo> it = allBuildInfos.iterator(); it.hasNext();)
    {
      BuildInfo buildInfo = it.next();
      if (!composedDrops.contains(buildInfo) && buildInfo.isVisible())
      {
        System.out.println();
        System.out.println("Deleting drop: " + buildInfo);

        int files = IO.delete(buildInfo.getDrop());
        System.out.println("Deleted files: " + files);

        it.remove();
      }
    }
  }

  public Ant<AntResult> createAnt()
  {
    File script = new File(PromoterConfig.INSTANCE.getWorkingArea(), "promoter.ant");
    File basedir = PromoterConfig.INSTANCE.getDownloadsArea();
    return new DefaultAnt(script, basedir);
  }

  /**
   * @author Eike Stepper
   */
  public class DefaultAnt extends Ant<AntResult>
  {
    public DefaultAnt(File script, File basedir)
    {
      super(script, basedir);
    }

    @Override
    protected AntResult create(XMLOutput xml) throws Exception
    {
      return processDropsAndComposeRepositories(xml);
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
