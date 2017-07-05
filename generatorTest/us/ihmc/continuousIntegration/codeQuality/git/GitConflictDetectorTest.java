package us.ihmc.continuousIntegration.codeQuality.git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.junit.Assert;
import org.junit.Test;

import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationPlan;
import us.ihmc.continuousIntegration.ContinuousIntegrationAnnotations.ContinuousIntegrationTest;
import us.ihmc.continuousIntegration.model.AgileTestingProject;
import us.ihmc.continuousIntegration.tools.SourceTools;
import us.ihmc.continuousIntegration.AgileTestingProjectLoader;
import us.ihmc.continuousIntegration.AgileTestingTools;
import us.ihmc.continuousIntegration.IntegrationCategory;

@ContinuousIntegrationPlan(categories = {IntegrationCategory.HEALTH})
public class GitConflictDetectorTest
{
   private final String DEVELOP_BRANCH_NAME = "develop";
   private final String MASTER_BRANCH_NAME = "master";

   @ContinuousIntegrationTest(estimatedDuration = 60.0)
   @Test(timeout = 120000)
   public void testNoBranchConflicts()
   {
      Map<String, AgileTestingProject> bambooEnabledProjects = AgileTestingTools.loadATProjects(new AgileTestingProjectLoader()
      {
         @Override
         public boolean meetsCriteria(AgileTestingProject atProject)
         {
            return atProject.isBambooEnabled();
         }
      }, SourceTools.getWorkspacePath());

      try
      {
         String errorMessage = "";
         for(String project : bambooEnabledProjects.keySet())
         {
            Path projectPath = bambooEnabledProjects.get(project).getPath();
            HashMap<Ref, List<DiffEntry>> repoDiffsMap = repoDiffsMap = getBranchDiffsWithDevelopBranchForRepo(projectPath.toString());
            HashMap<Ref, HashMap<Ref, List<String>>> conflictsMap = buildConflictsMap(repoDiffsMap);

            for(Ref branch : conflictsMap.keySet())
            {
               for(Ref conflictingBranch : conflictsMap.get(branch).keySet())
               {
                  List<String> conflicts = conflictsMap.get(branch).get(conflictingBranch);

                  if(conflicts.size() > 0)
                  {
                     errorMessage += "Conflict between " + branch.getName() + " and " + conflictingBranch.getName() + "\n";
                  }
               }
            }
         }

         Assert.assertTrue(errorMessage, errorMessage.isEmpty());
      }
      catch(IOException | GitAPIException e)
      {
         e.printStackTrace();
      }
   }

   private HashMap<Ref, List<DiffEntry>> getBranchDiffsWithDevelopBranchForRepo(String repositoryPath) throws IOException, GitAPIException
   {
      FileRepositoryBuilder builder = new FileRepositoryBuilder();
      try(Repository repo = builder.findGitDir(new File(repositoryPath)).readEnvironment().build())
      {
         try(Git git = new Git(repo))
         {
            Ref developBranch = repo.findRef(DEVELOP_BRANCH_NAME);
            Ref masterBranch = repo.findRef(MASTER_BRANCH_NAME);

            HashMap<Ref, List<DiffEntry>> branchDiffsWithDevelopMap = new HashMap<>();
            for(Ref branch : git.branchList().call())
            {
               if(branch != developBranch && branch != masterBranch)
               {
                  List<DiffEntry> branchDiffWithDevelopBranch = getBranchDiff(repo, git, developBranch, branch);
                  branchDiffsWithDevelopMap.put(branch, branchDiffWithDevelopBranch);
               }
            }

            return branchDiffsWithDevelopMap;
         }
      }
   }

   private List<DiffEntry> getBranchDiff(Repository repo, Git git, Ref branch, Ref otherBranch) throws IOException, GitAPIException
   {
      AbstractTreeIterator oldTreeParser = prepareTreeParser(repo, branch);
      AbstractTreeIterator newTreeParser = prepareTreeParser(repo, otherBranch);

      List<DiffEntry> diffs = git.diff().setOldTree(oldTreeParser).setNewTree(newTreeParser).call();

      return diffs;
   }

   private AbstractTreeIterator prepareTreeParser(Repository repo, Ref branchRef) throws IOException
   {
      try(RevWalk walk = new RevWalk(repo))
      {
         RevCommit commit = walk.parseCommit(branchRef.getObjectId());
         RevTree tree = walk.parseTree(commit.getTree().getId());
         CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
         try(ObjectReader oldReader = repo.newObjectReader())
         {
            oldTreeParser.reset(oldReader, tree.getId());
         }

         walk.dispose();

         return oldTreeParser;
      }
   }

   private HashMap<Ref, HashMap<Ref, List<String>>> buildConflictsMap(HashMap<Ref, List<DiffEntry>> diffMap)
   {
      Object[] refsArray = diffMap.keySet().toArray();

      HashMap<Ref, HashMap<Ref, List<String>>> conflictMap = new HashMap<>();

      for(int i = 0; i < refsArray.length; i++)
      {
         Ref initialBranch = (Ref) refsArray[i];
         List<DiffEntry> diffs = diffMap.get(initialBranch);
         for(int j = i + 1; j < refsArray.length; j++)
         {
            Ref branchToCompare = (Ref) refsArray[j];
            List<DiffEntry> otherDiffs = diffMap.get(branchToCompare);
            List<String> fileConflicts = findFileConflicts(diffs, otherDiffs);

            if(!fileConflicts.isEmpty())
            {
               addBranchConflictsToMap(conflictMap, initialBranch, branchToCompare, fileConflicts);
//               addBranchConflictsToMap(conflictMap, branchToCompare, initialBranch, fileConflicts);
            }
         }
      }

      return conflictMap;
   }

   private List<String> findFileConflicts(List<DiffEntry> diffs, List<DiffEntry> otherDiffs)
   {
      List<String> fileConflicts = new ArrayList<>();
      for(DiffEntry diff : diffs)
      {
         if(!diff.getChangeType().equals(DiffEntry.ChangeType.ADD))
         {
            for(DiffEntry otherDiff : otherDiffs)
            {
               if(diff.getOldPath().equals(otherDiff.getOldPath()))
               {
                  fileConflicts.add(diff.getOldPath());
               }
            }
         }
      }

      return fileConflicts;
   }

   private void addBranchConflictsToMap(HashMap<Ref, HashMap<Ref, List<String>>> conflictMap, Ref branch, Ref otherBranch, List<String> fileConflicts)
   {
      if(!conflictMap.containsKey(branch))
         conflictMap.put(branch, new HashMap<Ref, List<String>>());

      if(!conflictMap.get(branch).containsKey(otherBranch))
         conflictMap.get(branch).put(otherBranch, new ArrayList<String>());

      conflictMap.get(branch).get(otherBranch).addAll(fileConflicts);
   }
}
