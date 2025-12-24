/*
 * Copyright (c) 2004-2013 Eike Stepper (Loehne, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package promoter;

import java.io.File;

import promoter.util.IO;

/**
 * @author Eike Stepper
 */
public final class TPMacroSetup
{
  public static final String FILE_NAME = "tp-macro.setup";

  private static final String FIXED_TOKEN = "https://download.eclipse.org/modeling/emf/cdo/updates";

  private TPMacroSetup()
  {
  }

  private static File getTPMacroFile(BuildInfo buildInfo)
  {
    return new File(buildInfo.getDrop(), FILE_NAME);
  }

  public static File insertDropRepository(BuildInfo buildInfo)
  {
    File tpMacro = getTPMacroFile(buildInfo);
    if (tpMacro.isFile())
    {
      String value = buildInfo.getDropURL(null, false);

      String oldXML = IO.readTextFile(tpMacro);
      String newXML = oldXML.replace( //
          "<repository url=\"" + FIXED_TOKEN + "\"/>", //
          "<repository url=\"" + value + "\"/>");

      if (oldXML.equals(newXML))
      {
        IO.writeTextFile(tpMacro, newXML);
      }

      return tpMacro;
    }

    return null;
  }

  public static File copyToLatestRepository(BuildInfo latestDrop, Repository latestRepository)
  {
    File tpMacro = getTPMacroFile(latestDrop);
    if (tpMacro.isFile())
    {
      File target = new File(latestRepository.getFolder(), FILE_NAME);
      IO.copyFile(tpMacro, target);

      return target;
    }

    return null;
  }
}
