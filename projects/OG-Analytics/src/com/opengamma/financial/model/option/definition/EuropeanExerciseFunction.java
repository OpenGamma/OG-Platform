/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

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
