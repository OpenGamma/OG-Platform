/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

/**
 *
 * @param <S> The type of the data bundle
 */
public interface OptionPayoffFunction<S extends StandardOptionDataBundle> {

  Double getPayoff(S data, Double optionPrice);
}
