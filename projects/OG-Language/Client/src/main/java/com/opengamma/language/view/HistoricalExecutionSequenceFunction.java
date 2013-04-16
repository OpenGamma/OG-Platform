/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import static org.threeten.bp.temporal.ChronoUnit.SECONDS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.engine.marketdata.spec.HistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.view.execution.ArbitraryViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionSequence;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Generates an execution sequence that causes historical market data to be used.
 */
public class HistoricalExecutionSequenceFunction extends AbstractFunctionInvoker implements PublishedFunction {

  /**
   * Default instance.
   */
  public static final HistoricalExecutionSequenceFunction INSTANCE = new HistoricalExecutionSequenceFunction();

  private static final int DEFAULT_SAMPLE_PERIOD_SECONDS = 86400;

  private final MetaFunction _meta;

  private static List<MetaParameter> parameters() {
    return Arrays.asList(
        new MetaParameter("from", JavaTypeInfo.builder(Instant.class).get()),
        new MetaParameter("to", JavaTypeInfo.builder(Instant.class).get()),
        new MetaParameter("samplePeriod", JavaTypeInfo.builder(Integer.class).allowNull().get()),
        new MetaParameter("timeSeriesResolver", JavaTypeInfo.builder(String.class).allowNull().get()));
  }

  private HistoricalExecutionSequenceFunction() {
    this(new DefinitionAnnotater(HistoricalExecutionSequenceFunction.class));
  }

  protected HistoricalExecutionSequenceFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.VIEW, "HistoricalExecutionSequence", getParameters(), this));
  }

  public static ViewCycleExecutionSequence generate(final Instant from, final Instant to, Integer samplePeriodSeconds, final String timeSeriesResolverKey, final String timeSeriesFieldResolverKey) {
    ArgumentChecker.notNull(from, "from");
    ArgumentChecker.notNull(to, "to");
    if (samplePeriodSeconds == null) {
      samplePeriodSeconds = DEFAULT_SAMPLE_PERIOD_SECONDS;
    }
    final Collection<ViewCycleExecutionOptions> cycles = new ArrayList<ViewCycleExecutionOptions>(
        ((int) (to.getEpochSecond() - from.getEpochSecond()) + samplePeriodSeconds - 1) / samplePeriodSeconds);
    for (Instant valuationTime = from; !valuationTime.isAfter(to); valuationTime = valuationTime.plus(samplePeriodSeconds, SECONDS)) {
      final LocalDate date = ZonedDateTime.ofInstant(valuationTime, ZoneOffset.UTC).toLocalDate();
      final HistoricalMarketDataSpecification spec = MarketData.historical(date, timeSeriesResolverKey);
      final ViewCycleExecutionOptions options = ViewCycleExecutionOptions.builder().setValuationTime(valuationTime).setMarketDataSpecification(spec).create();
      cycles.add(options);
    }
    return new ArbitraryViewCycleExecutionSequence(cycles);
  }

  @Override
  protected Object invokeImpl(final SessionContext sessionContext, final Object[] parameters) throws AsynchronousExecution {
    return generate((Instant) parameters[0], (Instant) parameters[1], (Integer) parameters[2], (String) parameters[3], (String) parameters[4]);
  }

  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
