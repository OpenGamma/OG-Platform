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
 * PnL requirements gatherer. This is available as an interface so that it may be implemented either locally or as a remote service.
 * <p>
 * See {@link DefaultPnLRequirementsGatherer} for a possible implementation.
 */
public interface PnLRequirementsGatherer {

  Set<ValueRequirement> getFirstOrderRequirements(FinancialSecurity security, String samplingPeriod, String scheduleCalculator, String samplingFunction,
      ComputationTargetSpecification targetSpec, String currency);

}
