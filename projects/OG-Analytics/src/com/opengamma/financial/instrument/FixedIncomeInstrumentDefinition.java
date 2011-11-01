/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument;

import com.opengamma.financial.interestrate.InterestRateDerivative;

import javax.time.calendar.ZonedDateTime;

/**
 * 
 * @param <T> Type of the InterestRateDerivative that the definition returns
 */
public interface FixedIncomeInstrumentDefinition<T extends InterestRateDerivative> {

  T toDerivative(ZonedDateTime date, String... yieldCurveNames);

  <U, V> V accept(final FixedIncomeInstrumentDefinitionVisitor<U, V> visitor, final U data);

  <V> V accept(FixedIncomeInstrumentDefinitionVisitor<?, V> visitor);

}
