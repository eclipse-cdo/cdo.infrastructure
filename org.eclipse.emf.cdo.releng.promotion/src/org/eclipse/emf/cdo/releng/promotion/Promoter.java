/**
 * Copyright (c) 2004 - 2011 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package org.eclipse.emf.cdo.releng.promotion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * @author Eike Stepper
 */
public class Promoter
{
  private static String downloadsDir;

  private static String hudsonJobsDir;

  private static String jobName;

  private static File hudsonJob;

  private static File builds;

  private static File drops;

  public static void main(String[] args) throws Exception
  {
    if (args.length != 5)
    {
      System.err
          .println("Specify exactly five arguments, "
              + "e.g. Promoter /path/to/downloads /path/to/hudson/jobs hudson-job-name last-build-number next-build-number");
      System.exit(2);
    }

    String workingDir = new File("").getAbsolutePath();
    System.out.println("Working directory is " + workingDir);

    downloadsDir = args[0];
    hudsonJobsDir = args[1];
    jobName = args[2];

    hudsonJob = new File(hudsonJobsDir, jobName);
    builds = new File(hudsonJob, "builds");
    drops = new File(downloadsDir, "drops");

    final int lastBuildNumber = Integer.parseInt(args[3]);
    final int nextBuildNumber = Integer.parseInt(args[4]);
    int lastCheckedPromotion = lastBuildNumber;

    try
    {

      for (int buildNumber = lastBuildNumber; buildNumber < nextBuildNumber; buildNumber++)
      {
        File build = new File(builds, String.valueOf(buildNumber));
        if (build.exists())
        {
          checkBuild(build);
          lastCheckedPromotion = buildNumber;
        }
      }
    }
    finally
    {
      if (lastCheckedPromotion != lastBuildNumber)
      {
        saveNextBuildNumber(lastCheckedPromotion + 1);
      }
    }
  }

  private static void checkBuild(File build) throws IOException
  {
    // if (!build.exists())
    // {
    // build = new File("jobs/" + jobName + "/builds/" + build.getName());
    // if (!build.exists())
    // {
    // throw new IOException(build.getAbsolutePath() + " does not exist");
    // }
    // }

    File archive = new File(build, "archive");
    if (archive.exists() && archive.isDirectory())
    {
      File file = new File(archive, "build-info.xml");
      if (file.exists() && file.isFile())
      {
        BuildInfo buildInfo = XML.getBuildInfo(file);
        String buildType = buildInfo.getType();
        String buildQualifier = buildInfo.getQualifier();

        if (!"N".equals(buildType))
        {
          System.out.println("Ignoring " + buildQualifier);
        }
        else
        {
          File drop = new File(drops, buildQualifier);
          if (drop.exists())
          {
            if (!drop.isDirectory())
            {
              System.err.println("Warning: " + drop.getAbsolutePath() + " is not a directory!");
            }
          }
          else
          {
            System.out.println("Promoting " + buildQualifier);
            drop.mkdirs();
          }
        }
      }
    }
  }

  private static void saveNextBuildNumber(int nextBuildNumber) throws IOException
  {
    System.out.println("Remembering next build to check: " + nextBuildNumber);
    FileOutputStream out = null;

    try
    {
      out = new FileOutputStream("jobs/" + jobName + "/nextBuildNumber");
      PrintStream stream = new PrintStream(out);
      stream.println(nextBuildNumber);
      stream.flush();
    }
    finally
    {
      if (out != null)
      {
        out.close();
      }
    }
  }
}
