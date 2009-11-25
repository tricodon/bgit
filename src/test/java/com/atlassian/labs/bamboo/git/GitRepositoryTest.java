package com.atlassian.labs.bamboo.git;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.atlassian.labs.bamboo.git.model.CommitDescriptor;
import com.atlassian.labs.bamboo.git.model.HardCodedRepo;
import com.atlassian.labs.bamboo.git.model.Sha;
import edu.nyu.cs.javagit.api.JavaGitException;
import edu.nyu.cs.javagit.api.Ref;
import edu.nyu.cs.javagit.api.commands.GitCloneOptions;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.After;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import com.atlassian.bamboo.commit.Commit;
import com.atlassian.bamboo.repository.RepositoryException;

/**
 * @author Kristian Rosenvold
 */
public class GitRepositoryTest
{
    private static String getGitHubRepoUrl() {
        return "git://github.com/krosenvold/bgit-unittest.git";
    }


    @BeforeClass
    public static void getFromGitHub() throws IOException, JavaGitException {
        final File localRepo = getMasterRepoCheckoutDirectory();
        if ( !GitRepository.containsValidRepo(localRepo)){
            GitCloneOptions gitCloneOptions = new GitCloneOptions(false, false, true);
            GitRepository.clone( getGitHubRepoUrl(), localRepo, gitCloneOptions);
        }
    }


    @After
    public void deleteWorkingCopy() throws IOException, JavaGitException {
        deleteDir( getWorkingCopyDir());
    }


    @Test
    public void testClone() throws IOException, JavaGitException {
        GitRepository gitRepository = getGitRepository("feature1");
        File sourceDir = getFreshCheckoutDir();
        
        assertFalse( GitRepository.containsValidRepo( sourceDir));
        gitRepository.cloneOrFetch(sourceDir);
        assertTrue( GitRepository.containsValidRepo( sourceDir));

        assertEquals("Repository should be on feature1 branch", "feature1", gitRepository.gitStatus(sourceDir).getName());
    }

    private File getFreshCheckoutDir() {
        return getCheckoutDirectory(getFreshWorkingCopyDir());
    }


    @Test
    public void testCloneDefault() throws IOException, JavaGitException {
        GitRepository gitRepository = getGitRepository(null);
        File sourceDir = getFreshCheckoutDir();
        gitRepository.cloneOrFetch(sourceDir);
        assertEquals("featureDefault", gitRepository.gitStatus(sourceDir).getName());
    }

    @Test
    public void testIsOnBranch() throws IOException, JavaGitException {
        GitRepository gitRepository = getGitRepository( null);
        File sourceDir = getFreshCopyInCheckoutDir(gitRepository);

        assertTrue(gitRepository.isOnBranch(sourceDir, Ref.createBranchRef("featureDefault")));
        assertFalse(gitRepository.isOnBranch(sourceDir, Ref.createBranchRef("feature1")));
    }


    @Test
    public void testHistory() throws IOException, JavaGitException, RepositoryException {
        GitRepository gitRepository = getGitRepository( "featureDefault");
        File sourceDir = getFreshCopyInCheckoutDir(gitRepository);

        List<com.atlassian.bamboo.commit.Commit> results = new ArrayList<Commit>();

        gitRepository.detectCommitsForUrl(HardCodedRepo.first.getSha().getSha(), results, sourceDir, "UT-KEY");
        final CommitDescriptor commitDescriptor = HardCodedRepo.getBranchPointerFeatureDefault();


        System.out.println("commitDescriptor = " + commitDescriptor.collectNodesInRealGitLogOrder(HardCodedRepo.first.getSha()).toString());
        commitDescriptor.assertHistoryMatch( results, HardCodedRepo.first.getSha());
    }

    @Test
    public void testHistoryWithMergeCommit() throws IOException, JavaGitException, RepositoryException {
        GitRepository gitRepository = getGitRepository( "featureDefault");
        File sourceDir = getFreshCopyInCheckoutDir(gitRepository);

        List<com.atlassian.bamboo.commit.Commit> results = new ArrayList<Commit>();
        gitRepository.detectCommitsForUrl(HardCodedRepo.getFristCommitInBranch().getSha().getSha(), results, sourceDir, "UT-KEY");

        assertEquals( 7 , results.size());
    }

    @Test
    public void testHistoryFeature1() throws IOException, JavaGitException, RepositoryException {
        GitRepository gitRepository = getGitRepository( "feature1");
        File sourceDir = getFreshCopyInCheckoutDir(gitRepository);

        List<com.atlassian.bamboo.commit.Commit> results = new ArrayList<Commit>();
        final Sha untilSha = HardCodedRepo.getRootCommit().getSha();
        gitRepository.detectCommitsForUrl(untilSha.getSha(), results, sourceDir, "UT-KEY");

        HardCodedRepo.getFeature1Head().assertHistoryMatch( results, untilSha);
    }
    @Test
    public void testHistoryFeature2() throws IOException, JavaGitException, RepositoryException {
        GitRepository gitRepository = getGitRepository( "feature2");
        File sourceDir = getFreshCopyInCheckoutDir(gitRepository);

        List<com.atlassian.bamboo.commit.Commit> results = new ArrayList<Commit>();
        final Sha untilSha = HardCodedRepo.getRootCommit().getSha();
        gitRepository.detectCommitsForUrl(untilSha.getSha(), results, sourceDir, "UT-KEY");
        HardCodedRepo.getFeature2Head().assertHistoryMatch( results, untilSha);
    }

