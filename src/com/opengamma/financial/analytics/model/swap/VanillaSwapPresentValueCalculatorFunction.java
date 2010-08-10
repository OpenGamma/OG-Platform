/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swap;

import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.Currency;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.convention.businessday.HolidayRepositoryCalendarAdapter;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.interestrate.swap.VanillaSwapPresentValueCalculator;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.InterestRateLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 * 
 */
public class VanillaSwapPresentValueCalculatorFunction extends AbstractFunction implements FunctionInvoker {
  private static final VanillaSwapPresentValueCalculator CALCULATOR = new VanillaSwapPresentValueCalculator();

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ZonedDateTime now = executionContext.getSnapshotClock().zonedDateTime();
    final Currency currency = getCurrency(target);
    final Calendar calendar = new HolidayRepositoryCalendarAdapter(OpenGammaExecutionContext.getHolidayRepository(executionContext), currency);
    final SwapSecurity swap = (SwapSecurity) target.getSecurity();
    final double[] payPaymentTimes = SwapScheduleCalculator.getPayLegPaymentTimes(swap, calendar, now);
    final double[] receivePaymentTimes = SwapScheduleCalculator.getReceiveLegPaymentTimes(swap, calendar, now);
    double[] fixedPaymentTimes;
    double[] floatPaymentTimes;
    FixedInterestRateLeg fixedLeg;
    FloatingInterestRateLeg floatLeg;
    boolean payFixed;
    if (swap.getPayLeg() instanceof FixedInterestRateLeg) {
      fixedPaymentTimes = payPaymentTimes;
      floatPaymentTimes = receivePaymentTimes;
      fixedLeg = (FixedInterestRateLeg) swap.getPayLeg();
      floatLeg = (FloatingInterestRateLeg) swap.getReceiveLeg();
      payFixed = true;
    } else { // REVIEW Elaine: 5-7-2010 Presumably this assumption is safe because of the canApplyTo method
      fixedPaymentTimes = receivePaymentTimes;
      floatPaymentTimes = payPaymentTimes;
      fixedLeg = (FixedInterestRateLeg) swap.getReceiveLeg();
      floatLeg = (FloatingInterestRateLeg) swap.getPayLeg();
      payFixed = false;
    }
    final double[] fixedPayments = SwapPaymentCalculator.getFixedPayments(fixedLeg, fixedPaymentTimes, payFixed);
    final double floatPayment = SwapPaymentCalculator.getFirstFloatPayment(floatLeg, floatPaymentTimes, false);
    final YieldAndDiscountCurve fundingCurve = (YieldAndDiscountCurve) inputs.getValue(getRequirement(target));
    final double pv = CALCULATOR.getPresentValue(fixedPaymentTimes, fixedPayments, floatPaymentTimes[0], floatPayment, fundingCurve);
    return Sets.newHashSet(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE, swap)), pv));
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    if (target.getSecurity() instanceof SwapSecurity) {
      final SwapSecurity swap = (SwapSecurity) target.getSecurity();
      InterestRateLeg fixed = null, floating = null;
      if (swap.getPayLeg() instanceof FixedInterestRateLeg && swap.getReceiveLeg() instanceof FloatingInterestRateLeg) {
        fixed = (FixedInterestRateLeg) swap.getPayLeg();
        floating = (FloatingInterestRateLeg) swap.getReceiveLeg();
      } else if (swap.getPayLeg() instanceof FloatingInterestRateLeg && swap.getReceiveLeg() instanceof FixedInterestRateLeg) {
        fixed = (FixedInterestRateLeg) swap.getReceiveLeg();
        floating = (FloatingInterestRateLeg) swap.getPayLeg();
      }
      if (fixed == null) {
        return false;
      }
      return ((InterestRateNotional) fixed.getNotional()).getCurrency().equals(((InterestRateNotional) floating.getNotional()).getCurrency());
    }
    return false;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target) {
    return Sets.newHashSet(getRequirement(target));
  }

  private Currency getCurrency(final ComputationTarget target) {
    final SwapSecurity swap = (SwapSecurity) target.getSecurity();
    final InterestRateLeg leg = (InterestRateLeg) swap.getPayLeg();
    return ((InterestRateNotional) leg.getNotional()).getCurrency();
  }

  private ValueRequirement getRequirement(final ComputationTarget target) {
    // return new ValueRequirement(ValueRequirementNames.FUNDING_CURVE, ComputationTargetType.PRIMITIVE, getCurrency(target).getUniqueIdentifier());
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, getCurrency(target).getUniqueIdentifier());
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (!canApplyTo(context, target)) {
      return null;
    }
    return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PRESENT_VALUE, target.getSecurity())));
  }

  @Override
  public String getShortName() {
    return "VanillaSwapPVCalculator";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

}
