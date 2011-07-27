/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.HashSet;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.ircurve.MarketInstrumentImpliedYieldCurveFunction;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.interestrate.InstrumentSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXSecurity;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class ForexForwardYieldCurveNodeSensitivitiesFunction extends ForexForwardFunction {
  private static final InstrumentSensitivityCalculator CALCULATOR = InstrumentSensitivityCalculator.getInstance();

  public ForexForwardYieldCurveNodeSensitivitiesFunction(final String payCurveName, final String receiveCurveName) {
    super(payCurveName, receiveCurveName, ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES);
  }

  @Override
  protected Object getResult(final ForexDerivative fxForward, final YieldCurveBundle data) {

    return 0.6;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
    final FXForwardSecurity fxForward = (FXForwardSecurity) target.getSecurity();
    final FXSecurity fx = (FXSecurity) getSecuritySource().getSecurity(IdentifierBundle.of(fxForward.getUnderlyingIdentifier()));
    final String payCurveName = getPayCurveName();
    final String receiveCurveName = getReceiveCurveName();
    final Currency payCurrency = fx.getPayCurrency();
    final Currency receiveCurrency = fx.getReceiveCurrency();
    result.add(YieldCurveFunction.getCurveRequirement(payCurrency, payCurveName, payCurveName, payCurveName, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    result.add(YieldCurveFunction.getCurveRequirement(receiveCurrency, receiveCurveName, receiveCurveName, receiveCurveName,
        MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    result.add(YieldCurveFunction.getJacobianRequirement(payCurrency, payCurveName, payCurveName, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    result.add(YieldCurveFunction.getJacobianRequirement(receiveCurrency, receiveCurveName, receiveCurveName, MarketInstrumentImpliedYieldCurveFunction.PRESENT_VALUE_STRING));
    result.add(YieldCurveFunction.getCouponSensitivityRequirement(payCurrency, payCurveName, payCurveName));
    result.add(YieldCurveFunction.getCouponSensitivityRequirement(receiveCurrency, receiveCurveName, receiveCurveName));
    result.add(new ValueRequirement(ValueRequirementNames.FX_CURVE_SENSITIVITIES, target.getSecurity(), ValueProperties.builder().with(ValuePropertyNames.PAY_CURVE, payCurveName)
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName).get()));
    return result;
  }
}
