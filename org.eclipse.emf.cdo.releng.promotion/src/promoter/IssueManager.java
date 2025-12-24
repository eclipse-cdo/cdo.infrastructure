/*
 * Copyright (c) 2004 - 2012 Eike Stepper (Loehne, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package promoter;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Eike Stepper
 */
public abstract class IssueManager<INFO> extends PromoterComponent implements Comparator<Issue>
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
    getIssueIDs(commitID, commitMessage, (issueID, issueInfo) -> {
      Issue issue = cache.computeIfAbsent(issueID, k -> createIssue(issueID, issueInfo));
      if (issue != null)
      {
        issueConsumer.accept(issue);
      }
    });
  }

  public String getIssueLabelPrefix(Issue issue)
  {
    return "";
  }

  public String getIssueLabelSuffix(Issue issue)
  {
    return "";
  }

  protected String getIssueLabel(Issue issue)
  {
    String issueID = issue.getID();
    return issueID == null ? null : getIssueLabelPrefix(issue) + issueID + getIssueLabelSuffix(issue);
  }

  protected abstract void getIssueIDs(String commitID, String commitMessage, IssueIDConsumer<INFO> issueIDConsumer);

  protected abstract Issue createIssue(String issueID, INFO issueInfo);

  /**
   * @author Eike Stepper
   */
  @FunctionalInterface
  public interface IssueIDConsumer<INFO> extends BiConsumer<String, INFO>
  {
    @Override
    public abstract void accept(String id, INFO info);
  }
}
