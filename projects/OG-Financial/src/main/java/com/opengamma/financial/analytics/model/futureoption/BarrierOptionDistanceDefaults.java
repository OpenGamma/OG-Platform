/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.futureoption;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.util.ArgumentChecker;

/**
 * Default barrier distance output format
 */
public class BarrierOptionDistanceDefaults extends DefaultPropertyFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(BarrierOptionDistanceDefaults.class);
  /** Default value for barrier output */
  private final String _barrierOutput;

  /**
   * Value requirement names for which these properties apply
   */
  private static final String[] s_valueNames = new String[] {
    ValueRequirementNames.BARRIER_DISTANCE
  };

  /**
   * @param barrierOutput the barrier output format, not null
   */
  public BarrierOptionDistanceDefaults(final String barrierOutput) {
    super(FinancialSecurityTypes.EQUITY_BARRIER_OPTION_SECURITY
        .or(FinancialSecurityTypes.FX_BARRIER_OPTION_SECURITY)
        .or(FinancialSecurityTypes.FX_DIGITAL_OPTION_SECURITY),
        true);
    ArgumentChecker.notNull(barrierOutput, "barrier output format");
    _barrierOutput = barrierOutput;
  }


  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueName : s_valueNames) {
      defaults.addValuePropertyName(valueName, ValuePropertyNames.BARRIER_DISTANCE_OUTPUT_FORMAT);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (ValuePropertyNames.BARRIER_DISTANCE_OUTPUT_FORMAT.equals(propertyName)) {
      return Collections.singleton(_barrierOutput);
    }
    s_logger.error("Could not get default value for {}", propertyName);
    return null;
  }

}
