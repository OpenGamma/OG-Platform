/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions.library;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.net.URL;
import java.util.Set;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.solutions.library.engine.EngineModule;
import com.opengamma.solutions.library.storage.DataLoadModule;
import com.opengamma.solutions.library.storage.InMemoryStorageModule;
import com.opengamma.solutions.library.storage.SourcesModule;
import com.opengamma.solutions.library.tool.CurveBundleProvider;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Integration tests using OG as a library
 * Outputting the MulticurveBundle
 */
@Test(groups = TestGroup.INTEGRATION)
public class LibraryCurveBundleProviderTest {

  private MulticurveBundle _bundle;
  private static final ZonedDateTime VALUATION_TIME = DateUtils.getUTCDate(2014, 10, 16);
  private static final String CURVE_CONSTRUCTION_CONFIGURATION = "USD_FF_DSCON-OISFFS_L3M-FRAIRS_L1M-BS_L6M-BS";
  private static final String SNAPSHOT_NAME = "USDSnapshot";
  private static final String CURRENCY_MATRIX_NAME = "CurrencyMatrix";

  @BeforeClass
  public void setUp() {
    URL systemResource = ClassLoader.getSystemResource("curve-import-data");

    Set<Module> modules = Sets.newHashSet();
    modules.add(new InMemoryStorageModule());
    modules.add(new SourcesModule());
    modules.add(new DataLoadModule(systemResource.getPath()));
    modules.add(new EngineModule());
    Injector injector = Guice.createInjector(modules);

    CurveBundleProvider provider = injector.getInstance(CurveBundleProvider.class);
    _bundle = provider.buildMulticurve(CURVE_CONSTRUCTION_CONFIGURATION,
                                       SNAPSHOT_NAME,
                                       CURRENCY_MATRIX_NAME,
                                       VALUATION_TIME);
  }

  /**
   * Testing the expected values the MulticurveBundle
   */
  @Test
  public void testCurveBundleData() {

    MulticurveProviderDiscount multicurveProvider = _bundle.getMulticurveProvider();

    assertThat(multicurveProvider.getAllCurveNames().size(), is(4));
    assertThat(multicurveProvider.getDiscountingCurves().size(), is(1));
    assertThat(multicurveProvider.getForwardIborCurves().size(), is(3));
    assertThat(multicurveProvider.getForwardONCurves().size(), is(1));

    assertThat(multicurveProvider.getCurve("USD-FRAL3M-IRSL3M-NCS"), is(notNullValue()));
    assertThat(multicurveProvider.getCurve("USD-OIS-FFS-NCS"), is(notNullValue()));
    assertThat(multicurveProvider.getCurve("USD-FRAL6M-BSL3ML6M-NCS"), is(notNullValue()));
    assertThat(multicurveProvider.getCurve("USD-IRSL1M-BSL1ML3M-NCS"), is(notNullValue()));

    assertThat(multicurveProvider.getCurve("FAIL"), is(nullValue()));

    CurveBuildingBlockBundle buildingBlockBundle = _bundle.getCurveBuildingBlockBundle();

    assertThat(buildingBlockBundle.getData().size(), is(4));


  }

}
