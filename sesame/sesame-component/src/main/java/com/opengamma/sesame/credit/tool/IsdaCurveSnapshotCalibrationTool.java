/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.sesame.credit.tool;

import static com.opengamma.sesame.config.ConfigBuilder.argument;
import static com.opengamma.sesame.config.ConfigBuilder.arguments;
import static com.opengamma.sesame.config.ConfigBuilder.config;
import static com.opengamma.sesame.config.ConfigBuilder.function;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Collections2;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.component.tool.ToolUtils;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.impl.CachedHolidaySource;
import com.opengamma.core.link.SnapshotLink;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.financial.analytics.isda.credit.CreditCurveDataKey;
import com.opengamma.financial.analytics.isda.credit.CreditCurveDataSnapshot;
import com.opengamma.financial.analytics.isda.credit.YieldCurveData;
import com.opengamma.financial.analytics.isda.credit.YieldCurveDataSnapshot;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.VersionCorrection;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.SimpleEnvironment;
import com.opengamma.sesame.cache.CachingProxyDecorator;
import com.opengamma.sesame.config.FunctionModelConfig;
import com.opengamma.sesame.credit.DefaultIsdaCompliantYieldCurveFn;
import com.opengamma.sesame.credit.IsdaCompliantCreditCurveFn;
import com.opengamma.sesame.credit.IsdaCompliantYieldCurveFn;
import com.opengamma.sesame.credit.IsdaCreditCurve;
import com.opengamma.sesame.credit.IsdaYieldCurve;
import com.opengamma.sesame.credit.StandardIsdaCompliantCreditCurveFn;
import com.opengamma.sesame.credit.snapshot.SnapshotCreditCurveDataProviderFn;
import com.opengamma.sesame.credit.snapshot.SnapshotYieldCurveDataProviderFn;
import com.opengamma.sesame.engine.ComponentMap;
import com.opengamma.sesame.engine.DefaultCacheProvider;
import com.opengamma.sesame.engine.FixedInstantVersionCorrectionProvider;
import com.opengamma.sesame.function.AvailableImplementations;
import com.opengamma.sesame.function.AvailableImplementationsImpl;
import com.opengamma.sesame.function.AvailableOutputs;
import com.opengamma.sesame.function.AvailableOutputsImpl;
import com.opengamma.sesame.graph.FunctionModel;
import com.opengamma.sesame.marketdata.MarketDataBundle;
import com.opengamma.sesame.marketdata.MarketDataId;
import com.opengamma.sesame.proxy.ExceptionWrappingProxy;
import com.opengamma.timeseries.date.DateTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Result;
import com.opengamma.util.time.LocalDateRange;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Tests ISDA curve data by calibrating each curve in turn and dumping the result
 * out to the console.
 */
public class IsdaCurveSnapshotCalibrationTool extends AbstractTool<ToolContext> {

  private static final Logger s_logger = LoggerFactory.getLogger(IsdaCurveSnapshotCalibrationTool.class);
  private static final MarketDataBundle s_noOpMarketDataSource = new MarketDataBundle() {
    @Override
    public <T, I extends MarketDataId<T>> Result<T> get(I id, Class<T> dataType) {
      return Result.failure(FailureStatus.ERROR, "Not implemented");
    }

    @Override
    public <T, I extends MarketDataId<T>> Result<DateTimeSeries<LocalDate, T>> get(
        I id,
        Class<T> dataType,
        LocalDateRange dateRange) {
      return Result.failure(FailureStatus.ERROR, "Not implemented");
    }

    @Override
    public MarketDataBundle withTime(ZonedDateTime time) {
      return this;
    }

    @Override
    public MarketDataBundle withDate(LocalDate date) {
      return this;
    }
  };
  
  /**
   * @param args command line args, run with -h for details
   */
  public static void main(String[] args) {
    new IsdaCurveSnapshotCalibrationTool().invokeAndTerminate(args);
  }

