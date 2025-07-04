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
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import promoter.util.IO;

/**
 * @author Stefan Winkler
 */
public class Git extends SourceCodeManager
{
  private static final String GIT_COMMAND = "\"" + PromoterConfig.INSTANCE.getGitExecutable() + "\"";

  // The symbolic name (alternatively: the URL) of the upstream (main) Git repository
  @Deprecated
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

  private static final boolean WINDOWS = System.getProperty("os.name").contains("indows");

  private boolean fetched;

  public Git()
  {
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
  public void getCommits(String branch, String fromRevision, String toRevision, BiConsumer<String, String> handler)
  {
    try
    {
      final File outFile = File.createTempFile("promotion-", ".tmp");

      List<String> command = new ArrayList<>();

      if (WINDOWS)
      {
        command.add("cmd.exe");
        command.add("/c");
      }
      else
      {
        command.add("/bin/bash");
      }

      int exitValue = IO.executeProcess(command, out -> {
        PrintStream stream = new PrintStream(out);
        cloneIfNeeded(stream);

        stream.println("cd \"" + PromoterConfig.INSTANCE.getProjectCloneLocation() + "\"");
        fetchIfNeeded(stream);

        String range = fromRevision + ".." + toRevision;
        System.out.println("Getting log entries for " + branch + " (" + range + ")");

        String outputFormat = OUTPUT_FORMAT;
        if (WINDOWS)
        {
          outputFormat = outputFormat.replace("%", "%%");
        }

        stream.println(GIT_COMMAND + " log --format=\"" + outputFormat + "\" " + range + " > " + outFile);
        stream.flush();
      });

      if (exitValue != 0)
      {
        throw new RuntimeException("Command '" + command + "' ended with exit value " + exitValue);
      }

      FileReader fileReader = new FileReader(outFile);
      BufferedReader bufferedReader = new BufferedReader(fileReader);

      try
      {
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
          /* String committer = */ readLineSafe(bufferedReader);
          /* String date = */readLineSafe(bufferedReader);

          // Now follows the message until the summary marker is read.
          StringBuilder messageString = new StringBuilder();
          while (!(line = readLineSafe(bufferedReader)).equals("--BEGIN-SUMMARY--"))
          {
            messageString.append(line);
            messageString.append("\n");
          }

          handler.accept(commitHash, messageString.toString());

          summaryReading: //
          for (;;)
          {
            line = bufferedReader.readLine();
            if (line == null)
            {
              break processing; // End of file reached.
            }

            if (line.equals("--BEGIN-COMMIT--"))
            {
              break summaryReading; // End of summary section reached.
            }

            if (line.trim().length() == 0)
            {
              continue; // Read over empty lines.
            }
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
    catch (RuntimeException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  private void cloneIfNeeded(PrintStream stream)
  {
    File clone = PromoterConfig.INSTANCE.getProjectCloneLocation();
    if (!clone.exists())
    {
      File parent = clone.getParentFile();
      parent.mkdirs();

      String url = PromoterConfig.INSTANCE.getGitRepositoryURL();

      System.out.println("Cloning " + url + " to " + clone);
      stream.println(GIT_COMMAND + " clone --bare " + url + " " + clone);
      stream.flush();
      fetched = true;
    }
  }

  private void fetchIfNeeded(PrintStream stream)
  {
    if (!fetched)
    {
      System.out.println("Fetching " + PromoterConfig.INSTANCE.getProjectCloneLocation() + " to " + PromoterConfig.INSTANCE.getProjectCloneLocation());
      stream.println(GIT_COMMAND + " fetch");
      stream.flush();
      fetched = true;
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
}
