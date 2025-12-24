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

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author Eike Stepper
 */
public final class XML
{
  private static SAXParserFactory parserFactory;

  private XML()
  {
  }

  public static void parseXML(File file, DefaultHandler handler) throws IOException
  {
    InputStream in = null;

    try
    {
      in = new FileInputStream(file);
      parseXML(in, handler);
    }
    catch (ParserConfigurationException | SAXException ex)
    {
      throw wrapException(ex);
    }
    finally
    {
      IO.close(in);
    }
  }

  public static void parseXML(URL url, DefaultHandler handler) throws IOException
  {
    InputStream in = null;

    try
    {
      in = url.openStream();
      parseXML(in, handler);
    }
    catch (ParserConfigurationException | SAXException ex)
    {
      throw wrapException(ex);
    }
    finally
    {
      IO.close(in);
    }
  }

  public static void parseXML(InputStream in, DefaultHandler handler) throws IOException, ParserConfigurationException, SAXException
  {
    if (parserFactory == null)
    {
      parserFactory = SAXParserFactory.newInstance();
    }

    SAXParser parser = parserFactory.newSAXParser();
    parser.parse(in, handler);
  }

  private static RuntimeException wrapException(Exception exception)
  {
    if (exception instanceof RuntimeException)
    {
      return (RuntimeException)exception;
    }

    return new RuntimeException(exception);
  }
}
