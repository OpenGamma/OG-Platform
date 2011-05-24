/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.calculator;

import javax.time.calendar.ZonedDateTime;

/**
 * 
 * @param <T> Type of the InterestRateDerivative that the definition returns
 */
public interface ForexConverter<T extends ForexDerivative> {

  T toDerivative(ZonedDateTime date, String... yieldCurveNames);

  <U, V> V accept(final ForexDefinitionVisitor<U, V> visitor, final U data);

  <V> V accept(ForexDefinitionVisitor<?, V> visitor);

}