  @Override
  protected void doRun() throws Exception {
    
    CommandLine cmdLine = getCommandLine();
    
    String creditSnapshot = cmdLine.getOptionValue("cs");
    String yieldCurveSnapshot = cmdLine.getOptionValue("ys");
    
    //load snapshots
    MarketDataSnapshotSource source = getToolContext().getMarketDataSnapshotSource();
    YieldCurveDataSnapshot ycSnapshot = source.getSingle(YieldCurveDataSnapshot.class, 
                                                         yieldCurveSnapshot, 
                                                         VersionCorrection.LATEST);
    CreditCurveDataSnapshot ccSnapshot = source.getSingle(CreditCurveDataSnapshot.class, 
                                                          creditSnapshot, 
                                                          VersionCorrection.LATEST);
    
    ComponentMap componentMap = getComponentMap();
    
    FunctionModelConfig functionModelConfig = initGraph(ccSnapshot, ycSnapshot, componentMap);
    Cache<Object, Object> cache = buildCache();
    CachingProxyDecorator cachingDecorator = new CachingProxyDecorator(new DefaultCacheProvider(cache));
    
    IsdaCompliantYieldCurveFn ycFn = FunctionModel.build(DefaultIsdaCompliantYieldCurveFn.class, 
                                                         functionModelConfig, 
                                                         componentMap, 
                                                         ExceptionWrappingProxy.INSTANCE, 
                                                         cachingDecorator);
    IsdaCompliantCreditCurveFn ccFn = FunctionModel.build(StandardIsdaCompliantCreditCurveFn.class, 
                                                          functionModelConfig, 
                                                          componentMap, 
                                                          ExceptionWrappingProxy.INSTANCE, 
                                                          cachingDecorator);
    
    Environment env = new SimpleEnvironment(ZonedDateTime.now(), s_noOpMarketDataSource);
    calibrateYieldCurves(env, ycSnapshot, ycFn);
    
    calibrateCreditCurves(env, ccSnapshot, ccFn);
    
  }

  /**
   * Builds the component map from the tool context, overriding the holiday source
   * with a caching wrapper.
   */
  private ComponentMap getComponentMap() {
    ComponentMap componentMap = ComponentMap.loadComponents(getToolContext());
    HolidaySource holidaySource = componentMap.getComponent(HolidaySource.class);
    return componentMap.with(HolidaySource.class, new CachedHolidaySource(holidaySource));
  }


  private void calibrateCreditCurves(Environment env, 
                                     CreditCurveDataSnapshot ccSnapshot, 
                                     IsdaCompliantCreditCurveFn ccFn) {
    Map<CreditCurveDataKey, IsdaCreditCurve> creditCurves = Maps.newHashMap();
    Map<CreditCurveDataKey, Result<IsdaCreditCurve>> creditCurveFailures = Maps.newHashMap();
    int i = 1;
    for (CreditCurveDataKey key : ccSnapshot.getCreditCurves().keySet()) {
      Result<IsdaCreditCurve> curve;
      try {
        curve = ccFn.buildIsdaCompliantCreditCurve(env, key);
      } catch (Exception e) {
        curve = Result.failure(e);
      }
      if (curve.isSuccess()) {
        creditCurves.put(key, curve.getValue());
      } else {
        creditCurveFailures.put(key, curve);
      }
      if (i % 100 == 0) {
        s_logger.info("Calibrated {} credit curves", i);
      }
      i++;
    }
    s_logger.info("Calibrated {} credit curves in total", i);
    
    renderCreditCurves(creditCurves, creditCurveFailures);
  }


  private void calibrateYieldCurves(Environment env, 
                                    YieldCurveDataSnapshot ycSnapshot, 
                                    IsdaCompliantYieldCurveFn ycFn) {
    List<IsdaYieldCurve> yieldCurves = Lists.newArrayList();
    Map<Currency, Result<IsdaYieldCurve>> yieldCurveFailures = Maps.newTreeMap();
    for (Currency ccy : ycSnapshot.getYieldCurves().keySet()) {
      Result<IsdaYieldCurve> curve = ycFn.buildIsdaCompliantCurve(env, ccy);
      if (curve.isSuccess()) {
        yieldCurves.add(curve.getValue());
      } else {
        yieldCurveFailures.put(ccy, curve);
      }
    }
    
    renderYieldCurves(yieldCurves, yieldCurveFailures);
  }


  /**
   * Builds a basic cache.
   */
  private Cache<Object, Object> buildCache() {
    int concurrencyLevel = Runtime.getRuntime().availableProcessors() + 2;
    return CacheBuilder.newBuilder()
        .maximumSize(50000)
        .softValues()
        .concurrencyLevel(concurrencyLevel)
        .build();
  }


