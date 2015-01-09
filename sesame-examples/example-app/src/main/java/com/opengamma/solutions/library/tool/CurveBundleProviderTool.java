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
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.solutions.library.engine.EngineModule;
import com.opengamma.solutions.library.storage.DataLoadModule;
import com.opengamma.solutions.library.storage.InMemoryStorageModule;
import com.opengamma.solutions.library.storage.SourcesModule;
import com.opengamma.util.time.DateUtils;

/**
 * Curve Bundle Provider Tool
 */
public class CurveBundleProviderTool {

  private static final Logger s_logger = LoggerFactory.getLogger(CurveBundleProviderTool.class);
  private static final ZonedDateTime VALUATION_TIME = DateUtils.getUTCDate(2014, 10, 16);
  private static final String CURVE_CONSTRUCTION_CONFIGURATION = "USD_FF_DSCON-OISFFS_L3M-FRAIRS_L1M-BS_L6M-BS";
  private static final String SNAPSHOT_NAME = "USD_GBP_XCcy_Integration";
  private static final String CURRENCY_MATRIX_NAME = "BBG-Matrix";

  /**
   * Entry point to running the Curve Bundle Provider.
   * @param args, no args are need to run this tool
   */
  public static void main(String[] args) {

    URL systemResource = ClassLoader.getSystemResource("import-data");

    Set<Module> modules = Sets.newHashSet();
    modules.add(new InMemoryStorageModule());
    modules.add(new SourcesModule());
    modules.add(new DataLoadModule(systemResource.getPath()));
    modules.add(new EngineModule());
    Injector injector = Guice.createInjector(modules);

    CurveBundleProvider provider = injector.getInstance(CurveBundleProvider.class);
    MulticurveBundle bundle = provider.buildMulticurve(CURVE_CONSTRUCTION_CONFIGURATION,
                                                       SNAPSHOT_NAME,
                                                       CURRENCY_MATRIX_NAME,
                                                       VALUATION_TIME);
    s_logger.info("Got results:\n" + bundle.toString());
  }

}
