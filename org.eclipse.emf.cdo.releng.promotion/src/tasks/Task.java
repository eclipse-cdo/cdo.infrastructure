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

/**
 * @author Eike Stepper
 */
public abstract class Task implements Runnable, Comparable<Task>
{
  public static final int DEFAULT_PRIORITY = 500;

  private final TaskProvider provider;

  private final int priority;

  public Task(TaskProvider provider, int priority)
  {
    this.provider = provider;
    this.priority = priority;
  }

  public Task(TaskProvider provider)
  {
    this(provider, DEFAULT_PRIORITY);
  }

  public final TaskProvider getProvider()
  {
    return provider;
  }

  public final int getPriority()
  {
    return priority;
  }

  public final int compareTo(Task o)
  {
    int result = provider.compareTo(o.getProvider());
    if (result == 0)
    {
      result = new Integer(o.getPriority()).compareTo(priority);
    }

    return result;
  }

  public abstract void cleanup();
}
