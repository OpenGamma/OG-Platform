/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;

import com.opengamma.analytics.financial.instrument.FixedReceiveCashFlowVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.BondSecurityConverter;
import com.opengamma.financial.analytics.conversion.CashSecurityConverter;
import com.opengamma.financial.analytics.conversion.FRASecurityConverter;
import com.opengamma.financial.analytics.conversion.ForexSecurityConverter;
import com.opengamma.financial.analytics.conversion.InterestRateFutureSecurityConverter;
import com.opengamma.financial.analytics.conversion.SwapSecurityConverter;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.currency.ConfigDBCurrencyPairsSource;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *
 */
public class FixedReceiveCashFlowFunction extends AbstractFunction.NonCompiledInvoker {
  private static final FixedReceiveCashFlowVisitor FIXED_CASH_FLOW_CALCULATOR = FixedReceiveCashFlowVisitor.getInstance();
  private FinancialSecurityVisitor<InstrumentDefinition<?>> _visitor;

  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final ConfigDBCurrencyPairsSource currencyPairsSource = new ConfigDBCurrencyPairsSource(configSource);
    final CurrencyPairs baseQuotePairs = currencyPairsSource.getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    final CashSecurityConverter cashConverter = new CashSecurityConverter(holidaySource, regionSource);
    final FRASecurityConverter fraConverter = new FRASecurityConverter(holidaySource, regionSource, conventionSource);
    final SwapSecurityConverter swapConverter = new SwapSecurityConverter(holidaySource, conventionSource, regionSource, false);
    final BondSecurityConverter bondConverter = new BondSecurityConverter(holidaySource, conventionSource, regionSource);
    final InterestRateFutureSecurityConverter irFutureConverter = new InterestRateFutureSecurityConverter(holidaySource, conventionSource, regionSource);
    final ForexSecurityConverter fxConverter = new ForexSecurityConverter(baseQuotePairs);
    _visitor = FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder()
        .cashSecurityVisitor(cashConverter)
        .fraSecurityVisitor(fraConverter)
        .swapSecurityVisitor(swapConverter)
        .interestRateFutureSecurityVisitor(irFutureConverter)
        .bondSecurityVisitor(bondConverter)
        .fxForwardVisitor(fxConverter)
        .nonDeliverableFxForwardVisitor(fxConverter)
        .create();
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final Clock snapshotClock = executionContext.getValuationClock();
    final LocalDate date = snapshotClock.zonedDateTime().toLocalDate();
    final InstrumentDefinition<?> definition = ((FinancialSecurity) target.getSecurity()).accept(_visitor);
    final Map<LocalDate, MultipleCurrencyAmount> cashFlows = definition.accept(FIXED_CASH_FLOW_CALCULATOR, date);
    return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.FIXED_RECEIVE_CASH_FLOWS, target.toSpecification(), createValueProperties().get()),
        new PaymentScheduleMatrix(cashFlows)));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.FINANCIAL_SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.FIXED_RECEIVE_CASH_FLOWS, target.toSpecification(), createValueProperties().get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    return Collections.emptySet();
  }

}
