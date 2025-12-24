/*
 * Copyright (c) 2004 - 2012 Eike Stepper (Loehne, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package promoter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import promoter.util.Config;

/**
 * @author Eike Stepper
 */
public final class ReleaseNotesStream
{
  private final String name;

  private final String firstRevision;

  private final List<BuildInfo> buildInfos = new ArrayList<>();

  public ReleaseNotesStream(String name)
  {
    this.name = name;

    File configFolder = new File(PromoterConfig.INSTANCE.getConfigDirectory(), "streams");
    Properties streamProperties = Config.loadProperties(new File(configFolder, name + ".properties"), true);

    if (Config.isDisabled(streamProperties))
    {
      firstRevision = null;
    }
    else
    {
      firstRevision = streamProperties.getProperty("first.revision");
      if (firstRevision == null)
      {
        throw new IllegalStateException("First revision of stream " + name + "is not specified");
      }
    }
  }

  public String getName()
  {
    return name;
  }

  public boolean isDisabled()
  {
    return firstRevision == null;
  }

  public String getFirstRevision()
  {
    return firstRevision;
  }

  public List<BuildInfo> getBuildInfos()
  {
    return buildInfos;
  }

  @Override
  public String toString()
  {
    return name;
  }
}
