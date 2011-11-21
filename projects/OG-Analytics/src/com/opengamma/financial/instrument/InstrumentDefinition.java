/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument;

import com.opengamma.financial.interestrate.InstrumentDerivative;

import javax.time.calendar.ZonedDateTime;

/**
 * 
 * @param <T> Type of the InterestRateDerivative that the definition returns
 */
public interface InstrumentDefinition<T extends InstrumentDerivative> {

  T toDerivative(ZonedDateTime date, String... yieldCurveNames);

  <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data);

  <V> V accept(InstrumentDefinitionVisitor<?, V> visitor);

}
