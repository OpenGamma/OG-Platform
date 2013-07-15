/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswapoption.definition;

/**
 * Enumerate the exercise types of Credit Default Swap Options
 */
//TODO rename me - can be applied to many underlyings
public enum CDSOptionExerciseType {
  /**
   * European exercise
   */
  EUROPEAN,
  /**
   * American exercise
   */
  AMERICAN,
  /**
   * Bermudan exercise
   */
  BERMUDAN;
}
