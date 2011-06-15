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

/**
 * @author Eike Stepper
 */
public abstract class AntElement
{
  public void generateInit(XMLOutput out) throws SAXException
  {
  }

  public void generateDone(XMLOutput out) throws SAXException
  {
  }

  public abstract void generate(XMLOutput out) throws SAXException;
}
