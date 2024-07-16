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

import java.util.Objects;

/**
 * @author Eike Stepper
 */
public final class Issue implements Comparable<Issue>
{
  private final IssueManager<?> manager;

  private final String url;

  private final String id;

  private final String title;

  private final boolean enhancement;

  private final String severity;

  private final int severityIndex;

  private final String component;

  private final String version;

  private final String status;

  public Issue(IssueManager<?> manager, String url, String id, String title, boolean enhancement, String severity, int severityIndex, String component,
      String version, String status)
  {
    this.manager = manager;
    this.url = url;
    this.id = id;
    this.title = title;
    this.enhancement = enhancement;
    this.severity = severity;
    this.severityIndex = severityIndex;
    this.component = component;
    this.version = version;
    this.status = status;
  }

  public IssueManager<?> getManager()
  {
    return manager;
  }

  public String getURL()
  {
    return url;
  }

  public String getID()
  {
    return id;
  }

  public String getLabel()
  {
    if (manager != null)
    {
      return manager.getIssueLabel(id);
    }

    return id;
  }

  public String getType()
  {
    if (manager != null)
    {
      return manager.getType();
    }

    return "None";
  }

  public String getTitle()
  {
    return title;
  }

  public boolean isEnhancement()
  {
    return enhancement;
  }

  public String getSeverity()
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
    return Objects.hash(manager, id);
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
    return Objects.equals(manager, other.manager) && Objects.equals(id, other.id);
  }

  @Override
  public int compareTo(Issue o)
  {
    return Integer.compare(o.severityIndex, severityIndex);
  }

  @Override
  public String toString()
  {
    return "Issue[" + getLabel() + ", " + title + "]";
  }
}
