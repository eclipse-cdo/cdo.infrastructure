/*******************************************************************************
 *  Copyright (c) 2011 GitHub Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *    Kevin Sawicki (GitHub Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.egit.github.core;

import org.eclipse.egit.github.core.util.DateUtils;

import java.util.Date;
import java.util.List;

/**
 * GitHub Pull request model class.
 */
public class PullRequest extends AbstractIssue
{
  /** serialVersionUID */
  private static final long serialVersionUID = 7858604768525096763L;

  private Boolean mergeable;

  private String mergeableState;

  private boolean merged;

  private Date mergedAt;

  private int additions;

  private int changedFiles;

  private int reviewComments;

  private int commits;

  private int deletions;

  private PullRequestMarker base;

  private PullRequestMarker head;

  private String diffUrl;

  private String issueUrl;

  private String patchUrl;

  private User mergedBy;

  /**
   * Tells whether the pull request can be merged. The value may be
   * {@code null}, which indicates that GitHub is busy computing this
   * information but hasn't determined it yet.
   *
   * @return whether the pull request is mergeable, or {@code null} if not
   *         known yet and GitHub is busy calculating the mergeability.
   *
   * @see <a href=
   *      "https://developer.github.com/v3/pulls/#get-a-single-pull-request">GitHub
   *      Rest API v3: Pull Requests</a>
   */
  public Boolean isMergeable()
  {
    return mergeable;
  }

  /**
   * @param mergeable
   * @return this pull request
   */
  public PullRequest setMergeable(Boolean mergeable)
  {
    this.mergeable = mergeable;
    return this;
  }

  /**
   * @return mergeableState
   */
  public String getMergeableState()
  {
    return mergeableState;
  }

  /**
   * @param mergeableState
   * @return this pull request
   */
  public PullRequest setMergeableState(String mergeableState)
  {
    this.mergeableState = mergeableState;
    return this;
  }

  /**
   * @return merged
   */
  public boolean isMerged()
  {
    return merged;
  }

  /**
   * @param merged
   * @return this pull request
   */
  public PullRequest setMerged(boolean merged)
  {
    this.merged = merged;
    return this;
  }

  /**
   * @return mergedAt
   */
  public Date getMergedAt()
  {
    return DateUtils.clone(mergedAt);
  }

  /**
   * @param mergedAt
   * @return this pull request
   */
  public PullRequest setMergedAt(Date mergedAt)
  {
    this.mergedAt = DateUtils.clone(mergedAt);
    return this;
  }

  /**
   * @return additions
   */
  public int getAdditions()
  {
    return additions;
  }

  /**
   * @param additions
   * @return this pull request
   */
  public PullRequest setAdditions(int additions)
  {
    this.additions = additions;
    return this;
  }

  /**
   * @return changedFiles
   */
  public int getChangedFiles()
  {
    return changedFiles;
  }

  /**
   * @param changedFiles
   * @return this pull request
   */
  public PullRequest setChangedFiles(int changedFiles)
  {
    this.changedFiles = changedFiles;
    return this;
  }

  /**
   * @return number of inline comments on the diff in the pull request
   */
  public int getReviewComments()
  {
    return reviewComments;
  }

  /**
   * @param reviewComments {@link #getReviewComments()}
   * @return this pull request
   */
  public PullRequest setReviewComments(int reviewComments)
  {
    this.reviewComments = reviewComments;
    return this;
  }

  /**
   * @return commits
   */
  public int getCommits()
  {
    return commits;
  }

  /**
   * @param commits
   * @return this pull request
   */
  public PullRequest setCommits(int commits)
  {
    this.commits = commits;
    return this;
  }

  /**
   * @return deletions
   */
  public int getDeletions()
  {
    return deletions;
  }

  /**
   * @param deletions
   * @return this pull request
   */
  public PullRequest setDeletions(int deletions)
  {
    this.deletions = deletions;
    return this;
  }

  /**
   * @return base
   */
  public PullRequestMarker getBase()
  {
    return base;
  }

  /**
   * @param base
   * @return this pull request
   */
  public PullRequest setBase(PullRequestMarker base)
  {
    this.base = base;
    return this;
  }

  /**
   * @return head
   */
  public PullRequestMarker getHead()
  {
    return head;
  }

