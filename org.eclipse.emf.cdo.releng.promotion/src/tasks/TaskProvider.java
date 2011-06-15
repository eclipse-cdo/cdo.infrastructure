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
public abstract class TaskProvider implements Comparable<TaskProvider>
{
  private final int phase;

  public TaskProvider(int phase)
  {
    this.phase = phase;
  }

  public final int getPhase()
  {
    return phase;
  }

  public final int compareTo(TaskProvider o)
  {
    return new Integer(phase).compareTo(o.getPhase());
  }

  protected abstract Task provideTask();
}
