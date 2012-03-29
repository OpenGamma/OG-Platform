/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.financial.model.interestrate.definition.StandardDiscountBondModelDataBundle;

/**
 * 
 * @param <T>
 */
public interface DiscountBondModel<T extends StandardDiscountBondModelDataBundle> {

  Function1D<T, Double> getDiscountBondFunction(ZonedDateTime time, ZonedDateTime maturity);
}
