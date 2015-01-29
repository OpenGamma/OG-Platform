/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions.library.tool;

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
import com.opengamma.solutions.library.storage.DataLoadModule;
import com.opengamma.solutions.library.storage.InMemoryStorageModule;
import com.opengamma.solutions.library.storage.SourcesModule;
import com.opengamma.solutions.util.ViewUtils;
import com.opengamma.util.time.DateUtils;

/**
 * Credit pricer example
 */
public class CreditPricerExample {

  private static final Logger s_logger = LoggerFactory.getLogger(CreditPricerExample.class);
  private static final String CREDIT_CURVE_NAME = "Sample Credit Curve";
  private static final String YIELD_CURVE_NAME = "Sample Yield Curve";
  private static final ZonedDateTime VALUATION_TIME = DateUtils.getUTCDate(2014, 10, 16);


  /**
   * Entry point to running the Credit Pricer.
   * Log PV and CS01 results to the console
   * @param args, no args are need to run this tool
   */
  public static void main(String[] args) {

    Set<Module> modules = Sets.newHashSet();
    modules.add(new InMemoryStorageModule());
    modules.add(new SourcesModule());
    modules.add(new DataLoadModule("credit-import-data"));
    modules.add(new EngineModule());
    Injector injector = Guice.createInjector(modules);

    CreditPricer pricer = injector.getInstance(CreditPricer.class);
    Results results = pricer.price(VALUATION_TIME, CREDIT_CURVE_NAME, YIELD_CURVE_NAME);
    s_logger.info("Got results:\n" + ViewUtils.format(results));

  }

}
