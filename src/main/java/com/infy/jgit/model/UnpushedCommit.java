package com.infy.jgit.model;

import java.time.ZonedDateTime;

public class UnpushedCommit {

	private String commitId;
	private String commitMessage;
	private ZonedDateTime commitTime;

	public String getCommitId() {
		return commitId;
	}

	public void setCommitId(String commitId) {
		this.commitId = commitId;
	}

	public String getCommitMessage() {
		return commitMessage;
	}

	public void setCommitMessage(String commitMessage) {
		this.commitMessage = commitMessage;
	}

	public ZonedDateTime getCommitTime() {
		return commitTime;
	}

	public void setCommitTime(ZonedDateTime zonedDateTime) {
		this.commitTime = zonedDateTime;
	}

}
