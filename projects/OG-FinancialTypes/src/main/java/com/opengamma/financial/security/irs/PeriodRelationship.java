/*
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.financial.security.irs;

/**
 * Details a relationship to the start or end of a period.
 */
public enum PeriodRelationship {

  /**
   * The beginning of the period. Also known as <b>advance</b> when describing the reset period of
   * an interest rate swap.
   */
  BEGINNING,

  /**
   * The end of the period. Also known as <b>arrears</b> when describing the reset period of
   * an interest rate swap.
   */
  END
}
