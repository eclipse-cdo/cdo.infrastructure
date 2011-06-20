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

  public final Properties getProperties()
  {
    loadIfNeeded();
    return properties;
  }

  public File getDirectory(String key)
  {
    String path = getProperties().getProperty(key);
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

  private synchronized void loadIfNeeded()
  {
    if (properties == null)
    {
      properties = loadProperties(file, true);
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
}
