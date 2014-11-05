/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;

/**
 * 
 * @param <T> Type of the InterestRateDerivative that the definition returns
 */
public interface InstrumentDefinition<T extends InstrumentDerivative> {

  /**
   * Converts the definition to the time-dependent derivative form.
   * @param date The conversion date
   * @return The derivative
   */
  T toDerivative(ZonedDateTime date);
  
  /**
   * accept() method for visitors
   * @param <U> The type of the data
   * @param <V> The return type of the visitor
   * @param visitor The visitor, not null
   * @param data Data to be used in the visitor
   * @return The result from the supplied visitor appropriate to this type of instrument definition
   */
  <U, V> V accept(InstrumentDefinitionVisitor<U, V> visitor, U data);

  /**
   * accept() method for visitors
   * @param <V> The return type of the visitor
   * @param visitor The visitor, not null
   * @return The result from the supplied visitor appropriate to this type of instrument definition
   */
  <V> V accept(InstrumentDefinitionVisitor<?, V> visitor);

}
