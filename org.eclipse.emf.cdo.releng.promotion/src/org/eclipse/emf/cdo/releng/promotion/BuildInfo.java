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
package org.eclipse.emf.cdo.releng.promotion;

/**
 * @author Eike Stepper
 */
public class BuildInfo
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

  public void setHudson(String hudson)
  {
    this.hudson = hudson;
  }

  public String getJob()
  {
    return job;
  }

  public void setJob(String job)
  {
    this.job = job;
  }

  public String getNumber()
  {
    return number;
  }

  public void setNumber(String number)
  {
    this.number = number;
  }

  public String getQualifier()
  {
    return qualifier;
  }

  public void setQualifier(String qualifier)
  {
    this.qualifier = qualifier;
  }

  public String getRevision()
  {
    return revision;
  }

  public void setRevision(String revision)
  {
    this.revision = revision;
  }

  public String getStream()
  {
    return stream;
  }

  public void setStream(String stream)
  {
    this.stream = stream;
  }

  public String getTimestamp()
  {
    return timestamp;
  }

  public void setTimestamp(String timestamp)
  {
    this.timestamp = timestamp;
  }

  public String getTrigger()
  {
    return trigger;
  }

  public void setTrigger(String trigger)
  {
    this.trigger = trigger;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }
}
