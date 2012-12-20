/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.cashflow;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.time.calendar.LocalDate;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.BondSecurityConverter;
import com.opengamma.financial.analytics.conversion.CashSecurityConverter;
import com.opengamma.financial.analytics.conversion.FRASecurityConverter;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.ForexSecurityConverter;
import com.opengamma.financial.analytics.conversion.InterestRateFutureSecurityConverter;
import com.opengamma.financial.analytics.conversion.SwapSecurityConverter;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.currency.ConfigDBCurrencyPairsSource;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public abstract class FixedCashFlowFunction extends AbstractFunction.NonCompiledInvoker {
  private FinancialSecurityVisitor<InstrumentDefinition<?>> _visitor;
  private FixedIncomeConverterDataProvider _definitionConverter;
  private final String _valueRequirementName;
  private final InstrumentDefinitionVisitor<DoubleTimeSeries<LocalDate>, Map<LocalDate, MultipleCurrencyAmount>> _cashFlowVisitor;
  
  public FixedCashFlowFunction(final String valueRequirementName, InstrumentDefinitionVisitor<DoubleTimeSeries<LocalDate>, Map<LocalDate, MultipleCurrencyAmount>> cashFlowVisitor) {
    ArgumentChecker.notNull(valueRequirementName, "value requirement name");
    ArgumentChecker.notNull(cashFlowVisitor, "cash-flow visitor");
    _valueRequirementName = valueRequirementName;
    _cashFlowVisitor = cashFlowVisitor;
  }
  
  @Override
  public void init(final FunctionCompilationContext context) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final ConfigSource configSource = OpenGammaCompilationContext.getConfigSource(context);
    final HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final ConfigDBCurrencyPairsSource currencyPairsSource = new ConfigDBCurrencyPairsSource(configSource);
    final CurrencyPairs baseQuotePairs = currencyPairsSource.getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    final CashSecurityConverter cashConverter = new CashSecurityConverter(holidaySource, regionSource);
    final FRASecurityConverter fraConverter = new FRASecurityConverter(holidaySource, regionSource, conventionSource);
    final SwapSecurityConverter swapConverter = new SwapSecurityConverter(holidaySource, conventionSource, regionSource, false);
    final BondSecurityConverter bondConverter = new BondSecurityConverter(holidaySource, conventionSource, regionSource);
    final InterestRateFutureSecurityConverter irFutureConverter = new InterestRateFutureSecurityConverter(holidaySource, conventionSource, regionSource);
    final ForexSecurityConverter fxConverter = new ForexSecurityConverter(baseQuotePairs);
    _visitor = FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder().cashSecurityVisitor(cashConverter).fraSecurityVisitor(fraConverter)
        .swapSecurityVisitor(swapConverter).interestRateFutureSecurityVisitor(irFutureConverter).bondSecurityVisitor(bondConverter).fxForwardVisitor(fxConverter)
        .nonDeliverableFxForwardVisitor(fxConverter).create();
    _definitionConverter = new FixedIncomeConverterDataProvider(conventionSource, timeSeriesResolver);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final InstrumentDefinition<?> definition = ((FinancialSecurity) target.getSecurity()).accept(_visitor);
    final Map<LocalDate, MultipleCurrencyAmount> cashFlows;
    if (inputs.getAllValues().isEmpty()) {
      cashFlows = new TreeMap<LocalDate, MultipleCurrencyAmount>(definition.accept(_cashFlowVisitor));
    } else {
      HistoricalTimeSeries fixingSeries = (HistoricalTimeSeries) Iterables.getOnlyElement(inputs.getAllValues()).getValue();
      if (fixingSeries == null) {
        cashFlows = new TreeMap<LocalDate, MultipleCurrencyAmount>(definition.accept(_cashFlowVisitor));        
      } else {
        cashFlows = new TreeMap<LocalDate, MultipleCurrencyAmount>(definition.accept(_cashFlowVisitor, fixingSeries.getTimeSeries()));
      }
    }
    return Collections.singleton(new ComputedValue(new ValueSpecification(_valueRequirementName, target.toSpecification(), createValueProperties()
        .get()), new FixedPaymentMatrix(cashFlows)));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() == ComputationTargetType.SECURITY) {
      return true;
    }
    return target.getSecurity() instanceof FinancialSecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(_valueRequirementName, target.toSpecification(), createValueProperties().get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    InstrumentDefinition<?> definition = security.accept(_visitor);
    return _definitionConverter.getConversionTimeSeriesRequirements(security, definition);
  }
}
