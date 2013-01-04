/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class ISDALegacyCDSHazardCurveDefaults extends DefaultPropertyFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(ISDALegacyCDSHazardCurveDefaults.class);
  private static final String[] VALUE_REQUIREMENTS = new String[] {
    ValueRequirementNames.HAZARD_RATE_CURVE,
    ValueRequirementNames.CLEAN_PRICE,
    ValueRequirementNames.DIRTY_PRICE
  };
  private final PriorityClass _priority;
  private final String _nIterations;
  private final String _tolerance;
  private final String _rangeMultiplier;
  private final Map<String, String[]> _yieldCurvePropertiesForCurrency;

  public ISDALegacyCDSHazardCurveDefaults(final String priority, final String nIterations, final String tolerance, final String rangeMultiplier,
      final String... yieldCurvePropertiesForCurrency) {
    super(ComputationTargetType.SECURITY, true);
    ArgumentChecker.notNull(priority, "priority");
    ArgumentChecker.notNull(nIterations, "number of iterations");
    ArgumentChecker.notNull(tolerance, "tolerance");
    ArgumentChecker.notNull(rangeMultiplier, "range multiplier");
    ArgumentChecker.notNull(yieldCurvePropertiesForCurrency, "yield curve properties for currency");
    ArgumentChecker.isTrue(yieldCurvePropertiesForCurrency.length % 4 == 0, "must have one yield curve name, yield curve calculation config and yield curve calculation method per currency");
    _priority = PriorityClass.valueOf(priority);
    _nIterations = nIterations;
    _tolerance = tolerance;
    _rangeMultiplier = rangeMultiplier;
    _yieldCurvePropertiesForCurrency = new LinkedHashMap<String, String[]>();
    for (int i = 0; i < yieldCurvePropertiesForCurrency.length; i += 4) {
      final String currency = yieldCurvePropertiesForCurrency[i];
      final String[] defaults = new String[3];
      defaults[0] = yieldCurvePropertiesForCurrency[i + 1];
      defaults[1] = yieldCurvePropertiesForCurrency[i + 2];
      defaults[2] = yieldCurvePropertiesForCurrency[i + 3];
      _yieldCurvePropertiesForCurrency.put(currency, defaults);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    final Security security = target.getSecurity();
    if (!(security instanceof LegacyVanillaCDSSecurity)) {
      return false;
    }
    final String currency = FinancialSecurityUtils.getCurrency(security).getCode();
    if (_yieldCurvePropertiesForCurrency.containsKey(currency)) {
      return true;
    }
    return false;
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : VALUE_REQUIREMENTS) {
      defaults.addValuePropertyName(valueRequirement, CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE);
      defaults.addValuePropertyName(valueRequirement, CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE_CALCULATION_CONFIG);
      defaults.addValuePropertyName(valueRequirement, CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE_CALCULATION_METHOD);
      defaults.addValuePropertyName(valueRequirement, CreditInstrumentPropertyNamesAndValues.PROPERTY_HAZARD_RATE_CURVE_N_ITERATIONS);
      defaults.addValuePropertyName(valueRequirement, CreditInstrumentPropertyNamesAndValues.PROPERTY_HAZARD_RATE_CURVE_TOLERANCE);
      defaults.addValuePropertyName(valueRequirement, CreditInstrumentPropertyNamesAndValues.PROPERTY_HAZARD_RATE_CURVE_RANGE_MULTIPLIER);
    }
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
    final String currency = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    final String[] defaultYieldCurveValues = _yieldCurvePropertiesForCurrency.get(currency);
    if (defaultYieldCurveValues == null) {
      s_logger.error("Did not have defaults yield curve values for currency {}; should never happen", currency);
      return null;
    }
    if (CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE.equals(propertyName)) {
      return Collections.singleton(defaultYieldCurveValues[0]);
    }
    if (CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(defaultYieldCurveValues[1]);
    }
    if (CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE_CALCULATION_METHOD.equals(propertyName)) {
      return Collections.singleton(defaultYieldCurveValues[2]);
    }
    s_logger.warn("Did not have default value for property called {}", propertyName);
    return null;
  }

  @Override
  public PriorityClass getPriority() {
    return _priority;
  }

  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.ISDA_LEGACY_CDS_CURVE;
  }
}
