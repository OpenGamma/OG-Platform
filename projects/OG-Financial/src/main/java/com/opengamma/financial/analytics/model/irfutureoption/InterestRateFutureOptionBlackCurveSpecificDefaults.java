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

/**
 * Adds {@link ValuePropertyNames#CURVE} to the {@link ValueRequirement}'s produced by {@link InterestRateFutureOptionBlackFunction}
 * that require it, such as {@link ValueRequirementNames#POSITION_RHO}
 */
public class InterestRateFutureOptionBlackCurveSpecificDefaults extends InterestRateFutureOptionBlackDefaults {

  
  private static final String[] s_curveRequirements = new String[] {
    ValueRequirementNames.POSITION_RHO
  };
  
  private final HashMap<String, String> _currencyCurveNames;

  public InterestRateFutureOptionBlackCurveSpecificDefaults(final String[] currencyCurveNames, final String[] currencyCurveConfigAndSurfaceNames) {
    super(currencyCurveConfigAndSurfaceNames);
    
    ArgumentChecker.notNull(currencyCurveNames, "currency, curve names");
    final int nPairs = currencyCurveNames.length;
    ArgumentChecker.isTrue(nPairs % 2 == 0, "Must have one curve name per currency");
    _currencyCurveNames = new HashMap<String, String>();
    for (int i = 0; i < currencyCurveNames.length; i += 2) {
      final String currency = currencyCurveNames[i];
      final String curveName = currencyCurveNames[i + 1];
      _currencyCurveNames.put(currency, curveName);
    }
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    super.getDefaults(defaults);
    for (String requirement : s_curveRequirements) {
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
