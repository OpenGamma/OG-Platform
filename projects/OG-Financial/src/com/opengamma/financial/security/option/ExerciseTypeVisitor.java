/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

/**
 * Visitor for the {@code ExerciseType} subclasses.
 * 
 * @param <T> visitor method return type
 */
public interface ExerciseTypeVisitor<T> {

  T visitAmericanExerciseType(AmericanExerciseType exerciseType);

  T visitAsianExerciseType(AsianExerciseType exerciseType);

  T visitBermudanExerciseType(BermudanExerciseType exerciseType);

  T visitEuropeanExerciseType(EuropeanExerciseType exerciseType);

}
