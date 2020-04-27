package com.infy.jgit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infy.jgit.model.UnpushedCommitsResponse;
import com.infy.jgit.model.UnpushedFileResponse;
import com.infy.jgit.service.UnpushedCommitService;

/**
 * Controller to handle requests for fetching information from GIT.
 * <blockquote>Exception handling has not been taken care in this sample app.
 * Please do proper exception handling before using in actual
 * projects.</blockquote>
 * 
 * @author Sujay-PC
 *
 */
@RestController
public class UnpushedCommitsController {

	@Autowired
	private UnpushedCommitService service;

	@GetMapping(value = "/unpushed-commits", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UnpushedCommitsResponse> getUnpushedCommits(@RequestParam("repo") String repo) {
		UnpushedCommitsResponse unpushedCommits = null;
		unpushedCommits = service.getUnpushedCommits(repo);
		return new ResponseEntity<UnpushedCommitsResponse>(unpushedCommits, HttpStatus.OK);
	}

	@GetMapping(value = "/unpushed-files", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UnpushedFileResponse> getUnpushedFiles(@RequestParam("repo") String repo) {
		return new ResponseEntity<UnpushedFileResponse>(service.getunpushedFiles(repo), HttpStatus.OK);
	}
}
