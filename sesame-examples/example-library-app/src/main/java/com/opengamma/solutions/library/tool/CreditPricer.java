/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions.library.tool;

import java.util.List;

import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;

import com.google.inject.Inject;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.impl.RegionFileReader;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.engine.CalculationArguments;
import com.opengamma.sesame.engine.Engine;
import com.opengamma.sesame.engine.FixedInstantVersionCorrectionProvider;
import com.opengamma.sesame.engine.Results;
import com.opengamma.sesame.marketdata.EmptyMarketDataSpec;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.solutions.library.storage.DataLoader;
import com.opengamma.solutions.util.CreditViewUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.DateUtils;

/**
 * Sample Credit pricing method
 */
public class CreditPricer {

  private static final ZonedDateTime VALUATION_TIME = DateUtils.getUTCDate(2014, 10, 16);
  private final Engine _engine;
  private final DataLoader _databaseRestore;
  private final RegionMaster _regionMaster;

  /**
   * Create an instance of the Credit Pricer
   * @param engine the calculation engine.
   * @param databaseRestore utility to populateMulticurveData data into the in memory masters
   * @param regionMaster regions master
   */
  @Inject
  public CreditPricer(Engine engine, DataLoader databaseRestore, RegionMaster regionMaster) {
    _databaseRestore =  ArgumentChecker.notNull(databaseRestore, "databaseRestore");
    _regionMaster =  ArgumentChecker.notNull(regionMaster, "regionMaster");
    _engine = ArgumentChecker.notNull(engine, "engine");
  }

  /**
   * Calculate PV and CS01
   * @return Results containing PV and CS01 for a legacy and standard CS01
   */
  public Results price() {
    // Add sample data to the masters
    _databaseRestore.populateCreditData();
    // initialize the RegionMaster with data
    RegionFileReader.createPopulated(_regionMaster);

    MarketDataSpecification marketDataSpec = EmptyMarketDataSpec.INSTANCE;
    CalculationArguments calculationArguments =
        CalculationArguments.builder()
            .valuationTime(VALUATION_TIME)
            .marketDataSpecification(marketDataSpec)
            .configVersionCorrection(VersionCorrection.ofVersionAsOf(Instant.now()))
            .build();
    List<Object> trades = CreditViewUtils.INPUTS;
    MarketDataEnvironment marketDataEnvironment = MarketDataEnvironmentBuilder.empty();
    ViewConfig viewConfig = CreditViewUtils.createViewConfig("Sample Credit Curve", "Sample Yield Curve");

    // This is needed to ensure that the version correction provided is after the population of the masters
    ServiceContext serviceContext =  ThreadLocalServiceContext.getInstance().with(VersionCorrectionProvider.class, new FixedInstantVersionCorrectionProvider(Instant.now()));
    ThreadLocalServiceContext.init(serviceContext);

    return _engine.runView(viewConfig, calculationArguments, marketDataEnvironment, trades);
  }

}
