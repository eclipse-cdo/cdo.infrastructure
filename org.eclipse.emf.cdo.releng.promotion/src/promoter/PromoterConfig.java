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

import promoter.util.Config;
import promoter.util.Util;

/**
 * @author Eike Stepper
 */
public final class PromoterConfig extends Config
{
  private static final String FILE = System.getProperty("promoter.properties", "config/promoter.properties");

  public static final PromoterConfig INSTANCE = new PromoterConfig(FILE);

  private PromoterConfig(String filename)
  {
    super(filename);
  }

  public boolean isTest()
  {
    return "true".equalsIgnoreCase(getProperty("test"));
  }

  public File getGitExecutable()
  {
    return getFile("GIT_EXECUTABLE");
  }

  public String getGitRepositoryPath()
  {
    return getProperty("gitRepositoryPath");
  }

  public String getGitRepositoryURL()
  {
    return getProperty("gitRepositoryURL");
  }

  public File getProjectCloneLocation()
  {
    String gitRepositoryPath = getGitRepositoryPath();
    if (gitRepositoryPath != null)
    {
      return new File(getWorkingArea(), gitRepositoryPath);
    }

    return getDirectory("projectCloneLocation"); // Deprecated.
  }

  public File getAntHome()
  {
    return getDirectory("ANT_HOME");
  }

  public File getInstallDirectory()
  {
    return getUserDirectory();
  }

  public File getXSLDirectory()
  {
    return new File(getInstallDirectory(), "xsl");
  }

  public File getTemplatesDirectory()
  {
    return new File(getInstallDirectory(), "templates");
  }

  public File getConfigDirectory()
  {
    return new File(getInstallDirectory(), "config");
  }

  public File getConfigCompositesDirectory()
  {
    return new File(getConfigDirectory(), "composites");
  }

  public File getWorkingArea()
  {
    return getDirectory("workingArea");
  }

  public String getProjectName()
  {
    return getProperty("projectName");
  }

  public String getProjectPath()
  {
    String projectPath = getProperty("projectPath");
    projectPath = Util.rstrip(projectPath, "/");

    if (isTest())
    {
      projectPath += "/test";
    }

    return projectPath;
  }

  public File getCompositionArea()
  {
    return new File(getDownloadsArea(), getCompositionPath());
  }

  public File getCompositionTempArea()
  {
    return new File(getDownloadsArea(), getCompositionTempPath());
  }

  public String getCompositionPath()
  {
    return getProperty("compositionPath");
  }

  public String getCompositionTempPath()
  {
    return getProperty("compositionTempPath");
  }

  public File getDropsArea()
  {
    return new File(getDownloadsArea(), "drops");
  }

  public String getArchiveURL()
  {
    return getProperty("ARCHIVE_URL");
  }

  public File getArchiveHome()
  {
    return getDirectory("ARCHIVE_HOME");
  }

  public File getArchiveArea()
  {
    return new File(getArchiveHome(), getProjectPath());
  }

  public File getArchiveDropsArea()
  {
    return new File(getArchiveArea(), "drops");
  }

  public File getDownloadsArea()
  {
    return new File(getDownloadsHome(), getProjectPath());
  }

  @Deprecated
  public File getProjectRelengArea()
  {
    return getDirectory("projectRelengArea");
  }

  public File getDownloadsHome()
  {
    return getDirectory("DOWNLOADS_HOME");
  }

  public String getDownloadsURL()
  {
    return getProperty("DOWNLOADS_URL");
  }

  public String getJobsURL()
  {
    String jobURL = getProperty("JOB_URL");
    if (jobURL != null)
    {
      int firstJob = jobURL.indexOf("/job/");
      if (firstJob != -1)
      {
        return jobURL.substring(0, firstJob) + "/job";
      }
    }

    return getProperty("JOBS_URL"); // Deprecated.
  }

  public String getHelpURL()
  {
    return getProperty("HELP_URL");
  }

  public String getHelpTopicURL()
  {
    return getHelpURL() + "/index.jsp?topic=";
  }

  private String formatURL(String path, boolean mirror)
  {
    path = getProjectPath() + "/" + path;
    path = path.replace('\\', '/');

    if (mirror)
    {
      return "https://www.eclipse.org/downloads/download.php?file=/" + path + "&protocol=http";
    }

    return getDownloadsURL() + "/" + path;
  }

  public String formatDropURL(String path, boolean mirror)
  {
    return formatURL("drops/" + path, mirror);
  }

  public String formatUpdateURL(String path, boolean mirror)
  {
    return formatURL(getCompositionPath() + "/" + path, mirror);
  }
}
