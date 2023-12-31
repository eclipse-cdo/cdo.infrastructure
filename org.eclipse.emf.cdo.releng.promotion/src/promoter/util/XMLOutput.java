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

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.util.LinkedList;

/**
 * @author Eike Stepper
 */
public class XMLOutput
{
  private static final AttributesImpl NO_ATTRIBUTES = new AttributesImpl();

  private final OutputStream out;

  private TransformerHandler xmlHandler;

  private char[] newLine;

  private char[] indentation;

  private LinkedList<Element> stack = new LinkedList<>();

  private Element element;

  public XMLOutput(OutputStream out) throws TransformerConfigurationException, SAXException
  {
    this.out = out;
    setNewLine("\n");
    setIndentation("  ");
    SAXTransformerFactory factory = (SAXTransformerFactory)TransformerFactory.newInstance();

    xmlHandler = factory.newTransformerHandler();

    Transformer transformer = xmlHandler.getTransformer();
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

    xmlHandler.setResult(new StreamResult(out));
    xmlHandler.startDocument();
  }

  public void setNewLine(String newLine)
  {
    this.newLine = newLine.toCharArray();
  }

  public void setIndentation(String indentation)
  {
    this.indentation = indentation.toCharArray();
  }

  public XMLOutput processingInstruction(String name, String data) throws SAXException
  {
    flush();
    xmlHandler.processingInstruction(name, data);
    return this;
  }

  public XMLOutput comment(String str) throws SAXException
  {
    flush();
    xmlHandler.comment(str.toCharArray(), 0, str.length());
    return this;
  }

  public XMLOutput element(String name) throws SAXException
  {
    flush();
    element = new Element(name);
    return this;
  }

  public XMLOutput attribute(String name, File file) throws SAXException
  {
    return attribute(name, file.getAbsolutePath());
  }

  public XMLOutput attribute(String name, Object value) throws SAXException
  {
    if (value != null)
    {
      return attributeOrNull(name, value);
    }

    return this;
  }

  public XMLOutput attributeOrNull(String name, Object value) throws SAXException
  {
    checkElement();
    element.addAttribute(name, value);
    return this;
  }

  public Writer characters() throws SAXException
  {
    checkElement();
    newLine();
    element.start();
    xmlHandler.startCDATA();

    return new Writer()
    {
      @Override
      public void write(char[] cbuf, int off, int len) throws IOException
      {
        try
        {
          xmlHandler.characters(cbuf, off, len);
        }
        catch (SAXException ex)
        {
          throw new RuntimeException(ex);
        }
      }

      @Override
      public void flush() throws IOException
      {
        // Do nothing
      }

      @Override
      public void close() throws IOException
      {
        try
        {
          xmlHandler.endCDATA();
          element.end();
        }
        catch (SAXException ex)
        {
          throw new RuntimeException(ex);
        }
        finally
        {
          element = null;
        }
      }
    };
  }

  public XMLOutput push() throws SAXException
  {
    newLine();
    element.start();

    stack.add(element);
    element = null;
    return this;
  }

  public XMLOutput pop() throws SAXException
  {
    flush();
    Element element = stack.removeLast();

    if (element.hasChildren())
    {
      newLine();
    }

    element.end();
    return this;
  }

  public void done() throws SAXException
  {
    while (!stack.isEmpty())
    {
      pop();
    }

    xmlHandler.endDocument();
    PrintStream stream = new PrintStream(out);
    stream.println();
    stream.flush();
  }

  private void flush() throws SAXException
  {
    if (element != null)
    {
      newLine();
      element.start();
      element.end();
      element = null;
    }
  }

  private void newLine() throws SAXException
  {
    xmlHandler.ignorableWhitespace(newLine, 0, newLine.length);
    for (int i = 0; i < stack.size(); i++)
    {
      xmlHandler.ignorableWhitespace(indentation, 0, indentation.length);
    }
  }

  private void checkElement()
  {
    if (element == null)
    {
      throw new IllegalStateException("No element");
    }
  }

  /**
   * @author Eike Stepper
   */
  private final class Element
  {
    private String name;

    private AttributesImpl attributes;

    private boolean children;

    public Element(String name)
    {
      this.name = name;
    }

    public boolean hasChildren()
    {
      return children;
    }

    public void addChild()
    {
      children = true;
    }

    public void addAttribute(String name, Object value)
    {
      if (attributes == null)
      {
        attributes = new AttributesImpl();
      }

      if (value == null)
      {
        value = "";
      }

      attributes.addAttribute("", "", name, "", value.toString());
    }

    public void start() throws SAXException
    {
      if (!stack.isEmpty())
      {
        stack.getLast().addChild();
      }

      xmlHandler.startElement("", "", name, attributes == null ? NO_ATTRIBUTES : attributes);
    }

    public void end() throws SAXException
    {
      xmlHandler.endElement("", "", name);
    }
  }
}
