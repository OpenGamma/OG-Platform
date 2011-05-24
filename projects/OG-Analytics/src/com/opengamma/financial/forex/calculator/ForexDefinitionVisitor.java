/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.calculator;

import com.opengamma.financial.forex.definition.ForexDefinition;

/**
 * Visitor of Forex instrument.
 * @param <T> Type of the data.
 * @param <U> Type of the result.
 */
public interface ForexDefinitionVisitor<T, U> {

  U visit(ForexConverter<?> definition, T data);

  U visit(ForexConverter<?> definition);

  U visitForexDefinition(ForexDefinition fx, T data);

  U visitForexDefinition(ForexDefinition fx);

}
