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
package promoter.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Eike Stepper
 */
public class Config
{
  private final File file;

  private Properties properties;

  public Config(String filename)
  {
    this(new File(filename));
  }

  public Config(File file)
  {
    this.file = file;
  }

  public final File getFile()
  {
    return file;
  }

  public final synchronized Properties getProperties()
  {
    if (properties == null)
    {
      properties = loadProperties(file, true);
    }

    return properties;
  }

  public String getProperty(String key)
  {
    String property = System.getProperty(key);
    if (property != null)
    {
      return property;
    }

    return getProperties().getProperty(key);
  }

  public String getProperty(String key, String defaultValue)
  {
    String property = System.getProperty(key);
    if (property != null)
    {
      return property;
    }

    return getProperties().getProperty(key, defaultValue);
  }

  public File getFileOrDirectory(String key)
  {
    String path = getProperty(key);
    if (path == null)
    {
      throw new IllegalStateException("Property " + key + " is undefined");
    }

    File directory = new File(path);
    if (!directory.exists())
    {
      throw new IllegalStateException(path + " does not exist");
    }

    return directory;
  }

  public File getFile(String key)
  {
    File fileOrDirectory = getFileOrDirectory(key);
    if (!fileOrDirectory.isFile())
    {
      throw new IllegalStateException(fileOrDirectory + " is not a file");
    }

    return fileOrDirectory;
  }

  public File getDirectory(String key)
  {
    File fileOrDirectory = getFileOrDirectory(key);
    if (!fileOrDirectory.isDirectory())
    {
      throw new IllegalStateException(fileOrDirectory + " is not a directory");
    }

    return fileOrDirectory;
  }

  public File getUserDirectory()
  {
    return getDirectory("user.dir");
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
}
