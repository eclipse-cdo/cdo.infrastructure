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


/**
 * @author Eike Stepper
 */
public abstract class Promoter implements Runnable
{
  private BuildInfo buildInfo;

  public Promoter(BuildInfo buildInfo)
  {
    this.buildInfo = buildInfo;
  }

  public final BuildInfo getBuildInfo()
  {
    return buildInfo;
  }

  public final void run()
  {
    promoteBuild();
  }

  protected void promoteBuild()
  {
    String buildQualifier = buildInfo.getQualifier();
    out("Promoting " + buildQualifier);
    buildInfo.getDrop().mkdirs();
  }

  protected final void out(Object msg)
  {
    System.out.println(formatMessage(msg));
  }

  protected final void err(Object msg)
  {
    System.err.println(formatMessage(msg));
  }

  private String formatMessage(Object msg)
  {
    return buildInfo.getNumber() + ": " + msg;
  }
}
