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
package promoter.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * @author Eike Stepper
 */
public class FileSizeInserter
{
  private static final String START = "<!-- FILESIZE";

  private static final String END = "-->";

  public static void main(String[] args) throws Exception
  {
    replaceInFile(args[0]);
  }

  public static void replaceInFile(String filepath) throws IOException
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
          String size = formatFileSize(path);
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

  public static String formatFileSize(String path)
  {
    File file = new File(path);
    if (file.isFile())
    {
      long size = file.length();
      return FileSizeInserter.formatFileSize(size);
    }

    return "";
  }

  public static String formatFileSize(long size)
  {
    long kb = 1024L;
    if (size < kb)
    {
      return Long.toString(size) + (size == 1 ? " Byte" : " Bytes");
    }

    long mb = kb * kb;
    if (size < mb)
    {
      return Long.toString(size / kb) + " KB";
    }

    long gb = mb * kb;
    if (size < gb)
    {
      return Long.toString(size / mb) + " MB";
    }

    return Long.toString(size / gb) + " GB";
  }
}
