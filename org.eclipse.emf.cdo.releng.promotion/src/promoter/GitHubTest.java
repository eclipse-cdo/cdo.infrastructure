/*
 * Copyright (c) 2004-2013 Eike Stepper (Loehne, Germany) and others.
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
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.PullRequestService;

import java.util.Collection;
import java.util.List;

/**
 * @author Eike Stepper
 */
@SuppressWarnings("all")
public class GitHubTest
{
  static RepositoryId cdo = new RepositoryId("eclipse-cdo", "cdo");

  static RepositoryId platform = new RepositoryId("eclipse-platform", "eclipse.platform");

  static GitHubClient client = createGitHubClient();

  static CommitService commitService = new CommitService(client);

  static IssueService issueService = new IssueService(client);

  static PullRequestService pullRequestService = new PullRequestService(client);

  public static void main(String[] args) throws Exception
  {
    for (Collection<PullRequest> prs : pullRequestService.pagePullRequests(GitHub.REPO, "b4d9082bce976b2df23ffb8b45bba23a17f74577 is:merged"))
    {
      for (PullRequest pr : prs)
      {
        int id = pr.getNumber();
        System.out.println(id);

        String title = pr.getTitle();
        System.out.println(title);

        Milestone milestone = pr.getMilestone();
        System.out.println(milestone.getTitle());

        List<Label> labels = pr.getLabels();
        for (Label label : labels)
        {
          System.out.println(label.getName());
        }

        List<User> assignees = pr.getAssignees();
        for (User assignee : assignees)
        {
          System.out.println(assignee.getLogin());
        }
      }
    }

    // RepositoryCommit commit2 = commitService.getCommit(cdo, "75178686fa2f416da302753a320ceb3c591baeb2");
    //
    // String fromRevision = "19a6f161cd5ea428ef3f46101ec3f4f147199699";
    // String toRevision = "75178686fa2f416da302753a320ceb3c591baeb2";
    //
    // pages: for (Collection<RepositoryCommit> page : commitService.pageCommits(cdo, toRevision, null, 10))
    // {
    // for (RepositoryCommit repositoryCommit : page)
    // {
    // String sha = repositoryCommit.getSha();
    // if (sha.equals(fromRevision))
    // {
    // break pages;
    // }
    //
    // Commit commit = repositoryCommit.getCommit();
    // System.out.println(sha.substring(0, 6) + " - " + commit.getMessage());
    // }
    // }
    //
    // for (Issue issue : issueService.getIssues(cdo, null))
    // {
    // System.out.print(issue);
    // System.out.println(issue.getLabels());
    // }
    //
    // PullRequest pullRequest = pullRequestService.getPullRequest(platform, 555);
    // String url = pullRequest.getUrl();
    // String issueUrl = pullRequest.getIssueUrl();
    // pullRequest.getHtmlUrl();
    // System.out.println();
    //
    // Issue issue = issueService.getIssue(platform, 561);
    // System.out.println();

    // List<RepositoryCommit> commits = commitService.getCommits(cdo);
    //
    // // RepositoryCommit commit = commitService.getCommit(platform, "54ce9291f8f3c7e25d7e4b945c88c3c85b2e5793");
    // System.out.println();
  }

  private static GitHubClient createGitHubClient()
  {
    GitHubClient client = new GitHubClient();

    String github_token = System.getProperty("GITHUB_TOKEN");
    if (github_token != null && github_token.length() != 0)
    {
      client.setOAuth2Token(github_token);
    }

    return client;
  }
}
