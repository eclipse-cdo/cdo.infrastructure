package tasks.hudson;

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

import util.BuildInfo;
import util.XML;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.regex.Pattern;

/**
 * @author Eike Stepper
 */
public final class HudsonOld
{
  private static final Pattern QUALIFIER_PATTERN = Pattern
      .compile("[NIMSR]20[0-9][0-9][0-1][0-9][0-3][0-9]-[0-2][0-9][0-5][0-9]");

  private static String jobName;

  private static File hudsonJob;

  private static File builds;

  private static File drops;

  private HudsonOld()
  {
  }

  public static void main(String[] args) throws Exception
  {
    jobName = args[2];

    // hudsonJob = new File(hudsonJobsArea, jobName);
    // builds = new File(hudsonJob, "builds");
    // drops = new File(projectDownloadsArea, "drops");

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
    File archive = new File(build, "archive");
    if (!archive.exists() || !archive.isDirectory())
    {
      System.err.println(build.getName() + ": " + archive.getAbsolutePath() + " does not exist or is not a directory.");
      return;
    }

    File file = new File(archive, "build-info.xml");
    if (!file.exists() || !file.isFile())
    {
      System.err.println(build.getName() + ": " + file.getAbsolutePath() + " does not exist or is not a file.");
      return;
    }

    BuildInfo buildInfo = XML.readBuildInfo(file);
    String buildQualifier = buildInfo.getQualifier();
    if (!QUALIFIER_PATTERN.matcher(buildQualifier).matches())
    {
      System.err.println(build.getName() + ": Build qualifier " + buildQualifier + " is invalid.");
      return;
    }

    File drop = new File(drops, buildQualifier);
    if (drop.exists())
    {
      System.out.println(build.getName() + ": " + drop + " already exists.");
      return;
    }

    // buildInfo.setBuild(build);
    // buildInfo.setDrop(drop);

    Promoter promoter = createPromoter(buildInfo);
    if (promoter != null)
    {
      promoter.run();
    }
  }

  private static Promoter createPromoter(BuildInfo buildInfo)
  {
    String buildType = buildInfo.getType();
    if ("N".equals(buildType))
    {
      return new PromoterNightly(buildInfo);
    }

    if ("I".equals(buildType))
    {
      return new PromoterIntegration(buildInfo);
    }

    if ("M".equals(buildType))
    {
      return new PromoterMaintenance(buildInfo);
    }

    if ("S".equals(buildType))
    {
      return new PromoterStable(buildInfo);
    }

    if ("R".equals(buildType))
    {
      return new PromoterRelease(buildInfo);
    }

    throw new IllegalArgumentException("Unrecognized build type: " + buildType);
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