    @Test
    public void testNonLinearHistory() throws IOException, JavaGitException, RepositoryException {
        GitRepository gitRepository = getGitRepository( "featureDefault");
        File sourceDir = getFreshCopyInCheckoutDir(gitRepository);

        List<com.atlassian.bamboo.commit.Commit> results = new ArrayList<Commit>();

        gitRepository.detectCommitsForUrl(HardCodedRepo.COMMIT_fb65.getSha().getSha(), results, sourceDir, "UT-KEY");
        assertEquals(5, results.size());

        results = new ArrayList<Commit>();
        gitRepository.detectCommitsForUrl(HardCodedRepo.COMMIT_2d9b.getSha().getSha()  , results, sourceDir, "UT-KEY");
        HardCodedRepo.getBranchPointerFeatureDefault().assertHistoryMatch( results, HardCodedRepo.COMMIT_2d9b.getSha());


        // This tes fails because the test-data representation is still not correct.
        // Method collectNodesInRealGitLogOrder does not do it properly -- yet. Need to check both date and sha1
        results = new ArrayList<Commit>();
        gitRepository.detectCommitsForUrl(HardCodedRepo.COMMIT_3a45.getSha().getSha(), results, sourceDir, "UT-KEY");
        HardCodedRepo.getBranchPointerFeatureDefault().assertHistoryMatch( results, HardCodedRepo.COMMIT_3a45.getSha());
    }

    @Test
    public void testPluginUpgrade() throws IOException, JavaGitException, RepositoryException {
        GitRepository gitRepository = getGitRepository( "featureDefault");
        File sourceDir = getFreshCopyInCheckoutDir(gitRepository);

        List<com.atlassian.bamboo.commit.Commit> results = new ArrayList<Commit>();
        gitRepository.detectCommitsForUrl("Fri Oct 9 15:38:10 2009 +0200", results, sourceDir, "UT-KEY");

        assertEquals(8, results.size());
    }

    @Test
    public void testLastCheckedRevisionIsNull() throws IOException, JavaGitException, RepositoryException {
        GitRepository gitRepository = getGitRepository("featureDefault");
        File sourceDir = getFreshCopyInCheckoutDir(gitRepository);

        List<com.atlassian.bamboo.commit.Commit> results = new ArrayList<Commit>();
        gitRepository.detectCommitsForUrl(null, results, sourceDir, "UT-KEY");

        assertEquals(10, results.size());
    }

    private static File getMasterRepoWorkingDirectory() {
        File masterRepoDir = new File("masterRepo");
        ensureDirExists(masterRepoDir);
        return masterRepoDir;
    }

    private static File getWorkingCopyDir() {
        return new File("testRepo");
    }

    
    private static File getMasterRepoCheckoutDirectory() {
        return getMasterRepoCheckoutDirectory(getMasterRepoWorkingDirectory().getPath());
    }
    private static File getCWDRelativeMasterRepoCheckoutDirectory() {
        return getMasterRepoCheckoutDirectory(".." + File.separator + getMasterRepoWorkingDirectory().getPath());
    }
    private static File getMasterRepoCheckoutDirectory(String localPart) {
        try {
            final File file = new File(localPart + File.separator + GitRepository.getLocalCheckoutSubfolder());
            ensureDirExists( file);
            return file;
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }


    private static File getCheckoutDirectory(File workingDirectory){
        try {
            return new File(workingDirectory.getCanonicalPath() + File.separator + GitRepository.getLocalCheckoutSubfolder());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static File getFreshWorkingCopyDir() {
        File workingCopyDir = new File("testRepo");
        if (workingCopyDir.exists()) deleteDir( workingCopyDir);
        ensureDirExists(workingCopyDir);
        return workingCopyDir;
    }

    private static void ensureDirExists(File workingCopyDir) {
        if (!workingCopyDir.exists()){
            //noinspection ResultOfMethodCallIgnored
            workingCopyDir.mkdir();
        }
    }


    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

     private File getFreshCopyInCheckoutDir(GitRepository gitRepository) throws IOException, JavaGitException {
        final File directory = getCheckoutDirectory(getFreshWorkingCopyDir());
//        GitCloneOptions gitCloneOptions = new GitCloneOptions(false, true, false);
        gitRepository.cloneOrFetch( directory);
        return directory;
    }


    private GitRepository getGitRepository(String remoteBranch) throws IOException {
        return new GitRepository(getCWDRelativeMasterRepoCheckoutDirectory().getPath(), remoteBranch);
    }


}