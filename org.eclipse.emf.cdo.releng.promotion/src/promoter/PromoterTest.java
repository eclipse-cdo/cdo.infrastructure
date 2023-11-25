/*
 * Copyright (c) 2004-2013 Eike Stepper (Loehne, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package promoter;

import java.io.File;

import promoter.util.Ant;

/**
 * @author Eike Stepper
 */
public class PromoterTest extends Promoter
{
  private static final File TEST_DIR = new File("test");

  private static final File DOWNLOADS_HOME = new File(TEST_DIR, "downloads");

  private static final File ARCHIVE_HOME = new File(TEST_DIR, "archive");

  private static final File WORKING_AREA = new File(TEST_DIR, "workspace");

  private static final File GIT_EXECUTABLE = new File("C:\\Program Files\\Git\\bin\\git.exe");

  private static final boolean COPY_BUILDS = false;

  public PromoterTest()
  {
    super(true, !COPY_BUILDS, true, false);
  }

  @Override
  public Ant<AntResult> createAnt()
  {
    File script = new File(PromoterConfig.INSTANCE.getWorkingArea(), "promoter.ant");
    File basedir = PromoterConfig.INSTANCE.getDownloadsArea();
    return new DefaultAnt(script, basedir)
    {
      @Override
      protected void executeAntScript()
      {
        // super.executeAntScript();
      }
    };
  }

  public static void main(String[] args)
  {
    System.setProperty("DOWNLOADS_HOME", DOWNLOADS_HOME.getAbsolutePath());
    System.setProperty("ARCHIVE_HOME", ARCHIVE_HOME.getAbsolutePath());
    System.setProperty("workingArea", WORKING_AREA.getAbsolutePath());
    System.setProperty("GIT_EXECUTABLE", GIT_EXECUTABLE.getAbsolutePath());
    System.setProperty("JOBS_URL", "https://ci.eclipse.org/cdo/job");

    initDir(DOWNLOADS_HOME);
    initDir(ARCHIVE_HOME);
    initDir(WORKING_AREA);
    initDir(PromoterConfig.INSTANCE.getCompositionTempArea());

    PromoterTest test = new PromoterTest();
    test.run();
  }

  private static void initDir(File dir)
  {
    dir.mkdirs();
  }
}
