package com.infy.jgit.service;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.BranchTrackingStatus;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.springframework.stereotype.Service;

import com.infy.jgit.model.UnpushedCommit;
import com.infy.jgit.model.UnpushedCommitsResponse;
import com.infy.jgit.model.UnpushedFileResponse;

/**
 * Service class to fetch unpushed commits/files from current git branch.
 * <blockquote>Exception handling has not been taken care in this sample app.
 * Please do proper exception handling before using in actual
 * projects.</blockquote>
 * 
 * @author Sujay-PC
 *
 */
@Service
public class UnpushedCommitService {

	/**
	 * This method will check for unpushed commits in your current branchc.
	 * 
	 * @param repo
	 * @return {@link UnpushedCommitsResponse}
	 */
	public UnpushedCommitsResponse getUnpushedCommits(String repo) {
		// this method will be responsible to create the object of unpushed commits.
		FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
		UnpushedCommitsResponse response = new UnpushedCommitsResponse();
		try (Repository repository = repositoryBuilder.setGitDir(new File(repo)).readEnvironment().findGitDir().build();
				Git git = new Git(repository)) {
			List<Integer> counts = new ArrayList<Integer>();
			int aheadCount = 0;
			counts = getCounts(repository, repository.getBranch());
			aheadCount = counts.get(0);
			Iterable<RevCommit> call = git.log().all().call();
			response.setNumberOfUnpushedCommits(aheadCount);
			int count = 0;
			if (aheadCount > 0) {
				List<UnpushedCommit> unpushedCommits = new ArrayList<>();
				for (RevCommit revCommit : call) {
					UnpushedCommit commit = new UnpushedCommit();
					commit.setCommitId(revCommit.getName());
					commit.setCommitMessage(revCommit.getFullMessage());
					commit.setCommitTime(
							Instant.ofEpochSecond(revCommit.getCommitTime()).atZone(ZoneId.systemDefault()));
					unpushedCommits.add(commit);
					count++;
					if (aheadCount == count)
						break;
				}
				response.setUnpushedCommit(unpushedCommits);
				return response;
			} else {
				System.out.println("No Unpushed commits present");
				throw new RuntimeException("No Unpushed commits present");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private List<Integer> getCounts(Repository repository, String name) throws IOException {
		BranchTrackingStatus trackingStatus = BranchTrackingStatus.of(repository, name);
		if (trackingStatus != null) {
			List<Integer> counts = new ArrayList<Integer>();
			counts.add(trackingStatus.getAheadCount());
			counts.add(trackingStatus.getBehindCount());
			return counts;
		} else {
			throw new RuntimeException("No difference found");
		}

	}

	private List<String> listDiff(Repository repository, Git git, String oldCommit, String newCommit)
			throws GitAPIException, IOException {
		final List<DiffEntry> diffs = git.diff().setOldTree(prepareTreeParser(repository, oldCommit))
				.setNewTree(prepareTreeParser(repository, newCommit)).call();

		System.out.println("Found: " + diffs.size() + " differences");
		List<String> unpushedFiles = new ArrayList<>();
		for (DiffEntry diff : diffs) {
			System.out.println("Diff: " + diff.getChangeType() + ": "
					+ (diff.getOldPath().equals(diff.getNewPath()) ? diff.getNewPath()
							: diff.getOldPath() + " -> " + diff.getNewPath()));
			unpushedFiles.add(diff.getNewPath());
		}
		return unpushedFiles;
	}

	private AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException {
		try (RevWalk walk = new RevWalk(repository)) {
			RevCommit commit = walk.parseCommit(repository.resolve(objectId));
			RevTree tree = walk.parseTree(commit.getTree().getId());

			CanonicalTreeParser treeParser = new CanonicalTreeParser();
			try (ObjectReader reader = repository.newObjectReader()) {
				treeParser.reset(reader, tree.getId());
			}

			walk.dispose();

			return treeParser;
		}
	}

	/**
	 * This method will fetch unpushed files for you in your current branch.
	 * 
	 * @param repo
	 * @return {@link UnpushedFileResponse}
	 */
	public UnpushedFileResponse getunpushedFiles(String repo) {
		FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
		try (Repository repository = repositoryBuilder.setGitDir(new File(repo)).readEnvironment().findGitDir().build();
				Git git = new Git(repository)) {
			ObjectId objectId = repository.resolve("HEAD");
			String remoteHeadID = getRemoteHeadID(repositoryBuilder);
			UnpushedFileResponse response = new UnpushedFileResponse();
			response.setUnpushedFiles(listDiff(repository, git, remoteHeadID, objectId.getName()));
			return response;
		} catch (Exception exception) {
			throw new RuntimeException("Failed to get unpushed files");
		}

	}

	private String getRemoteHeadID(FileRepositoryBuilder repositoryBuilder)
			throws GitAPIException, InvalidRemoteException, TransportException, IOException {
		try (Repository repository = repositoryBuilder.readEnvironment().findGitDir().build()) {
			System.out.println("Starting fetch");
			try (Git git = new Git(repository)) {
				FetchResult result = git.fetch().setCheckFetchedObjects(true).call();
				Collection<Ref> advertisedRefs = result.getAdvertisedRefs();
				String remoteHeadID = null;
				for (Iterator<Ref> iterator = advertisedRefs.iterator(); iterator.hasNext();) {
					Ref ref = (Ref) iterator.next();
					if (ref.getName().equals("HEAD")) {
						remoteHeadID = ref.getObjectId().name();
						break;
					}
				}
				return remoteHeadID;
			}
		}
	}
}
