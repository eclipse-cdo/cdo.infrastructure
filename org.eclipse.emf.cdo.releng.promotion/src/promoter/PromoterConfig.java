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

import promoter.util.Config;

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

  public File getGitExecutable()
  {
    return getFile("GIT_EXECUTABLE");
  }

  public String getGitRepositoryPath()
  {
    return getProperty("gitRepositoryPath");
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
    return getProperty("projectPath");
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
    String jobName = getProperty("JOB_NAME");
    if (jobURL != null && jobName != null)
    {
      if (jobURL.endsWith("/"))
      {
        jobURL = jobURL.substring(jobURL.length() - "/".length());
      }

      if (jobURL.endsWith(jobName))
      {
        jobURL = jobURL.substring(jobURL.length() - jobName.length());
      }

      if (jobURL.endsWith("/"))
      {
        jobURL = jobURL.substring(jobURL.length() - "/".length());
      }

      return jobURL;
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
