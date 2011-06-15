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
package downloads;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Eike Stepper
 */
public abstract class Area<CHILD>
{
  private final File directory;

  private final List<CHILD> children = new ArrayList<CHILD>();

  public Area(File directory, boolean createOnDemand)
  {
    if (!directory.exists())
    {
      if (!createOnDemand)
      {
        throw new IllegalStateException(directory + " does not exist");
      }

      directory.mkdirs();
    }

    this.directory = directory;
    for (File resource : directory.listFiles())
    {
      CHILD child = createChild(resource);
      if (child != null)
      {
        children.add(child);
      }
    }

    validate();
  }

  public final File getDirectory()
  {
    return directory;
  }

  public final List<CHILD> getChildren()
  {
    return children;
  }

  protected abstract CHILD createChild(File resource);

  protected void validate()
  {
    // Sub classes may override.
  }
}
