/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity;

import com.opengamma.analytics.financial.equity.option.EquityIndexOptionDefinition;

/**
 * Visitors of the Definitions, as opposed to Derivatives, work on calendars and events
 * @param <T> Type of the data 
 * @param <U> Type of the result
 */
public interface EquityInstrumentDefinitionVisitor<T, U> {

  U visit(EquityInstrumentDefinition<?> definition, T data);

  U visit(EquityInstrumentDefinition<?> definition);

  U visitEquityIndexOptionDefinition(EquityIndexOptionDefinition definition, T data);

  U visitEquityIndexOptionDefinition(EquityIndexOptionDefinition definition);

}
