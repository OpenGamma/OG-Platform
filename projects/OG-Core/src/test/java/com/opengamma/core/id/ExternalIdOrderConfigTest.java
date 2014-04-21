/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.id;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link ExternalIdOrderConfig} class.
 */
@Test(groups = TestGroup.UNIT)
@SuppressWarnings("deprecation")
public class ExternalIdOrderConfigTest {

  public void testGetPreferred_empty() {
    assertEquals(ExternalIdOrderConfig.DEFAULT_CONFIG.getPreferred(ExternalIdBundle.EMPTY), null);
  }

  public void testGetPreferred_single() {
    assertEquals(ExternalIdOrderConfig.DEFAULT_CONFIG.getPreferred(ExternalId.of("Foo", "Bar").toBundle()), ExternalId.of("Foo", "Bar"));
  }

  public void testGetPreferred_notListed() {
    assertEquals(ExternalIdOrderConfig.DEFAULT_CONFIG.getPreferred(ExternalIdBundle.of(ExternalId.of("Foo", "Bar"), ExternalId.of("Bar", "Foo"))), ExternalId.of("Bar", "Foo"));
  }

  public void testGetPreferred_default() {
    assertEquals(ExternalIdOrderConfig.DEFAULT_CONFIG.getPreferred(ExternalIdBundle.of(ExternalId.of(ExternalSchemes.BLOOMBERG_TCM, "tcm"), ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "ticker"),
        ExternalId.of("Foo", "Bar"))), ExternalId.of(ExternalSchemes.BLOOMBERG_TCM, "tcm"));
  }

  public void testSort() {
    final ExternalId a = ExternalId.of(ExternalSchemes.BLOOMBERG_TCM, "bbg_tcm");
    final ExternalId b = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "bbg_ticker");
    final ExternalId c = ExternalId.of(ExternalSchemes.RIC, "ric");
    final ExternalId d = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER_WEAK, "bbg_ticker_weak");
    final ExternalId e = ExternalId.of(ExternalSchemes.ACTIVFEED_TICKER, "activ_ticker");
    final ExternalId f = ExternalId.of(ExternalSchemes.SURF, "surf");
    final ExternalId g = ExternalId.of(ExternalSchemes.ISIN, "isin");
    final ExternalId h = ExternalId.of(ExternalSchemes.CUSIP, "cusip");
    final ExternalId i = ExternalId.of(ExternalSchemes.SEDOL1, "sedol1");
    final ExternalId j = ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "opengamma");
    final ExternalId k = ExternalId.of(ExternalSchemes.BLOOMBERG_BUID, "bbg_buid");
    final ExternalId l = ExternalId.of(ExternalSchemes.BLOOMBERG_BUID_WEAK, "bbg_buid_weak");
    final ExternalId m = ExternalId.of("Foo", "Bar");
    final ExternalId n = ExternalId.of("Foo", "Cow");
    final ExternalIdBundle bundle = ExternalIdBundle.of(d, l, a, b, c, g, m, n, h, i, e, f, k, j);
    final List<ExternalId> sorted = ExternalIdOrderConfig.DEFAULT_CONFIG.sort(bundle);
    assertEquals(sorted, Arrays.asList(a, b, c, d, e, f, g, h, i, j, k, l, m, n));
  }

}
