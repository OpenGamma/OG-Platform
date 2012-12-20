/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.futureoption;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;

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
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.lambdava.tuple.Pair;

/**
 *
 */
public class FutureOptionBlackDefaultPropertiesFunction extends DefaultPropertyFunction {

  private final Map<Currency, Pair<String, String>> _currencyCurveConfigAndDiscountingCurveNames;
  private final String _volSurfaceName;
  private final String _smileInterpolator;
  private final PriorityClass _priority;

  private static final String[] s_valueNames = new String[] {
    ValueRequirementNames.PRESENT_VALUE,
  };

  /**
   * @param priority The priority class of {@link DefaultPropertyFunction} instances, allowing them to be ordered relative to each other. ABOVE_NORMAL, NORMAL, BELOW_NORMAL, LOWEST
   * @param volSurface Prefix of the volatility surface to use (e.g. BBG)
   * @param smileInterpolator Name of the interpolation method for the smile (e.g. Spline)
   * @param currencyCurveConfigAndDiscountingCurveNames Choice of MultiCurveCalculationConfig. e.g. DefaultTwoCurveUSDConfig
   */
  public FutureOptionBlackDefaultPropertiesFunction(final String priority, final String volSurface, final String smileInterpolator,
      final String... currencyCurveConfigAndDiscountingCurveNames) {

    super(ComputationTargetType.SECURITY, true);
    Validate.notNull(priority, "No priority was provided.");
    Validate.notNull(volSurface, "No volSurface name was provided to use as default value.");
    Validate.notNull(smileInterpolator, "No smileInterpolator name was provided to use as default value.");
    Validate.notNull(currencyCurveConfigAndDiscountingCurveNames, "No curveCalculationConfigName was provided to use as default value.");
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
  protected Set<String> getDefaultValue(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue, String propertyName) {
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final Pair<String, String> pair = _currencyCurveConfigAndDiscountingCurveNames.get(ccy);
    if (ValuePropertyNames.CURVE.equals(propertyName)) {
      return Collections.singleton(pair.getSecond());
    } else if (ValuePropertyNames.CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(pair.getFirst());
    } else if (ValuePropertyNames.SURFACE.equals(propertyName)) {
      return Collections.singleton(_volSurfaceName);
    } else if (BlackVolatilitySurfacePropertyNamesAndValues.PROPERTY_SMILE_INTERPOLATOR.equals(propertyName)) {
      return Collections.singleton(_smileInterpolator);
    }
    return null;
  }

  @Override
  /** Applies to EquityIndexOptionSecurity and EquityBarrierOptionSecurity */
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    final Security security = target.getSecurity();
    if (!(security instanceof CommodityFutureOptionSecurity)) {
      return false;
    }
    final Currency ccy = FinancialSecurityUtils.getCurrency(security);
    final boolean applies = _currencyCurveConfigAndDiscountingCurveNames.containsKey(ccy);
    return applies;
  }

  @Override
  public PriorityClass getPriority() {
    return _priority;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.FUTURE_OPTION_BLACK;
  }

}
