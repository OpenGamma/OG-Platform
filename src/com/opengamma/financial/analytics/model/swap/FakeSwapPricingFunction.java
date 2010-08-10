/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swap;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.Currency;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.Notional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.time.DateUtil;

/**
 * Note the numbers that this produces are absolute rubbish - it's a placeholder for a real funciton.
 *
 */
public class FakeSwapPricingFunction extends AbstractFunction implements FunctionInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(FakeSwapPricingFunction.class);

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    if (target.getSecurity() instanceof SwapSecurity) {
      return true;
    }
    return false;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final SwapSecurity swap = (SwapSecurity) target.getSecurity();
      
      SecuritySource secMaster = context.getSecuritySource();

      final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
      requirements.add(getDiscountCurveMarketDataRequirement(Currency.getInstance("USD").getUniqueIdentifier()));
      return requirements;
    }
    return null;
  }
  
  protected ValueRequirement getDiscountCurveMarketDataRequirement(final UniqueIdentifier uid) {
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, uid);
  }

  @Override
  public String getShortName() {
    return "FakeSwapPricingFunction";
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs,
      ComputationTarget target, Set<ValueRequirement> desiredValues) {
    final SwapSecurity security = (SwapSecurity) target.getSecurity();
    final Notional payNotional = security.getPayLeg().getNotional();
    final Notional receiveNotional = security.getReceiveLeg().getNotional();
    double payAmount = 0.0d;
    double receiveAmount = 0.0d;
    if (payNotional instanceof InterestRateNotional) {
      InterestRateNotional irPayNotional = (InterestRateNotional) payNotional;
      payAmount = irPayNotional.getAmount();
    }
    if (receiveNotional instanceof InterestRateNotional) {
      InterestRateNotional irReceiveNotional = (InterestRateNotional) receiveNotional;
      receiveAmount = irReceiveNotional.getAmount();
    }
    final YieldAndDiscountCurve discountCurve = (YieldAndDiscountCurve) inputs.getValue(getDiscountCurveMarketDataRequirement(Currency.getInstance("USD").getUniqueIdentifier()));
    double rate = discountCurve.getInterestRate(DateUtil.getDifferenceInYears(security.getEffectiveDate().toLocalDate(), security.getMaturityDate()));
    payAmount *= (1 + rate);
    receiveAmount *= (1 + 0.03); // 3% for some unknown reason
    double fv = payAmount - receiveAmount;
    Set<ComputedValue> results = new HashSet<ComputedValue>();
    final ValueSpecification resultSpecification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.FAIR_VALUE, security));
    final ComputedValue resultValue = new ComputedValue(resultSpecification, fv);
    results.add(resultValue);
    return results;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    if (!canApplyTo(context, target)) {
      return null;
    }
    final SwapSecurity security = (SwapSecurity) target.getSecurity();
    final Set<ValueSpecification> results = new HashSet<ValueSpecification>();
    results.add(new ValueSpecification(new ValueRequirement(ValueRequirementNames.FAIR_VALUE, security)));
    return results;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

}
