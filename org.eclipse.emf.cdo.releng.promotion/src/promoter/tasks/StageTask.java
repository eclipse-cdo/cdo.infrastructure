/*
 * Copyright (c) 2004 - 2011 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package promoter.tasks;

import promoter.util.IO;

import java.io.File;
import java.util.List;

/**
 * @author Eike Stepper
 */
public class StageTask extends AbstractDropTask
{
  public StageTask()
  {
  }

  @Override
  protected boolean execute(File drop, List<String> args)
  {
    String train = args.remove(0);
    System.out.println("   Train = " + train);

    String old = args.remove(0);
    System.out.println("   Old = " + old);

    File file = new File(drop, ".staged");
    IO.emptyFile(file);

    if (old != null && old.length() != 0)
    {
      File oldDrop = getDrop(old);
      File oldFile = new File(oldDrop, ".staged");
      if (oldFile.isFile())
      {
        IO.delete(oldFile);
      }
    }

    return true; // Order recomposition
  }
}
