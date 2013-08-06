/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;


/**
 * Default Exercise type visitor
 */
public final class ExerciseTypeVisitorImpl implements ExerciseTypeVisitor<ExerciseType> {

  @Override
  public ExerciseType visitAmericanExerciseType(AmericanExerciseType exerciseType) {
    return new AmericanExerciseType();
  }

  @Override
  public ExerciseType visitAsianExerciseType(AsianExerciseType exerciseType) {
    return new AsianExerciseType();
  }

  @Override
  public ExerciseType visitBermudanExerciseType(BermudanExerciseType exerciseType) {
    return new BermudanExerciseType();
  }

  @Override
  public ExerciseType visitEuropeanExerciseType(EuropeanExerciseType exerciseType) {
    return new EuropeanExerciseType();
  }

}
