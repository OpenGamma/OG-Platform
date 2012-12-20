/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswapoption.definition;

/**
 * Enumerate the knockout types of Credit Default Swap Options
 */
public enum CDSOptionKnockoutType {
  /**
   * Knockout - Contract cancels if there is a default between trade date and option expiry date
   */
  KNOCKOUT,
  /**
   * Non-knockout - Contract does not cancel is there is a default between trade date and option expiry date
   */
  NONKNOCKOUT;
}
