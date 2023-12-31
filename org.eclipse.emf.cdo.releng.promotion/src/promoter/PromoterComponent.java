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
