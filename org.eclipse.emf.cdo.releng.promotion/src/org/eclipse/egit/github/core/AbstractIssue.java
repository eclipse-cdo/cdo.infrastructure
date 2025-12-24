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

import org.eclipse.egit.github.core.util.DateUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Model base class for GitHub issues and GitHub  pull requests.
 */
public abstract class AbstractIssue implements Serializable
{
  private static final long serialVersionUID = -2347313645866707436L;

  private long id;

  private Date createdAt;

  private User createdBy;

  private Date updatedAt;

  private User updatedBy;

  private Date closedAt;

  private User closedBy;

  private User assignee;

  private List<User> assignees;

  private int comments;

  private int number;

  private List<Label> labels;

  private Milestone milestone;

  private String body;

  private String bodyHtml;

  private String bodyText;

  private String htmlUrl;

  private String state;

  private String title;

  private String url;

  private User user;

  /**
   * @return createdAt
   */
  public Date getCreatedAt()
  {
    return DateUtils.clone(createdAt);
  }

  /**
   * @param createdAt
   * @return this issue
   */
  public AbstractIssue setCreatedAt(Date createdAt)
  {
    this.createdAt = DateUtils.clone(createdAt);
    return this;
  }

  /**
   * @return createdBy
   */
  public User getCreatedBy()
  {
    return createdBy;
  }

  /**
   * @param createdBy
   * @return this issue
   */
  public AbstractIssue setCreatedBy(User createdBy)
  {
    this.createdBy = createdBy;
    return this;
  }

  /**
   * @return updatedAt
   */
  public Date getUpdatedAt()
  {
    return DateUtils.clone(updatedAt);
  }

  /**
   * @param updatedAt
   * @return this issue
   */
  public AbstractIssue setUpdatedAt(Date updatedAt)
  {
    this.updatedAt = DateUtils.clone(updatedAt);
    return this;
  }

  /**
   * @return createdBy
   */
  public User getUpdatedBy()
  {
    return updatedBy;
  }

  /**
   * @param updatedBy
   * @return this issue
   */
  public AbstractIssue setUpdatedBy(User updatedBy)
  {
    this.updatedBy = updatedBy;
    return this;
  }

  /**
   * @return closedAt
   */
  public Date getClosedAt()
  {
    return DateUtils.clone(closedAt);
  }

  /**
   * @param closedAt
   * @return this issue
   */
  public AbstractIssue setClosedAt(Date closedAt)
  {
    this.closedAt = DateUtils.clone(closedAt);
    return this;
  }

  /**
   * @return closedBy
   */
  public User getClosedBy()
  {
    return closedBy;
  }

  /**
   * @param closedBy
   * @return this issue
   */
  public AbstractIssue setClosedBy(User closedBy)
  {
    this.closedBy = closedBy;
    return this;
  }

  /**
   * @return assignee
   */
  public User getAssignee()
  {
    return assignee;
  }

  /**
   * @param assignee
   * @return this issue
   */
  public AbstractIssue setAssignee(User assignee)
  {
    this.assignee = assignee;
    return this;
  }

  /**
   * @return assignees
   */
  public List<User> getAssignees()
  {
    return assignees;
  }

  /**
   * @param assignees
   * @return this issue
   */
  public AbstractIssue setAssignees(List<User> assignees)
  {
    this.assignees = assignees;
    return this;
  }

  /**
   * @return user
   */
  public User getUser()
  {
    return user;
  }

  /**
   * @param user
   * @return this issue
   */
  public AbstractIssue setUser(User user)
  {
    this.user = user;
    return this;
  }

  /**
   * @return comments
   */
  public int getComments()
  {
    return comments;
  }

  /**
   * @param comments
   * @return this issue
   */
  public AbstractIssue setComments(int comments)
  {
    this.comments = comments;
    return this;
  }

  /**
   * @return number
   */
  public int getNumber()
  {
    return number;
  }

  /**
   * @param number
   * @return this issue
   */
  public AbstractIssue setNumber(int number)
  {
    this.number = number;
    return this;
  }

  /**
   * @return labels
   */
  public List<Label> getLabels()
  {
    return labels;
  }

  /**
   * @param labels
   * @return this issue
   */
  public AbstractIssue setLabels(List<Label> labels)
  {
    this.labels = labels != null ? new ArrayList<>(labels) : null;
    return this;
  }

  /**
   * @return milestone
   */
  public Milestone getMilestone()
  {
    return milestone;
  }

  /**
   * @param milestone
   * @return this issue
   */
  public AbstractIssue setMilestone(Milestone milestone)
  {
    this.milestone = milestone;
    return this;
  }

  /**
   * @return body
   */
  public String getBody()
  {
    return body;
  }

  /**
   * @param body
   * @return this issue
   */
  public AbstractIssue setBody(String body)
  {
    this.body = body;
    return this;
  }

  /**
   * @return bodyHtml
   */
  public String getBodyHtml()
  {
    return bodyHtml;
  }

  /**
   * @param bodyHtml
   * @return this issue
   */
  public AbstractIssue setBodyHtml(String bodyHtml)
  {
    this.bodyHtml = bodyHtml;
    return this;
  }

  /**
   * @return bodyText
   */
  public String getBodyText()
  {
    return bodyText;
  }

  /**
   * @param bodyText
   * @return this issue
   */
  public AbstractIssue setBodyText(String bodyText)
  {
    this.bodyText = bodyText;
    return this;
  }

  /**
   * @return htmlUrl
   */
  public String getHtmlUrl()
  {
    return htmlUrl;
  }

  /**
   * @param htmlUrl
   * @return this issue
   */
  public AbstractIssue setHtmlUrl(String htmlUrl)
  {
    this.htmlUrl = htmlUrl;
    return this;
  }

  /**
   * @return state
   */
  public String getState()
  {
    return state;
  }

  /**
   * @param state
   * @return this issue
   */
  public AbstractIssue setState(String state)
  {
    this.state = state;
    return this;
  }

  /**
   * @return title
   */
  public String getTitle()
  {
    return title;
  }

  /**
   * @param title
   * @return this issue
   */
  public AbstractIssue setTitle(String title)
  {
    this.title = title;
    return this;
  }

  /**
   * @return url
   */
  public String getUrl()
  {
    return url;
  }

  /**
   * @param url
   * @return this issue
   */
  public AbstractIssue setUrl(String url)
  {
    this.url = url;
    return this;
  }

  /**
   * @return id
   */
  public long getId()
  {
    return id;
  }

  /**
   * @param id
   * @return this issue
   */
  public AbstractIssue setId(long id)
  {
    this.id = id;
    return this;
  }

  @Override
  public abstract String toString();
}
