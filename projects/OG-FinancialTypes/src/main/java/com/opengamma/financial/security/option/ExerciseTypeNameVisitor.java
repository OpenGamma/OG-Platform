/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

/**
 * Visitor that gives the name of an Exercise type
 */
public class ExerciseTypeNameVisitor implements ExerciseTypeVisitor<String> {
  /** American */
  private static final String AMERICAN = "American";
  /** Asian type */
  private static final String ASIAN = "Asian";
  /** Bermudan type */
  private static final String BERMUDAN = "Bermudan";
  /** European type */
  private static final String EUROPEAN = "European";

  @Override
  public String visitAmericanExerciseType(AmericanExerciseType exerciseType) {
    return AMERICAN;
  }

  @Override
  public String visitAsianExerciseType(AsianExerciseType exerciseType) {
    return ASIAN;
  }

  @Override
  public String visitBermudanExerciseType(BermudanExerciseType exerciseType) {
    return BERMUDAN;
  }

  @Override
  public String visitEuropeanExerciseType(EuropeanExerciseType exerciseType) {
    return EUROPEAN;
  }

}
