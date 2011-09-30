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

import promoter.PromoterConfig;
import promoter.Task;
import promoter.util.IO;

import java.io.File;

/**
 * @author Eike Stepper
 */
public class ShowTask extends Task
{
  public ShowTask()
  {
  }

  @Override
  protected boolean execute(String[] args) throws Exception
  {
    String qualifier = args[1];

    File drop = new File(PromoterConfig.INSTANCE.getDropsArea(), qualifier);
    File file = new File(drop, ".invisible");
    IO.delete(file);

    return true; // Order recomposition
  }
}
