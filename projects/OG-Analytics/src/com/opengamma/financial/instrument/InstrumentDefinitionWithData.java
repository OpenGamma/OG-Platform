/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.interestrate.InstrumentDerivative;

/**
 * @param <S> Type of the data needed for conversion
 * @param <T> Type of the interest rate derivative produced
 */
public interface InstrumentDefinitionWithData<T extends InstrumentDerivative, S> extends InstrumentDefinition<T> {

  T toDerivative(ZonedDateTime date, S data, String... yieldCurveNames);

}
