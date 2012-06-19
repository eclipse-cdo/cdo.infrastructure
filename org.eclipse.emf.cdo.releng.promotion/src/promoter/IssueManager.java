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

    File file = null;
    if (isCaching())
    {
      file = new File(issuesFolder, id);
      if (file.isFile())
      {
        return new Issue(file);
      }
    }

    Issue issue = doGetIssue(id);
    if (issue != null && file != null)
    {
      issue.write(file);
    }

    return issue;
  }

  public abstract String parseID(String message);

  public abstract String getURL(Issue issue);

  protected abstract Issue doGetIssue(String id);

  protected boolean isCaching()
  {
    return true;
  }
}
