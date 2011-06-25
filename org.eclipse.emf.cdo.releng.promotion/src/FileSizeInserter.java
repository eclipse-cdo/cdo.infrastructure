import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

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

/**
 * @author Eike Stepper
 */
public class FileSizeInserter
{
  private static final String START = "<!-- PROMOTER-FILESIZE";

  private static final String END = "-->";

  public static void main(String[] args) throws Exception
  {
    replaceInFile(args[0]);
  }

  public static void replaceInFile(String filepath) throws FileNotFoundException, IOException
  {
    File file = new File(filepath);
    File temp = new File(filepath + ".tmp");

    InputStream in = null;
    PrintStream out = null;

    try
    {
      in = new FileInputStream(file);
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));

      out = new PrintStream(temp);

      String line;
      while ((line = reader.readLine()) != null)
      {
        for (;;)
        {
          int start = line.indexOf(START);
          if (start < 0)
          {
            break;
          }

          int end = line.indexOf(END, start);
          String path = line.substring(start + START.length(), end).trim();
          String size = formatSize(new File(path).length());
          line = line.substring(0, start) + size + line.substring(end + END.length());
        }

        out.println(line);
      }
    }
    finally
    {
      try
      {
        IO.close(out);
      }
      finally
      {
        IO.close(in);
      }
    }

    file.delete();
    temp.renameTo(file);
  }

  private static String formatSize(long length)
  {
    return null;
  }
}
