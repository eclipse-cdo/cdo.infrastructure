/*
 * Copyright (c) 2004 - 2012 Eike Stepper (Loehne, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package promoter.util;

/**
 * @author Eike Stepper
 */
public final class Util
{
  private Util()
  {
  }

  public static String rstrip(String line, String needle)
  {
    if (line.endsWith(needle))
    {
      line = line.substring(0, line.length() - needle.length());
    }

    return line;
  }
}
