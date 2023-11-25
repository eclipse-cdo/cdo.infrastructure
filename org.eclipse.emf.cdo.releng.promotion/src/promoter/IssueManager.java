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
package promoter;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Eike Stepper
 */
public abstract class IssueManager extends PromoterComponent implements Comparator<Issue>
{
  private final Map<String, Issue> cache = new HashMap<>();

  public IssueManager()
  {
  }

  public String getType()
  {
    return getClass().getSimpleName();
  }

  public final void getCommitIssues(String commitID, String commitMessage, Consumer<Issue> issueConsumer)
  {
    getIssueIDs(commitID, commitMessage, issueID -> {
      Issue issue = cache.computeIfAbsent(issueID, k -> createIssue(issueID));
      if (issue != null)
      {
        issueConsumer.accept(issue);
      }
    });
  }

  public String getIssueLabelPrefix()
  {
    return "";
  }

  public String getIssueLabelSuffix()
  {
    return "";
  }

  protected String getIssueLabel(String issueID)
  {
    return issueID == null ? null : getIssueLabelPrefix() + issueID + getIssueLabelSuffix();
  }

  protected abstract void getIssueIDs(String commitID, String commitMessage, Consumer<String> issueIDConsumer);

  protected abstract Issue createIssue(String issueID);
}
