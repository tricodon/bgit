package edu.nyu.cs.javagit.client.cli;

import edu.nyu.cs.javagit.api.JavaGitConfiguration;
import edu.nyu.cs.javagit.api.JavaGitException;
import edu.nyu.cs.javagit.api.Ref;
import edu.nyu.cs.javagit.api.commands.CommandResponse;
import edu.nyu.cs.javagit.client.IGitMerge;
import edu.nyu.cs.javagit.utilities.CheckUtilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CliGitMerge implements IGitMerge
{

    public void merge(File repoDirectory, Ref remoteBranch)
            throws JavaGitException, IOException
    {
        CheckUtilities.checkFileValidity(repoDirectory);
        GitMergeParser parser = new GitMergeParser();
        List<String> command = buildCommand(repoDirectory, remoteBranch.getName());
        ProcessUtilities.runCommand(repoDirectory,
                command, parser);
        //if (response.containsError()) {
        //	throw new JavaGitException(418001, "Git Init error");
        //}
    }

    /*
      * Build the command to be executed using the Git Init method
      */
    private List<String> buildCommand(File repoDirectory, String remoteBranch)
    {
        List<String> command = new ArrayList<String>();
        command.add(JavaGitConfiguration.getGitCommand());
        command.add("merge");
        command.add(remoteBranch);
        return command;
    }

    /**
     * Parser class to parse the output generated by git init and return a
     * <code>GitInitResponse</code> object.
     */
    public class GitMergeParser implements IParser
    {

        public void parseLine(String line)
        {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void processExitCode(int code)
        {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public CommandResponse getResponse() throws JavaGitException
        {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}