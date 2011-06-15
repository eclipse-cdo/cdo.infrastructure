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
package util;

import java.io.PrintStream;

/**
 * @author Eike Stepper
 */
public class Log
{
  public static Log out = new Log(System.out);

  public static Log err = new Log(System.err);

  private final PrintStream stream;

  private final String indentation;

  private String prefix = "";

  public Log(PrintStream stream, String indentation)
  {
    this.stream = stream;
    this.indentation = indentation;
  }

  public Log(PrintStream stream)
  {
    this(stream, "   ");
  }

  public synchronized void println(Object msg)
  {
    if (prefix.length() != 0)
    {
      stream.print(prefix);
    }

    stream.println(msg);
  }

  public synchronized void indent()
  {
    prefix += indentation;
  }

  public synchronized void outdent()
  {
    int length = indentation.length();
    if (prefix.length() >= length)
    {
      prefix = prefix.substring(length);
    }
  }
}
