/*
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

import promoter.util.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Eike Stepper
 */
public class ReleaseNotesStream
{
  private String name;

  private String firstRevision;

  private List<BuildInfo> buildInfos = new ArrayList<BuildInfo>();

  public ReleaseNotesStream(String name)
  {
    this.name = name;

    Properties streamProperties = Config.loadProperties(new File("streams", name + ".properties"), true);
    firstRevision = streamProperties.getProperty("first.revision");
    if (firstRevision == null)
    {
      throw new IllegalStateException("First revision of stream " + name + "is not specified");
    }
  }

  public final String getName()
  {
    return name;
  }

  public String getFirstRevision()
  {
    return firstRevision;
  }

  public final List<BuildInfo> getBuildInfos()
  {
    return buildInfos;
  }
}