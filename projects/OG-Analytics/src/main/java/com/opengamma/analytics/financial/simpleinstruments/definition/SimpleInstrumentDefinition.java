/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.simpleinstruments.definition;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.simpleinstruments.derivative.SimpleInstrument;

/**
 * 
 * @param <T> The type of the derivative to which the definition is converted.
 */
public interface SimpleInstrumentDefinition<T extends SimpleInstrument> {

  T toDerivative(final ZonedDateTime date);
}
