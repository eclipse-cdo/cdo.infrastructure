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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.egit.github.core.util.DateUtils;

/**
 * Pull request model class.
 */
public class _PullRequest implements Serializable {

	/** serialVersionUID */
	private static final long serialVersionUID = 7858604768525096763L;

	private Boolean mergeable;

	private String mergeableState;

	private boolean merged;

	private Date closedAt;

	private Date mergedAt;

	private Date updatedAt;

	private Date createdAt;

	private long id;

	private int additions;

	private int changedFiles;

	private int comments;

	private int reviewComments;

	private int commits;

	private int deletions;

	private int number;

	private List<Label> labels;

	private Milestone milestone;

	private PullRequestMarker base;

	private PullRequestMarker head;

	private String body;

	private String bodyHtml;

	private String bodyText;

	private String diffUrl;

	private String htmlUrl;

	private String issueUrl;

	private String patchUrl;

	private String state;

	private String title;

	private String url;

	private User assignee;

	private User mergedBy;

	private User user;

	private List<User> assignees;

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
	public Boolean isMergeable() {
		return mergeable;
	}

	/**
	 * @param mergeable
	 * @return this pull request
	 */
	public _PullRequest setMergeable(Boolean mergeable) {
		this.mergeable = mergeable;
		return this;
	}

	/**
	 * @return mergeableState
	 */
	public String getMergeableState() {
		return mergeableState;
	}

	/**
	 * @param mergeableState
	 * @return this pull request
	 */
	public _PullRequest setMergeableState(String mergeableState) {
		this.mergeableState = mergeableState;
		return this;
	}

	/**
	 * @return merged
	 */
	public boolean isMerged() {
		return merged;
	}

	/**
	 * @param merged
	 * @return this pull request
	 */
	public _PullRequest setMerged(boolean merged) {
		this.merged = merged;
		return this;
	}

	/**
	 * @return closedAt
	 */
	public Date getClosedAt() {
		return DateUtils.clone(closedAt);
	}

	/**
	 * @param closedAt
	 * @return this pull request
	 */
	public _PullRequest setClosedAt(Date closedAt) {
		this.closedAt = DateUtils.clone(closedAt);
		return this;
	}

	/**
	 * @return mergedAt
	 */
	public Date getMergedAt() {
		return DateUtils.clone(mergedAt);
	}

	/**
	 * @param mergedAt
	 * @return this pull request
	 */
	public _PullRequest setMergedAt(Date mergedAt) {
		this.mergedAt = DateUtils.clone(mergedAt);
		return this;
	}

	/**
	 * @return updatedAt
	 */
	public Date getUpdatedAt() {
		return DateUtils.clone(updatedAt);
	}

	/**
	 * @param updatedAt
	 * @return this pull request
	 */
	public _PullRequest setUpdatedAt(Date updatedAt) {
		this.updatedAt = DateUtils.clone(updatedAt);
		return this;
	}

	/**
	 * @return createdAt
	 */
	public Date getCreatedAt() {
		return DateUtils.clone(createdAt);
	}

	/**
	 * @param createdAt
	 * @return this pull request
	 */
	public _PullRequest setCreatedAt(Date createdAt) {
		this.createdAt = DateUtils.clone(createdAt);
		return this;
	}

	/**
	 * @return additions
	 */
	public int getAdditions() {
		return additions;
	}

	/**
	 * @param additions
	 * @return this pull request
	 */
	public _PullRequest setAdditions(int additions) {
		this.additions = additions;
		return this;
	}

	/**
	 * @return changedFiles
	 */
	public int getChangedFiles() {
		return changedFiles;
	}

	/**
	 * @param changedFiles
	 * @return this pull request
	 */
	public _PullRequest setChangedFiles(int changedFiles) {
		this.changedFiles = changedFiles;
		return this;
	}

	/**
	 * @return comments
	 */
	public int getComments() {
		return comments;
	}

