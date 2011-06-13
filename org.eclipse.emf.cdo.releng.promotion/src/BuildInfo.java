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

  private File build;

  private File drop;

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

  public File getBuild()
  {
    return build;
  }

  public File getDrop()
  {
    return drop;
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

  void setBuild(File build)
  {
    this.build = build;
  }

  void setDrop(File drop)
  {
    this.drop = drop;
  }
}
