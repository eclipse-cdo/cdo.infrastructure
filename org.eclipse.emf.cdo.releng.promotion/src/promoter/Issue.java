/*
 * Copyright (c) 2004 - 2012, 2023 Eike Stepper (Loehne, Germany) and others.
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

import promoter.util.IO;

/**
 * @author Eike Stepper
 */
public final class Issue
{
  private final String id;

  private final String title;

  private final String severity;

  private final String component;

  private final String version;

  private final String status;

  public Issue(String id, String title, String severity, String component, String version, String status)
  {
    this.id = id;
    this.title = title;
    this.severity = severity;
    this.component = component;
    this.version = version;
    this.status = status;
  }

  public Issue(File file)
  {
    id = file.getName();

    String content = IO.readTextFile(file);
    String[] lines = content.split("\n");

    title = lines.length > 0 ? lines[0] : null;
    severity = lines.length > 1 ? lines[1] : null;
    component = lines.length > 2 ? lines[2] : null;
    version = lines.length > 3 ? lines[3] : null;
    status = lines.length > 4 ? lines[4] : null;
  }

  public void write(File file)
  {
    String content = title + "\n" + severity + "\n" + component + "\n" + version + "\n" + status;
    IO.writeFile(file, content.getBytes());
  }

  public final String getID()
  {
    return id;
  }

  public final String getTitle()
  {
    return title;
  }

  public final String getSeverity()
  {
    return severity;
  }

  public String getComponent()
  {
    return component;
  }

  public String getVersion()
  {
    return version;
  }

  public String getStatus()
  {
    return status;
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + (id == null ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
    {
      return true;
    }

    if (obj == null)
    {
      return false;
    }

    if (getClass() != obj.getClass())
    {
      return false;
    }

    Issue other = (Issue)obj;
    if (id == null)
    {
      if (other.id != null)
      {
        return false;
      }
    }
    else if (!id.equals(other.id))
    {
      return false;
    }

    return true;
  }

  @Override
  public String toString()
  {
    return "Issue [id=" + id + ", title=" + title + "]";
  }
}
