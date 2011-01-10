/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

/**
 * 
 * @param <S> The type of the data bundle
 * 
 */
public interface OptionExerciseFunction<S> {

  boolean shouldExercise(S data, Double optionPrice);
}
