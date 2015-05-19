/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions.library.tool;

import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.opengamma.sesame.engine.Results;
import com.opengamma.solutions.library.engine.EngineModule;
import com.opengamma.solutions.library.storage.InMemoryStorageModule;
import com.opengamma.solutions.library.storage.SourcesModule;
import com.opengamma.solutions.util.ViewUtils;
import com.opengamma.util.time.DateUtils;

/**
 * Bond Future Option example
 */
public class BondFutureOptionPricerExample {

  private static final Logger s_logger = LoggerFactory.getLogger(BondFutureOptionPricerExample.class);
  private static final String DISCOUNTING_CURVES = "bond-data/curves/discounting-curves.csv";
  private static final String ISSUER_CURVES = "bond-data/curves/issuer-curves.csv";
  private static final String VOLATILITY_SURFACES = "bond-data/vols/surface.csv";
  private static final String TRADES = "bond-data/trades/bond-future-options.csv";
  private static final ZonedDateTime VALUATION_TIME = DateUtils.getUTCDate(2015, 4, 27);

  /**
   * Entry point to running the Bond Future Option Pricer.
   * Log calculation results to the console
   * @param args, no args are need to run this tool
   */
  public static void main(String[] args) throws IOException {

    Set<Module> modules = Sets.newHashSet();
    modules.add(new InMemoryStorageModule());
    modules.add(new SourcesModule());
    modules.add(new EngineModule());
    Injector injector = Guice.createInjector(modules);

    BondFutureOptionPricer pricer = injector.getInstance(BondFutureOptionPricer.class);
    Results results = pricer.price(VALUATION_TIME,
                                   TRADES,
                                   DISCOUNTING_CURVES,
                                   ISSUER_CURVES,
                                   VOLATILITY_SURFACES);
    s_logger.info("Got results:\n" + ViewUtils.format(results));
  }

}
