/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

/**
 *
 * @param <S> The type of the data bundle
 */
public interface OptionPayoffFunction<S> {

  double getPayoff(S data, Double optionPrice);
}
