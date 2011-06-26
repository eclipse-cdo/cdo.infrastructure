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
package promoter.util;

import promoter.util.IO.OutputHandler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author Eike Stepper
 */
public class SVN extends SCM
{
  public static final String SVN_ROOT = "https://dev.eclipse.org/svnroot/modeling/org.eclipse.emf.cdo/";

  public SVN()
  {
  }

  @Override
  public void setTag(final String branch, final String tag)
  {
    IO.executeProcess("/bin/bash", new OutputHandler()
    {
      public void handleOutput(OutputStream out) throws IOException
      {
        String message = "Tagging " + branch + " as " + tag;
        System.out.println(message);

        String from = SVN_ROOT + branch;
        String to = SVN_ROOT + "tags/drops/" + tag;

        PrintStream stream = new PrintStream(out);
        stream.println("svn cp -m \"" + message + "\" \"" + from + "\" \"" + to + "\"");
        stream.flush();
      }
    });
  }

  @Override
  public void commit(String comment, File... checkouts)
  {
    throw new UnsupportedOperationException();
  }
}
