/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model;

import static com.opengamma.engine.value.ValueRequirementNames.MARGIN_PRICE;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.MarginPriceVisitor;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
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
import com.opengamma.financial.analytics.conversion.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.conversion.FutureTradeConverter;
import com.opengamma.financial.analytics.conversion.InterestRateFutureOptionSecurityConverter;
import com.opengamma.financial.analytics.conversion.InterestRateFutureOptionTradeConverter;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Provides the reference margin price,
 * for futures, options and other exchange traded securities that are margined
 */
public class MarginPriceFunction extends AbstractFunction {
  //TODO move from this package
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(MarginPriceFunction.class);
  /** The margin price calculator */
  private static final MarginPriceVisitor s_priceVisitor = MarginPriceVisitor.getInstance();

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    final SecuritySource securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionBundleSource = OpenGammaCompilationContext.getConventionBundleSource(context); // TODO [PLAT-5966] Remove
    final ConventionSource conventionSource = OpenGammaCompilationContext.getConventionSource(context);
    final HistoricalTimeSeriesResolver timeSeriesResolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final InterestRateFutureOptionSecurityConverter irFutureOptionConverter = new InterestRateFutureOptionSecurityConverter(holidaySource, conventionSource, regionSource, securitySource);
    final InterestRateFutureOptionTradeConverter optionTradeToTxnDefnConverter = new InterestRateFutureOptionTradeConverter(irFutureOptionConverter);
    final FutureTradeConverter futureTradeConverter = new FutureTradeConverter();
    final FixedIncomeConverterDataProvider definitionConverter = new FixedIncomeConverterDataProvider(conventionBundleSource, securitySource, timeSeriesResolver);

    return new AbstractInvokingCompiledFunction() {

      @Override
      public final Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
        final Clock snapshotClock = executionContext.getValuationClock();
        final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final Trade trade = target.getTrade();
        final Security security = trade.getSecurity();
        final InstrumentDefinition<?> definition;
        if (security instanceof IRFutureOptionSecurity) {
          definition = optionTradeToTxnDefnConverter.convert(trade);  
        } else {
          definition = futureTradeConverter.convert(trade);  
        }
        final HistoricalTimeSeriesBundle timeSeries = HistoricalTimeSeriesFunctionUtils.getHistoricalTimeSeriesInputs(executionContext, inputs);
        final InstrumentDerivative derivative = definitionConverter.convert(security, definition, now, timeSeries);
        final Double price = derivative.accept(s_priceVisitor);
        final ValueSpecification spec = new ValueSpecification(MARGIN_PRICE, target.toSpecification(), desiredValue.getConstraints().copy().get());
        return Collections.singleton(new ComputedValue(spec, price));
      }

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.TRADE;
      }

      @Override
      public boolean canApplyTo(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
        final Security security = target.getTrade().getSecurity();
        return security instanceof IRFutureOptionSecurity ||
            security instanceof InterestRateFutureSecurity;
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
        return Collections.singleton(new ValueSpecification(MARGIN_PRICE, target.toSpecification(), createValueProperties().get()));
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext compilationContext, final ComputationTarget target, final ValueRequirement desiredValue) {
        try {
          final Trade trade = target.getTrade();
          final Security security = trade.getSecurity();
          Set<ValueRequirement> tsRequirements = null;
          if (security instanceof IRFutureOptionSecurity) {
            tsRequirements = definitionConverter.getConversionTimeSeriesRequirements(security, optionTradeToTxnDefnConverter.convert(trade));
          } else {
            tsRequirements = definitionConverter.getConversionTimeSeriesRequirements(security, futureTradeConverter.convert(trade));
          }
          if (tsRequirements == null) {
            return null;
          }
          return tsRequirements;
        } catch (final Exception e) {
          s_logger.error(e.getMessage());
          return null;
        }
      }
    };
  }
}