	/**
	 * @param comments
	 * @return this pull request
	 */
	public _PullRequest setComments(int comments) {
		this.comments = comments;
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
	public _PullRequest setReviewComments(int reviewComments)
	{
		this.reviewComments = reviewComments;
		return this;
	}

	/**
	 * @return commits
	 */
	public int getCommits() {
		return commits;
	}

	/**
	 * @param commits
	 * @return this pull request
	 */
	public _PullRequest setCommits(int commits) {
		this.commits = commits;
		return this;
	}

	/**
	 * @return deletions
	 */
	public int getDeletions() {
		return deletions;
	}

	/**
	 * @param deletions
	 * @return this pull request
	 */
	public _PullRequest setDeletions(int deletions) {
		this.deletions = deletions;
		return this;
	}

	/**
	 * @return number
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * @param number
	 * @return this pull request
	 */
	public _PullRequest setNumber(int number) {
		this.number = number;
		return this;
	}

	/**
	 * @return base
	 */
	public PullRequestMarker getBase() {
		return base;
	}

	/**
	 * @param base
	 * @return this pull request
	 */
	public _PullRequest setBase(PullRequestMarker base) {
		this.base = base;
		return this;
	}

	/**
	 * @return head
	 */
	public PullRequestMarker getHead() {
		return head;
	}

	/**
	 * @param head
	 * @return this pull request
	 */
	public _PullRequest setHead(PullRequestMarker head) {
		this.head = head;
		return this;
	}

	/**
	 * @return body
	 */
	public String getBody() {
		return body;
	}

	/**
	 * @param body
	 * @return this pull request
	 */
	public _PullRequest setBody(String body) {
		this.body = body;
		return this;
	}

	/**
	 * @return bodyHtml
	 */
	public String getBodyHtml() {
		return bodyHtml;
	}

	/**
	 * @param bodyHtml
	 * @return this pull request
	 */
	public _PullRequest setBodyHtml(String bodyHtml) {
		this.bodyHtml = bodyHtml;
		return this;
	}

	/**
	 * @return bodyText
	 */
	public String getBodyText() {
		return bodyText;
	}

	/**
	 * @param bodyText
	 * @return this pull request
	 */
	public _PullRequest setBodyText(String bodyText) {
		this.bodyText = bodyText;
		return this;
	}

	/**
	 * @return diffUrl
	 */
	public String getDiffUrl() {
		return diffUrl;
	}

	/**
	 * @param diffUrl
	 * @return this pull request
	 */
	public _PullRequest setDiffUrl(String diffUrl) {
		this.diffUrl = diffUrl;
		return this;
	}

	/**
	 * @return htmlUrl
	 */
	public String getHtmlUrl() {
		return htmlUrl;
	}

	/**
	 * @param htmlUrl
	 * @return this pull request
	 */
	public _PullRequest setHtmlUrl(String htmlUrl) {
		this.htmlUrl = htmlUrl;
		return this;
	}

	/**
	 * @return issueUrl
	 */
	public String getIssueUrl() {
		return issueUrl;
	}

	/**
	 * @param issueUrl
	 * @return this pull request
	 */
	public _PullRequest setIssueUrl(String issueUrl) {
		this.issueUrl = issueUrl;
		return this;
	}

	/**
	 * @return patchUrl
	 */
	public String getPatchUrl() {
		return patchUrl;
	}

	/**
	 * @param patchUrl
	 * @return this pull request
	 */
	public _PullRequest setPatchUrl(String patchUrl) {
		this.patchUrl = patchUrl;
		return this;
	}

	/**
	 * @return state
	 */
	public String getState() {
		return state;
	}

	/**
	 * @param state
	 * @return this pull request
	 */
	public _PullRequest setState(String state) {
		this.state = state;
		return this;
	}

	/**
	 * @return title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 * @return this pull request
	 */
	public _PullRequest setTitle(String title) {
		this.title = title;
		return this;
	}

	/**
	 * @return url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url
	 * @return this pull request
	 */
	public _PullRequest setUrl(String url) {
		this.url = url;
		return this;
	}

	/**
	 * @return mergedBy
	 */
	public User getMergedBy() {
		return mergedBy;
	}

	/**
	 * @param mergedBy
	 * @return this pull request
	 */
	public _PullRequest setMergedBy(User mergedBy) {
		this.mergedBy = mergedBy;
		return this;
	}

	/**
	 * @return user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user
	 * @return this pull request
	 */
	public _PullRequest setUser(User user) {
		this.user = user;
		return this;
	}

	/**
	 * @return id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id
	 * @return this pull request
	 */
	public _PullRequest setId(long id) {
		this.id = id;
		return this;
	}

	/**
	 * @return labels
	 */
	public List<Label> getLabels() {
		return labels;
	}

	/**
	 * @param labels
	 * @return this pull request
	 */
	public _PullRequest setLabels(List<Label> labels) {
		this.labels = labels != null ? new ArrayList<>(labels) : null;
		return this;
	}

	/**
	 * @return milestone
	 */
	public Milestone getMilestone() {
		return milestone;
	}

	/**
	 * @param milestone
	 * @return this pull request
	 */
	public _PullRequest setMilestone(Milestone milestone) {
		this.milestone = milestone;
		return this;
	}

	/**
	 * @return assignee
	 */
	public User getAssignee() {
		return assignee;
	}

	/**
	 * @param assignee
	 * @return this pull request
	 */
	public _PullRequest setAssignee(User assignee) {
		this.assignee = assignee;
		return this;
	}

	/**
	 *
	 * @param assignees
	 * @return this pull request
	 */
	public _PullRequest setAssignees(List<User> assignees) {
		this.assignees = assignees;
		return this;
	}

	/**
	 *
	 * @return assignees
	 */
	public List<User> getAssignees() {
		return assignees;
	}

	@Override
	public String toString() {
		return "Pull Request " + number; //$NON-NLS-1$
	}
}
