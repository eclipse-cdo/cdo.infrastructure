/*
 * Copyright (c) 2004 - 2012 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package org.eclipse.emf.cdo.tests.benchmark.fw;

import org.eclipse.emf.cdo.server.CDOServerUtil;
import org.eclipse.emf.cdo.server.IRepository;
import org.eclipse.emf.cdo.server.mem.IMEMStore;
import org.eclipse.emf.cdo.server.mem.MEMStoreUtil;
import org.eclipse.emf.cdo.server.net4j.CDONet4jServerUtil;
import org.eclipse.emf.cdo.util.CommitException;

import org.eclipse.net4j.Net4jUtil;
import org.eclipse.net4j.tcp.TCPUtil;
import org.eclipse.net4j.util.container.IManagedContainer;
import org.eclipse.net4j.util.container.IPluginContainer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Eike Stepper
 */
public class BenchmarkServer
{
  public static final String REPOSITORY_NAME = "repo1";

  public static void main(String[] args) throws CommitException, Exception
  {
    IManagedContainer container = IPluginContainer.INSTANCE;
    Net4jUtil.prepareContainer(container);
    TCPUtil.prepareContainer(container);
    CDONet4jServerUtil.prepareContainer(container);
    container.activate();
    Map<String, String> props = new HashMap<String, String>();

    IMEMStore store = MEMStoreUtil.createMEMStore();
    IRepository repository = CDOServerUtil.createRepository(REPOSITORY_NAME, store, props);
    CDOServerUtil.addRepository(container, repository);

    TCPUtil.getAcceptor(container, "0.0.0.0");

    System.out.println("Press enter to shutdown...");
    while (System.in.available() == 0)
    {
      Thread.sleep(100L);
    }
  }
}
