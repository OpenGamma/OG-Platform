/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Adds {@link ValuePropertyNames#CURVE} to the {@link ValueRequirement}'s produced by {@link InterestRateFutureOptionBlackFunction}
 * that require it, such as {@link ValueRequirementNames#POSITION_RHO}
 * @deprecated The functions for which these defaults apply are deprecated.
 */
@Deprecated
public class InterestRateFutureOptionBlackCurveSpecificDefaults extends InterestRateFutureOptionBlackDefaults {


  private static final String[] s_curveRequirements = new String[] {
    ValueRequirementNames.POSITION_RHO
  };

  private final HashMap<String, String> _currencyCurveNames;

  public InterestRateFutureOptionBlackCurveSpecificDefaults(final String[] currencyCurveConfigAndSurfaceNames) {
    ArgumentChecker.notNull(currencyCurveConfigAndSurfaceNames, "currency, curve names");
    final int argLenth = currencyCurveConfigAndSurfaceNames.length;
    ArgumentChecker.isTrue(argLenth % 4 == 0, "Must have one curve, one curv config and one surface name per currency");
    _currencyCurveNames = new HashMap<>();
    final HashMap<String, Pair<String, String>> currencyConfigAndSurfaceMap = new HashMap<>();
    for (int i = 0; i < argLenth; i += 4) {
      final String currency = currencyCurveConfigAndSurfaceNames[i];
      final String curve = currencyCurveConfigAndSurfaceNames[i + 1];
      final String config = currencyCurveConfigAndSurfaceNames[i + 2];
      final String surface = currencyCurveConfigAndSurfaceNames[i + 3];
      _currencyCurveNames.put(currency, curve);
      currencyConfigAndSurfaceMap.put(currency, Pairs.of(config, surface));
    }
    setCurrencyCurveConfigAndSurfaceNames(currencyConfigAndSurfaceMap);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    super.getDefaults(defaults);
    for (final String requirement : s_curveRequirements) {
      defaults.addValuePropertyName(requirement, ValuePropertyNames.CURVE);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    if (ValuePropertyNames.CURVE.equals(propertyName)) {
      final String currencyName = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode();
      if (!_currencyCurveNames.containsKey(currencyName)) {
        s_logger.error("Could not curve name for currency " + currencyName + "; should never happen");
        return null;
      }
      final String curveName = _currencyCurveNames.get(currencyName);
      return Collections.singleton(curveName);
    }
    return super.getDefaultValue(context, target, desiredValue, propertyName);
  }

  private static final Logger s_logger = LoggerFactory.getLogger(InterestRateFutureOptionBlackCurveSpecificDefaults.class);
}
