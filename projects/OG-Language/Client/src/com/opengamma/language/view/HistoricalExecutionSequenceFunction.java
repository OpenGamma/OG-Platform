/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.time.Instant;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.view.execution.ArbitraryViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionSequence;
import com.opengamma.language.async.AsynchronousExecution;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.DefinitionAnnotater;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.function.AbstractFunctionInvoker;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.function.PublishedFunction;
import com.opengamma.util.ArgumentChecker;

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
        new MetaParameter("sample_period", JavaTypeInfo.builder(Integer.class).allowNull().get()),
        new MetaParameter("ts_resolver_key", JavaTypeInfo.builder(String.class).allowNull().get()),
        new MetaParameter("ts_field_resolver_key", JavaTypeInfo.builder(String.class).allowNull().get()));
  }
  
  private HistoricalExecutionSequenceFunction() {
    this(new DefinitionAnnotater(HistoricalExecutionSequenceFunction.class));
  }
  
  protected HistoricalExecutionSequenceFunction(final DefinitionAnnotater info) {
    super(info.annotate(parameters()));
    _meta = info.annotate(new MetaFunction(Categories.VIEW, "HistoricalExecutionSequence", getParameters(), this));
  }
  
  public static ViewCycleExecutionSequence generate(Instant from, Instant to, Integer samplePeriodSeconds, String timeSeriesResolverKey, String timeSeriesFieldResolverKey) {
    ArgumentChecker.notNull(from, "from");
    ArgumentChecker.notNull(to, "to");
    if (samplePeriodSeconds == null) {
      samplePeriodSeconds = DEFAULT_SAMPLE_PERIOD_SECONDS;
    }
    final Collection<ViewCycleExecutionOptions> cycles = new ArrayList<ViewCycleExecutionOptions>(
        ((int) (to.getEpochSeconds() - from.getEpochSeconds()) + samplePeriodSeconds - 1) / samplePeriodSeconds);
    for (Instant valuationTime = from; !valuationTime.isAfter(to); valuationTime = valuationTime.plus(samplePeriodSeconds, TimeUnit.SECONDS)) {
      final ViewCycleExecutionOptions options = new ViewCycleExecutionOptions(valuationTime);
      options.setMarketDataSpecification(MarketData.historical(ZonedDateTime.ofInstant(valuationTime, TimeZone.UTC).toLocalDate(), timeSeriesResolverKey, timeSeriesFieldResolverKey));
      cycles.add(options);
    }
    ArbitraryViewCycleExecutionSequence executionSequence = new ArbitraryViewCycleExecutionSequence(cycles);
    return executionSequence;
  }
  
  @Override
  protected Object invokeImpl(SessionContext sessionContext, Object[] parameters) throws AsynchronousExecution {
    return generate((Instant) parameters[0], (Instant) parameters[1], (Integer) parameters[2], (String) parameters[3], (String) parameters[4]);
  }
  
  @Override
  public MetaFunction getMetaFunction() {
    return _meta;
  }

}
