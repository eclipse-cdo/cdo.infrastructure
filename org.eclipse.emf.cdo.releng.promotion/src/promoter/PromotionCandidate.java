/*
 * Copyright (c) 2004 - 2012 Eike Stepper (Loehne, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package promoter;

import java.util.Properties;

/**
 * A completed build that has been selected as a candidate for promotion.
 *
 * @author Eike Stepper
 */
public final class PromotionCandidate
{
  private final Properties jobProperties;

  private final String buildURL;

  private final BuildInfo buildInfo;

  public PromotionCandidate(Properties jobProperties, String buildURL, BuildInfo buildInfo, boolean visible)
  {
    this.jobProperties = jobProperties;
    this.buildURL = buildURL;
    this.buildInfo = buildInfo;
    buildInfo.setVisible(visible);
  }

  public Properties getJobProperties()
  {
    return jobProperties;
  }

  public String getBuildURL()
  {
    return buildURL;
  }

  public BuildInfo getBuildInfo()
  {
    return buildInfo;
  }
}
