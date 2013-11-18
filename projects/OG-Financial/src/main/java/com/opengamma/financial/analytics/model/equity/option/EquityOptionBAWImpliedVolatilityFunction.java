/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import java.util.Collections;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.EqyOptBaroneAdesiWhaleyPresentValueCalculator;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.option.EquityIndexFutureOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityOption;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BaroneAdesiWhaleyModel;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/** Calculates the implied volatility of an equity index or equity option using the {@link BaroneAdesiWhaleyModel} */
public class EquityOptionBAWImpliedVolatilityFunction extends EquityOptionBAWFunction {

  /** The Barone-Adesi Whaley present value calculator */
  private static final EqyOptBaroneAdesiWhaleyPresentValueCalculator s_pvCalculator = EqyOptBaroneAdesiWhaleyPresentValueCalculator.getInstance();
  
  /** Default constructor */
  public EquityOptionBAWImpliedVolatilityFunction() {
    super(ValueRequirementNames.IMPLIED_VOLATILITY);
  }
  
  @Override
  protected Set<ComputedValue> computeValues(InstrumentDerivative derivative, StaticReplicationDataBundle market, FunctionInputs inputs, Set<ValueRequirement> desiredValues,
      ComputationTargetSpecification targetSpec, ValueProperties resultProperties) {
    
    final double optionPrice;
    final double strike;
    final double timeToExpiry;
    final boolean isCall;
    if (derivative instanceof EquityOption) {
      final EquityOption option = (EquityOption) derivative;
      strike = option.getStrike();
      timeToExpiry = option.getTimeToExpiry();
      isCall = option.isCall();
      optionPrice = derivative.accept(s_pvCalculator, market) / option.getUnitAmount();
    } else if (derivative instanceof EquityIndexOption) {
      final EquityIndexOption option = (EquityIndexOption) derivative;
      strike = option.getStrike();
      timeToExpiry = option.getTimeToExpiry();
      isCall = option.isCall();
      optionPrice = derivative.accept(s_pvCalculator, market) / option.getUnitAmount();
    } else if (derivative instanceof EquityIndexFutureOption) {
      final EquityIndexFutureOption option = (EquityIndexFutureOption) derivative;
      strike = option.getStrike();
      timeToExpiry = option.getExpiry();
      isCall = option.isCall();
      optionPrice = derivative.accept(s_pvCalculator, market) / option.getPointValue();
    } else {
      throw new OpenGammaRuntimeException("Unexpected InstrumentDerivative type");
    }
    final double spot = market.getForwardCurve().getSpot();
    final double discountRate = market.getDiscountCurve().getInterestRate(timeToExpiry);
    final double costOfCarry = discountRate - Math.log(market.getForwardCurve().getForward(timeToExpiry) / spot) / timeToExpiry;
    final double impliedVol = (new BaroneAdesiWhaleyModel()).impliedVolatility(optionPrice, spot, strike, discountRate, costOfCarry, timeToExpiry, isCall);
    
    final ValueSpecification resultSpec = new ValueSpecification(getValueRequirementNames()[0], targetSpec, resultProperties);
    return Collections.singleton(new ComputedValue(resultSpec, impliedVol));
  }
}
