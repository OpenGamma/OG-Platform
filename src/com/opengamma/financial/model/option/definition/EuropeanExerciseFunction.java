/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

/**
 * 
 * @param <S> Extends StandardOptionDataBundle: is never used.
 */
public class EuropeanExerciseFunction<S extends StandardOptionDataBundle> implements OptionExerciseFunction<S> {

  @Override
  public boolean shouldExercise(final S data, final Double optionPrice) {
    return false;
  }
}
