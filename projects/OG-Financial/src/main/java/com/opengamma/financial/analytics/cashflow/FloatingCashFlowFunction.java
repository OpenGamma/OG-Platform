/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.cashflow;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.conversion.BondSecurityConverter;
import com.opengamma.financial.analytics.conversion.CashSecurityConverter;
import com.opengamma.financial.analytics.conversion.FRASecurityConverterDeprecated;
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.ForexSecurityConverter;
import com.opengamma.financial.analytics.conversion.InterestRateFutureSecurityConverterDeprecated;
import com.opengamma.financial.analytics.conversion.SwapSecurityConverterDeprecated;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
public abstract class FloatingCashFlowFunction extends AbstractFunction {

  private final String _valueRequirementName;
  private final InstrumentDefinitionVisitor<Object, Map<LocalDate, MultipleCurrencyAmount>> _cashFlowVisitor;

  public FloatingCashFlowFunction(final String valueRequirementName, final InstrumentDefinitionVisitor<Object, Map<LocalDate, MultipleCurrencyAmount>> cashFlowVisitor) {
    ArgumentChecker.notNull(valueRequirementName, "value requirement names");
    ArgumentChecker.notNull(cashFlowVisitor, "cash-flow visitor");
    _valueRequirementName = valueRequirementName;
    _cashFlowVisitor = cashFlowVisitor;
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context); // TODO [PLAT-5966] Remove
    final HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final CurrencyPairs baseQuotePairs = OpenGammaCompilationContext.getCurrencyPairsSource(context).getCurrencyPairs(CurrencyPairs.DEFAULT_CURRENCY_PAIRS);
    final CashSecurityConverter cashConverter = new CashSecurityConverter(holidaySource, regionSource);
    final FRASecurityConverterDeprecated fraConverter = new FRASecurityConverterDeprecated(holidaySource, regionSource, conventionSource);
    final SwapSecurityConverterDeprecated swapConverter = new SwapSecurityConverterDeprecated(holidaySource, conventionSource, regionSource, false);
    final BondSecurityConverter bondConverter = new BondSecurityConverter(holidaySource, conventionSource, regionSource);
    final InterestRateFutureSecurityConverterDeprecated irFutureConverter = new InterestRateFutureSecurityConverterDeprecated(holidaySource, conventionSource, regionSource);
    final ForexSecurityConverter fxConverter = new ForexSecurityConverter(baseQuotePairs);
    return new Compiled(FinancialSecurityVisitorAdapter.<InstrumentDefinition<?>>builder().cashSecurityVisitor(cashConverter).fraSecurityVisitor(fraConverter)
        .swapSecurityVisitor(swapConverter).interestRateFutureSecurityVisitor(irFutureConverter).bondSecurityVisitor(bondConverter).fxForwardVisitor(fxConverter)
        .nonDeliverableFxForwardVisitor(fxConverter).create(), new FixedIncomeConverterDataProvider(conventionSource, securitySource, timeSeriesResolver));
  }

  /**
   * The compiled form of the function.
   */
  protected class Compiled extends AbstractInvokingCompiledFunction {

    private final FinancialSecurityVisitor<InstrumentDefinition<?>> _visitor;
    private final FixedIncomeConverterDataProvider _definitionConverter;

    public Compiled(final FinancialSecurityVisitor<InstrumentDefinition<?>> visitor, final FixedIncomeConverterDataProvider definitionConverter) {
      _visitor = visitor;
      _definitionConverter = definitionConverter;
    }

    // CompiledFunctionDefinition

    @Override
    public ComputationTargetType getTargetType() {
      return FinancialSecurityTypes.FINANCIAL_SECURITY;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      return Collections.singleton(new ValueSpecification(_valueRequirementName, target.toSpecification(), createValueProperties().get()));
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
      final InstrumentDefinition<?> definition = security.accept(_visitor);
      return _definitionConverter.getConversionTimeSeriesRequirements(security, definition);
    }

    // FunctionInvoker

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
        final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
      final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
      final InstrumentDefinition<?> definition = security.accept(_visitor);
      final Map<LocalDate, MultipleCurrencyAmount> cashFlows;
      if (inputs.getAllValues().isEmpty()) {
        cashFlows = new TreeMap<>(definition.accept(_cashFlowVisitor));
      } else {
        final HistoricalTimeSeries fixingSeries = (HistoricalTimeSeries) Iterables.getOnlyElement(inputs.getAllValues()).getValue();
        if (fixingSeries == null) {
          cashFlows = new TreeMap<>(definition.accept(_cashFlowVisitor));
        } else {
          cashFlows = new TreeMap<>(definition.accept(_cashFlowVisitor, fixingSeries.getTimeSeries()));
        }
      }
      final String label = security.accept(CashFlowFunctionHelper.getReferenceIndexVisitor());
      final Map<LocalDate, List<Pair<CurrencyAmount, String>>> result = Maps.newHashMap();
      for (final Map.Entry<LocalDate, MultipleCurrencyAmount> entry : cashFlows.entrySet()) {
        final MultipleCurrencyAmount mca = entry.getValue();
        final List<Pair<CurrencyAmount, String>> list = Lists.newArrayListWithCapacity(mca.size());
        for (final CurrencyAmount ca : mca) {
          list.add(Pairs.of(ca, label));
        }
        result.put(entry.getKey(), list);
      }
      return Collections.singleton(new ComputedValue(new ValueSpecification(_valueRequirementName, target.toSpecification(), createValueProperties()
          .get()), new FloatingPaymentMatrix(result)));
    }

  }

}
