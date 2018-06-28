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
package promoter;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.net.URL;

import promoter.util.XML;

/**
 * @author Eike Stepper
 */
public final class BuildInfo implements Comparable<BuildInfo>
{
  private String branch;

  private String hudson;

  private String job;

  private String number;

  private String qualifier;

  private String revision;

  private String relnotesRevision;

  private String stream;

  private String timestamp;

  private String trigger;

  private String type;

  public BuildInfo()
  {
  }

  public String getBranch()
  {
    return branch;
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

  public String getRelnotesRevision()
  {
    if (relnotesRevision != null)
    {
      return relnotesRevision;
    }

    return getRevision();
  }

  public String getStream()
  {
    return stream;
  }

  public int getStreamMajor()
  {
    String[] segments = stream.split("\\.");
    return Integer.parseInt(segments[0]);
  }

  public int getStreamMinor()
  {
    String[] segments = stream.split("\\.");
    return Integer.parseInt(segments[1]);
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

  public String getDownloadsURL(String... paths)
  {
    StringBuilder builder = new StringBuilder();
    for (String p : paths)
    {
      builder.append("/");
      builder.append(p);
    }

    return PromoterConfig.INSTANCE.formatUpdateURL(builder.toString());
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

  @Override
  public String toString()
  {
    return "BuildInfo [branch=" + branch + ", hudson=" + hudson + ", job=" + job + ", number=" + number + ", qualifier=" + qualifier + ", revision=" + revision
        + ", relnotes=" + relnotesRevision + ", stream=" + stream + ", timestamp=" + timestamp + ", trigger=" + trigger + ", type=" + type + "]";
  }

  public int compareTo(BuildInfo o)
  {
    return o.getTimestamp().compareTo(timestamp);
  }

  public boolean isLaterThan(BuildInfo o)
  {
    if (o == null)
    {
      return true;
    }

    if (getStreamMajor() > o.getStreamMajor())
    {
      return true;
    }

    if (getStreamMinor() > o.getStreamMinor())
    {
      return true;
    }

    if (compareTo(o) < 0)
    {
      return true;
    }

    return false;
  }

  void setBranch(String branch)
  {
    this.branch = branch;
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

  void setRelnotesRevision(String relnotesRevision)
  {
    this.relnotesRevision = relnotesRevision;
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
          result.setBranch(attributes.getValue("branch"));
          result.setHudson(attributes.getValue("hudson"));
          result.setJob(attributes.getValue("job"));
          result.setNumber(attributes.getValue("number"));
          result.setQualifier(attributes.getValue("qualifier"));
          result.setRevision(attributes.getValue("revision"));
          result.setRelnotesRevision(attributes.getValue("relnotes"));
          result.setStream(attributes.getValue("stream"));
          result.setTimestamp(attributes.getValue("timestamp"));
          result.setTrigger(attributes.getValue("trigger"));
          result.setType(attributes.getValue("type"));
        }
      }
    });

    return result;
  }

  public static BuildInfo read(URL url)
  {
    final BuildInfo result = new BuildInfo();
    XML.parseXML(url, new DefaultHandler()
    {
      @Override
      public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
      {
        if ("build".equals(qName))
        {
          result.setBranch(attributes.getValue("branch"));
          result.setHudson(attributes.getValue("hudson"));
          result.setJob(attributes.getValue("job"));
          result.setNumber(attributes.getValue("number"));
          result.setQualifier(attributes.getValue("qualifier"));
          result.setRevision(attributes.getValue("revision"));
          result.setRelnotesRevision(attributes.getValue("relnotes"));
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
