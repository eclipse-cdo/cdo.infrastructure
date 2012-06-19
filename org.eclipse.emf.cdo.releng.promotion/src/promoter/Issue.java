/*
 * Copyright (c) 2004 - 2012 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package promoter;

import promoter.util.IO;

import java.io.File;

/**
 * @author Eike Stepper
 */
public class Issue
{
  private String id;

  private String title;

  private String severity;

  private String component;

  public Issue(String id, String title, String severity, String component)
  {
    this.id = id;
    this.title = title;
    this.severity = severity;
    this.component = component;
  }

  public Issue(File file)
  {
    id = file.getName();

    String content = IO.readTextFile(file);
    String[] lines = content.split("\n");

    if (lines.length > 0)
    {
      title = lines[0];
    }

    if (lines.length > 1)
    {
      severity = lines[1];
    }

    if (lines.length > 2)
    {
      component = lines[2];
    }
  }

  public void write(File file)
  {
    String content = title + "\n" + severity + "\n" + component;
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
