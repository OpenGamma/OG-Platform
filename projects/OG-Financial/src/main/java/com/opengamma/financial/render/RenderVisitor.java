/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.render;

import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;

/**
 * Visitor pattern visitor.
 * 
 * @param <T>  the type being visited
 */
public interface RenderVisitor<T> {

  /**
   * Visit yield / discount curve.
   * @param curve  the curve, not null
   * @return the visitor type
   */
  T visitYieldAndDiscountCurve(YieldAndDiscountCurve curve);

  /**
   * Visit volatility surface.
   * @param volatilitySurface  the surface, not null
   * @return the visitor type
   */
  T visitVolatilitySurface(VolatilitySurface volatilitySurface);

  /**
   * Visit discount curves.
   * @param greekResultCollection  the greek results, not null
   * @return the visitor type
   */
  T visitGreekResultCollection(GreekResultCollection greekResultCollection);

}
