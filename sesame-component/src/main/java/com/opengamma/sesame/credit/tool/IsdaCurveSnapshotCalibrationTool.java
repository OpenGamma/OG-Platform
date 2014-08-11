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
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.FutureTask;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.component.tool.ToolUtils;
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
import com.opengamma.sesame.cache.ExecutingMethodsThreadLocal;
import com.opengamma.sesame.cache.MethodInvocationKey;
import com.opengamma.sesame.config.CompositeFunctionModelConfig;
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
import com.opengamma.sesame.engine.FixedInstantVersionCorrectionProvider;
import com.opengamma.sesame.function.AvailableImplementations;
import com.opengamma.sesame.function.AvailableImplementationsImpl;
import com.opengamma.sesame.function.AvailableOutputs;
import com.opengamma.sesame.function.AvailableOutputsImpl;
import com.opengamma.sesame.function.DefaultImplementationProvider;
import com.opengamma.sesame.graph.FunctionModel;
import com.opengamma.sesame.marketdata.MarketDataSource;
import com.opengamma.sesame.proxy.ExceptionWrappingProxy;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.Result;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Tests ISDA curve data by calibrating each curve in turn and dumping the result
 * out to the console.
 */
public class IsdaCurveSnapshotCalibrationTool extends AbstractTool<ToolContext> {

  private static final Logger s_logger = LoggerFactory.getLogger(IsdaCurveSnapshotCalibrationTool.class);
  private Environment _env; 
  
  {
    //note - market data source not used
    MarketDataSource marketDataSource = mock(MarketDataSource.class);
    _env = new SimpleEnvironment(ZonedDateTime.now(), marketDataSource);
  }
  
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
    
    ComponentMap componentMap = ComponentMap.loadComponents(getToolContext());
    
    FunctionModelConfig functionModelConfig = initGraph(ccSnapshot, ycSnapshot, componentMap);
    Cache<MethodInvocationKey, FutureTask<Object>> cache = buildCache();
    CachingProxyDecorator cachingDecorator =
            new CachingProxyDecorator(cache, new ExecutingMethodsThreadLocal());
    
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
    
    calibrateYieldCurves(ycSnapshot, ycFn);
    
