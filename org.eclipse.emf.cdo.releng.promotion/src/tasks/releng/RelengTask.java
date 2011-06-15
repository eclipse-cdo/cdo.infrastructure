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
package tasks.releng;

import tasks.Task;
import tasks.TaskProvider;

/**
 * @author Eike Stepper
 */
public class RelengTask extends Task
{
  public RelengTask(TaskProvider provider, int priority)
  {
    super(provider, priority);
  }

  public RelengTask(TaskProvider provider)
  {
    super(provider);
  }

  public void run()
  {
  }

  @Override
  public void cleanup()
  {
  }
}
