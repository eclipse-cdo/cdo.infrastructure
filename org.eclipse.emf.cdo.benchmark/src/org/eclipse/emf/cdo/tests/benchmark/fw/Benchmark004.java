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
import org.eclipse.emf.cdo.tests.model1.OrderDetail;
import org.eclipse.emf.cdo.tests.model1.Product1;
import org.eclipse.emf.cdo.tests.model1.SalesOrder;
import org.eclipse.emf.cdo.transaction.CDOTransaction;

import org.eclipse.emf.ecore.EObject;

import java.util.Iterator;

/**
 * @author Eike Stepper
 */
public class Benchmark004 extends BenchmarkClient
{
  public static void main(String[] args)
  {
    new Benchmark004().run();
  }

  @Override
  protected boolean prepare(CDOTransaction transaction) throws Exception
  {
    CDOResource resource = transaction.createResource("/res1");

    int i = 0;
    while (++i < 1000)
    {
      if (i % 100 == 0)
      {
        System.out.println(i);
        // System.gc();

        // int objects = i * 25;
        // long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        // System.out.println(/* "" + objects + "\t" + */usedMemory);
      }

      try
      {
        Company company = Model1Factory.eINSTANCE.createCompany();
        company.setName("Company " + i);
        company.setStreet("Street " + i);
        company.setCity("City " + i);
        company.getCategories().add(createTree(2, 2, 3));
        company.getCategories().add(createTree(2, 2, 3));

        resource.getContents().add(company);

        for (int j = 0; j < 10; j++)
        {
          SalesOrder salesOrder = Model1Factory.eINSTANCE.createSalesOrder();
          resource.getContents().add(salesOrder);

          for (Iterator<EObject> it = company.eAllContents(); it.hasNext();)
          {
            EObject object = it.next();
            if (object instanceof Product1)
            {
              Product1 product = (Product1)object;
              OrderDetail orderDetail = Model1Factory.eINSTANCE.createOrderDetail();
              orderDetail.setProduct(product);

              salesOrder.getOrderDetails().add(orderDetail);
            }
          }
        }
      }
      catch (Throwable ex)
      {
        ex.printStackTrace();
        System.err.println("i = " + i);
        break;
      }
    }

    return false;
  }

  @Override
  protected void benchmark(CDOTransaction transaction) throws Exception
  {
    Thread.sleep(100000);
  }
}