  /**
   * Dumps hazard rates for credit curves in a tabular CSV format. Failures are appended at the end.
   */
  private void renderCreditCurves(Map<CreditCurveDataKey, IsdaCreditCurve> creditCurves, 
                                  Map<CreditCurveDataKey, Result<IsdaCreditCurve>> creditCurveFailures) {
    
    List<String> headers = Lists.newArrayList("Curve name", "Currency", "Restructuring", "Seniority", "Yield curve");
    Set<Tenor> allTenors = Sets.newTreeSet();
    for (IsdaCreditCurve curve : creditCurves.values()) {
      allTenors.addAll(curve.getCurveData().getCdsQuotes().keySet());
    }
    
    headers.addAll(Collections2.transform(allTenors, Functions.toStringFunction()));
    
    Map<CreditCurveDataKey, List<String>> rows = Maps.newTreeMap(new Comparator<CreditCurveDataKey>() {

      @Override
      public int compare(CreditCurveDataKey o1, CreditCurveDataKey o2) {
        return ComparisonChain.start()
                              .compare(o1.getCurveName(), o2.getCurveName())
                              .compare(o1.getCurrency(), o2.getCurrency())
                              .compare(o1.getRestructuring().toString(), o2.getRestructuring().toString())
                              .compare(o1.getSeniority().toString(), o2.getSeniority().toString())
                              .result();
      }
    });
    for (Entry<CreditCurveDataKey, IsdaCreditCurve> entry : creditCurves.entrySet()) {
      
      CreditCurveDataKey key = entry.getKey();
      IsdaCreditCurve curve = entry.getValue();
      
      Currency yieldCurveCcy = curve.getYieldCurve().getCurveData().getCurrency();
      
      List<Object> rowObjs = Lists.<Object>newArrayList(key.getCurveName(),
                                                        key.getCurrency(),
                                                        key.getRestructuring(),
                                                        key.getSeniority(),
                                                        yieldCurveCcy);
      
      int i = 0;
      for (Tenor tenor : allTenors) {
        if (curve.getCurveData().getCdsQuotes().containsKey(tenor)) {
          ISDACompliantCreditCurve calibratedCurve = curve.getCalibratedCurve();
          double timeAtI = calibratedCurve.getTimeAtIndex(i);
          double hazardRate = calibratedCurve.getHazardRate(timeAtI);
          rowObjs.add(hazardRate);
          i++;
        } else {
          rowObjs.add("");
        }
      }
      rows.put(key, Lists.transform(rowObjs, Functions.toStringFunction()));
      
    }
    
    renderTable("Credit curve hazard rates", headers, rows.values());
    
    System.out.println("Credit curve failures:");
    for (Entry<CreditCurveDataKey, Result<IsdaCreditCurve>> entry : creditCurveFailures.entrySet()) {
      System.out.format("%s: %s\n", entry.getKey(), entry.getValue());
    }
  }

  /**
   * Renders zero rates for yield curves in a tabular CSV format. Failures are appended to the end.
   */
  private void renderYieldCurves(List<IsdaYieldCurve> yieldCurves, 
                                 Map<Currency, Result<IsdaYieldCurve>> yieldCurveFailures) {
    
    Pair<SortedSet<Tenor>, SortedSet<Tenor>> tenors = getTenors(yieldCurves);
    Set<Tenor> cashTenors = tenors.getFirst();
    Set<Tenor> swapTenors = tenors.getSecond();
    Set<Tenor> allTenors = Sets.newLinkedHashSet(Iterables.concat(cashTenors, swapTenors));
    List<String> headers = Lists.newArrayList("Curve");
    headers.addAll(Collections2.transform(cashTenors, new Function<Object, String>() {

      @Override
      public String apply(Object input) {
        return "C: " + input.toString();
      }
    }));
    headers.addAll(Collections2.transform(swapTenors, new Function<Object, String>() {

      @Override
      public String apply(Object input) {
        return "S: " + input.toString();
      }
    }));
    
    Map<Currency, List<String>> inputRows = Maps.newTreeMap();
    Map<Currency, List<String>> rows = Maps.newTreeMap();
    for (IsdaYieldCurve curve : yieldCurves) {
      
      Currency ccy = curve.getCurveData().getCurrency();
      List<Object> row = Lists.<Object>newArrayList(ccy);
      List<Object> inputRow = Lists.<Object>newArrayList(ccy);
      
      SortedSet<Tenor> cashTerms = curve.getCurveData().getCashData().keySet();
      SortedSet<Tenor> swapTerms = curve.getCurveData().getSwapData().keySet();
      ISDACompliantYieldCurve calibratedCurve = curve.getCalibratedCurve();
      Set<Tenor> curveTerms = Sets.newLinkedHashSet(Iterables.concat(cashTerms, swapTerms));
      int i = 0;
      for (Tenor tenor : allTenors) {
        if (curveTerms.contains(tenor)) {
          double forwardRate = calibratedCurve.getZeroRateAtIndex(i);
          row.add(Double.toString(forwardRate));
          i++;
          YieldCurveData curveData = curve.getCurveData();
          if (cashTerms.contains(tenor)) {
            inputRow.add(curveData.getCashData().get(tenor).toString());
          } else {
            inputRow.add(curveData.getSwapData().get(tenor).toString());
          }
        } else {
          row.add("");
          inputRow.add("");
        }
      }
      rows.put(ccy, Lists.transform(row, Functions.toStringFunction()));
      inputRows.put(ccy, Lists.transform(inputRow, Functions.toStringFunction()));
    }
    
    renderTable("Yield curve market quotes", headers, inputRows.values());
    renderTable("Yield curve zero rates", headers, rows.values());
    
    for (Map.Entry<Currency, Result<IsdaYieldCurve>> failure : yieldCurveFailures.entrySet()) {
      System.out.println(failure.getKey() + ": " + failure.getValue());
    }
    
  }

