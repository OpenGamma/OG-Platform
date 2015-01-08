/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions.library;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.hamcrest.core.Is;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.engine.CalculationArguments;
import com.opengamma.sesame.engine.Engine;
import com.opengamma.sesame.engine.RemoteEngine;
import com.opengamma.sesame.engine.Results;
import com.opengamma.sesame.marketdata.EmptyMarketDataSpec;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.solutions.library.engine.EngineModule;
import com.opengamma.solutions.library.storage.DataLoadModule;
import com.opengamma.solutions.library.storage.InMemoryStorageModule;
import com.opengamma.solutions.library.storage.SourcesModule;
import com.opengamma.solutions.library.tool.CreditPricer;
import com.opengamma.solutions.remote.RemoteTestUtils;
import com.opengamma.solutions.util.CreditViewUtils;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Integration tests using OG as a library
 * Input: Credit Default Swap, Snapshot Market Data
 * Output: Present Value, CS01
 */
@Test(groups = TestGroup.INTEGRATION)
public class LibraryCreditTest {

  private Results _results;

  @BeforeClass
  public void setUp() {
    URL systemResource = ClassLoader.getSystemResource("import-data");

    Set<Module> modules = Sets.newHashSet();
    modules.add(new InMemoryStorageModule());
    modules.add(new SourcesModule());
    modules.add(new DataLoadModule(systemResource.getPath()));
    modules.add(new EngineModule());
    Injector injector = Guice.createInjector(modules);

    CreditPricer pricer = injector.getInstance(CreditPricer.class);
    _results= pricer.price();

  }

  /**
   * Testing the expected values of these results is taken care of in the 'Remote' equivalent of this test
   */
  @Test
  public void assertSuccess() {

    Result result00 = _results.get(0, 0).getResult();
    assertThat(result00.isSuccess(), is(true));

    Result result10 = _results.get(1, 0).getResult();
    assertThat(result10.isSuccess(), is(true));

    Result result01 = _results.get(0, 1).getResult();
    assertThat(result01.isSuccess(), is(true));

    Result result11 = _results.get(1, 1).getResult();
    assertThat(result11.isSuccess(), is(true));

  }

}
