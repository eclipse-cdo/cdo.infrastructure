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
package tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Eike Stepper
 */
public class TaskManager implements Runnable
{
  private List<TaskProvider> providers = new ArrayList<TaskProvider>();

  public TaskManager()
  {
  }

  public synchronized void addTaskProvider(TaskProvider provider)
  {
    if (!providers.contains(provider))
    {
      providers.add(provider);
    }
  }

  public synchronized void removeTaskProvider(TaskProvider provider)
  {
    providers.remove(provider);
  }

  public synchronized void run()
  {

    for (;;)
    {
      List<Task> tasks = collectTasks();

      if (tasks.isEmpty())
      {
        break;
      }

      for (Task task : tasks)
      {
        try
        {
          task.run();
        }
        finally
        {
          task.cleanup();
        }
      }
    }
  }

  private List<Task> collectTasks()
  {
    List<Task> result = new ArrayList<Task>();

    Collections.sort(providers);
    for (TaskProvider provider : providers)
    {
      for (;;)
      {
        Task task = provider.provideTask();
        if (task == null)
        {
          break;
        }

        result.add(task);
      }
    }

    Collections.sort(result);
    return result;
  }
}
