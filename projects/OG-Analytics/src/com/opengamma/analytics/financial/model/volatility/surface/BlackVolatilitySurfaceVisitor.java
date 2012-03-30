/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;


/**
 * @param <S> Type of the arguments
 * @param <T> Return type of the function
 */
public interface BlackVolatilitySurfaceVisitor<S, T> {

  T visitDelta(final BlackVolatilitySurfaceDelta surface, final S data);

  T visitStrike(final BlackVolatilitySurfaceStrike surface, final S data);

  T visitMoneyness(final BlackVolatilitySurfaceMoneyness surface, final S data);

  T visitLogMoneyness(final BlackVolatilitySurfaceLogMoneyness surface, final S data);

  T visitDelta(final BlackVolatilitySurfaceDelta surface);

  T visitStrike(final BlackVolatilitySurfaceStrike surface);

  T visitMoneyness(final BlackVolatilitySurfaceMoneyness surface);

  T visitLogMoneyness(final BlackVolatilitySurfaceLogMoneyness surface);
}
