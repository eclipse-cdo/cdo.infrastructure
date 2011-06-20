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
package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Eike Stepper
 */
public final class Config
{
  public static final String FILENAME = "promoter.properties";

  public static Properties getProperties()
  {
    loadIfNeeded();
    return properties;
  }

  public static File getProjectWorkingArea()
  {
    loadIfNeeded();
    return projectWorkingArea;
  }

  public static File getProjectDownloadsArea()
  {
    loadIfNeeded();
    return projectDownloadsArea;
  }

  public static File getProjectRelengArea()
  {
    loadIfNeeded();
    return projectRelengArea;
  }

  public static File getHudsonJobsArea()
  {
    loadIfNeeded();
    return hudsonJobsArea;
  }

  private static Properties properties;

  private static File projectWorkingArea;

  private static File projectDownloadsArea;

  private static File projectRelengArea;

  private static File hudsonJobsArea;

  private Config()
  {
  }

  private static synchronized void loadIfNeeded()
  {
    if (properties == null)
    {
      properties = loadProperties(new File(FILENAME), true);
      projectWorkingArea = getDirectory("projectWorkingArea");
      projectDownloadsArea = getDirectory("projectDownloadsArea");
      projectRelengArea = getDirectory("projectRelengArea");
      hudsonJobsArea = getDirectory("hudsonJobsArea");
    }
  }

  public static Properties loadProperties(File file, boolean failIfNotExists)
  {
    Properties properties = new Properties();
    if (file.isFile())
    {
      try
      {
        InputStream in = null;

        try
        {
          in = new FileInputStream(file);

          properties.load(in);
        }
        finally
        {
          if (in != null)
          {
            in.close();
          }
        }
      }
      catch (Exception ex)
      {
        throw new RuntimeException("Problem while loading properties from " + file.getAbsolutePath(), ex);
      }
    }
    else if (failIfNotExists)
    {
      throw new RuntimeException("Properties file missing: " + file.getAbsolutePath());
    }

    return properties;
  }

  private static File getDirectory(String key)
  {
    String path = properties.getProperty(key);
    if (path == null)
    {
      throw new IllegalStateException("Property " + key + " is undefined");
    }

    File directory = new File(path);
    if (!directory.isDirectory())
    {
      throw new IllegalStateException(path + " does not exist or is not a directory");
    }

    return directory;
  }
}
