/*
 * Copyright (c) 2004 - 2011 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package promoter;

/**
 * @author Eike Stepper
 */
public class Issue
{
  private String id;

  private String title;

  private String severity;

  public Issue(String id, String title, String severity)
  {
    this.id = id;
    this.title = title;
    this.severity = severity;
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
