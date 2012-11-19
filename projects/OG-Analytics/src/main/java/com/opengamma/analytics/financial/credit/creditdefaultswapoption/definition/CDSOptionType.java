/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswapoption.definition;

/**
 * Enumerate the types of Credit Default Swap Options
 */
public enum CDSOptionType {
  /**
   * Payer swaption - option to exercise into a buy protection CDS position
   */
  PAYER,
  /**
   * Receiver swaption - option to exercise into a sell protection CDS position
   */
  RECEIVER;
}
