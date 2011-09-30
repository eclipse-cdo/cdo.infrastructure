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
public class HideTask extends AbstractDropTask
{
  public HideTask()
  {
  }

  @Override
  protected boolean execute(File drop, List<String> args)
  {
    IO.delete(drop);

    return true; // Order recomposition
  }
}
