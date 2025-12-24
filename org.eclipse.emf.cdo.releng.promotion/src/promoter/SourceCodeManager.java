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

import java.util.function.BiConsumer;

/**
 * @author Eike Stepper
 */
public abstract class SourceCodeManager extends PromoterComponent
{
  public SourceCodeManager()
  {
  }

  public abstract void setTag(String branch, String revision, String tag);

  public abstract void getCommits(String branch, String fromRevision, String toRevision, BiConsumer<String, String> commitConsumer);
}
