/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondfutureoption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackCalculator;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeAndForwardBundle;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.id.ExternalId;

/**
 *
 */
public class BondFutureOptionBlackFromFuturePresentValueFunction extends BondFutureOptionBlackFunction {
  /** String indicating the calculation method */
  public static final String FUTURES_PRICE = "FromFuturePrice";
  private static final PresentValueBlackCalculator s_calculator = PresentValueBlackCalculator.getInstance();

  public BondFutureOptionBlackFromFuturePresentValueFunction() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative bondFutureOption, final YieldCurveWithBlackCubeBundle data, final MultiCurveCalculationConfig curveCalculationConfig,
      final ValueSpecification spec, final FunctionInputs inputs, final Set<ValueRequirement> desiredValue, final BondFutureOptionSecurity security) {
    final Object underlyingValue = inputs.getValue(MarketDataRequirementNames.MARKET_VALUE);
    if (underlyingValue == null) {
      throw new OpenGammaRuntimeException("Could not get market data for underlying future");
    }
    final double futurePrice = (Double) underlyingValue;
    final YieldCurveWithBlackCubeAndForwardBundle dataWithFuture = YieldCurveWithBlackCubeAndForwardBundle.from(data, futurePrice);
    final double pv = bondFutureOption.accept(s_calculator, dataWithFuture);
    return Collections.singleton(new ComputedValue(spec, pv));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
    if (requirements == null) {
      return null;
    }
    final BondFutureOptionSecurity security = (BondFutureOptionSecurity) target.getTrade().getSecurity();
    final ExternalId underlyingId = security.getUnderlyingId();
    requirements.add(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, underlyingId));
    return requirements;
  }

  @Override
  protected ValueProperties getResultProperties(final String currency) {
    final ValueProperties.Builder properties = super.getResultProperties(currency).copy();
    properties.withoutAny(ValuePropertyNames.CALCULATION_METHOD).with(ValuePropertyNames.CALCULATION_METHOD, FUTURES_PRICE);
    return properties.get();
  }

  @Override
  protected ValueProperties getResultProperties(final ValueRequirement desiredValue, final BondFutureOptionSecurity security) {
    final ValueProperties.Builder properties = super.getResultProperties(desiredValue, security).copy();
    properties.withoutAny(ValuePropertyNames.CALCULATION_METHOD).with(ValuePropertyNames.CALCULATION_METHOD, FUTURES_PRICE);
    return properties.get();
  }
}
