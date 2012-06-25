/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial;

/**
 * Enum representing the exercise decision type of single payoff options
 */
public enum ExerciseDecisionType {
  /**
   * European => decision to be made only on single Expiry
   */
  EUROPEAN,
  /**
   * American => decision may be made at any time before Expiry 
   */
  AMERICAN;

}
