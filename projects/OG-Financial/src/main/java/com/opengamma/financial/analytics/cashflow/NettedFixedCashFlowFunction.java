/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.cashflow;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.DateTimeException;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.NettedFixedCashFlowFromDateCalculator;
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
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
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
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *
 */
public class NettedFixedCashFlowFunction extends AbstractFunction {

  /** Property name for the date field */
  public static final String PROPERTY_DATE = "Date";
  private static final Logger s_logger = LoggerFactory.getLogger(NettedFixedCashFlowFunction.class);
  private static final NettedFixedCashFlowFromDateCalculator NETTING_CASH_FLOW_CALCULATOR = NettedFixedCashFlowFromDateCalculator.getInstance();

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
   * The compiled form.
   */
  protected class Compiled extends AbstractInvokingCompiledFunction {

    private final FinancialSecurityVisitor<InstrumentDefinition<?>> _visitor;
    private final FixedIncomeConverterDataProvider _definitionConverter;

    public Compiled(final FinancialSecurityVisitor<InstrumentDefinition<?>> visitor, final FixedIncomeConverterDataProvider definitionConverter) {
      _visitor = visitor;
      _definitionConverter = definitionConverter;
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      final ValueProperties properties = createValueProperties().withAny(PROPERTY_DATE).get();
      return Collections.singleton(new ValueSpecification(ValueRequirementNames.NETTED_FIXED_CASH_FLOWS, target.toSpecification(), properties));
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      final ValueProperties constraints = desiredValue.getConstraints();
      if (!OpenGammaCompilationContext.isPermissive(context)) {
        final String date = constraints.getStrictValue(PROPERTY_DATE);
        if (date == null) {
          s_logger.error("Must supply a date from which to calculate the cash-flows");
          return null;
        }
        try {
          LocalDate.parse(date);
        } catch (final DateTimeException e) {
          s_logger.error("Could not parse date {} - must be in form YYYY-MM-DD", date);
          return null;
        }
      }
      final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
      final InstrumentDefinition<?> definition = security.accept(_visitor);
      return _definitionConverter.getConversionTimeSeriesRequirements(security, definition);
    }

    // CompiledFunctionDefinition

    @Override
    public ComputationTargetType getTargetType() {
      return FinancialSecurityTypes.FINANCIAL_SECURITY;
    }

    // FunctionInvoker

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
      final InstrumentDefinition<?> definition = ((FinancialSecurity) target.getSecurity()).accept(_visitor);
      final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
      final ValueProperties properties = desiredValue.getConstraints();
      final String dateString = properties.getSingleValue(PROPERTY_DATE);
      final LocalDate date = (dateString != null) ? LocalDate.parse(dateString) : LocalDate.now(executionContext.getValuationClock());
      final Map<LocalDate, MultipleCurrencyAmount> cashFlows;
      if (inputs.getAllValues().isEmpty()) {
        cashFlows = NETTING_CASH_FLOW_CALCULATOR.getCashFlows(definition, date);
      } else {
        final HistoricalTimeSeries fixingSeries = (HistoricalTimeSeries) Iterables.getOnlyElement(inputs.getAllValues()).getValue();
        if (fixingSeries == null) {
          cashFlows = NETTING_CASH_FLOW_CALCULATOR.getCashFlows(definition, date);
        } else {
          cashFlows = NETTING_CASH_FLOW_CALCULATOR.getCashFlows(definition, fixingSeries.getTimeSeries(), date);
        }
      }
      return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.NETTED_FIXED_CASH_FLOWS, target.toSpecification(), properties), new FixedPaymentMatrix(
          cashFlows)));
    }

  }

}
