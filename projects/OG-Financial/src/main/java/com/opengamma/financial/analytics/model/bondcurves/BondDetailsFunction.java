/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcurves;

import static com.opengamma.engine.value.ValueRequirementNames.BOND_DETAILS;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondTransactionDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.interestrate.AnnuityAccrualDatesVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityFixedRatesVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityNotionalsVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityPaymentAmountsVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityPaymentFractionsVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityPaymentTimesVisitor;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondSecurity;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondDiscountFactorsVisitor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.BondAndBondFutureFunctionUtils;
import com.opengamma.financial.analytics.model.fixedincome.FixedSwapLegDetails;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.Pair;

/**
 * Produces information about cash-flows for a bond trade.
 */
public class BondDetailsFunction extends BondAndBondFutureFromCurvesFunction<IssuerProviderInterface, Void> {

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#BOND_DETAILS}.
   */
  public BondDetailsFunction() {
    super(BOND_DETAILS, null);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final ValueProperties properties = desiredValue.getConstraints();
    final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());
    final BondTransactionDefinition<? extends PaymentDefinition, ? extends CouponDefinition> definition =
        (BondTransactionDefinition<? extends PaymentDefinition, ? extends CouponDefinition>) BondAndBondFutureFunctionUtils.getDefinition(executionContext, target, now);
    final BondSecurity<? extends Payment, ? extends Coupon> derivative = definition.toDerivative(now).getBondTransaction();
    final IssuerProviderInterface issuerCurves = (IssuerProviderInterface) inputs.getValue(CURVE_BUNDLE);
    final ValueSpecification spec = new ValueSpecification(BOND_DETAILS, target.toSpecification(), properties);
    final AnnuityDefinition<? extends CouponDefinition> couponDefinitions = definition.getUnderlyingBond().getCoupons();
    final Annuity<? extends Coupon> couponDerivatives = derivative.getCoupon();
    final CurrencyAmount[] notionals = couponDefinitions.accept(AnnuityNotionalsVisitor.getInstance(), now);
    final Pair<LocalDate[], LocalDate[]> accrualDates = couponDefinitions.accept(AnnuityAccrualDatesVisitor.getInstance(), now);
    final double[] paymentTimes = couponDerivatives.accept(AnnuityPaymentTimesVisitor.getInstance());
    final double[] paymentFractions = couponDerivatives.accept(AnnuityPaymentFractionsVisitor.getInstance());
    final CurrencyAmount[] paymentAmounts = couponDerivatives.accept(AnnuityPaymentAmountsVisitor.getInstance());
    final Double[] fixedRates = couponDerivatives.accept(AnnuityFixedRatesVisitor.getInstance());
    final double[] discountFactors = derivative.accept(BondDiscountFactorsVisitor.getInstance(), issuerCurves);
    final FixedSwapLegDetails details = new FixedSwapLegDetails(accrualDates.getFirst(), accrualDates.getSecond(), discountFactors, paymentTimes, paymentFractions, paymentAmounts,
        notionals, fixedRates);
    return Collections.singleton(new ComputedValue(spec, details));
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getTrade().getSecurity();
    return security instanceof com.opengamma.financial.security.bond.BondSecurity;
  }

}
