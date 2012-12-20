/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

/**
 * Provides a security-specific normalization rule.
 */
public interface SecurityRuleProvider {

  /**
   * Gets a normalization rule for a security.
   * 
   * @param securityUniqueId  the data provider's unique ID of the security, not null
   * @return the normalization rule, or null if no rule applies
   */
  NormalizationRule getRule(String securityUniqueId);
  
}
