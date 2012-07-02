/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity;

import javax.time.calendar.ZonedDateTime;

/**
 * 
 * @param <T> Type of the EquityDerivative that the definition returns
 */
public interface EquityInstrumentDefinition<T extends EquityDerivative> {

  T toDerivative(ZonedDateTime date, String... yieldCurveNames);

  <U, V> V accept(final EquityInstrumentDefinitionVisitor<U, V> visitor, final U data);

  <V> V accept(EquityInstrumentDefinitionVisitor<?, V> visitor);

}
