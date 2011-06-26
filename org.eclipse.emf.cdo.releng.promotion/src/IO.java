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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;

/**
 * @author Eike Stepper
 */
public final class IO
{
  public static final int EOF = -1;

  public static final int DEFAULT_BUFFER_SIZE = 8192;

  private IO()
  {
  }

  public static boolean isExcluded(String name)
  {
    if (".svn".equalsIgnoreCase(name))
    {
      return true;
    }

    if ("cvs".equalsIgnoreCase(name))
    {
      return true;
    }

    if (".git".equalsIgnoreCase(name))
    {
      return true;
    }

    if (".hg".equalsIgnoreCase(name))
    {
      return true;
    }

    if (".bzr".equalsIgnoreCase(name))
    {
      return true;
    }

    if ("SCCS".equalsIgnoreCase(name))
    {
      return true;
    }

    return false;
  }

  public static FileInputStream openInputStream(String fileName)
  {
    return openInputStream(new File(fileName));
  }

  public static FileInputStream openInputStream(File file)
  {
    try
    {
      return new FileInputStream(file);
    }
    catch (IOException ex)
    {
      throw new RuntimeException(ex);
    }
  }

  public static FileOutputStream openOutputStream(String fileName)
  {
    return openOutputStream(new File(fileName));
  }

  public static FileOutputStream openOutputStream(File file)
  {
    try
    {
      return new FileOutputStream(file);
    }
    catch (IOException ex)
    {
      throw new RuntimeException(ex);
    }
  }

  public static FileReader openReader(String fileName)
  {
    return openReader(new File(fileName));
  }

  public static FileReader openReader(File file)
  {
    try
    {
      return new FileReader(file);
    }
    catch (IOException ex)
    {
      throw new RuntimeException(ex);
    }
  }

  public static FileWriter openWriter(String fileName)
  {
    return openWriter(new File(fileName));
  }

  public static FileWriter openWriter(File file)
  {
    try
    {
      return new FileWriter(file);
    }
    catch (IOException ex)
    {
      throw new RuntimeException(ex);
    }
  }

  public static void close(Closeable closeable)
  {
    try
    {
      if (closeable != null)
      {
        closeable.close();
      }
    }
    catch (IOException ex)
    {
      throw new RuntimeException(ex);
    }
  }

  public static void mkdirs(File folder)
  {
    if (!folder.exists())
    {
      if (!folder.mkdirs())
      {
        throw new RuntimeException("Unable to create directory " + folder.getAbsolutePath()); //$NON-NLS-1$
      }
    }
  }

  public static int delete(File file)
  {
    if (file == null)
    {
      return 0;
    }

    int deleted = 0;
    if (file.isDirectory())
    {
      for (File child : file.listFiles())
      {
        deleted += delete(child);
      }
    }

    if (file.delete())
    {
      return deleted + 1;
    }

    file.deleteOnExit();
    return deleted;
  }

  public static void copyTree(File source, File target)
  {
    if (source.isDirectory())
    {
      mkdirs(target);
      File[] files = source.listFiles();
      for (File file : files)
      {
        String name = file.getName();
        copyTree(new File(source, name), new File(target, name));
      }
    }
    else
    {
      copyFile(source, target);
    }
  }

  public static void copyTrees(Collection<File> sources, File target)
  {
    for (File source : sources)
    {
      copyTree(source, target);
    }
  }

  public static long copyBinary(InputStream inputStream, OutputStream outputStream) throws IOException
  {
    if (!(inputStream instanceof BufferedInputStream) && !(inputStream instanceof ByteArrayInputStream))
    {
      inputStream = new BufferedInputStream(inputStream);
    }

    if (!(outputStream instanceof BufferedOutputStream) && !(outputStream instanceof ByteArrayOutputStream))
    {
      outputStream = new BufferedOutputStream(outputStream);
    }

    long size = 0;
    int b;
    while ((b = inputStream.read()) != EOF)
    {
      outputStream.write(b);
      ++size;
    }

    outputStream.flush();
    return size;
  }

  public static void copyBinary(InputStream inputStream, OutputStream outputStream, long size) throws IOException
  {
    if (!(inputStream instanceof BufferedInputStream) && !(inputStream instanceof ByteArrayInputStream))
    {
      inputStream = new BufferedInputStream(inputStream);
    }

    if (!(outputStream instanceof BufferedOutputStream) && !(outputStream instanceof ByteArrayOutputStream))
    {
      outputStream = new BufferedOutputStream(outputStream);
    }

    while (size > 0L)
    {
      int b = inputStream.read();
      if (b == EOF)
      {
        throw new EOFException();
      }

      outputStream.write(b);
      --size;
    }

    outputStream.flush();
  }

