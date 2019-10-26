/*
 * Copyright (c) 2004 - 2012 Eike Stepper (Loehne, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package promoter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import promoter.util.IO;
import promoter.util.IO.OutputHandler;

/**
 * @author Stefan Winkler
 */
public class Git extends SourceCodeManager
{
  // TODO Make CLONE configurable
  public static final String CLONE = "cd " + PromoterConfig.INSTANCE.getProjectCloneLocation() + ";\n";

  public static final String GIT_BINARY = CLONE + PromoterConfig.INSTANCE.getGitExecutable();

  // The symbolic name (alternatively: the URL) of the upstream (main) Git repository
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

  private boolean fetched;

  public Git()
  {
  }

  private void fetchIfNeeded(PrintStream stream)
  {
    if (!fetched)
    {
      stream.println(GIT_BINARY + " fetch");
      stream.flush();

      fetched = true;
    }
  }

  @Override
  public void setTag(final String branch, final String revision, final String qualifier)
  {
    // TODO No write access to direct Git anymore ;-(

    // IO.executeProcess("/bin/bash", new OutputHandler()
    // {
    // public void handleOutput(OutputStream out) throws IOException
    // {
    // PrintStream stream = new PrintStream(out);
    // fetchIfNeeded(stream);
    //
    // String tag = "drops/" + qualifier;
    // System.out.println("Tagging " + revision + " in " + branch + " as " + tag);
    //
    // // Create the tag
    // String message = qualifier + " in " + branch;
    // stream.println(GIT_BINARY + " tag -a -m \"" + message + "\" \"" + tag + "\" \"" + revision + "\"");
    //
    // // Push the tag
    // stream.println(GIT_BINARY + " push \"" + REMOTE_GIT + "\" \"" + tag + "\"");
    // stream.flush();
    // }
    // });
  }

  @Override
  public void handleLogEntries(final String branch, final String fromRevision, final String toRevision, final boolean withPaths, final LogEntryHandler handler)
  {
    try
    {
      final File outFile = File.createTempFile("promotion-", ".tmp");

      IO.executeProcess("/bin/bash", new OutputHandler()
      {
        public void handleOutput(OutputStream out) throws IOException
        {
          PrintStream stream = new PrintStream(out);
          fetchIfNeeded(stream);

          String range = fromRevision + ".." + toRevision;
          System.out.println("Getting log entries for " + branch + " (" + range + ")");

          String command = GIT_BINARY + " log " + (withPaths ? "--name-only " : "") + " --format=\"" + OUTPUT_FORMAT + "\" " + range + " > " + outFile;
          stream.println(command);
          stream.flush();
        }
      });

      FileReader fileReader = new FileReader(outFile);
      BufferedReader bufferedReader = new BufferedReader(fileReader);

      try
      {
        LogEntry logEntry;

        // Start of file. First line has to be "--BEGIN-COMMIT--".
        String line = bufferedReader.readLine();
        if (line == null)
        {
          return; // Empty log.
        }

        if (!line.equals("--BEGIN-COMMIT--"))
        {
          throw new IllegalStateException("Read unexpected line " + line + " at beginning of file " + outFile.getAbsolutePath());
        }

        // First line successfully read. Start processing of log entries:

        processing: //
        for (;;)
        {
          String commitHash = readLineSafe(bufferedReader);
          logEntry = new LogEntry(commitHash);

          String committer = readLineSafe(bufferedReader);
          logEntry.setAuthor(committer);

          String date = readLineSafe(bufferedReader);
          logEntry.setDate(date);

          // Now follows the message until the summary marker is read.
          StringBuilder messageString = new StringBuilder();
          while (!(line = readLineSafe(bufferedReader)).equals("--BEGIN-SUMMARY--"))
          {
            messageString.append(line);
            messageString.append("\n");
          }

          logEntry.setMessage(messageString.toString());

          summaryReading: //
          for (;;)
          {
            line = bufferedReader.readLine();
            if (line == null)
            {
              handler.handleLogEntry(logEntry);
              break processing; // End of file reached.
            }

            if (line.equals("--BEGIN-COMMIT--"))
            {
              handler.handleLogEntry(logEntry);
              break summaryReading; // End of summary section reached.
            }

            if (line.trim().length() == 0)
            {
              continue; // Read over empty lines.
            }

            // We are in the summary section. Read line should contain a path.
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
