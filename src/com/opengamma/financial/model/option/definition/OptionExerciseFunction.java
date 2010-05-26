/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

/**
 * 
 */
public interface OptionExerciseFunction<S extends StandardOptionDataBundle> {

  public Boolean shouldExercise(S data, Double optionPrice);
}
