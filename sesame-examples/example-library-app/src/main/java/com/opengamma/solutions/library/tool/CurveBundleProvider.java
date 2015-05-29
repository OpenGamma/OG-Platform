/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions.library.tool;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.impl.RegionFileReader;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.engine.CalculationArguments;
import com.opengamma.sesame.engine.ComponentMap;
import com.opengamma.sesame.engine.Engine;
import com.opengamma.sesame.engine.FixedInstantVersionCorrectionProvider;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.sesame.marketdata.MarketDataRequirement;
import com.opengamma.sesame.marketdata.MulticurveId;
import com.opengamma.sesame.marketdata.ScenarioMarketDataEnvironment;
import com.opengamma.sesame.marketdata.SingleValueRequirement;
import com.opengamma.sesame.marketdata.SnapshotMarketDataFactory;
import com.opengamma.sesame.marketdata.builders.MarketDataBuilder;
import com.opengamma.sesame.marketdata.builders.MarketDataBuilders;
import com.opengamma.sesame.marketdata.builders.MarketDataEnvironmentFactory;
import com.opengamma.sesame.marketdata.scenarios.ScenarioDefinition;
import com.opengamma.sesame.marketdata.scenarios.SingleScenarioDefinition;
import com.opengamma.solutions.library.storage.DataLoader;
import com.opengamma.util.ArgumentChecker;

import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;

import java.util.List;
import java.util.Set;

/**
 * Sample Curve Bundle Provider
 */
public class CurveBundleProvider {

  private final DataLoader _dataload;
  private final RegionMaster _regionMaster;
  private final ComponentMap _componentMap;
  private final MarketDataSnapshotSource _snapshotSource;
  private final Engine _engine;

  /**
   * Create an instance of the Curve Bundle Provider
   * @param dataload utility to populateMulticurveData data into the in memory masters
   * @param regionMaster regions master
   * @param snapshotSource market data snapshot source
   * @param componentMap component map to build the MarketDataEnvironment
   * @param engine awesome calculator
   */
  @Inject
  public CurveBundleProvider(DataLoader dataload,
                             RegionMaster regionMaster,
                             MarketDataSnapshotSource snapshotSource,
                             ComponentMap componentMap,
                             Engine engine) {
    _dataload =  ArgumentChecker.notNull(dataload, "dataload");
    _regionMaster = ArgumentChecker.notNull(regionMaster, "regionMaster");
    _componentMap = ArgumentChecker.notNull(componentMap, "componentMap");
    _snapshotSource = ArgumentChecker.notNull(snapshotSource, "snapshotSource");
    _engine = ArgumentChecker.notNull(engine, "engine");
  }


  /**
   * Return a curve bundle
   * @param bundleName the name of the curve construction configuration
   * @param snapshotName the name of the data snapshot
   * @param currencyMatrixName the name of the currency matrix
   * @param valuationTime zone data time valuation time
   * @return MulticurveBundle
   */
  public MulticurveBundle buildMulticurve(String bundleName,
                                          String snapshotName,
                                          String currencyMatrixName,
                                          ZonedDateTime valuationTime) {
    _dataload.populateMulticurveData();
    RegionFileReader.createPopulated(_regionMaster);

    // This is needed to ensure that the version correction provided is after the population of the masters
    ServiceContext serviceContext =
        ThreadLocalServiceContext.getInstance().with(VersionCorrectionProvider.class,
                                                     new FixedInstantVersionCorrectionProvider(Instant.now()));
    ThreadLocalServiceContext.init(serviceContext);

    // Need to create another MarketDataEnvironmentFactory here, rather than using the one provided by Guice.
    // This is because resolving the currency matrix link can only happen after the population of masters
    ConfigLink<CurrencyMatrix> currencyMatrixLink  = ConfigLink.resolvable(currencyMatrixName, CurrencyMatrix.class);
    List<MarketDataBuilder> builders = ImmutableList.of(
        MarketDataBuilders.raw(_componentMap, "DEFAULT"),
        MarketDataBuilders.multicurve(_componentMap, currencyMatrixLink),
        MarketDataBuilders.fxMatrix());


    // Get the snapshot
    SnapshotMarketDataFactory snapshotMarketDataFactory = new SnapshotMarketDataFactory(_snapshotSource);
    MarketDataEnvironmentFactory environmentFactory = new MarketDataEnvironmentFactory(snapshotMarketDataFactory, builders);
    ManageableMarketDataSnapshot snapshot = _snapshotSource.getSingle(ManageableMarketDataSnapshot.class,
                                                                                snapshotName,
                                                                                VersionCorrection.LATEST);
    MarketDataSpecification marketDataSpec = UserMarketDataSpecification.of(snapshot.getUniqueId());

    //Build the MarketDataEnvironment
    MarketDataEnvironment suppliedData = MarketDataEnvironmentBuilder.empty();
    MulticurveId multicurveId = MulticurveId.of(bundleName);
    SingleValueRequirement requirement = SingleValueRequirement.of(multicurveId);
    Set<MarketDataRequirement> requirements = ImmutableSet.<MarketDataRequirement>of(requirement);

    SingleScenarioDefinition perturbations = SingleScenarioDefinition.base();
    MarketDataEnvironment marketData =
        environmentFactory.build(suppliedData, requirements, perturbations, marketDataSpec, valuationTime);

    return (MulticurveBundle) marketData.getData().get(requirement);
  }

  /**
   * Return a curve bundle for each scenario, as a ScenarioMarketDataEnvironment, derived from the scenario definition.
   * 
   * @param snapshotName the name of the data snapshot
   * @param valuationTime zone data time valuation time
   * @param config view configuration of the engine that will run the curve calibration on each scenario
   * @param scenarioDefinition scenario definition as a list of single scenarios for which the market data will be
   * calibrated separately
   * @param trades the list of relevant trades which drives the market data requirements
   * @return ScenarioMarketDataEnvironment a container for the market data for each single scenario
   */
  public ScenarioMarketDataEnvironment buildScenarioMarketDataEnvironment(String snapshotName,
      ZonedDateTime valuationTime,
      ViewConfig config,
      ScenarioDefinition scenarioDefinition,
      List<?> trades) {
    _dataload.populateMulticurveData();
    
    RegionFileReader.createPopulated(_regionMaster);
    ServiceContext serviceContext =
        ThreadLocalServiceContext.getInstance().with(VersionCorrectionProvider.class,
            new FixedInstantVersionCorrectionProvider(Instant.now()));
    ThreadLocalServiceContext.init(serviceContext);
    ManageableMarketDataSnapshot snapshot = _snapshotSource.getSingle(ManageableMarketDataSnapshot.class,
        snapshotName,
        VersionCorrection.LATEST);
    MarketDataSpecification marketDataSpec = UserMarketDataSpecification.of(snapshot.getUniqueId());
    MarketDataEnvironment suppliedData = 
        MarketDataEnvironmentBuilder.empty().toBuilder().valuationTime(valuationTime).build();
    CalculationArguments calculationArguments = CalculationArguments
        .builder()
        .marketDataSpecification(marketDataSpec)
        .valuationTime(valuationTime)
        .build();

    return _engine.buildScenarioMarketData(
        config,
        suppliedData,
        scenarioDefinition,
        calculationArguments,
        trades);
  }
  
}
