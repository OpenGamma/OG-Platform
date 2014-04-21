/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.trs;

import static com.opengamma.engine.value.ValueRequirementNames.FUNDING_LEG_DETAILS;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondTotalReturnSwapDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.interestrate.AnnuityFixedRatesVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityPaymentAmountsVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityPaymentFractionsVisitor;
import com.opengamma.analytics.financial.interestrate.AnnuityPaymentTimesVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondTotalReturnSwap;
import com.opengamma.analytics.financial.interestrate.cashflow.AnnuityAccrualDatesVisitor;
import com.opengamma.analytics.financial.interestrate.cashflow.AnnuityFixingDatesVisitor;
import com.opengamma.analytics.financial.interestrate.cashflow.AnnuityFixingYearFractionsVisitor;
import com.opengamma.analytics.financial.interestrate.cashflow.AnnuityGearingsVisitor;
import com.opengamma.analytics.financial.interestrate.cashflow.AnnuityIndexTenorsVisitor;
import com.opengamma.analytics.financial.interestrate.cashflow.AnnuityNotionalsVisitor;
import com.opengamma.analytics.financial.interestrate.cashflow.AnnuityPaymentDatesVisitor;
import com.opengamma.analytics.financial.interestrate.cashflow.AnnuitySpreadsVisitor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.provider.AnnuityDiscountFactorsVisitor;
import com.opengamma.analytics.financial.interestrate.swap.provider.AnnuityForwardRatesVisitor;
import com.opengamma.analytics.financial.interestrate.swap.provider.AnnuityProjectedPaymentsVisitor;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.SecuritySource;
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
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.model.fixedincome.FloatingSwapLegDetails;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.security.swap.BondTotalReturnSwapSecurity;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * Produces information about the cash-flows of the funding leg of a bond total return swap.
 */
public class BondTotalReturnSwapFundingLegDetailsFunction extends BondTotalReturnSwapFunction {

  /**
   * Sets the value requirement to {@link ValueRequirementNames#FUNDING_LEG_DETAILS}.
   */
  public BondTotalReturnSwapFundingLegDetailsFunction() {
    super(FUNDING_LEG_DETAILS);
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
        final HistoricalTimeSeriesBundle timeSeries = HistoricalTimeSeriesFunctionUtils.getHistoricalTimeSeriesInputs(executionContext, inputs);
        final Trade trade = target.getTrade();
        final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(executionContext);
        final IssuerProviderInterface issuerCurves = getMergedWithIssuerProviders(inputs, getFXMatrix(inputs, target, securitySource));
        final ValueSpecification spec = new ValueSpecification(FUNDING_LEG_DETAILS, target.toSpecification(), properties);
        final BondTotalReturnSwapDefinition trsDefinition = (BondTotalReturnSwapDefinition) getTargetToDefinitionConverter(context).convert(trade);
        final AnnuityDefinition<? extends PaymentDefinition> definition = trsDefinition.getFundingLeg();
        final Annuity<? extends Payment> derivative = ((BondTotalReturnSwap) getDerivative(target, now, timeSeries, trsDefinition)).getFundingLeg();
        final LocalDate localDate = now.toLocalDate();
        final CurrencyAmount[] notionals = definition.accept(AnnuityNotionalsVisitor.getInstance(), localDate);
        final Pair<LocalDate[], LocalDate[]> accrualDates = definition.accept(AnnuityAccrualDatesVisitor.getInstance(), localDate);
        final double[] paymentTimes = derivative.accept(AnnuityPaymentTimesVisitor.getInstance());
        final double[] paymentFractions = derivative.accept(AnnuityPaymentFractionsVisitor.getInstance());
        final CurrencyAmount[] paymentAmounts = derivative.accept(AnnuityPaymentAmountsVisitor.getInstance());
        final Double[] fixedRates = derivative.accept(AnnuityFixedRatesVisitor.getInstance());
        final double[] discountFactors = derivative.accept(AnnuityDiscountFactorsVisitor.getInstance(), issuerCurves.getMulticurveProvider());
        final Pair<LocalDate[], LocalDate[]> fixingDates = definition.accept(AnnuityFixingDatesVisitor.getInstance(), localDate);
        final Double[] fixingYearFractions = definition.accept(AnnuityFixingYearFractionsVisitor.getInstance(), localDate);
        final Double[] forwardRates = derivative.accept(AnnuityForwardRatesVisitor.getInstance(), issuerCurves.getMulticurveProvider());
        final LocalDate[] paymentDates = definition.accept(AnnuityPaymentDatesVisitor.getInstance(), localDate);
        final CurrencyAmount[] projectedAmounts = derivative.accept(AnnuityProjectedPaymentsVisitor.getInstance(), issuerCurves.getMulticurveProvider());
        final double[] spreads = definition.accept(AnnuitySpreadsVisitor.getInstance(), localDate);
        final double[] gearings = definition.accept(AnnuityGearingsVisitor.getInstance(), localDate);
        final Tenor[] indexTenors = definition.accept(AnnuityIndexTenorsVisitor.getInstance(), localDate);
        final FloatingSwapLegDetails details = new FloatingSwapLegDetails(accrualDates.getFirst(), accrualDates.getSecond(), paymentFractions, fixingDates.getFirst(), fixingDates.getSecond(),
            fixingYearFractions, forwardRates, fixedRates, paymentDates, paymentTimes, discountFactors, paymentAmounts, projectedAmounts, notionals, spreads, gearings, indexTenors);
        return Collections.singleton(new ComputedValue(spec, details));
      }

      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative, final FXMatrix fxMatrix) {
        throw new IllegalStateException("Should never reach this code");
      }

      @Override
      protected String getCurrencyOfResult(final BondTotalReturnSwapSecurity security) {
        throw new IllegalStateException("This function does not set the Currency property");
      }

    };

  }

}
