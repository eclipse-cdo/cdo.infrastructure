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

import javax.xml.transform.TransformerConfigurationException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import promoter.PromoterConfig;

/**
 * @author Eike Stepper
 */
public abstract class Ant<RESULT>
{
  private File script;

  private File basedir;

  public Ant(File script, File basedir)
  {
    this.script = script;
    this.basedir = basedir;
  }

  public final File getScript()
  {
    return script;
  }

  public final File getBasedir()
  {
    return basedir;
  }

  public final RESULT run()
  {
    RESULT result = null;
    OutputStream out = null;

    try
    {
      out = new FileOutputStream(script);

      XMLOutput xml = createXMLOutput(out);
      init(xml);
      result = create(xml);
      done(xml);
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
    finally
    {
      IO.close(out);
    }

    executeAntScript();
    return result;
  }

  protected XMLOutput createXMLOutput(OutputStream out) throws TransformerConfigurationException, SAXException
  {
    return new XMLOutput(out)
    {
      @Override
      public XMLOutput attribute(String name, File file) throws SAXException
      {
        String path = convertFileAttribute(file);
        return super.attribute(name, path);
      }
    };
  }

  protected String convertFileAttribute(File file)
  {
    String basepath = basedir.getAbsolutePath();
    String path = file.getAbsolutePath();
    if (path.startsWith(basepath))
    {
      path = path.substring(basepath.length() + 1);
    }

    return path;
  }

  protected void executeAntScript()
  {
    File anthome = PromoterConfig.INSTANCE.getAntHome();

    String ant = anthome + "/bin/ant";
    if (System.getProperty("os.name", "").startsWith("Windows"))
    {
      ant += ".bat";
    }

    String path = getScript().getAbsolutePath();

    System.out.println();
    System.out.println("Executing Ant script " + path);

    IO.executeProcess(ant, "-f", path);
  }

  protected void init(XMLOutput xml) throws SAXException
  {
    xml.element("project");
    xml.attribute("name", "promoter");
    xml.attribute("default", "main");
    xml.attribute("basedir", basedir.getAbsolutePath()); // Do not convert attribute
    xml.push();

    xml.element("target");
    xml.attribute("name", "main");
    xml.push();
  }

  protected void done(XMLOutput xml) throws SAXException
  {
    xml.pop();
    xml.pop();
    xml.done();
  }

  protected abstract RESULT create(XMLOutput xml) throws Exception;
}
