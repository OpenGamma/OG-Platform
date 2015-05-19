/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions.library.tool;

import java.io.IOException;
import java.util.HashMap;

import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.impl.RegionFileReader;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.engine.CalculationArguments;
import com.opengamma.sesame.engine.Engine;
import com.opengamma.sesame.engine.FixedInstantVersionCorrectionProvider;
import com.opengamma.sesame.engine.Results;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.solutions.util.BondFutureOptionViewUtils;
import com.opengamma.util.ArgumentChecker;

/**
 * Sample Bond Future Option pricing method
 */
public class BondFutureOptionPricer {

  private final Engine _engine;
  private final RegionMaster _regionMaster;
  private final SecurityMaster _securityMaster;

  /**
   * Create an instance of the Bond Future Option Pricer
   * @param engine the calculation engine.
   * @param regionMaster the region master.
   * @param securityMaster the security master to persist underlying securities.
   */
  @Inject
  public BondFutureOptionPricer(Engine engine,
                                RegionMaster regionMaster,
                                SecurityMaster securityMaster) {
    _engine = ArgumentChecker.notNull(engine, "engine");
    _regionMaster = ArgumentChecker.notNull(regionMaster, "regionMaster");
    _securityMaster = ArgumentChecker.notNull(securityMaster, "securityMaster");
  }

  /**
   * Bond Future Option price function
   * @param valuationTime ZoneDateTime valuation time
   * @param trades the path to the input trades file
   * @param discountingCurves the path to the discounting curve file
   * @param issuerCurves the path to the issuer curve file
   * @param volatilitySurfaces the path to the volatility surface file
   * @return calculation results for an Bond Future Option
   */
  public Results price(ZonedDateTime valuationTime,
                       String trades,
                       String discountingCurves,
                       String issuerCurves,
                       String volatilitySurfaces) throws IOException {

    RegionFileReader.createPopulated(_regionMaster);

    CalculationArguments calculationArguments = CalculationArguments.builder().valuationTime(valuationTime).build();
    HashMap<Object, String> portfolio = BondFutureOptionViewUtils.parsePortfolio(trades, _securityMaster);
    ViewConfig viewConfig = BondFutureOptionViewUtils.createViewConfig(portfolio.values());

    MarketDataEnvironmentBuilder marketData = new MarketDataEnvironmentBuilder();
    BondFutureOptionViewUtils.parseCurves(marketData, discountingCurves, issuerCurves);
    BondFutureOptionViewUtils.parseVolatilitySurfaces(marketData, volatilitySurfaces);
    marketData.valuationTime(valuationTime);

    // This is needed to ensure that the version correction provided is after the population of the masters
    ServiceContext serviceContext =
        ThreadLocalServiceContext.getInstance().with(VersionCorrectionProvider.class,
                                                     new FixedInstantVersionCorrectionProvider(Instant.now()));
    ThreadLocalServiceContext.init(serviceContext);

    return _engine.runView(viewConfig, calculationArguments, marketData.build(), Lists.newArrayList(portfolio.keySet()));
  }

}
