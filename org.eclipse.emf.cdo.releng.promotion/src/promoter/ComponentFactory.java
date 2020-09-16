/*
 * Copyright (c) 2004 - 2012 Eike Stepper (Loehne, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package promoter;

import java.lang.reflect.Constructor;

/**
 * @author Eike Stepper
 */
public class ComponentFactory
{
  public <T> T createComponent(Class<T> type)
  {
    String name = PromoterConfig.INSTANCE.getProperty("class" + type.getSimpleName(), type.getName());
    return createComponent(name);
  }

  public <T> T createComponent(String name)
  {
    try
    {
      @SuppressWarnings("unchecked")
      Class<T> c = (Class<T>)getClass().getClassLoader().loadClass(name);
      Constructor<T> constructor = c.getConstructor();

      T component = constructor.newInstance();
      if (component instanceof PromoterComponent)
      {
        PromoterComponent promoterComponent = (PromoterComponent)component;
        promoterComponent.setPromoter((Promoter)this);
      }

      return component;
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }

  public SourceCodeManager createSourceCodeManager()
  {
    return createComponent(SourceCodeManager.class);
  }

  public IssueManager createIssueManager()
  {
    return createComponent(IssueManager.class);
  }

  public BuildCopier createBuildCopier()
  {
    return createComponent(BuildCopier.class);
  }

  public DropProcessor createDropProcessor()
  {
    return createComponent(DropProcessor.class);
  }

  public ReleaseNotesGenerator createReleaseNotesGenerator()
  {
    return createComponent(ReleaseNotesGenerator.class);
  }

  public RepositoryComposer createRepositoryComposer()
  {
    return createComponent(RepositoryComposer.class);
  }

  public WebGenerator createWebGenerator()
  {
    return createComponent(WebGenerator.class);
  }
}