  public static long copyCharacter(Reader reader, Writer writer) throws IOException
  {
    if (!(reader instanceof BufferedReader) && !(reader instanceof CharArrayReader))
    {
      reader = new BufferedReader(reader);
    }

    if (!(writer instanceof BufferedWriter) && !(writer instanceof CharArrayWriter))
    {
      writer = new BufferedWriter(writer);
    }

    long size = 0;
    int c;
    while ((c = reader.read()) != EOF)
    {
      writer.write(c);
      ++size;
    }

    writer.flush();
    return size;
  }

  public static void copyCharacter(Reader reader, Writer writer, long size) throws IOException
  {
    if (!(reader instanceof BufferedReader) && !(reader instanceof CharArrayReader))
    {
      reader = new BufferedReader(reader);
    }

    if (!(writer instanceof BufferedWriter) && !(writer instanceof CharArrayWriter))
    {
      writer = new BufferedWriter(writer);
    }

    while (size > 0L)
    {
      int c = reader.read();
      if (c == EOF)
      {
        throw new EOFException();
      }

      writer.write(c);
      --size;
    }

    writer.flush();
  }

  public static int copy(InputStream input, OutputStream output, int size, byte buffer[])
  {
    try
    {
      int written = 0;
      int bufferSize = buffer.length;
      int n = Math.min(size, bufferSize);
      while (n > 0 && (n = input.read(buffer, 0, n)) != -1)
      {
        output.write(buffer, 0, n);
        written += n;
        size -= n;
        n = Math.min(size, bufferSize);
      }

      return written;
    }
    catch (IOException ex)
    {
      throw new RuntimeException(ex);
    }
  }

  public static void copy(InputStream input, OutputStream output, byte buffer[])
  {
    try
    {
      int n;
      while ((n = input.read(buffer)) != -1)
      {
        output.write(buffer, 0, n);
      }
    }
    catch (IOException ex)
    {
      throw new RuntimeException(ex);
    }
  }

  public static void copy(InputStream input, OutputStream output, int bufferSize)
  {
    copy(input, output, new byte[bufferSize]);
  }

  public static void copy(InputStream input, OutputStream output)
  {
    copy(input, output, DEFAULT_BUFFER_SIZE);
  }

  public static void copyFile(File source, File target)
  {
    mkdirs(target.getParentFile());
    FileInputStream input = null;
    FileOutputStream output = null;

    try
    {
      input = openInputStream(source);
      output = openOutputStream(target);
      copy(input, output);
    }
    finally
    {
      try
      {
        close(input);
      }
      finally
      {
        close(output);
      }
    }
  }

  public static String readTextFile(File file)
  {
    Reader input = openReader(file);

    try
    {
      CharArrayWriter output = new CharArrayWriter();
      copyCharacter(input, output);
      return output.toString();
    }
    catch (IOException ex)
    {
      throw new RuntimeException(ex);
    }
    finally
    {
      close(input);
    }
  }

  public static byte[] readFile(File file)
  {
    if (file.length() > Integer.MAX_VALUE)
    {
      throw new IllegalArgumentException("File too long: " + file.length()); //$NON-NLS-1$
    }

    int size = (int)file.length();
    FileInputStream input = openInputStream(file);

    try
    {
      ByteArrayOutputStream output = new ByteArrayOutputStream(size);
      copy(input, output);
      return output.toByteArray();
    }
    finally
    {
      close(input);
    }
  }

  public static void writeFile(File file, byte[] bytes)
  {
    FileOutputStream output = openOutputStream(file);

    try
    {
      ByteArrayInputStream input = new ByteArrayInputStream(bytes);
      copy(input, output);
    }
    finally
    {
      close(output);
    }
  }

  public static void writeFile(File file, OutputHandler handler)
  {
    FileOutputStream output = openOutputStream(file);

    try
    {
      handler.handleOutput(output);
    }
    catch (IOException ex)
    {
      throw new RuntimeException(ex);
    }
    finally
    {
      close(output);
    }
  }

  public static boolean equals(InputStream stream1, InputStream stream2)
  {
    try
    {
      for (;;)
      {
        int byte1 = stream1.read();
        int byte2 = stream2.read();

        if (byte1 != byte2)
        {
          return false;
        }

        if (byte1 == -1)// Implies byte2 == -1
        {
          return true;
        }
      }
    }
    catch (IOException ex)
    {
      throw new RuntimeException(ex);
    }
  }

  /**
   * @author Eike Stepper
   */
  public interface OutputHandler
  {
    public static final OutputHandler EMPTY = new OutputHandler()
    {
      public void handleOutput(OutputStream out) throws IOException
      {
        // Do nothing
      }
    };

    public void handleOutput(OutputStream out) throws IOException;
  }
}
