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

import org.eclipse.emf.cdo.eresource.CDOResource;
import org.eclipse.emf.cdo.tests.model1.Company;
import org.eclipse.emf.cdo.tests.model1.Model1Factory;
import org.eclipse.emf.cdo.transaction.CDOTransaction;

import org.eclipse.emf.ecore.EObject;

import java.util.Iterator;

/**
 * @author Eike Stepper
 */
public class Benchmark002 extends BenchmarkClient
{
  public static void main(String[] args)
  {
    new Benchmark002().run();
  }

  @Override
  protected boolean prepare(CDOTransaction transaction) throws Exception
  {
    CDOResource resource = transaction.createResource("/res1");
    for (int i = 0; i < LOOP; i++)
    {
      Company company = Model1Factory.eINSTANCE.createCompany();
      company.setName("Company " + i);
      company.setStreet("Street " + i);
      company.setCity("City " + i);

      resource.getContents().add(company);
    }

    transaction.commit();
    return false;
  }

  @Override
  protected void benchmark(CDOTransaction transaction) throws Exception
  {
    CDOResource resource = transaction.getResource("/res1");
    for (Iterator<EObject> it = resource.getContents().iterator(); it.hasNext();)
    {
      Company company = (Company)it.next();
      assertNotNull(company.getName());
      assertNotNull(company.getStreet());
      assertNotNull(company.getCity());
    }
  }
}