    calibrateCreditCurves(ccSnapshot, ccFn);
    
  }


  private void calibrateCreditCurves(CreditCurveDataSnapshot ccSnapshot, IsdaCompliantCreditCurveFn ccFn) {
    Map<CreditCurveDataKey, IsdaCreditCurve> creditCurves = Maps.newHashMap();
    Map<CreditCurveDataKey, Result<IsdaCreditCurve>> creditCurveFailures = Maps.newHashMap();
    int i = 1;
    for (CreditCurveDataKey key : ccSnapshot.getCreditCurves().keySet()) {
      Result<IsdaCreditCurve> curve;
      try {
        curve = ccFn.buildIsdaCompliantCreditCurve(_env, key);
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
    
    renderCreditCurves(creditCurves, creditCurveFailures);
  }


  private void calibrateYieldCurves(YieldCurveDataSnapshot ycSnapshot, IsdaCompliantYieldCurveFn ycFn) {
    List<IsdaYieldCurve> yieldCurves = Lists.newArrayList();
    Map<String, Result<IsdaYieldCurve>> yieldCurveFailures = Maps.newTreeMap();
    for (Currency ccy : ycSnapshot.getYieldCurves().keySet()) {
      Result<IsdaYieldCurve> curve = ycFn.buildIsdaCompliantCurve(_env, ccy);
      if (curve.isSuccess()) {
        yieldCurves.add(curve.getValue());
      } else {
        yieldCurveFailures.put(ccy.toString(), curve);
      }
    }
    
    renderYieldCurves(yieldCurves, yieldCurveFailures);
  }


  /**
   * Builds a basic cache.
   */
  private Cache<MethodInvocationKey, FutureTask<Object>> buildCache() {
    int concurrencyLevel = Runtime.getRuntime().availableProcessors() + 2;
    Cache<MethodInvocationKey, FutureTask<Object>> cache = CacheBuilder.newBuilder()
        .maximumSize(50000)
        .softValues()
        .concurrencyLevel(concurrencyLevel)
        .build();
    return cache;
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
    
    SortedMap<String, List<String>> rows = Maps.newTreeMap();
    for (Entry<CreditCurveDataKey, IsdaCreditCurve> entry : creditCurves.entrySet()) {
      
      CreditCurveDataKey key = entry.getKey();
      IsdaCreditCurve curve = entry.getValue();
      
      List<String> row = Lists.newArrayList(key.getCurveName(), 
                                            str(key.getCurrency()), 
                                            str(key.getRestructuring()), 
                                            str(key.getSeniority()));
      row.add(str(curve.getYieldCurve().getCurveData().getCurrency()));
      
      //used as a simple string key for ordering results
      String strKey = String.format("%s_%s_%s_%s", 
                                    key.getCurveName(), 
                                    str(key.getCurrency()), 
                                    str(key.getRestructuring()), 
                                    str(key.getSeniority()));
      
      int i = 0;
      for (Tenor tenor : allTenors) {
        if (curve.getCurveData().getCdsQuotes().containsKey(tenor)) {
          ISDACompliantCreditCurve calibratedCurve = curve.getCalibratedCurve();
          double timeAtI = calibratedCurve.getTimeAtIndex(i);
          double hazardRate = calibratedCurve.getHazardRate(timeAtI);
          row.add(Double.toString(hazardRate));
          i++;
        } else {
          row.add("");
        }
      }
      rows.put(strKey, row);
      
    }
    
    renderTable("Credit curve hazard rates", headers, rows);
    
    System.out.println("Credit curve failures:");
    for (Entry<CreditCurveDataKey, Result<IsdaCreditCurve>> entry : creditCurveFailures.entrySet()) {
      System.out.format("%s: %s\n", entry.getKey(), entry.getValue());
    }
  }

  /**
   * Helper toString() method for succinctness.
   */
  private static String str(Object o) {
    return String.valueOf(o);
  }

  /**
   * Renders zero rates for yield curves in a tabular CSV format. Failures are appended to the end.
   */
  private void renderYieldCurves(List<IsdaYieldCurve> yieldCurves, 
                                 Map<String, Result<IsdaYieldCurve>> yieldCurveFailures) {
    
    Pair<SortedSet<Tenor>, SortedSet<Tenor>> tenors = getTenors(yieldCurves);
    SortedSet<Tenor> cashTenors = tenors.getFirst();
    SortedSet<Tenor> swapTenors = tenors.getSecond();
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
    
    SortedMap<String, List<String>> inputRows = Maps.newTreeMap();
    SortedMap<String, List<String>> rows = Maps.newTreeMap();
    for (IsdaYieldCurve curve : yieldCurves) {
      
      String ccy = curve.getCurveData().getCurrency().toString();
      List<String> row = Lists.newArrayList(ccy);
      List<String> inputRow = Lists.newArrayList(ccy);
      
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
      rows.put(ccy, row);
      inputRows.put(ccy, inputRow);
    }
    
    renderTable("Yield curve market quotes", headers, inputRows);
    renderTable("Yield curve zero rates", headers, rows);
    
    for (Map.Entry<String, Result<IsdaYieldCurve>> failure : yieldCurveFailures.entrySet()) {
      System.out.println(failure.getKey() + ": " + failure.getValue());
    }
    
  }


  /**
   * Renders as a CSV table with a title.
   */
  private void renderTable(String title, List<String> headers, SortedMap<String, List<String>> rows) {
    Joiner joiner = Joiner.on(",");
    
    System.out.println(title);
    
    System.out.println(joiner.join(headers));
    for (List<String> row : rows.values()) {
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
    return ObjectsPair.of(cashTenors, swapTenors);
  }


  /**
   * Inits a {@link FunctionModelConfig} instance using the passed snapshot names and component map.
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
                                      StandardIsdaCompliantCreditCurveFn.class
    );
    
    FunctionModelConfig provider = new DefaultImplementationProvider(availableImplementations);
    
    FunctionModelConfig config = config(
        arguments(
            function(SnapshotYieldCurveDataProviderFn.class,
                     argument("snapshotLink", SnapshotLink.resolved(ycSnapshot))),
             function(SnapshotCreditCurveDataProviderFn.class,
                      argument("snapshotLink", 
                              SnapshotLink.resolved(ccSnapshot)))));

    FunctionModelConfig functionModelConfig = CompositeFunctionModelConfig.compose(config, provider);
    return functionModelConfig;
  }
  
  
  @Override
  protected Options createOptions(boolean mandatoryConfigResource) {
    Options options = super.createOptions(mandatoryConfigResource);
    
    ToolUtils.option(options, "cs", "credit-snapshot", true, "The credit snapshot to use");
    ToolUtils.option(options, "ys", "yieldcurve-snapshot", true, "The yield curve snapshot to use");
    
    return options;
  }

}
