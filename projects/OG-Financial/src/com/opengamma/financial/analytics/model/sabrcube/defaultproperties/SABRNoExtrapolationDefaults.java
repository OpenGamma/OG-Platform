/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sabrcube.defaultproperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.conversion.SwapSecurityUtils;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.model.volatility.VolatilityDataFittingDefaults;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class SABRNoExtrapolationDefaults extends DefaultPropertyFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(SABRNoExtrapolationDefaults.class);
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.PRESENT_VALUE,
    ValueRequirementNames.PRESENT_VALUE_CURVE_SENSITIVITY,
    ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY,
    ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY,
    ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY,
    ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
    ValueRequirementNames.VEGA_QUOTE_CUBE
  };
  private final PriorityClass _priority;
  private final String _fittingMethod;
  private final Map<String, Pair<String, String>> _currencyCurveConfigAndCubeNames;

  public SABRNoExtrapolationDefaults(final String priority, final String fittingMethod, final String... currencyCurveConfigAndCubeNames) {
    super(ComputationTargetType.SECURITY, true);
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(fittingMethod, "fitting method");
    ArgumentChecker.notNull(currencyCurveConfigAndCubeNames, "currency, curve config and surface names");
    _priority = PriorityClass.valueOf(priority);
    _fittingMethod = fittingMethod;
    final int nPairs = currencyCurveConfigAndCubeNames.length;
    ArgumentChecker.isTrue(nPairs % 3 == 0, "Must have one curve config and surface name per currency");
    _currencyCurveConfigAndCubeNames = new HashMap<String, Pair<String, String>>();
    for (int i = 0; i < currencyCurveConfigAndCubeNames.length; i += 3) {
      final Pair<String, String> pair = Pair.of(currencyCurveConfigAndCubeNames[i + 1], currencyCurveConfigAndCubeNames[i + 2]);
      _currencyCurveConfigAndCubeNames.put(currencyCurveConfigAndCubeNames[i], pair);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    final Security security = target.getSecurity();
    if (!(security instanceof SwaptionSecurity
        || (security instanceof SwapSecurity && (SwapSecurityUtils.getSwapType(((SwapSecurity) security)) == InterestRateInstrumentType.SWAP_FIXED_CMS
        || SwapSecurityUtils.getSwapType(((SwapSecurity) security)) == InterestRateInstrumentType.SWAP_CMS_CMS
        || SwapSecurityUtils.getSwapType(((SwapSecurity) security)) == InterestRateInstrumentType.SWAP_IBOR_CMS))
        || security instanceof CapFloorSecurity
        || security instanceof CapFloorCMSSpreadSecurity)) {
      return false;
    }
    final String currencyName = FinancialSecurityUtils.getCurrency(security).getCode();
    return _currencyCurveConfigAndCubeNames.containsKey(currencyName);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE_CALCULATION_CONFIG);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CUBE);
      defaults.addValuePropertyName(valueRequirement, VolatilityDataFittingDefaults.PROPERTY_FITTING_METHOD);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (VolatilityDataFittingDefaults.PROPERTY_FITTING_METHOD.equals(propertyName)) {
      return Collections.singleton(_fittingMethod);
    }
    final String currencyName = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    if (!_currencyCurveConfigAndCubeNames.containsKey(currencyName)) {
      s_logger.error("Could not config and surface names for currency " + currencyName + "; should never happen");
      return null;
    }
    final Pair<String, String> pair = _currencyCurveConfigAndCubeNames.get(currencyName);
    if (ValuePropertyNames.CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(pair.getFirst());
    }
    if (ValuePropertyNames.CUBE.equals(propertyName)) {
      return Collections.singleton(pair.getSecond());
    }
    return null;
  }

  @Override
  public PriorityClass getPriority() {
    return _priority;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.SABR_FITTING_DEFAULTS;
  }

}
