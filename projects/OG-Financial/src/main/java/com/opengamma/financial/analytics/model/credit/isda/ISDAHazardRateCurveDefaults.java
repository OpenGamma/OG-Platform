/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isda;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ISDAHazardRateCurveDefaults extends DefaultPropertyFunction {
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
    ValueRequirementNames.PAR_SPREAD,
    ValueRequirementNames.HEDGE_NOTIONAL,
    ValueRequirementNames.NET_MARKET_VALUE
  };
  private final PriorityClass _priority;
  //private final Map<String, String> _currencyToHazardRateCurveName;
  private final Map<String, String> _currencyToHazardRateCurveCalculationMethodName;

  public ISDAHazardRateCurveDefaults(final String priority, final String... perCurrencyDefaults) {
    super(FinancialSecurityTypes.STANDARD_VANILLA_CDS_SECURITY
        .or(FinancialSecurityTypes.LEGACY_VANILLA_CDS_SECURITY)
        .or(FinancialSecurityTypes.CREDIT_DEFAULT_SWAP_OPTION_SECURITY)
        .or(FinancialSecurityTypes.CREDIT_DEFAULT_SWAP_INDEX_SECURITY), true);
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(perCurrencyDefaults, "per currency defaults");
    ArgumentChecker.isTrue(perCurrencyDefaults.length % 2 == 0, "must have one hazard rate curve calculation method name per currency");
    _priority = PriorityClass.valueOf(priority);
    //    _currencyToHazardRateCurveName = new HashMap<>();
    _currencyToHazardRateCurveCalculationMethodName = new HashMap<>();
    for (int i = 0; i < perCurrencyDefaults.length; i += 2) {
      final String currency = perCurrencyDefaults[i];
      _currencyToHazardRateCurveCalculationMethodName.put(currency, perCurrencyDefaults[i + 1]);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    return _currencyToHazardRateCurveCalculationMethodName.containsKey(currency);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      //defaults.addValuePropertyName(valueRequirement, CreditInstrumentPropertyNamesAndValues.PROPERTY_HAZARD_RATE_CURVE);
      defaults.addValuePropertyName(valueRequirement, CreditInstrumentPropertyNamesAndValues.PROPERTY_HAZARD_RATE_CURVE_CALCULATION_METHOD);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    //    if (CreditInstrumentPropertyNamesAndValues.PROPERTY_HAZARD_RATE_CURVE.equals(propertyName)) {
    //      return Collections.singleton(_currencyToHazardRateCurveName.get(currency));
    //    }
    if (CreditInstrumentPropertyNamesAndValues.PROPERTY_HAZARD_RATE_CURVE_CALCULATION_METHOD.equals(propertyName)) {
      return Collections.singleton(_currencyToHazardRateCurveCalculationMethodName.get(currency));
    }
    return null;
  }

  @Override
  public PriorityClass getPriority() {
    return _priority;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.ISDA_COMPLIANT_HAZARD_CURVE_DEFAULTS;
  }

}
