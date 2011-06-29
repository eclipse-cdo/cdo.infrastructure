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
package promoter.issues;

import promoter.issues.IssueManager.Issue;

import java.util.Comparator;

/**
 * @author Eike Stepper
 */
public abstract class IssueManager implements Comparator<Issue>
{
  public abstract String parseID(String message);

  public abstract Issue getIssue(String id);

  public abstract String getURL(Issue issue);

  /**
   * @author Eike Stepper
   */
  public static class Issue
  {
    private String id;

    private String title;

    public Issue(String id, String title)
    {
      this.id = id;
      this.title = title;
    }

    public final String getID()
    {
      return id;
    }

    public final String getTitle()
    {
      return title;
    }

    @Override
    public String toString()
    {
      return "Issue [id=" + id + ", title=" + title + "]";
    }
  }
}