  /**
   * Renders as a CSV table with a title.
   */
  private void renderTable(String title, List<String> headers, Iterable<List<String>> rows) {
    Joiner joiner = Joiner.on(",");
    
    System.out.println(title);
    
    System.out.println(joiner.join(headers));
    for (List<String> row : rows) {
      System.out.println(joiner.join(row));
    }
  }

  /**
   * Pulls all cash and swap tenors from all yield curves, returning the result as a pair.
   */
  private Pair<SortedSet<Tenor>, SortedSet<Tenor>> getTenors(List<IsdaYieldCurve> yieldCurves) {
    SortedSet<Tenor> cashTenors = Sets.newTreeSet();
    SortedSet<Tenor> swapTenors = Sets.newTreeSet();
    for (IsdaYieldCurve curve : yieldCurves) {
      cashTenors.addAll(curve.getCurveData().getCashData().keySet());
      swapTenors.addAll(curve.getCurveData().getSwapData().keySet());
    }
    return Pairs.of(cashTenors, swapTenors);
  }

  /**
   * Initializes a {@link FunctionModelConfig} instance using the passed snapshot names and component map.
   */
  private FunctionModelConfig initGraph(CreditCurveDataSnapshot ccSnapshot, 
                                        YieldCurveDataSnapshot ycSnapshot, 
                                        ComponentMap componentMap) {
    ThreadLocalServiceContext.init(
        ServiceContext.of(componentMap.getComponents())
           .with(VersionCorrectionProvider.class, new FixedInstantVersionCorrectionProvider(Instant.now())));
    
    AvailableOutputs availableOutputs = new AvailableOutputsImpl();
    availableOutputs.register(IsdaCompliantYieldCurveFn.class,
                              IsdaCompliantCreditCurveFn.class);
    
    AvailableImplementations availableImplementations = new AvailableImplementationsImpl();
    availableImplementations.register(DefaultIsdaCompliantYieldCurveFn.class,
                                      SnapshotYieldCurveDataProviderFn.class,
                                      SnapshotCreditCurveDataProviderFn.class,
                                      StandardIsdaCompliantCreditCurveFn.class);
    
    FunctionModelConfig provider = new FunctionModelConfig(availableImplementations.getDefaultImplementations());
    FunctionModelConfig config =
        config(
            arguments(
                function(
                    SnapshotYieldCurveDataProviderFn.class,
                    argument("snapshotLink", SnapshotLink.resolved(ycSnapshot))),
                function(
                    SnapshotCreditCurveDataProviderFn.class,
                    argument("snapshotLink", SnapshotLink.resolved(ccSnapshot)))));

    return config.mergedWith(provider);
  }
  
  @Override
  protected Options createOptions(boolean mandatoryConfigResource) {
    Options options = super.createOptions(mandatoryConfigResource);
    
    ToolUtils.option(options, "cs", "credit-snapshot", true, "The credit snapshot to use");
    ToolUtils.option(options, "ys", "yieldcurve-snapshot", true, "The yield curve snapshot to use");
    
    return options;
  }

}
