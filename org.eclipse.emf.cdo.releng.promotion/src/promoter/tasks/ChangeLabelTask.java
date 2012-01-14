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
package promoter.tasks;

import promoter.util.IO;

import java.io.File;
import java.util.List;

/**
 * @author Eike Stepper
 */
public class ChangeLabelTask extends AbstractDropTask
{
  public ChangeLabelTask()
  {
  }

  @Override
  protected boolean execute(File drop, List<String> args)
  {
    File file = new File(drop, "web.properties");
    if (args.isEmpty())
    {
      System.out.println("   Label = <unlabelled>");
      IO.delete(file);
    }
    else
    {
      String label = args.remove(0);
      System.out.println("   Label = " + label);

      String content = "web.label=" + label;
      IO.writeFile(file, content.getBytes());
    }

    return true; // Order recomposition
  }
}
