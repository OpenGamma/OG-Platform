/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.bond;

import javax.time.calendar.LocalDate;

/**
 * 
 * @param <T> The type of the definition
 */
public interface InterestRateDerivativeProvider<T> {

  T toDerivative(LocalDate date, String... yieldCurveNames);

}
