package com.infy.jgit.model;

import java.util.List;

public class UnpushedCommitsResponse {

	private int numberOfUnpushedCommits;
	private List<UnpushedCommit> unpushedCommits;

	public int getNumberOfUnpushedCommits() {
		return numberOfUnpushedCommits;
	}

	public void setNumberOfUnpushedCommits(int numberOfUnpushedCommits) {
		this.numberOfUnpushedCommits = numberOfUnpushedCommits;
	}

	public List<UnpushedCommit> getUnpushedCommits() {
		return unpushedCommits;
	}

	public void setUnpushedCommit(List<UnpushedCommit> unpushedCommits) {
		this.unpushedCommits = unpushedCommits;
	}

}
