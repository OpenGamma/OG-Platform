/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

/**
 * Which of the three accrual on default formulae to use 
 */
public enum AccrualOnDefaultFormulae {

  /**
   * the formula in v1.8.1 and below
   */
  OrignalISDA,
  /**
   * the correction proposed by Markit (v 1.8.2)
   */
  MarkitFix,
  /**
   * The mathematically correct formula 
   */
  Correct

}
