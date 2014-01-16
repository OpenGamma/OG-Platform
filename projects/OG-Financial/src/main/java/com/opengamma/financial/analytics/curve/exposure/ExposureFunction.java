/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import java.util.List;

import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.id.ExternalId;

/**
 *
 */
public interface ExposureFunction extends FinancialSecurityVisitor<List<ExternalId>> {
  /** Separator */
  String SEPARATOR = "_";
  /** Security identifier */
  String SECURITY_IDENTIFIER = "SecurityType";
  /** Contract identifier */
  String CONTRACT_IDENTIFIER = "ContractType";

  /**
   * Gets the name of the exposure function.
   * @return The name
   */
  String getName();
}
