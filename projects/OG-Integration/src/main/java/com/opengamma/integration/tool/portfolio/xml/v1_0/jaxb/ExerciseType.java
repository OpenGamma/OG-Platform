/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import javax.xml.bind.annotation.XmlEnumValue;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.BermudanExerciseType;
import com.opengamma.financial.security.option.EuropeanExerciseType;

public enum ExerciseType {

  @XmlEnumValue(value = "European")
  EUROPEAN,
  @XmlEnumValue(value = "American")
  AMERICAN,
  @XmlEnumValue(value = "Bermudan")
  BERMUDAN;


  public com.opengamma.financial.security.option.ExerciseType convert() {

    switch (this) {

      case AMERICAN:
        return new AmericanExerciseType();
      case BERMUDAN:
        return new BermudanExerciseType();
      case EUROPEAN:
        return new EuropeanExerciseType();
      default:
        throw new OpenGammaRuntimeException("Unexpected exercise type: " + name());
    }
  }

}
