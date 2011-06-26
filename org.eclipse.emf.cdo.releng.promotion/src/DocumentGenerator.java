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
import java.io.File;
import java.io.PrintStream;

/**
 * @author Eike Stepper
 */
public class DocumentGenerator
{
  public DocumentGenerator()
  {
  }

  public void generateDocument(WebNode webNode)
  {
    System.out.println();
    PrintStream out = null;

    try
    {
      out = new PrintStream(new File(PromoterConfig.INSTANCE.getCompositionTempArea(), "index.html"));
      webNode.generate(out, 0);
      out.flush();
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
    finally
    {
      IO.close(out);
    }
  }
}
