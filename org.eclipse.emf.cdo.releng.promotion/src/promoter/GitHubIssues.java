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

import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * https://stackoverflow.com/questions/32112818/create-issue-over-github-using-java
 *
 * @author Eike Stepper
 */
public class GitHubIssues extends IssueManager
{
  @SuppressWarnings("unused")
  private static final Pattern USERNAME_PATTERN = Pattern.compile("(^[a-z\\d](?:[a-z\\d]|-(?=[a-z\\d])){0,38}$)", Pattern.CASE_INSENSITIVE);

  private static final String ISSUE_LABEL_PREFIX = "GH-";

  private static final Pattern ISSUE_REF_PATTERN = Pattern.compile("(#|" + ISSUE_LABEL_PREFIX + ")([\\d]+)");

  public GitHubIssues()
  {
  }

  @Override
  public String getType()
  {
    return "GitHub";
  }

  @Override
  public String getIssueLabelPrefix()
  {
    return ISSUE_LABEL_PREFIX;
  }

  @Override
  protected void getIssueIDs(String commitID, String commitMessage, Consumer<String> issueIDConsumer)
  {
    // Restrict search to first line of commit message.
    int newline = commitMessage.indexOf(13);
    if (newline != -1)
    {
      commitMessage = commitMessage.substring(0, newline);
    }

    Matcher matcher = ISSUE_REF_PATTERN.matcher(commitMessage);
    while (matcher.find())
    {
      String issueID = matcher.group(2);
      issueIDConsumer.accept(issueID);
    }
  }

  @Override
  protected Issue createIssue(String issueID)
  {
    int id = Integer.parseInt(issueID);
    org.eclipse.egit.github.core.Issue githubIssue;

    try
    {
      githubIssue = GitHub.getIssueService().getIssue(GitHub.REPO, id);
    }
    catch (IOException ex)
    {
      throw new RuntimeException(ex);
    }

    if (githubIssue == null)
    {
      return null;
    }

    String url = githubIssue.getHtmlUrl();
    String title = githubIssue.getTitle();
    boolean enhancement = isEnhancement(githubIssue);
    String severity = getSeverity(githubIssue);
    int severityIndex = enhancement ? 0 : 1;
    String component = getComponent(githubIssue);
    String version = getVersion(githubIssue);
    String status = githubIssue.getState();

    return new Issue(this, url, issueID, title, enhancement, severity, severityIndex, component, version, status);
  }

  private boolean isEnhancement(org.eclipse.egit.github.core.Issue githubIssue)
  {
    for (Label label : githubIssue.getLabels())
    {
      String name = label.getName();
      if (GitHub.ENHANCEMENT_LABEL_NAME.equals(name))
      {
        return true;
      }
    }

    return false;
  }

  private String getSeverity(org.eclipse.egit.github.core.Issue githubIssue)
  {
    for (Label label : githubIssue.getLabels())
    {
      String name = label.getName();
      if (GitHub.ENHANCEMENT_LABEL_NAME.equals(name))
      {
        return "enhancement";
      }
    }

    return "normal";
  }

  private String getComponent(org.eclipse.egit.github.core.Issue githubIssue)
  {
    List<Label> labels = githubIssue.getLabels();
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

  private String getVersion(org.eclipse.egit.github.core.Issue githubIssue)
  {
    Milestone milestone = githubIssue.getMilestone();
    if (milestone != null)
    {
      return milestone.getTitle();
    }

    return null;
  }

  @Override
  public int compare(Issue i1, Issue i2)
  {
    return Integer.valueOf(i1.getID()).compareTo(Integer.valueOf(i2.getID()));
  }
}
