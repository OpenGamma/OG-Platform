/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.AsianExerciseType;
import com.opengamma.financial.security.option.BermudanExerciseType;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.ExerciseTypeVisitor;

/**
 *
 */
public final class ExerciseTypeAnalyticsVisitorAdapter implements ExerciseTypeVisitor<ExerciseDecisionType> {
  private static final ExerciseTypeAnalyticsVisitorAdapter s_instance = new ExerciseTypeAnalyticsVisitorAdapter();

  public static ExerciseTypeAnalyticsVisitorAdapter getInstance() {
    return s_instance;
  }
  /**
   *
   */
  private ExerciseTypeAnalyticsVisitorAdapter() {
  }

  @Override
  public ExerciseDecisionType visitAmericanExerciseType(AmericanExerciseType exerciseType) {
    return ExerciseDecisionType.AMERICAN;
  }

  @Override
  public ExerciseDecisionType visitAsianExerciseType(AsianExerciseType exerciseType) {
    throw new NotImplementedException();
  }

  @Override
  public ExerciseDecisionType visitBermudanExerciseType(BermudanExerciseType exerciseType) {
    throw new NotImplementedException();
  }

  @Override
  public ExerciseDecisionType visitEuropeanExerciseType(EuropeanExerciseType exerciseType) {
    return ExerciseDecisionType.EUROPEAN;
  }

}
