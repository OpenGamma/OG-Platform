/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Set;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.security.FinancialSecurity;

/**
 * Gathers contributions for sensitivity-based historical P&L calculations.
 * <p>
 * This is available as an interface so that it may be implemented either locally or as a remote service.
 * See {@link DefaultPnLRequirementsGatherer} for a possible implementation.
 */
public interface PnLRequirementsGatherer {

  /**
   * @param security The security
   * @param samplingPeriod The sampling period
   * @param scheduleCalculator The schedule calculator
   * @param samplingFunction The sampling function
   * @param targetSpec The target specification
   * @param currency The currency
   * @return A set of requirements for calculating the first-order P&L
   */
  Set<ValueRequirement> getFirstOrderRequirements(FinancialSecurity security, String samplingPeriod, String scheduleCalculator, String samplingFunction,
      ComputationTargetSpecification targetSpec, String currency);

}
