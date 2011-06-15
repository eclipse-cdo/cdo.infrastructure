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

import org.xml.sax.SAXException;

import util.XMLOutput;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eike Stepper
 */
public class AntPhase extends AntElement
{
  private final List<AntElement> elements = new ArrayList<AntElement>();

  private final String comment;

  public AntPhase(String comment)
  {
    this.comment = comment;
  }

  public final List<AntElement> getElements()
  {
    return elements;
  }

  @Override
  public void generateInit(XMLOutput out) throws SAXException
  {
    out.comment(comment);
  }

  @Override
  public void generate(XMLOutput out) throws SAXException
  {
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
}
