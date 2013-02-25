/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma
 group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.BermudanExerciseType;
import com.opengamma.financial.security.option.EuropeanExerciseType;

public enum ExerciseType {
  European, American, Bermudan;


  public com.opengamma.financial.security.option.ExerciseType convert() {

    switch (this) {

      case American:
        return new AmericanExerciseType();
      case Bermudan:
        return new BermudanExerciseType();
      case European:
        return new EuropeanExerciseType();
      default:
        throw new OpenGammaRuntimeException("Unexpected exercise type: " + name());
    }
  }

}