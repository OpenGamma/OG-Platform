/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

import static com.opengamma.sesame.config.ConfigBuilder.column;
import static com.opengamma.sesame.config.ConfigBuilder.configureView;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Set;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.MoreExecutors;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.sesame.EngineTestUtils;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.cache.NoOpCacheInvalidator;
import com.opengamma.sesame.config.FunctionModelConfig;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.function.AvailableImplementationsImpl;
import com.opengamma.sesame.function.AvailableOutputsImpl;
import com.opengamma.sesame.function.Output;
import com.opengamma.sesame.graph.FunctionBuilder;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.sesame.marketdata.MarketDataRequirement;
import com.opengamma.sesame.marketdata.RawId;
import com.opengamma.sesame.marketdata.SingleValueRequirement;
import com.opengamma.sesame.marketdata.TimeSeriesRequirement;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.LocalDateRange;

@Test(groups = TestGroup.UNIT)
public class ViewTest {

  private static final String FOO = "Foo";
  private static final RawId<Double> ID1 = RawId.of(ExternalIdBundle.of("s", "1"));
  private static final RawId<Double> ID2 = RawId.of(ExternalIdBundle.of("s", "2"));
  private static final RawId<Double> ID3 = RawId.of(ExternalIdBundle.of("s", "3"));
  private static final LocalDateRange DATE_RANGE1 =
      LocalDateRange.of(
          LocalDate.of(2011, 3, 8),
          LocalDate.of(2012, 3, 7),
          true);
  private static final LocalDateRange DATE_RANGE2 =
      LocalDateRange.of(
          LocalDate.of(2010, 4, 8),
          LocalDate.of(2013, 6, 7),
          true);

  @BeforeMethod
  public void setUp() throws Exception {
    ThreadLocalServiceContext.init(ServiceContext.of(ImmutableMap.<Class<?>, Object>of()));
  }

  public void gatherRequirements() {
    ViewConfig config = configureView("test view", column(FOO));
    View view = view(config);
    CalculationArguments calculationArguments = CalculationArguments.builder().valuationTime(ZonedDateTime.now()).build();
    Set<MarketDataRequirement> requirements =
        view.gatherRequirements(
            MarketDataEnvironmentBuilder.empty(),
            calculationArguments,
            ImmutableList.of("a string"));

    ImmutableSet<? extends MarketDataRequirement> expected =
        ImmutableSet.of(
            SingleValueRequirement.of(ID1),
            TimeSeriesRequirement.of(ID2, DATE_RANGE1),
            TimeSeriesRequirement.of(ID3, DATE_RANGE2));

    assertEquals(expected, requirements);
  }

  public void gatherRequirementsWithSuppliedData() {
    ViewConfig config = configureView("test view", column(FOO));
    View view = view(config);
    ZonedDateTime valuationTime = ZonedDateTime.now();
    CalculationArguments calculationArguments = CalculationArguments.builder().valuationTime(valuationTime).build();

    // this satisfies the requirement in the function
    LocalDateDoubleTimeSeries timeSeries1 =
        ImmutableLocalDateDoubleTimeSeries.builder()
            .put(DATE_RANGE1.getStartDateInclusive(), 1d)
            .put(DATE_RANGE1.getEndDateInclusive(), 2d)
            .build();

    // This shouldn't satisfy the requirement in the function because the date range is different but it does
    // See the comment in MapMarketDataBundle.get(id, type, dateRange) and PLT-633
    LocalDateDoubleTimeSeries timeSeries2 =
        ImmutableLocalDateDoubleTimeSeries.builder()
            .put(DATE_RANGE2.getStartDateInclusive(), 1d)
            .put(DATE_RANGE2.getEndDateInclusive().minusDays(1), 2d)
            .build();

    MarketDataEnvironment suppliedData =
        new MarketDataEnvironmentBuilder()
            .add(ID1, 1d)
            .add(ID2, timeSeries1)
            .add(ID3, timeSeries2)
            .valuationTime(valuationTime)
            .build();

    Set<MarketDataRequirement> requirements =
        view.gatherRequirements(
            suppliedData,
            calculationArguments,
            ImmutableList.of("a string"));

    ImmutableSet<? extends MarketDataRequirement> expected = ImmutableSet.of();
    // this should be the correct behaviour but currently isn't because of PLT-633
    //ImmutableSet<? extends MarketDataRequirement> expected = ImmutableSet.of(TimeSeriesRequirement.of(ID3, DATE_RANGE2));

    assertEquals(expected, requirements);
  }

  private static View view(ViewConfig config) {
    ImmutableSet<Class<?>> inputTypes = ImmutableSet.<Class<?>>of(String.class);
    AvailableOutputsImpl availableOutputs = new AvailableOutputsImpl(inputTypes);
    availableOutputs.register(Fn1.class);
    AvailableImplementationsImpl availableImplementations = new AvailableImplementationsImpl();
    availableImplementations.register(Impl1.class, Impl2.class);

    return new View(
        config,
        MoreExecutors.sameThreadExecutor(),
        FunctionModelConfig.EMPTY,
        new FunctionBuilder(),
        FunctionService.NONE,
        ComponentMap.EMPTY,
        inputTypes,
        availableOutputs,
        availableImplementations,
        EngineTestUtils.createCacheProvider(),
        CacheBuilder.newBuilder(),
        new NoOpCacheInvalidator(),
        Optional.<MetricRegistry>absent()
    );
  }

  public interface Fn1 {

    @Output(FOO)
    Object foo(Environment env, String s);
  }

  public static class Impl1 implements Fn1 {

    private final Fn2 _fn2;

    public Impl1(Fn2 fn2) {
      _fn2 = fn2;
    }

    @Override
    public Object foo(Environment env, String s) {
      env.getMarketDataBundle().get(ID1, Double.class);
      return _fn2.bar(env);
    }
  }

  public interface Fn2 {

    String bar(Environment env);
  }

  public static class Impl2 implements Fn2 {

    @Override
    public String bar(Environment env) {
      env.getMarketDataBundle().get(ID2, Double.class, DATE_RANGE1);
      env.getMarketDataBundle().get(ID3, Double.class, DATE_RANGE2);
      return "BAR";
    }
  }
}
