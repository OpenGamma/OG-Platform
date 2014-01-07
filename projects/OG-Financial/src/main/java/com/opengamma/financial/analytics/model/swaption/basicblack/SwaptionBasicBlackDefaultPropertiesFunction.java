/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption.basicblack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.OpenGammaFunctionExclusions;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Default properties function for swaptions that are to be priced using the basic Black method.
 * @deprecated The functions for which these default properties apply are deprecated.
 */
@Deprecated
public class SwaptionBasicBlackDefaultPropertiesFunction extends DefaultPropertyFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(SwaptionBasicBlackDefaultPropertiesFunction.class);
  /** The requirements for which these defaults apply */
  private static final String[] s_valueRequirements = new String[] {
    ValueRequirementNames.PRESENT_VALUE,
    ValueRequirementNames.VALUE_VEGA,
    ValueRequirementNames.PV01,
    ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES,
    ValueRequirementNames.SECURITY_IMPLIED_VOLATILITY,
  };
  /** A map of currency to default curve calculation configuration names */
  private final Map<String, String> _currencyAndCurveConfigNames;

  /**
   * @param currencyAndCurveConfigNames A list of alternating currency and curve calculation configuration names, not null
   */
  public SwaptionBasicBlackDefaultPropertiesFunction(final String... currencyAndCurveConfigNames) {
    super(FinancialSecurityTypes.SWAPTION_SECURITY, true);
    ArgumentChecker.notNull(currencyAndCurveConfigNames, "currency and curve config names");
    final int nPairs = currencyAndCurveConfigNames.length;
    ArgumentChecker.isTrue(nPairs % 2 == 0, "Must have one curve config name per currency");
    _currencyAndCurveConfigNames = new HashMap<>();
    for (int i = 0; i < currencyAndCurveConfigNames.length; i += 2) {
      _currencyAndCurveConfigNames.put(currencyAndCurveConfigNames[i], currencyAndCurveConfigNames[i + 1]);
    }
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final SwaptionSecurity swaption = (SwaptionSecurity) target.getSecurity();
    final String currencyName = FinancialSecurityUtils.getCurrency(swaption).getCode();
    return _currencyAndCurveConfigNames.containsKey(currencyName);
  }

  @Override
  protected void getDefaults(final PropertyDefaults defaults) {
    for (final String valueRequirement : s_valueRequirements) {
      defaults.addValuePropertyName(valueRequirement, ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    }
  }

  @Override
  protected Set<String> getDefaultValue(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue, final String propertyName) {
    final String currencyName = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    if (!_currencyAndCurveConfigNames.containsKey(currencyName)) {
      s_logger.error("Could not config and surface names for currency " + currencyName + "; should never happen");
      return null;
    }
    if (ValuePropertyNames.CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(_currencyAndCurveConfigNames.get(currencyName));
    }
    return null;
  }
/**
  @Override
  public String getMutualExclusionGroup() {
    return OpenGammaFunctionExclusions.SWAPTION_BASIC_BLACK_DEFAULTS;
  }
  */
}
