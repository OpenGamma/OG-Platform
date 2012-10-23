/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.futures;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.equity.future.pricing.EquityFuturePricerFactory;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.future.EquityFutureSecurity;

/**
 *
 */
public class EquityFuturesDefaultPropertiesFunction extends DefaultPropertyFunction {

  private final String _fundingCurveName;
  private final String _curveCalculationConfigName;
  private final String _pricingMethodName;


  private static final String[] s_valueNames = new String[] {
    ValueRequirementNames.PRESENT_VALUE,
    ValueRequirementNames.VALUE_DELTA,
    ValueRequirementNames.FORWARD,
    ValueRequirementNames.SPOT,
    ValueRequirementNames.VALUE_RHO,
    ValueRequirementNames.PV01,
    ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES
  };


  /**
   * @param fundingCurveName Name of curve used in discounting, e.g. Discounting or FUNDING
   * @param curveCalculationConfigName Choice of MultiCurveCalculationConfig. e.g. DefaultTwoCurveUSDConfig
   * @param pricingMethodName One of MARK_TO_MARKET, COST_OF_CARRY or DIVIDEND_YIELD
   */
  public EquityFuturesDefaultPropertiesFunction(final String fundingCurveName, final String curveCalculationConfigName, final String pricingMethodName) {
    super(ComputationTargetType.TRADE, true);
    Validate.notNull(fundingCurveName, "No fundingCurveName was provided to use as default value.");
    Validate.notNull(curveCalculationConfigName, "No curveCalculationConfigName was provided to use as default value.");
    Validate.notNull(pricingMethodName, "No pricingMethodName was provided to use as default value.");
    _fundingCurveName = fundingCurveName;
    _curveCalculationConfigName = curveCalculationConfigName;
    _pricingMethodName = pricingMethodName;
    Validate.isTrue(pricingMethodName.equals(EquityFuturePricerFactory.MARK_TO_MARKET)
        || pricingMethodName.equals(EquityFuturePricerFactory.COST_OF_CARRY)
        || pricingMethodName.equals(EquityFuturePricerFactory.DIVIDEND_YIELD),
        "OG-Analytics provides the following pricing methods for EquityFutureSecurity: MARK_TO_MARKET, DIVIDEND_YIELD and COST_OF_CARRY. " +
      "If specifying a default, it must be one of these three.");
  }

  @Override
  protected void getDefaults(PropertyDefaults defaults) {
    for (final String valueName : s_valueNames) {
      defaults.addValuePropertyName(valueName, ValuePropertyNames.CURVE);
      defaults.addValuePropertyName(valueName, ValuePropertyNames.CURVE_CALCULATION_CONFIG);
      defaults.addValuePropertyName(valueName, ValuePropertyNames.CALCULATION_METHOD);
    }
  }

  @Override
  protected Set<String> getDefaultValue(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue, String propertyName) {
    if (ValuePropertyNames.CURVE.equals(propertyName)) {
      return Collections.singleton(_fundingCurveName);
    } else if (ValuePropertyNames.CURVE_CALCULATION_CONFIG.equals(propertyName)) {
      return Collections.singleton(_curveCalculationConfigName);
    } else if (ValuePropertyNames.CALCULATION_METHOD.equals(propertyName)) {
      return Collections.singleton(_pricingMethodName);
    }
    return null;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    return target.getTrade().getSecurity() instanceof EquityFutureSecurity;
  }

}
