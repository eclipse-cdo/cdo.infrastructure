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

import java.io.File;

/**
 * @author Eike Stepper
 */
public final class BuildInfo
{
  private String hudson;

  private String job;

  private String number;

  private String qualifier;

  private String revision;

  private String stream;

  private String timestamp;

  private String trigger;

  private String type;

  public BuildInfo()
  {
  }

  public String getHudson()
  {
    return hudson;
  }

  public String getJob()
  {
    return job;
  }

  public String getNumber()
  {
    return number;
  }

  public String getQualifier()
  {
    return qualifier;
  }

  public String getRevision()
  {
    return revision;
  }

  public String getStream()
  {
    return stream;
  }

  public String getTimestamp()
  {
    return timestamp;
  }

  public String getTrigger()
  {
    return trigger;
  }

  public String getType()
  {
    return type;
  }

  public String substitute(String pattern)
  {
    pattern = pattern.replaceAll("\\$\\{hudson}", hudson);
    pattern = pattern.replaceAll("\\$\\{job}", job);
    pattern = pattern.replaceAll("\\$\\{number}", number);
    pattern = pattern.replaceAll("\\$\\{qualifier}", qualifier);
    pattern = pattern.replaceAll("\\$\\{revision}", revision);
    pattern = pattern.replaceAll("\\$\\{hudson}", hudson);
    pattern = pattern.replaceAll("\\$\\{stream}", stream);
    pattern = pattern.replaceAll("\\$\\{timestamp}", timestamp);
    pattern = pattern.replaceAll("\\$\\{trigger}", trigger);
    pattern = pattern.replaceAll("\\$\\{type}", type);
    return pattern;
  }

  void setHudson(String hudson)
  {
    this.hudson = hudson;
  }

  void setJob(String job)
  {
    this.job = job;
  }

  void setNumber(String number)
  {
    this.number = number;
  }

  void setQualifier(String qualifier)
  {
    this.qualifier = qualifier;
  }

  void setRevision(String revision)
  {
    this.revision = revision;
  }

  void setStream(String stream)
  {
    this.stream = stream;
  }

  void setTimestamp(String timestamp)
  {
    this.timestamp = timestamp;
  }

  void setTrigger(String trigger)
  {
    this.trigger = trigger;
  }

  void setType(String type)
  {
    this.type = type;
  }

  public static BuildInfo read(File file)
  {
    final BuildInfo result = new BuildInfo();
    XML.parseXML(file, new DefaultHandler()
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
}
