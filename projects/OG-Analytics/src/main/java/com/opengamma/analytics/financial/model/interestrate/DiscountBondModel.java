/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.definition.StandardDiscountBondModelDataBundle;
import com.opengamma.analytics.math.function.Function1D;

/**
 * 
 * @param <T>
 */
public interface DiscountBondModel<T extends StandardDiscountBondModelDataBundle> {

  Function1D<T, Double> getDiscountBondFunction(ZonedDateTime time, ZonedDateTime maturity);
}
