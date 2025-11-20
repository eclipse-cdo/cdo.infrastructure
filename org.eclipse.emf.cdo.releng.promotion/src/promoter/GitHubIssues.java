/*
 * Copyright (c) 2023 Eike Stepper (Loehne, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package promoter;

import org.eclipse.egit.github.core.AbstractIssue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.client.PageIterator;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * https://stackoverflow.com/questions/32112818/create-issue-over-github-using-java
 *
 * @author Eike Stepper
 */
public class GitHubIssues extends IssueManager<AbstractIssue>
{
  @SuppressWarnings("unused")
  private static final Pattern USERNAME_PATTERN = Pattern.compile("(^[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38}$)", Pattern.CASE_INSENSITIVE);

  private static final String SUBTYPE_ISSUE = "Issue";

  private static final String SUBTYPE_PULL_REQUEST = "PullRequest";

  private static final String ISSUE_LABEL_PREFIX = "GH-";

  private static final String PR_LABEL_PREFIX = "PR-";

  private static final Pattern ISSUE_REF_PATTERN = Pattern.compile("(#|" + ISSUE_LABEL_PREFIX + "|" + PR_LABEL_PREFIX + ")([\\d]+)");

  public GitHubIssues()
  {
  }

  @Override
  public String getType()
  {
    return "GitHub";
  }

  @Override
  public int compare(Issue i1, Issue i2)
  {
    return Integer.valueOf(i1.getID()).compareTo(Integer.valueOf(i2.getID()));
  }

  @Override
  public String getIssueLabelPrefix(Issue issue)
  {
    String subtype = issue.getSubtype();
    if (SUBTYPE_PULL_REQUEST.equals(subtype))
    {
      return PR_LABEL_PREFIX;
    }

    return ISSUE_LABEL_PREFIX;
  }

  @Override
  protected void getIssueIDs(String commitID, String commitMessage, IssueIDConsumer<AbstractIssue> issueIDConsumer)
  {
    // Restrict search to first line of commit message.
    int newline = commitMessage.indexOf(13);
    if (newline != -1)
    {
      commitMessage = commitMessage.substring(0, newline);
    }

    int issues = 0;

    Matcher matcher = ISSUE_REF_PATTERN.matcher(commitMessage);
    while (matcher.find())
    {
      String issueID = matcher.group(2);
      issueIDConsumer.accept(issueID, null);
      ++issues;
    }

    if (issues == 0 && commitMessage != null && !commitMessage.startsWith("[Releng]"))
    {
      for (PageIterator<PullRequest> it = GitHub.getPullRequestService().pagePullRequests(GitHub.REPO, commitID + " is:merged"); it.hasNext();)
      {
        Collection<PullRequest> prs = it.next();

        for (PullRequest pr : prs)
        {
          String issueID = Integer.toString(pr.getNumber());
          issueIDConsumer.accept(issueID, pr);
        }
      }
    }
  }

  @Override
  protected Issue createIssue(String issueID, AbstractIssue issueInfo)
  {
    int id = Integer.parseInt(issueID);

    if (issueInfo == null)
    {
      try
      {
        issueInfo = GitHub.getIssueService().getIssue(GitHub.REPO, id);
      }
      catch (IOException ex)
      {
        ex.printStackTrace();
      }
    }

    if (issueInfo == null)
    {
      return null;
    }

    String subtype = issueInfo instanceof PullRequest ? SUBTYPE_PULL_REQUEST : SUBTYPE_ISSUE;
    String url = issueInfo.getHtmlUrl();
    String title = issueInfo.getTitle();
    boolean enhancement = isEnhancement(issueInfo);
    String severity = getSeverity(issueInfo);
    int severityIndex = enhancement ? 0 : 1;
    String component = getComponent(issueInfo);
    String version = getVersion(issueInfo);
    String status = issueInfo.getState();

    return new Issue(this, url, issueID, subtype, title, enhancement, severity, severityIndex, component, version, status);
  }

  private static boolean isEnhancement(AbstractIssue issueInfo)
  {
    for (Label label : issueInfo.getLabels())
    {
      String name = label.getName();
      if (GitHub.ENHANCEMENT_LABEL_NAME.equals(name))
      {
        return true;
      }
    }

    return false;
  }

  private static String getSeverity(AbstractIssue issueInfo)
  {
    for (Label label : issueInfo.getLabels())
    {
      String name = label.getName();
      if (GitHub.ENHANCEMENT_LABEL_NAME.equals(name))
      {
        return "enhancement";
      }
    }

    return "normal";
  }

  private static String getComponent(AbstractIssue issueInfo)
  {
    List<Label> labels = issueInfo.getLabels();
    for (Label label : labels)
    {
      String color = label.getColor();
      if (color != null)
      {
        if (color.startsWith("#"))
        {
          color = color.substring(1);
        }

        if (GitHub.COMPONENT_LABEL_COLOR.equalsIgnoreCase(color))
        {
          String name = label.getName();
          if (name != null)
          {
            name = name.replace('-', '.');
          }

          return name;
        }
      }
    }

    return null;
  }

  private static String getVersion(AbstractIssue issueInfo)
  {
    Milestone milestone = issueInfo.getMilestone();
    if (milestone != null)
    {
      return milestone.getTitle();
    }

    return null;
  }
}
