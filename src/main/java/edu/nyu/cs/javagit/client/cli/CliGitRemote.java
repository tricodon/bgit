package edu.nyu.cs.javagit.client.cli;

import edu.nyu.cs.javagit.api.JavaGitConfiguration;
import edu.nyu.cs.javagit.api.JavaGitException;
import edu.nyu.cs.javagit.api.Ref;
import edu.nyu.cs.javagit.api.commands.CommandResponse;
import edu.nyu.cs.javagit.client.IGitRemote;
import edu.nyu.cs.javagit.utilities.CheckUtilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CliGitRemote implements IGitRemote
{

    public void remote(File repoDirectory, Ref remoteBranch, String remoteUrl)
            throws JavaGitException, IOException
    {
        CheckUtilities.checkFileValidity(repoDirectory);
        GitRemoteParser parser = new GitRemoteParser();
        List<String> command = buildCommand(repoDirectory, remoteBranch.getName(), remoteUrl);
        ProcessUtilities.runCommand(repoDirectory,
                command, parser);
        //if (response.containsError()) {
        //	throw new JavaGitException(418001, "Git Init error");
        //}
    }

    /*
      * Build the command to be executed using the Git Init method
      */
    private List<String> buildCommand(File repoDirectory, String remoteBranch, String remoteUrl)
    {
        List<String> command = new ArrayList<String>();
        command.add(JavaGitConfiguration.getGitCommand());
        command.add("remote");
        command.add("add");
        command.add(remoteBranch);
        command.add(remoteUrl);
        return command;
    }

    /**
     * Parser class to parse the output generated by git init and return a
     * <code>GitInitResponse</code> object.
     */
    public class GitRemoteParser implements IParser
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