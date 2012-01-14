/*
 * Copyright (c) 2004 - 2012 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package promoter;

import promoter.util.IO;
import promoter.util.IO.OutputHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author Stefan Winkler
 */
public class Git extends SourceCodeManager
{
  public static final String GIT_BINARY = "/usr/local/bin/git";

  // TODO Make GIT_REPO configurable
  public static final String GIT_REPO = "ssh://USER@git.eclipse.org/gitroot/cdo/org.eclipse.emf.cdo.git";

  // TODO the symbolic name (alternatively: the URL) of the upstream (main) Git repository
  public static final String REMOTE_GIT = "origin";

  // the following makes Git output
  // "--BEGIN-COMMIT--
  // hash
  // committer name
  // committer date
  // message (multiline)
  // --BEGIN-SUMMARY--
  // summary (multiline)"
  // for each history entry
  public static final String OUTPUT_FORMAT = "--BEGIN-COMMIT--%n%h%n%cn%n%ci%n%B%n--BEGIN-SUMMARY--%n";

  public Git()
  {
  }

  @Override
  public String getNextRevision(String revision)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setTag(final String branch, final String tag)
  {
    IO.executeProcess("/bin/bash", new OutputHandler()
    {
      public void handleOutput(OutputStream out) throws IOException
      {
        String message = "Tagging " + branch + " as drops/" + tag;
        System.out.println(message);

        String to = "drops/" + tag;
        String from = branch;

        @SuppressWarnings("resource")
        PrintStream stream = new PrintStream(out);
        // create the tag
        stream.println(GIT_BINARY + " tag -a -m \"" + message + "\" \"" + to + "\" \"" + from + "\"");
        // push the tag
        stream.println(GIT_BINARY + " push \"" + REMOTE_GIT + "\" \"" + to + "\"");
        stream.flush();
      }
    });
  }

  @Override
  public void handleLogEntries(final String branch, final String fromRevision, final String toRevision,
      final boolean withPaths, final LogEntryHandler handler)
  {
    try
    {
      final File outFile = File.createTempFile("promotion-", ".tmp");

      IO.executeProcess("/bin/bash", new OutputHandler()
      {
        public void handleOutput(OutputStream out) throws IOException
        {
          String range = fromRevision + ".." + toRevision;
          System.out.println("Getting log entries for " + branch + " (" + range + ")");

          @SuppressWarnings("resource")
          PrintStream stream = new PrintStream(out);
          stream.println(GIT_BINARY + " log " + (withPaths ? "--name-only " : "") + " --format=\"" + OUTPUT_FORMAT
              + "\" " + range + " > " + outFile);
          stream.flush();
        }
      });

      FileReader fileReader = new FileReader(outFile);
      BufferedReader bufferedReader = new BufferedReader(fileReader);

      try
      {
        LogEntry logEntry;

        // start of file. First line has to be "--BEGIN-COMMIT--"
        String line = bufferedReader.readLine();
        if (line == null)
        {
          return; // empty log
        }
        if (!line.equals("--BEGIN-COMMIT--"))
        {
          throw new IllegalStateException("Read unexpected line " + line + " at beginning of file "
              + outFile.getAbsolutePath());
        }

        // first line successfully read. Start processing of log entries:

        processing: for (;;)
        {
          String commitHash = readLineSafe(bufferedReader);
          logEntry = new LogEntry(commitHash);

          String committer = readLineSafe(bufferedReader);
          logEntry.setAuthor(committer);

          String date = readLineSafe(bufferedReader);
          logEntry.setDate(date);

          // new follows the message until the summary marker is read
          StringBuilder messageString = new StringBuilder();
          while (!(line = readLineSafe(bufferedReader)).equals("--BEGIN-SUMMARY--"))
          {
            messageString.append(line);
            messageString.append("\n");
          }

          summaryReading: for (;;)
          {
            line = bufferedReader.readLine();
            if (line == null)
            {
              handler.handleLogEntry(logEntry);
              break processing; // end of file reached
            }
            if (line.equals("--BEGIN-COMMIT--"))
            {
              handler.handleLogEntry(logEntry);
              break summaryReading; // end of summary section reached
            }
            if (line.trim().length() == 0)
            {
              continue; // read over empty lines
            }

            // we are in the summary section. Read line should contain a path
            logEntry.getPaths().add(line);
          }
        }
      }
      finally
      {
        bufferedReader.close();
        fileReader.close();
        outFile.delete();
      }
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  private String readLineSafe(BufferedReader bufferedReader) throws IOException
  {
    String result = bufferedReader.readLine();
    if (result == null)
    {
      throw new IllegalStateException("Unexpected end of stream");
    }
    return result;
  }

  @Override
  public void commit(String comment, File... checkouts)
  {
    throw new UnsupportedOperationException();
  }
}
