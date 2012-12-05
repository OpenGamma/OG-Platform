/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.indexoption;

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
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Populates {@link EquityIndexOptionFunction}, including {@link EquityIndexVanillaBarrierOptionFunction}, with defaults.
 */
public class EquityIndexOptionDefaults extends DefaultPropertyFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(EquityIndexOptionDefaults.class);
  private final Map<Currency, Pair<String, String>> _currencyCurveConfigAndDiscountingCurveNames;
  private final String _volSurfaceName;
  private final String _smileInterpolator;
  private final PriorityClass _priority;

  private static final String[] s_valueNames = new String[] {
    ValueRequirementNames.PRESENT_VALUE,
    ValueRequirementNames.VEGA_QUOTE_MATRIX,
    ValueRequirementNames.VALUE_VEGA,
    ValueRequirementNames.IMPLIED_VOLATILITY,
    ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
    ValueRequirementNames.FORWARD,
    ValueRequirementNames.SPOT,
    ValueRequirementNames.VALUE_DELTA,
    ValueRequirementNames.VALUE_GAMMA,
    ValueRequirementNames.VALUE_VOMMA,
    ValueRequirementNames.VALUE_VANNA,
    ValueRequirementNames.VALUE_RHO
  };

  /**
   * @param priority The priority class of {@link DefaultPropertyFunction} instances, allowing them to be ordered relative to each other. ABOVE_NORMAL, NORMAL, BELOW_NORMAL, LOWEST
   * @param volSurface Prefix of the volatility surface to use (e.g. BBG)
   * @param smileInterpolator Name of the interpolation method for the smile (e.g. Spline)
   * @param currencyCurveConfigAndDiscountingCurveNames Choice of MultiCurveCalculationConfig. e.g. DefaultTwoCurveUSDConfig
   */
  public EquityIndexOptionDefaults(final String priority, final String volSurface, final String smileInterpolator,
      final String... currencyCurveConfigAndDiscountingCurveNames) {

    super(ComputationTargetType.SECURITY, true);
    ArgumentChecker.notNull(priority, "No priority was provided.");
    ArgumentChecker.notNull(volSurface, "No volSurface name was provided to use as default value.");
    ArgumentChecker.notNull(smileInterpolator, "No smileInterpolator name was provided to use as default value.");
    ArgumentChecker.notNull(currencyCurveConfigAndDiscountingCurveNames, "No curveCalculationConfigName was provided to use as default value.");
    _priority = PriorityClass.valueOf(priority);
    _volSurfaceName = volSurface;
    _smileInterpolator = smileInterpolator;

    final int nPairs = currencyCurveConfigAndDiscountingCurveNames.length;
    ArgumentChecker.isTrue(nPairs % 3 == 0, "Must have one curve config and discounting curve name per currency");
    _currencyCurveConfigAndDiscountingCurveNames = new HashMap<Currency, Pair<String, String>>();
    for (int i = 0; i < currencyCurveConfigAndDiscountingCurveNames.length; i += 3) {
      final Pair<String, String> pair = Pair.of(currencyCurveConfigAndDiscountingCurveNames[i + 1], currencyCurveConfigAndDiscountingCurveNames[i + 2]);
      final Currency ccy = Currency.of(currencyCurveConfigAndDiscountingCurveNames[i]);
      _currencyCurveConfigAndDiscountingCurveNames.put(ccy, pair);
    }

  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueName : s_valueNames) {
      defaults.addValuePropertyName(valueName, ValuePropertyNames.CURVE);
      defaults.addValuePropertyName(valueName, ValuePropertyNames.CURVE_CALCULATION_CONFIG);
      defaults.addValuePropertyName(valueName, ValuePropertyNames.SURFACE);
      defaults.addValuePropertyName(valueName, BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final Pair<String, String> pair = _currencyCurveConfigAndDiscountingCurveNames.get(ccy);
    if (ValuePropertyNames.CURVE.equals(propertyName)) {
      return Collections.singleton(pair.getSecond());
    }
    if (ValuePropertyNames.CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(pair.getFirst());
    }
    if (ValuePropertyNames.SURFACE.equals(propertyName)) {
      return Collections.singleton(_volSurfaceName);
    }
    if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR.equals(propertyName)) {
      return Collections.singleton(_smileInterpolator);
    }
    s_logger.error("Cannot get a default value for {}", propertyName);
    return null;
  }

  @Override
  /* Applies to EquityIndexOptionSecurity and EquityBarrierOptionSecurity */
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    final Security eqSec = target.getSecurity();
    if (!((eqSec instanceof EquityIndexOptionSecurity) ||
          (eqSec instanceof EquityBarrierOptionSecurity))) {
      return false;
    }
    final Currency ccy = FinancialSecurityUtils.getCurrency(eqSec);
    final boolean applies = _currencyCurveConfigAndDiscountingCurveNames.containsKey(ccy);
    return applies;
  }

  @Override
  public PriorityClass getPriority() {
    return _priority;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.EQUITY_OPTION_DEFAULTS;
  }
}
