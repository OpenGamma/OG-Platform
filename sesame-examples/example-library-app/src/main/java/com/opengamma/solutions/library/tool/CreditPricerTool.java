/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions.library.tool;

import java.net.URL;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * Credit pricer tool
 */
public class CreditPricerTool {

  private static final Logger s_logger = LoggerFactory.getLogger(CreditPricerTool.class);

  /**
   * Entry point to running the Credit Pricer.
   * Log PV and CS01 results to the console
   * @param args, no args are need to run this tool
   */
  public static void main(String[] args) {

    URL systemResource = ClassLoader.getSystemResource("credit-import-data");

    Set<Module> modules = Sets.newHashSet();
    modules.add(new InMemoryStorageModule());
    modules.add(new SourcesModule());
    modules.add(new DataLoadModule(systemResource.getPath()));
    modules.add(new EngineModule());
    Injector injector = Guice.createInjector(modules);

    CreditPricer pricer = injector.getInstance(CreditPricer.class);
    Results results = pricer.price();
    s_logger.info("Got results:\n" + ViewUtils.format(results));

  }

}
