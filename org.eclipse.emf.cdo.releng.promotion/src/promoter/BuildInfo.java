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
  private final Location location;

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

  private BuildInfo(Location location)
  {
    this.location = location;
  }

  public Location getLocation()
  {
    return location;
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

  public File getDrop()
  {
    File area = location == Location.ARCHIVE ? PromoterConfig.INSTANCE.getArchiveDropsArea() : PromoterConfig.INSTANCE.getDropsArea();
    return new File(area, qualifier);
  }

  public String getDropURL(String path, boolean mirror)
  {
    String file = "/" + PromoterConfig.INSTANCE.getDownloadsPath() + "/drops/" + qualifier;
    if (path != null)
    {
      file += "/" + path;
    }

    if (location == Location.ARCHIVE)
    {
      return PromoterConfig.INSTANCE.getArchiveURL() + file;
    }

    if (mirror)
    {
      return "https://www.eclipse.org/downloads/download.php?file=" + file + "&amp;protocol=http";
    }

    return PromoterConfig.INSTANCE.getDownloadsURL() + file;
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
    return "BuildInfo [location=" + location + ", branch=" + branch + ", hudson=" + hudson + ", job=" + job + ", number=" + number + ", qualifier=" + qualifier
        + ", revision=" + revision + ", relnotes=" + relnotesRevision + ", stream=" + stream + ", timestamp=" + timestamp + ", trigger=" + trigger + ", type="
        + type + "]";
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
    Location location = null;
    if (file.getAbsolutePath().startsWith(PromoterConfig.INSTANCE.getDownloadsArea().getAbsolutePath()))
    {
      location = Location.DOWNLOADS;
    }
    else if (file.getAbsolutePath().startsWith(PromoterConfig.INSTANCE.getArchiveArea().getAbsolutePath()))
    {
      location = Location.ARCHIVE;
    }

    final BuildInfo result = new BuildInfo(location);
    XML.parseXML(file, new DefaultHandler()
    {
      @Override
      public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
      {
        if ("build".equals(qName))
        {
          String type = attributes.getValue("type");
          String timestamp = attributes.getValue("timestamp");
          String qualifier = attributes.getValue("qualifier");
          if (qualifier == null)
          {
            qualifier = type + timestamp;
          }

          result.setBranch(attributes.getValue("branch"));
          result.setHudson(attributes.getValue("hudson"));
          result.setJob(attributes.getValue("job"));
          result.setNumber(attributes.getValue("number"));
          result.setQualifier(qualifier);
          result.setRevision(attributes.getValue("revision"));
          result.setRelnotesRevision(attributes.getValue("relnotes"));
          result.setStream(attributes.getValue("stream"));
          result.setTimestamp(timestamp);
          result.setTrigger(attributes.getValue("trigger"));
          result.setType(type);
        }
      }
    });

    return result;
  }

  public static BuildInfo read(URL url)
  {
    final BuildInfo result = new BuildInfo(Location.HUDSON);
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

  /**
   * @author Eike Stepper
   */
  public enum Location
  {
    HUDSON, DOWNLOADS, ARCHIVE
  }
}
