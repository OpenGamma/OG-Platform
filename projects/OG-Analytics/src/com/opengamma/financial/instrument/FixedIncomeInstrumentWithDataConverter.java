/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.interestrate.InterestRateDerivative;

/**
 * @param <S> Type of the data needed for conversion
 * @param <T> Type of the interest rate derivative produced
 */
public interface FixedIncomeInstrumentWithDataConverter<T extends InterestRateDerivative, S> extends FixedIncomeInstrumentConverter<T> {

  T toDerivative(ZonedDateTime date, S data, String... yieldCurveNames);

}
