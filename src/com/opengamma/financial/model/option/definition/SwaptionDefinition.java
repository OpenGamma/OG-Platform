/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import com.opengamma.util.time.Expiry;

/**
 * 
 * @author emcleod
 */
public class SwaptionDefinition extends OptionDefinition {

  public SwaptionDefinition(final Double strike, final Expiry expiry, final Boolean isCall) {
    super(strike, expiry, isCall);
    // TODO Auto-generated constructor stub
  }

  @Override
  public <T extends StandardOptionDataBundle> OptionExerciseFunction<T> getExerciseFunction() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends StandardOptionDataBundle> OptionPayoffFunction<T> getPayoffFunction() {
    // TODO Auto-generated method stub
    return null;
  }

}
