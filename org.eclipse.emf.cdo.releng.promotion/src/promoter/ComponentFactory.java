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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Eike Stepper
 */
public class ComponentFactory
{
  private <T> String getComponentName(Class<T> type)
  {
    return PromoterConfig.INSTANCE.getProperty("class" + type.getSimpleName(), type.getName());
  }

  public <T> List<T> createComponents(Class<T> type)
  {
    List<T> components = new ArrayList<>();

    StringTokenizer tokenizer = new StringTokenizer(getComponentName(type), ",");
    while (tokenizer.hasMoreTokens())
    {
      String name = tokenizer.nextToken().trim();

      T component = createComponent(name);
      if (component != null)
      {
        components.add(component);
      }
    }

    return components;
  }

  public <T> T createComponent(Class<T> type)
  {
    String name = getComponentName(type);
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

  @SuppressWarnings("rawtypes")
  public List<IssueManager<?>> createIssueManagers()
  {
    List<IssueManager<?>> issueManagers = new ArrayList<>();

    List<IssueManager> components = createComponents(IssueManager.class);
    for (IssueManager issueManager : components)
    {
      issueManagers.add(issueManager);
    }

    return issueManagers;
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
