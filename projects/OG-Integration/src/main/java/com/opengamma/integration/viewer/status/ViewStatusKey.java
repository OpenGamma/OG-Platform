/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status;

import com.opengamma.util.money.Currency;


/**
 * View status result key
 */
public interface ViewStatusKey {
  /**
   * Gets the security type.
   * 
   * @return the security type, not-null.
   */
  String getSecurityType();
  /**
   * Gets the value requirement name.
   * 
   * @return the value requirement name, not-null.
   */
  String getValueRequirementName();
  /**
   * Gets the currency.
   * 
   * @return the currency, not-null.
   */
  Currency getCurrency();
  
}
