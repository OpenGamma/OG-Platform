/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.bumpers;

/**
 * Enumerate the types of bumps that can be applied to recovery rates (to compute recovery rate sensitivities)
 *@deprecated this will be deleted 
 */
@Deprecated
public enum RecoveryRateBumpType {
  /**
   * delta -> delta + bump
   */
  ADDITIVE,
  /**
   * delta -> delta x (1 + bump)
   */
  MULTIPLICATIVE;
}
