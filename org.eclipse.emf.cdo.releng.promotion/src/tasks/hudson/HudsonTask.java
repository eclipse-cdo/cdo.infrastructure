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
package tasks.hudson;

import tasks.Task;
import tasks.TaskProvider;

/**
 * @author Eike Stepper
 */
public class HudsonTask extends Task
{
  public static final int PRIORITY = 1000;

  private HudsonTask(TaskProvider provider)
  {
    super(provider, PRIORITY);
  }

  public void run()
  {
    // TODO: implement HudsonTask.run()
    throw new UnsupportedOperationException();
  }

  @Override
  public void cleanup()
  {
    // TODO: implement HudsonTask.enclosing_method(enclosing_method_arguments)
    throw new UnsupportedOperationException();
  }

  /**
   * @author Eike Stepper
   */
  public static class Provider extends TaskProvider
  {
    public Provider(int phase)
    {
      super(phase);
    }

    @Override
    public Task provideTask()
    {
      return new HudsonTask(this);
    }
  }
}
