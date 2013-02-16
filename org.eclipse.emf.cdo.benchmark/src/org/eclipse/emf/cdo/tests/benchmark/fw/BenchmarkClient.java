package org.eclipse.emf.cdo.tests.benchmark.fw;

import org.eclipse.emf.cdo.net4j.CDONet4jSessionConfiguration;
import org.eclipse.emf.cdo.net4j.CDONet4jUtil;
import org.eclipse.emf.cdo.session.CDOSession;
import org.eclipse.emf.cdo.tests.model1.Category;
import org.eclipse.emf.cdo.tests.model1.Model1Factory;
import org.eclipse.emf.cdo.tests.model1.Product1;
import org.eclipse.emf.cdo.transaction.CDOTransaction;

import org.eclipse.net4j.Net4jUtil;
import org.eclipse.net4j.connector.IConnector;
import org.eclipse.net4j.tcp.TCPUtil;
import org.eclipse.net4j.util.container.IManagedContainer;
import org.eclipse.net4j.util.container.IPluginContainer;
import org.eclipse.net4j.util.lifecycle.LifecycleUtil;

/**
 * @author Eike Stepper
 * @since 4.0
 */
public abstract class BenchmarkClient
{
  public static final int LOOP = 100000;

  public final void run()
  {
    IManagedContainer container = IPluginContainer.INSTANCE;
    Net4jUtil.prepareContainer(container);
    TCPUtil.prepareContainer(container);
    container.activate();

    IConnector connector = null;
    CDOSession session = null;
    CDOTransaction transaction = null;

    try
    {
      connector = TCPUtil.getConnector(container, "localhost");

      CDONet4jSessionConfiguration configuration = CDONet4jUtil.createNet4jSessionConfiguration();
      configuration.setConnector(connector);
      configuration.setRepositoryName(BenchmarkServer.REPOSITORY_NAME);

      session = configuration.openNet4jSession();
      transaction = session.openTransaction();

      long prepareStart = System.currentTimeMillis();
      boolean reopenTransaction = prepare(transaction);
      System.err.println(System.currentTimeMillis() - prepareStart);

      if (reopenTransaction)
      {
        transaction.close();
        transaction = session.openTransaction();
      }

      long benchmarkStart = System.currentTimeMillis();
      benchmark(transaction);
      System.err.println(System.currentTimeMillis() - benchmarkStart);
    }
    catch (Throwable ex)
    {
      ex.printStackTrace();
    }
    finally
    {
      LifecycleUtil.deactivate(transaction);
      LifecycleUtil.deactivate(session);
      LifecycleUtil.deactivate(connector);
      LifecycleUtil.deactivate(container);
    }
  }

  protected abstract boolean prepare(CDOTransaction transaction) throws Exception;

  protected abstract void benchmark(CDOTransaction transaction) throws Exception;

  public static void assertNotNull(Object object)
  {
    if (object == null)
    {
      throw new AssertionError("Should not be null");
    }
  }

  public static Category createTree(int depth, int categories, int products)
  {
    if (depth == 0)
    {
      return null;
    }

    Category category = Model1Factory.eINSTANCE.createCategory();
    for (int i = 0; i < categories; i++)
    {
      Category child = createTree(depth - 1, categories, products);
      if (child != null)
      {
        category.getCategories().add(child);
      }
    }

    for (int i = 0; i < products; i++)
    {
      Product1 child = Model1Factory.eINSTANCE.createProduct1();
      category.getProducts().add(child);
    }

    return category;
  }
}
