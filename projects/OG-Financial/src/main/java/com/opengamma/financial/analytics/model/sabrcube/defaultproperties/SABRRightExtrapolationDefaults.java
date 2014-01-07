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
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.analytics.conversion.SwapSecurityUtils;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.model.sabrcube.SABRRightExtrapolationFunction;
import com.opengamma.financial.analytics.model.volatility.SmileFittingPropertyNamesAndValues;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Defaults for SABR functions with right extrapolation
 * @deprecated The functions to which these defaults apply are deprecated.
 */
@Deprecated
public class SABRRightExtrapolationDefaults extends DefaultPropertyFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(SABRRightExtrapolationDefaults.class);
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.PRESENT_VALUE,
    ValueRequirementNames.PRESENT_VALUE_CURVE_SENSITIVITY,
    ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY,
    ValueRequirementNames.PRESENT_VALUE_SABR_RHO_SENSITIVITY,
    ValueRequirementNames.PRESENT_VALUE_SABR_NU_SENSITIVITY,
    ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_NODE_SENSITIVITY,
    ValueRequirementNames.PRESENT_VALUE_SABR_RHO_NODE_SENSITIVITY,
    ValueRequirementNames.PRESENT_VALUE_SABR_NU_NODE_SENSITIVITY,
    ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
  };
  private final String _fittingMethod;
  private final String _cutoff;
  private final String _mu;
  private final Map<String, Pair<String, String>> _currencyCurveConfigAndCubeNames;

  public SABRRightExtrapolationDefaults(final String fittingMethod, final String cutoff, final String mu,
      final String... currencyCurveConfigAndCubeNames) {
    super(FinancialSecurityTypes.SWAPTION_SECURITY.or(FinancialSecurityTypes.SWAP_SECURITY).or(FinancialSecurityTypes.CAP_FLOOR_SECURITY).or(FinancialSecurityTypes.CAP_FLOOR_CMS_SPREAD_SECURITY),
        true);
    ArgumentChecker.notNull(fittingMethod, "fitting method");
    ArgumentChecker.notNull(fittingMethod, "fitting method");
    ArgumentChecker.notNull(cutoff, "cutoff");
    ArgumentChecker.notNull(mu, "mu");
    ArgumentChecker.notNull(currencyCurveConfigAndCubeNames, "currency, curve config and surface names");
    _fittingMethod = fittingMethod;
    _cutoff = cutoff;
    _mu = mu;
    final int nPairs = currencyCurveConfigAndCubeNames.length;
    ArgumentChecker.isTrue(nPairs % 3 == 0, "Must have one curve config and surface name per currency");
    _currencyCurveConfigAndCubeNames = new HashMap<>();
    for (int i = 0; i < currencyCurveConfigAndCubeNames.length; i += 3) {
      final Pair<String, String> pair = Pairs.of(currencyCurveConfigAndCubeNames[i + 1], currencyCurveConfigAndCubeNames[i + 2]);
      _currencyCurveConfigAndCubeNames.put(currencyCurveConfigAndCubeNames[i], pair);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getSecurity();
    if (security instanceof SwapSecurity) {
      if (!InterestRateInstrumentType.isFixedIncomeInstrumentType((SwapSecurity) security)) {
        return false;
      }
      final InterestRateInstrumentType type = SwapSecurityUtils.getSwapType((SwapSecurity) security);
      if ((type != InterestRateInstrumentType.SWAP_FIXED_CMS) && (type != InterestRateInstrumentType.SWAP_CMS_CMS) && (type != InterestRateInstrumentType.SWAP_IBOR_CMS)) {
        return false;
      }
    }
    final String currencyName = FinancialSecurityUtils.getCurrency(security).getCode();
    return _currencyCurveConfigAndCubeNames.containsKey(currencyName);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE_CALCULATION_CONFIG);
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CUBE);
      defaults.addValuePropertyName(valueRequirement, SmileFittingPropertyNamesAndValues.PROPERTY_FITTING_METHOD);
      defaults.addValuePropertyName(valueRequirement, SABRRightExtrapolationFunction.PROPERTY_CUTOFF_STRIKE);
      defaults.addValuePropertyName(valueRequirement, SABRRightExtrapolationFunction.PROPERTY_TAIL_THICKNESS_PARAMETER);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (SmileFittingPropertyNamesAndValues.PROPERTY_FITTING_METHOD.equals(propertyName)) {
      return Collections.singleton(_fittingMethod);
    }
    if (SABRRightExtrapolationFunction.PROPERTY_CUTOFF_STRIKE.equals(propertyName)) {
      return Collections.singleton(_cutoff);
    }
    if (SABRRightExtrapolationFunction.PROPERTY_TAIL_THICKNESS_PARAMETER.equals(propertyName)) {
      return Collections.singleton(_mu);
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
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.SABR_FITTING_DEFAULTS;
  }

}
