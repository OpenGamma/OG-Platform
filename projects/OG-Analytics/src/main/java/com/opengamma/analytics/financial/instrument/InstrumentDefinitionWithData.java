/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;

/**
 * @param <S> Type of the data needed for conversion
 * @param <T> Type of the interest rate derivative produced
 */
public interface InstrumentDefinitionWithData<T extends InstrumentDerivative, S> extends InstrumentDefinition<T> {

  /**
   * Converts the definition to the time-dependent derivative form.
   * @param date The conversion date
   * @param data Data needed for conversion
   * @param yieldCurveNames The yield curve names
   * @return The derivative
   * @deprecated Use the version that does not require yield curve names
   */
  @Deprecated
  T toDerivative(ZonedDateTime date, S data, String... yieldCurveNames);

  /**
   * Converts the definition to the time-dependent derivative form.
   * @param date The conversion date
   * @param data needed for conversion
   * @return The derivative
   */
  T toDerivative(ZonedDateTime date, S data);
}
