/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.trs;

import static com.opengamma.engine.value.ValueRequirementNames.BOND_DETAILS;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondTotalReturnSwapDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.interestrate.AnnuityAccrualDatesVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityFixedRatesVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityNotionalsVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityPaymentAmountsVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityPaymentFractionsVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityPaymentTimesVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondSecurity;
import com.opengamma.analytics.financial.interestrate.bond.provider.BondDiscountFactorsVisitor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.core.position.Trade;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.fixedincome.FixedSwapLegDetails;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.Pair;

/**
 * Returns cash-flow information for the asset of a bond total return swap.
 */
public class BondTotalReturnSwapAssetLegDetailsFunction extends BondTotalReturnSwapFunction {

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#BOND_DETAILS}.
   */
  public BondTotalReturnSwapAssetLegDetailsFunction() {
    super(BOND_DETAILS);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new BondTotalReturnSwapCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), false) {

      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final ValueProperties properties = desiredValue.getConstraints();
        final ZonedDateTime now = ZonedDateTime.now(executionContext.getValuationClock());
        final Trade trade = target.getTrade();
        final ValueSpecification spec = new ValueSpecification(BOND_DETAILS, target.toSpecification(), properties);
        final BondTotalReturnSwapDefinition trsDefinition = (BondTotalReturnSwapDefinition) getTargetToDefinitionConverter(context).convert(trade);
        final IssuerProviderInterface issuerCurves = (IssuerProviderInterface) inputs.getValue(CURVE_BUNDLE);
        final BondFixedSecurityDefinition bondDefinition = (BondFixedSecurityDefinition) trsDefinition.getAsset();
        final BondSecurity<? extends Payment, ? extends Coupon> bondDerivative = bondDefinition.toDerivative(now);
        final AnnuityDefinition<? extends CouponDefinition> couponDefinitions = bondDefinition.getCoupons();
        final Annuity<? extends Payment> couponDerivatives = couponDefinitions.toDerivative(now);
        final CurrencyAmount[] notionals = couponDefinitions.accept(AnnuityNotionalsVisitor.getInstance(), now);
        final Pair<LocalDate[], LocalDate[]> accrualDates = couponDefinitions.accept(AnnuityAccrualDatesVisitor.getInstance(), now);
        final double[] paymentTimes = couponDerivatives.accept(AnnuityPaymentTimesVisitor.getInstance());
        final double[] paymentFractions = couponDerivatives.accept(AnnuityPaymentFractionsVisitor.getInstance());
        final CurrencyAmount[] paymentAmounts = couponDerivatives.accept(AnnuityPaymentAmountsVisitor.getInstance());
        final Double[] fixedRates = couponDerivatives.accept(AnnuityFixedRatesVisitor.getInstance());
        final double[] discountFactors = bondDerivative.accept(BondDiscountFactorsVisitor.getInstance(), issuerCurves);
        final FixedSwapLegDetails details = new FixedSwapLegDetails(accrualDates.getFirst(), accrualDates.getSecond(), discountFactors, paymentTimes, paymentFractions, paymentAmounts,
            notionals, fixedRates);
        return Collections.singleton(new ComputedValue(spec, details));
      }

      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues,
          final InstrumentDerivative derivative, final FXMatrix fxMatrix) {
        throw new IllegalStateException("Should never reach this method");
      }
    };
  }
}
