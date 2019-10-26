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

  public File getProjectCloneLocation()
  {
    return getDirectory("projectCloneLocation");
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

  public File getWorkingArea()
  {
    return getDirectory("workingArea");
  }

  public String getDownloadsPath()
  {
    return getProperty("downloadsPath");
  }

  public File getCompositionArea()
  {
    return new File(getDownloadsArea(), getProperty("compositionPath"));
  }

  public File getCompositionTempArea()
  {
    return new File(getDownloadsArea(), getProperty("compositionTempPath"));
  }

  public File getDropsArea()
  {
    return new File(getDownloadsArea(), "drops");
  }

  public File getDownloadsArea()
  {
    return new File(getDownloadsHome(), getDownloadsPath());
  }

  public File getProjectRelengArea()
  {
    return getDirectory("projectRelengArea");
  }

  public File getDownloadsHome()
  {
    return getDirectory("DOWNLOADS_HOME");
  }

  public String getJobsURL()
  {
    return getProperty("JOBS_URL");
  }

  public String formatDownloadURL(String path)
  {
    return "https://www.eclipse.org/downloads/download.php?file=/" + getDownloadsPath() + "/" + path + "&amp;protocol=http";
  }

  public String formatDropURL(String path)
  {
    return formatDownloadURL("drops/" + path);
  }

  public String formatUpdateURL(String path)
  {
    return formatDownloadURL(getProperty("compositionPath") + "/" + path);
  }
}
