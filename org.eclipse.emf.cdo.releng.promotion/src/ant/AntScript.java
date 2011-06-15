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
package ant;

import util.XMLOutput;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Eike Stepper
 */
public class AntScript
{
  private final List<AntElement> elements = new ArrayList<AntElement>();

  public AntScript()
  {
  }

  public final List<AntElement> getElements()
  {
    return elements;
  }

  public void generate(File file)
  {
    OutputStream stream = null;

    try
    {
      stream = new FileOutputStream(file);
      XMLOutput out = new XMLOutput(stream);

      for (AntElement element : elements)
      {
        element.generateInit(out);
      }

      for (AntElement element : elements)
      {
        element.generate(out);
      }

      for (AntElement element : elements)
      {
        element.generateDone(out);
      }
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
}
