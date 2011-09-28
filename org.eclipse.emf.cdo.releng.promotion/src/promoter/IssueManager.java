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

import promoter.util.IO;

import java.io.File;
import java.util.Comparator;

/**
 * @author Eike Stepper
 */
public abstract class IssueManager extends PromoterComponent implements Comparator<Issue>
{
  private File issuesFolder;

  public IssueManager()
  {
  }

  public synchronized Issue getIssue(String id)
  {
    if (issuesFolder == null)
    {
      issuesFolder = new File(PromoterConfig.INSTANCE.getWorkingArea(), "issues");
      issuesFolder.mkdirs();
    }

    File file = new File(issuesFolder, id);
    if (file.isFile())
    {
      String content = IO.readTextFile(file);
      String[] lines = content.split("\n");
      if (lines.length == 2)
      {
        return new Issue(id, lines[0], lines[1]);
      }
    }

    Issue issue = doGetIssue(id);

    String content = issue.getTitle() + "\n" + issue.getSeverity();
    IO.writeFile(file, content.getBytes());

    return issue;
  }

  public abstract String parseID(String message);

  public abstract String getURL(Issue issue);

  protected abstract Issue doGetIssue(String id);
}
