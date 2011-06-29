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
package promoter;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import promoter.util.IO;
import promoter.util.IO.OutputHandler;
import promoter.util.XML;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author Eike Stepper
 */
public class Subversion extends SourceCodeManager
{
  // TODO Make SVN_ROOT configurable
  public static final String SVN_ROOT = "https://dev.eclipse.org/svnroot/modeling/org.eclipse.emf.cdo/";

  public Subversion()
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
  public void handleLogEntries(final String branch, final String fromRevision, final String toRevision,
      final boolean withPaths, final LogEntryHandler handler)
  {
    try
    {
      final File xmlFile = File.createTempFile("promotion-", ".tmp");

      IO.executeProcess("/bin/bash", new OutputHandler()
      {
        public void handleOutput(OutputStream out) throws IOException
        {
          String range = fromRevision + ":" + toRevision;
          System.out.println("Getting log entries for " + branch + " (" + range + ")");

          PrintStream stream = new PrintStream(out);
          stream.println("svn log " + (withPaths ? "-v " : "") + "--xml -r " + range + " \"" + SVN_ROOT + branch
              + "\" > " + xmlFile);
          stream.flush();
        }
      });

      try
      {
        XML.parseXML(xmlFile, new DefaultHandler()
        {
          private LogEntry logEntry;

          private StringBuilder builder;

          @Override
          public void startElement(String uri, String localName, String qName, Attributes attributes)
              throws SAXException
          {
            builder = new StringBuilder();
            if ("logentry".equalsIgnoreCase(qName))
            {
              String revision = attributes.getValue("revision");
              logEntry = new LogEntry(revision);
            }
          }

          @Override
          public void endElement(String uri, String localName, String qName) throws SAXException
          {
            if (logEntry != null)
            {
              if ("author".equalsIgnoreCase(qName))
              {
                logEntry.setAuthor(builder.toString());
              }
              else if ("date".equalsIgnoreCase(qName))
              {
                logEntry.setDate(builder.toString());
              }
              else if ("msg".equalsIgnoreCase(qName))
              {
                logEntry.setMessage(builder.toString());
              }
              else if ("path".equalsIgnoreCase(qName))
              {
                logEntry.getPaths().add(builder.toString());
              }
              else if ("logentry".equalsIgnoreCase(qName))
              {
                handler.handleLogEntry(logEntry);
                logEntry = null;
              }
            }
          }

          @Override
          public void characters(char[] ch, int start, int length) throws SAXException
          {
            builder.append(ch, start, length);
          }
        });

        xmlFile.delete();
      }
      catch (Exception ex)
      {
        System.err.println("Error in XML file " + xmlFile);
        throw ex;
      }
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void commit(String comment, File... checkouts)
  {
    throw new UnsupportedOperationException();
  }
}
