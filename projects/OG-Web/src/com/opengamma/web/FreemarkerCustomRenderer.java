/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web;

import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.AsianExerciseType;
import com.opengamma.financial.security.option.BermudanExerciseType;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.ExerciseTypeVisitor;
import com.opengamma.util.ArgumentChecker;

/**
 * Utility class for custom object rendering
 */
public final class FreemarkerCustomRenderer {

  /**
   * Restrictive constructor
   */
  private FreemarkerCustomRenderer() {
  }

  /**
   * Singleton
   */
  public static final Object INSTANCE = new FreemarkerCustomRenderer();

  public String printExerciseType(ExerciseType exerciseType) {
    ArgumentChecker.notNull(exerciseType, "exerciseType");
    String result = exerciseType.accept(new ExerciseTypeVisitor<String>() {

      @Override
      public String visitAmericanExerciseType(AmericanExerciseType exerciseType) {
        return "AMERICAN";
      }

      @Override
      public String visitAsianExerciseType(AsianExerciseType exerciseType) {
        return "ASIAN";
      }

      @Override
      public String visitBermudanExerciseType(BermudanExerciseType exerciseType) {
        return "BERMUDAN";
      }

      @Override
      public String visitEuropeanExerciseType(EuropeanExerciseType exerciseType) {
        return "EUROPEAN";
      }
    });
    return result;
  }
}
