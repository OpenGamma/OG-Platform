/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.discounting;

import static com.opengamma.engine.value.ValueRequirementNames.SWAP_PAY_LEG_DETAILS;
import static com.opengamma.engine.value.ValueRequirementNames.SWAP_RECEIVE_LEG_DETAILS;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.interestrate.AnnuityAccrualDatesVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityFixedRatesVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityFixingDatesVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityFixingYearFractionsVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityGearingsVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityIndexTenorsVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityNotionalsVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityPaymentAmountsVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityPaymentDatesVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityPaymentFractionsVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityPaymentTimesVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuitySpreadsVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.provider.AnnuityDiscountFactorsVisitor;
import com.opengamma.analytics.financial.interestrate.swap.provider.AnnuityForwardRatesVisitor;
import com.opengamma.analytics.financial.interestrate.swap.provider.AnnuityProjectedPaymentsVisitor;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.fixedincome.FixedSwapLegDetails;
import com.opengamma.financial.analytics.model.fixedincome.FloatingSwapLegDetails;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * Produces information about cash-flows for a swap leg.
 */
public class DiscountingSwapLegDetailFunction extends DiscountingFunction {
  /** Whether this function returns details for the pay leg or the receive leg */
  private final boolean _payLeg;

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#SWAP_PAY_LEG_DETAILS} or {@link ValueRequirementNames#SWAP_RECEIVE_LEG_DETAILS}
   *
   * @param payLeg True if the details to be returned are for the pay leg; false returns details for receive legs.
   */
  public DiscountingSwapLegDetailFunction(final String payLeg) {
    this(Boolean.parseBoolean(payLeg));
  }

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#SWAP_PAY_LEG_DETAILS} or {@link ValueRequirementNames#SWAP_RECEIVE_LEG_DETAILS}
   *
   * @param payLeg True if the details to be returned are for the pay leg; false returns details for receive legs.
   */
  public DiscountingSwapLegDetailFunction(final boolean payLeg) {
    super(payLeg ? SWAP_PAY_LEG_DETAILS : SWAP_RECEIVE_LEG_DETAILS);
    _payLeg = payLeg;
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new DiscountingCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), false) {

      @Override
      public boolean canApplyTo(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
        final Security security = target.getTrade().getSecurity();
        return security instanceof SwapSecurity;
      }

      @SuppressWarnings("synthetic-access")
      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
          final ComputationTarget target, final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative,
          final FXMatrix fxMatrix) {
        final MulticurveProviderInterface data = getMergedProviders(inputs, fxMatrix);
        final SwapSecurity security = (SwapSecurity) target.getTrade().getSecurity();
        final Clock snapshotClock = executionContext.getValuationClock();
        final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
        final SwapDefinition definition = (SwapDefinition) getDefinitionFromTarget(target);
        if (definition == null) {
          throw new OpenGammaRuntimeException("Definition for security " + security + " was null");
        }
        @SuppressWarnings("unchecked")
        final Swap<? extends Payment, ? extends Payment> swap = (Swap<? extends Payment, ? extends Payment>) derivative;
        final AnnuityDefinition<? extends PaymentDefinition> legDefinition;
        final Annuity<? extends Payment> legDerivative;

        final boolean isFixed;
        if (_payLeg) {
          isFixed = security.getPayLeg() instanceof FixedInterestRateLeg;
          final boolean payFirstLeg = definition.getFirstLeg().getNthPayment(0).getReferenceAmount() < 0;
          legDefinition = payFirstLeg ? definition.getFirstLeg() : definition.getSecondLeg();
          legDerivative = payFirstLeg ? swap.getFirstLeg() : swap.getSecondLeg();
        } else {
          isFixed = security.getReceiveLeg() instanceof FixedInterestRateLeg;
          final boolean payFirstLeg = definition.getFirstLeg().getNthPayment(0).getReferenceAmount() < 0;
          legDefinition = payFirstLeg ? definition.getSecondLeg() : definition.getFirstLeg();
          legDerivative = payFirstLeg ? swap.getSecondLeg() : swap.getFirstLeg();
        }
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final ValueSpecification spec = new ValueSpecification(getValueRequirementNames()[0], target.toSpecification(), desiredValue.getConstraints());
        final CurrencyAmount[] notionals = legDefinition.accept(AnnuityNotionalsVisitor.getInstance(), now);
        final Pair<LocalDate[], LocalDate[]> accrualDates = legDefinition.accept(AnnuityAccrualDatesVisitor.getInstance(), now);
        final double[] paymentTimes = legDerivative.accept(AnnuityPaymentTimesVisitor.getInstance());
        final double[] paymentFractions = legDerivative.accept(AnnuityPaymentFractionsVisitor.getInstance());
        final CurrencyAmount[] paymentAmounts = legDerivative.accept(AnnuityPaymentAmountsVisitor.getInstance());
        final Double[] fixedRates = legDerivative.accept(AnnuityFixedRatesVisitor.getInstance());
        final double[] discountFactors = legDerivative.accept(AnnuityDiscountFactorsVisitor.getInstance(), data);
        if (isFixed) {
          final FixedSwapLegDetails details = new FixedSwapLegDetails(accrualDates.getFirst(), accrualDates.getSecond(), discountFactors, paymentTimes, paymentFractions, paymentAmounts,
              notionals, fixedRates);
          return Collections.singleton(new ComputedValue(spec, details));
        }
        final Pair<LocalDate[], LocalDate[]> fixingDates = legDefinition.accept(AnnuityFixingDatesVisitor.getInstance(), now);
        final Double[] fixingYearFractions = legDefinition.accept(AnnuityFixingYearFractionsVisitor.getInstance(), now);
        final Double[] forwardRates = legDerivative.accept(AnnuityForwardRatesVisitor.getInstance(), data);
        final LocalDate[] paymentDates = legDefinition.accept(AnnuityPaymentDatesVisitor.getInstance(), now);
        final CurrencyAmount[] projectedAmounts = legDerivative.accept(AnnuityProjectedPaymentsVisitor.getInstance(), data);
        final double[] spreads = legDefinition.accept(AnnuitySpreadsVisitor.getInstance(), now);
        final double[] gearings = legDefinition.accept(AnnuityGearingsVisitor.getInstance(), now);
        final List<Set<Tenor>> indexTenors = legDefinition.accept(AnnuityIndexTenorsVisitor.getInstance(), now);
        final FloatingSwapLegDetails details = new FloatingSwapLegDetails(accrualDates.getFirst(), accrualDates.getSecond(), paymentFractions, fixingDates.getFirst(), fixingDates.getSecond(),
            fixingYearFractions, forwardRates, fixedRates, paymentDates, paymentTimes, discountFactors, paymentAmounts, projectedAmounts, notionals, spreads, gearings, indexTenors);
        return Collections.singleton(new ComputedValue(spec, details));
      }


    };
  }

}
