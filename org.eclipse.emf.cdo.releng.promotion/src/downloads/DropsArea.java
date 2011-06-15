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

import util.Config;

import java.io.File;

/**
 * @author Eike Stepper
 */
public class DropsArea extends Area<Drop>
{
  private static final long serialVersionUID = 1L;

  public DropsArea()
  {
    super(Config.getProjectDownloadsArea(), true);
  }

  @Override
  protected Drop createChild(File resource)
  {
    if (resource.exists() && resource.isDirectory())
    {

      return null;
    }

    return null;
  }
}
