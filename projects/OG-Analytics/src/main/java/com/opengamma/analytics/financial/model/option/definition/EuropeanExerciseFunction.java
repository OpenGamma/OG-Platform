/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;


/**
 * 
 * @param <S> Never used.
 */
public class EuropeanExerciseFunction<S> implements OptionExerciseFunction<S> {

  @Override
  public boolean shouldExercise(final S data, final Double optionPrice) {
    return false;
  }
}
