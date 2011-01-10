/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.option;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.AsianExerciseType;
import com.opengamma.financial.security.option.BermudanExerciseType;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.ExerciseTypeVisitor;

/**
 * Exercise type of the option.
 */
public enum OptionExerciseType {

  /** American */
  AMERICAN,
  /** Asian */
  ASIAN,
  /** Bermudan */
  BERMUDAN,
  /** European */
  EUROPEAN;

  public static OptionExerciseType identify(ExerciseType exerciseType) {
    return exerciseType.accept(new ExerciseTypeVisitor<OptionExerciseType>() {

      @Override
      public OptionExerciseType visitAmericanExerciseType(AmericanExerciseType exerciseType) {
        return AMERICAN;
      }

      @Override
      public OptionExerciseType visitAsianExerciseType(AsianExerciseType exerciseType) {
        return ASIAN;
      }

      @Override
      public OptionExerciseType visitBermudanExerciseType(BermudanExerciseType exerciseType) {
        return BERMUDAN;
      }

      @Override
      public OptionExerciseType visitEuropeanExerciseType(EuropeanExerciseType exerciseType) {
        return EUROPEAN;
      }

    });
  }

  public <T> T accept(final ExerciseTypeVisitor<T> visitor) {
    switch (this) {
      case AMERICAN :
        return visitor.visitAmericanExerciseType(null);
      case ASIAN :
        return visitor.visitAsianExerciseType(null);
      case BERMUDAN :
        return visitor.visitBermudanExerciseType(null);
      case EUROPEAN :
        return visitor.visitEuropeanExerciseType(null);
      default:
        throw new OpenGammaRuntimeException("unexpected enum value " + this);
    }
  }

}
