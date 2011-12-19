/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

/**
 * @param <S> The type of the parameters
 * @param <T> The type of the data 
 */
public interface VaRCalculator<S, T> {

  Double evaluate(final S parameters, final T... data);
}
