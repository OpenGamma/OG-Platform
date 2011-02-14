/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument;

import javax.time.calendar.LocalDate;

import com.opengamma.financial.interestrate.InterestRateDerivative;

/**
 * 
 * @param <T> Type of the InterestRateDerivative that the definition can be converted to
 */
public interface FixedIncomeFutureInstrumentDefinition<T extends InterestRateDerivative> {

  T toDerivative(LocalDate date, double price, String... yieldCurveNames);

  <U, V> V accept(FixedIncomeFutureInstrumentDefinitionVisitor<U, V> visitor, U data);

  <V> V accept(FixedIncomeFutureInstrumentDefinitionVisitor<?, V> visitor);

}
