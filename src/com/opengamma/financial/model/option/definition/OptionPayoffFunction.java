/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

/**
 * 
 */
public interface OptionPayoffFunction<S extends StandardOptionDataBundle> {

  public Double getPayoff(S data, Double optionPrice);
}
