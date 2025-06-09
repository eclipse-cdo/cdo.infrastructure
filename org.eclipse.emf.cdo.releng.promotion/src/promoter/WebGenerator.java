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
import java.io.IOException;
import java.io.PrintStream;

import promoter.util.IO;
import promoter.util.IO.PrintHandler;

/**
 * @author Eike Stepper
 */
public class WebGenerator extends PromoterComponent
{
  public WebGenerator()
  {
  }

  public final void generateWeb(WebNode root)
  {
    System.out.println();
    printFile("index.html", out -> generateWeb(root, out));
  }

  protected void generateWeb(WebNode root, PrintStream out) throws IOException
  {
    root.generate(out, 0);
  }

  protected static void printFile(String fileName, PrintHandler handler)
  {
    File file = new File(PromoterConfig.INSTANCE.getCompositionTempArea(), fileName);
    IO.printFile(file, handler);
  }
}
