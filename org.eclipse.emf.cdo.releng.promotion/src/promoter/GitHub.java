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

import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.PullRequestService;

/**
 * @author Eike Stepper
 */
public final class GitHub
{
  private static final GitHubClient CLIENT = createGitHubClient();

  private static final CommitService COMMIT_SERVICE = new CommitService(CLIENT);

  private static final IssueService ISSUE_SERVICE = new IssueService(CLIENT);

  private static final PullRequestService PULL_REQUEST_SERVICE = new PullRequestService(CLIENT);

  private static final String REPO_OWNER = System.getProperty("REPO_OWNER", "eclipse-cdo");

  private static final String REPO_NAME = System.getProperty("REPO_NAME", "cdo");

  public static final RepositoryId REPO = new RepositoryId(REPO_OWNER, REPO_NAME);

  public static final String COMPONENT_LABEL_COLOR = System.getProperty("COMPONENT_LABEL_COLOR", "FBCA04");

  public static final String ENHANCEMENT_LABEL_NAME = System.getProperty("ENHANCEMENT_LABEL_NAME", "enhancement");

  public static final String BUG_LABEL_NAME = System.getProperty("BUG_LABEL_NAME", "bug");

  private GitHub()
  {
  }

  private static GitHubClient createGitHubClient()
  {
    GitHubClient client = new GitHubClient();

    String github_token = System.getProperty("GITHUB_TOKEN");
    if (github_token == null)
    {
      github_token = System.getenv("GITHUB_TOKEN");
    }

    if (github_token != null && github_token.length() != 0)
    {
      client.setOAuth2Token(github_token);
    }

    return client;
  }

  public static CommitService getCommitService()
  {
    return COMMIT_SERVICE;
  }

  public static IssueService getIssueService()
  {
    return ISSUE_SERVICE;
  }

  public static PullRequestService getPullRequestService()
  {
    return PULL_REQUEST_SERVICE;
  }
}
