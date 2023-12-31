/*
 * Copyright (c) 2004 - 2012 Eike Stepper (Loehne, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package promoter;

import java.io.File;
import java.util.List;

/**
 * @author Eike Stepper
 */
public abstract class Task extends PromoterComponent
{
  private int id;

  public Task()
  {
  }

  public final int getID()
  {
    return id;
  }

  final void setID(int id)
  {
    this.id = id;
  }

  protected abstract boolean execute(List<String> args, List<BuildInfo> builds) throws Exception;

  public static File getDrop(String qualifier)
  {
    return new File(PromoterConfig.INSTANCE.getDropsArea(), qualifier);
  }
}
