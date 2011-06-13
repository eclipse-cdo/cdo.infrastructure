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


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Eike Stepper
 */
public final class XML
{
  private static SAXParserFactory parserFactory;

  private XML()
  {
  }

  public static BuildInfo getBuildInfo(File file)
  {
    final BuildInfo result = new BuildInfo();
    parseXML(file, new DefaultHandler()
    {
      @Override
      public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
      {
        if ("build".equals(qName))
        {
          result.setHudson(attributes.getValue("hudson"));
          result.setJob(attributes.getValue("job"));
          result.setNumber(attributes.getValue("number"));
          result.setQualifier(attributes.getValue("qualifier"));
          result.setRevision(attributes.getValue("revision"));
          result.setStream(attributes.getValue("stream"));
          result.setTimestamp(attributes.getValue("timestamp"));
          result.setTrigger(attributes.getValue("trigger"));
          result.setType(attributes.getValue("type"));
        }
      }
    });

    return result;
  }

  private static void parseXML(File file, DefaultHandler handler)
  {
    InputStream in = null;

    try
    {
      in = new FileInputStream(file);

      if (parserFactory == null)
      {
        parserFactory = SAXParserFactory.newInstance();
      }

      SAXParser parser = parserFactory.newSAXParser();
      parser.parse(in, handler);
    }
    catch (Exception ex)
    {
      throw wrapException(ex);
    }
    finally
    {
      if (in != null)
      {
        try
        {
          in.close();
        }
        catch (IOException ex)
        {
          throw wrapException(ex);
        }
      }
    }
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