  /**
   * @param head
   * @return this pull request
   */
  public PullRequest setHead(PullRequestMarker head)
  {
    this.head = head;
    return this;
  }

  /**
   * @return diffUrl
   */
  public String getDiffUrl()
  {
    return diffUrl;
  }

  /**
   * @param diffUrl
   * @return this pull request
   */
  public PullRequest setDiffUrl(String diffUrl)
  {
    this.diffUrl = diffUrl;
    return this;
  }

  /**
   * @return issueUrl
   */
  public String getIssueUrl()
  {
    return issueUrl;
  }

  /**
   * @param issueUrl
   * @return this pull request
   */
  public PullRequest setIssueUrl(String issueUrl)
  {
    this.issueUrl = issueUrl;
    return this;
  }

  /**
   * @return patchUrl
   */
  public String getPatchUrl()
  {
    return patchUrl;
  }

  /**
   * @param patchUrl
   * @return this pull request
   */
  public PullRequest setPatchUrl(String patchUrl)
  {
    this.patchUrl = patchUrl;
    return this;
  }

  /**
   * @return mergedBy
   */
  public User getMergedBy()
  {
    return mergedBy;
  }

  /**
   * @param mergedBy
   * @return this pull request
   */
  public PullRequest setMergedBy(User mergedBy)
  {
    this.mergedBy = mergedBy;
    return this;
  }

  @Override
  public PullRequest setCreatedAt(Date createdAt)
  {
    return (PullRequest)super.setCreatedAt(createdAt);
  }

  @Override
  public PullRequest setCreatedBy(User createdBy)
  {
    return (PullRequest)super.setCreatedBy(createdBy);
  }

  @Override
  public PullRequest setUpdatedAt(Date updatedAt)
  {
    return (PullRequest)super.setUpdatedAt(updatedAt);
  }

  @Override
  public PullRequest setUpdatedBy(User updatedBy)
  {
    return (PullRequest)super.setUpdatedBy(updatedBy);
  }

  @Override
  public PullRequest setClosedAt(Date closedAt)
  {
    return (PullRequest)super.setClosedAt(closedAt);
  }

  @Override
  public PullRequest setClosedBy(User closedBy)
  {
    return (PullRequest)super.setClosedBy(closedBy);
  }

  @Override
  public PullRequest setAssignee(User assignee)
  {
    return (PullRequest)super.setAssignee(assignee);
  }

  @Override
  public PullRequest setAssignees(List<User> assignees)
  {
    return (PullRequest)super.setAssignees(assignees);
  }

  @Override
  public PullRequest setUser(User user)
  {
    return (PullRequest)super.setUser(user);
  }

  @Override
  public PullRequest setComments(int comments)
  {
    return (PullRequest)super.setComments(comments);
  }

  @Override
  public PullRequest setNumber(int number)
  {
    return (PullRequest)super.setNumber(number);
  }

  @Override
  public PullRequest setLabels(List<Label> labels)
  {
    return (PullRequest)super.setLabels(labels);
  }

  @Override
  public PullRequest setMilestone(Milestone milestone)
  {
    return (PullRequest)super.setMilestone(milestone);
  }

  @Override
  public PullRequest setBody(String body)
  {
    return (PullRequest)super.setBody(body);
  }

  @Override
  public PullRequest setBodyHtml(String bodyHtml)
  {
    return (PullRequest)super.setBodyHtml(bodyHtml);
  }

  @Override
  public PullRequest setBodyText(String bodyText)
  {
    return (PullRequest)super.setBodyText(bodyText);
  }

  @Override
  public PullRequest setHtmlUrl(String htmlUrl)
  {
    return (PullRequest)super.setHtmlUrl(htmlUrl);
  }

  @Override
  public PullRequest setState(String state)
  {
    return (PullRequest)super.setState(state);
  }

  @Override
  public PullRequest setTitle(String title)
  {
    return (PullRequest)super.setTitle(title);
  }

  @Override
  public PullRequest setUrl(String url)
  {
    return (PullRequest)super.setUrl(url);
  }

  @Override
  public PullRequest setId(long id)
  {
    return (PullRequest)super.setId(id);
  }

  @Override
  public String toString()
  {
    return "Pull Request " + getNumber(); //$NON-NLS-1$
  }
}
