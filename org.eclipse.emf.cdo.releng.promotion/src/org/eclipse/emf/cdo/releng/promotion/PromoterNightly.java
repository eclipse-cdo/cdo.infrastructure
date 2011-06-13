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
public class PromoterNightly extends Promoter
{
  public PromoterNightly(BuildInfo buildInfo)
  {
    super(buildInfo);
  }

  @Override
  public void promoteBuild()
  {
    String buildQualifier = getBuildInfo().getQualifier();
    out("Ignoring " + buildQualifier);
  }
}
