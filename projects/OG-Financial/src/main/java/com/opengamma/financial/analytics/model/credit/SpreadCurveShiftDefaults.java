/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class SpreadCurveShiftDefaults extends DefaultPropertyFunction {
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.CS01,
    ValueRequirementNames.BUCKETED_CS01,
    ValueRequirementNames.GAMMA_CS01,
    ValueRequirementNames.BUCKETED_GAMMA_CS01,
    ValueRequirementNames.RR01,
    ValueRequirementNames.IR01,
    ValueRequirementNames.BUCKETED_IR01,
    ValueRequirementNames.JUMP_TO_DEFAULT,
    ValueRequirementNames.PRESENT_VALUE,
    ValueRequirementNames.VALUE_VEGA,
    ValueRequirementNames.HAZARD_RATE_CURVE,
    ValueRequirementNames.CREDIT_SPREAD_CURVE,
    ValueRequirementNames.NET_MARKET_VALUE
  };
  private final String _shiftType;

  public SpreadCurveShiftDefaults(final String shiftType) {
    super(FinancialSecurityTypes.STANDARD_VANILLA_CDS_SECURITY
        .or(FinancialSecurityTypes.LEGACY_VANILLA_CDS_SECURITY)
        .or(FinancialSecurityTypes.CREDIT_DEFAULT_SWAP_OPTION_SECURITY)
        .or(FinancialSecurityTypes.CREDIT_DEFAULT_SWAP_INDEX_SECURITY), true);
    ArgumentChecker.notNull(shiftType, "shift type");
    _shiftType = shiftType;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_CURVE_SHIFT_TYPE);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_CURVE_SHIFT_TYPE.equals(propertyName)) {
      return Collections.singleton(_shiftType);
    }
    return null;
  }

}
