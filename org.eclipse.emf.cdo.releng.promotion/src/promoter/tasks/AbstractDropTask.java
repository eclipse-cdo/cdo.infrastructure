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

import promoter.BuildInfo;
import promoter.Task;

import java.io.File;
import java.util.List;

/**
 * @author Eike Stepper
 */
public abstract class AbstractDropTask extends Task
{
  public AbstractDropTask()
  {
  }

  @Override
  protected final boolean execute(List<String> args, List<BuildInfo> builds) throws Exception
  {
    String qualifier = args.remove(0);
    System.out.println("   Drop = " + qualifier);

    File drop = getDrop(qualifier);
    return execute(drop, args, builds);
  }

  protected abstract boolean execute(File drop, List<String> args, List<BuildInfo> builds);
}
