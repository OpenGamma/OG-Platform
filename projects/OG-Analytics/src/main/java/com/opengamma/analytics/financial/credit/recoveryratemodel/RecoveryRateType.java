/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.recoveryratemodel;

/**
 * Enumerates the different types of recovery rate model obligors may have
 */
public enum RecoveryRateType {

  /**
   * Recovery rate is a constant value
   */
  CONSTANT,
  /**
   * Recovery rate is computed according to some stochastic model e.g. sampled from a beta distribution
   */
  STOCHASTIC;

}
