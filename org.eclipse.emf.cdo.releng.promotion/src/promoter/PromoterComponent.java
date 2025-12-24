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

/**
 * @author Eike Stepper
 */
public class PromoterComponent
{
  private Promoter promoter;

  public PromoterComponent()
  {
  }

  public final Promoter getPromoter()
  {
    return promoter;
  }

  void setPromoter(Promoter promoter)
  {
    this.promoter = promoter;
  }
}
