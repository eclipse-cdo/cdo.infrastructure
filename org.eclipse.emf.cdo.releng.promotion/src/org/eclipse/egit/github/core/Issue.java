/*******************************************************************************
 *  Copyright (c) 2011 GitHub Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which is available at https://www.eclipse.org/legal/epl-2.0
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *    Kevin Sawicki (GitHub Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.egit.github.core;

import java.util.Date;
import java.util.List;

/**
 * GitHub issue model class.
 */
public class Issue extends AbstractIssue
{
  /** serialVersionUID */
  private static final long serialVersionUID = 6358575015023539051L;

  private PullRequest pullRequest;

  /**
   * @return pullRequest
   */
  public PullRequest getPullRequest()
  {
    return pullRequest;
  }

  /**
   * @param pullRequest
   * @return this issue
   */
  public Issue setPullRequest(PullRequest pullRequest)
  {
    this.pullRequest = pullRequest;
    return this;
  }

  @Override
  public Issue setCreatedAt(Date createdAt)
  {
    return (Issue)super.setCreatedAt(createdAt);
  }

  @Override
  public Issue setCreatedBy(User createdBy)
  {
    return (Issue)super.setCreatedBy(createdBy);
  }

  @Override
  public Issue setUpdatedAt(Date updatedAt)
  {
    return (Issue)super.setUpdatedAt(updatedAt);
  }

  @Override
  public Issue setUpdatedBy(User updatedBy)
  {
    return (Issue)super.setUpdatedBy(updatedBy);
  }

  @Override
  public Issue setClosedAt(Date closedAt)
  {
    return (Issue)super.setClosedAt(closedAt);
  }

  @Override
  public Issue setClosedBy(User closedBy)
  {
    return (Issue)super.setClosedBy(closedBy);
  }

  @Override
  public Issue setAssignee(User assignee)
  {
    return (Issue)super.setAssignee(assignee);
  }

  @Override
  public Issue setAssignees(List<User> assignees)
  {
    return (Issue)super.setAssignees(assignees);
  }

  @Override
  public Issue setUser(User user)
  {
    return (Issue)super.setUser(user);
  }

  @Override
  public Issue setComments(int comments)
  {
    return (Issue)super.setComments(comments);
  }

  @Override
  public Issue setNumber(int number)
  {
    return (Issue)super.setNumber(number);
  }

  @Override
  public Issue setLabels(List<Label> labels)
  {
    return (Issue)super.setLabels(labels);
  }

  @Override
  public Issue setMilestone(Milestone milestone)
  {
    return (Issue)super.setMilestone(milestone);
  }

  @Override
  public Issue setBody(String body)
  {
    return (Issue)super.setBody(body);
  }

  @Override
  public Issue setBodyHtml(String bodyHtml)
  {
    return (Issue)super.setBodyHtml(bodyHtml);
  }

  @Override
  public Issue setBodyText(String bodyText)
  {
    return (Issue)super.setBodyText(bodyText);
  }

  @Override
  public Issue setHtmlUrl(String htmlUrl)
  {
    return (Issue)super.setHtmlUrl(htmlUrl);
  }

  @Override
  public Issue setState(String state)
  {
    return (Issue)super.setState(state);
  }

  @Override
  public Issue setTitle(String title)
  {
    return (Issue)super.setTitle(title);
  }

  @Override
  public Issue setUrl(String url)
  {
    return (Issue)super.setUrl(url);
  }

  @Override
  public Issue setId(long id)
  {
    return (Issue)super.setId(id);
  }

  @Override
  public String toString()
  {
    return "Issue " + getNumber(); //$NON-NLS-1$
  }
}
