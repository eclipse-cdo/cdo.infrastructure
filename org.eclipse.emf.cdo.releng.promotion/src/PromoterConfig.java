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

import java.io.File;

/**
 * @author Eike Stepper
 */
public final class PromoterConfig extends Config
{
  public static final PromoterConfig INSTANCE = new PromoterConfig("promoter.properties");

  private PromoterConfig(String filename)
  {
    super(filename);
  }

  public File getWorkingArea()
  {
    return getDirectory("workingArea");
  }

  public String getDownloadsPath()
  {
    return getProperties().getProperty("downloadsPath");
  }

  public File getCompositionArea()
  {
    return new File(getDownloadsArea(), getProperties().getProperty("compositionPath"));
  }

  public File getCompositionTempArea()
  {
    return new File(getDownloadsArea(), getProperties().getProperty("compositionTempPath"));
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

  public File getJobsHome()
  {
    return getDirectory("JOBS_HOME");
  }

  public String formatDownloadURL(String path)
  {
    return "http://www.eclipse.org/downloads/download.php?file=/" + getDownloadsPath() + "/" + path
        + "&amp;protocol=http";
  }

  public String formatDropURL(String path)
  {
    return formatDownloadURL("drops/" + path);
  }

  public String formatUpdateURL(String path)
  {
    return formatDownloadURL(getProperties().getProperty("compositionPath") + "/" + path);
  }
}