/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions.library;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Set;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
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
import com.opengamma.solutions.library.tool.CreditPricer;
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
  private static final String CREDIT_CURVE_NAME = "Sample Credit Curve";
  private static final String YIELD_CURVE_NAME = "Sample Yield Curve";
  private static final ZonedDateTime VALUATION_TIME = DateUtils.getUTCDate(2014, 10, 16);

  @BeforeClass
  public void setUp() {

    Set<Module> modules = Sets.newHashSet();
    modules.add(new InMemoryStorageModule());
    modules.add(new SourcesModule());
    modules.add(new DataLoadModule("credit-import-data"));
    modules.add(new EngineModule());
    Injector injector = Guice.createInjector(modules);

    CreditPricer pricer = injector.getInstance(CreditPricer.class);
    _results= pricer.price(VALUATION_TIME, CREDIT_CURVE_NAME, YIELD_CURVE_NAME);

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
