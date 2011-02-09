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
 * @param <T> Type of the InterestRateDerivative that the definition returns
 */
public interface FixedIncomeInstrumentDefinition<T extends InterestRateDerivative> {

  T toDerivative(LocalDate date, String... yieldCurveNames);

  <U, V> V accept(FixedIncomeInstrumentDefinitionVisitor<U, V> visitor, U data);

  <V> V accept(FixedIncomeInstrumentDefinitionVisitor<?, V> visitor);
}
