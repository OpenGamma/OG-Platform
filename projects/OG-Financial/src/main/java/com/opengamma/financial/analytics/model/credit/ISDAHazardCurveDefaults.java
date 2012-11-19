/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class ISDAHazardCurveDefaults extends DefaultPropertyFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(ISDAHazardCurveDefaults.class);
  private final PriorityClass _priority;
  private final String _nIterations;
  private final String _tolerance;
  private final String _rangeMultiplier;

  public ISDAHazardCurveDefaults(final String priority, final String nIterations, final String tolerance, final String rangeMultiplier) {
    super(ComputationTargetType.SECURITY, true);
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(nIterations, "number of iterations");
    ArgumentChecker.notNull(tolerance, "tolerance");
    ArgumentChecker.notNull(rangeMultiplier, "range multiplier");
    _priority = PriorityClass.valueOf(priority);
    _nIterations = nIterations;
    _tolerance = tolerance;
    _rangeMultiplier = rangeMultiplier;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    return target.getSecurity() instanceof LegacyVanillaCDSSecurity;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    defaults.addValuePropertyName(ValueRequirementNames.HAZARD_RATE_CURVE, CreditInstrumentPropertyNamesAndValues.PROPERTY_HAZARD_RATE_CURVE_N_ITERATIONS);
    defaults.addValuePropertyName(ValueRequirementNames.HAZARD_RATE_CURVE, CreditInstrumentPropertyNamesAndValues.PROPERTY_HAZARD_RATE_CURVE_TOLERANCE);
    defaults.addValuePropertyName(ValueRequirementNames.HAZARD_RATE_CURVE, CreditInstrumentPropertyNamesAndValues.PROPERTY_HAZARD_RATE_CURVE_RANGE_MULTIPLIER);
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (CreditInstrumentPropertyNamesAndValues.PROPERTY_HAZARD_RATE_CURVE_N_ITERATIONS.equals(propertyName)) {
      return Collections.singleton(_nIterations);
    }
    if (CreditInstrumentPropertyNamesAndValues.PROPERTY_HAZARD_RATE_CURVE_TOLERANCE.equals(propertyName)) {
      return Collections.singleton(_tolerance);
    }
    if (CreditInstrumentPropertyNamesAndValues.PROPERTY_HAZARD_RATE_CURVE_RANGE_MULTIPLIER.equals(propertyName)) {
      return Collections.singleton(_rangeMultiplier);
    }
    s_logger.error("Did not have default value for property called {}", propertyName);
    return null;
  }

  @Override
  public PriorityClass getPriority() {
    return _priority;
  }
}
