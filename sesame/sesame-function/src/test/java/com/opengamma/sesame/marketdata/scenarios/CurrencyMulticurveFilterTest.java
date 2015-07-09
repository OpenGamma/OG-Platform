/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.marketdata.MulticurveId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class CurrencyMulticurveFilterTest {

  /**
   * Tests matching curves by currency in an existing {@link MulticurveBundle}.
   */
  @Test
  public void bundle() {
    CurrencyMulticurveFilter usdFilter = new CurrencyMulticurveFilter(Currency.USD);
    Set<MulticurveMatchDetails> usdMatches =
        usdFilter.apply(MulticurveId.of("not used"), MulticurveFilterTestUtils.bundle());
    ImmutableSet<MulticurveMatchDetails> expectedUsdMatches =
        ImmutableSet.of(
            StandardMatchDetails.multicurve(MulticurveFilterTestUtils.USD_DISCOUNTING),
            StandardMatchDetails.multicurve(MulticurveFilterTestUtils.USD_LIBOR_3M),
            StandardMatchDetails.multicurve(MulticurveFilterTestUtils.USD_OVERNIGHT));
    assertEquals(expectedUsdMatches, usdMatches);

    CurrencyMulticurveFilter gbpFilter = new CurrencyMulticurveFilter(Currency.GBP);
    Set<MulticurveMatchDetails> gbpMatches =
        gbpFilter.apply(MulticurveId.of("not used"), MulticurveFilterTestUtils.bundle());
    ImmutableSet<MulticurveMatchDetails> expectedGbpMatches =
        ImmutableSet.of(
            StandardMatchDetails.multicurve(MulticurveFilterTestUtils.GBP_DISCOUNTING),
            StandardMatchDetails.multicurve(MulticurveFilterTestUtils.GBP_OVERNIGHT));
    assertEquals(expectedGbpMatches, gbpMatches);

    CurrencyMulticurveFilter eurFilter = new CurrencyMulticurveFilter(Currency.EUR);
    Set<MulticurveMatchDetails> eurMatches =
        eurFilter.apply(MulticurveId.of("not used"), MulticurveFilterTestUtils.bundle());
    ImmutableSet<MulticurveMatchDetails> expectedEurMatches =
        ImmutableSet.of(StandardMatchDetails.multicurve(MulticurveFilterTestUtils.EUR_LIBOR_6M));
    assertEquals(expectedEurMatches, eurMatches);

    CurrencyMulticurveFilter chfFilter = new CurrencyMulticurveFilter(Currency.CHF);
    Set<MulticurveMatchDetails> chfMatches =
        chfFilter.apply(MulticurveId.of("not used"), MulticurveFilterTestUtils.bundle());
    assertEquals(ImmutableSet.<MulticurveMatchDetails>of(), chfMatches);
  }

  /**
   * Tests matching curves by currency in a {@link CurveConstructionConfiguration}.
   */
  @Test
  public void config() {
    MulticurveFilterTestUtils.initializeServiceContext();
    MulticurveId multicurveId = MulticurveId.of(MulticurveFilterTestUtils.CURVE_CONFIG_NAME);

    CurrencyMulticurveFilter usdFilter = new CurrencyMulticurveFilter(Currency.USD);
    Set<MulticurveMatchDetails> usdMatches = usdFilter.apply(multicurveId);
    ImmutableSet<MulticurveMatchDetails> expectedUsdMatches =
        ImmutableSet.of(
            StandardMatchDetails.multicurve(MulticurveFilterTestUtils.USD_DISCOUNTING),
            StandardMatchDetails.multicurve(MulticurveFilterTestUtils.USD_LIBOR_3M),
            StandardMatchDetails.multicurve(MulticurveFilterTestUtils.USD_OVERNIGHT));
    assertEquals(expectedUsdMatches, usdMatches);

    CurrencyMulticurveFilter gbpFilter = new CurrencyMulticurveFilter(Currency.GBP);
    Set<MulticurveMatchDetails> gbpMatches = gbpFilter.apply(multicurveId);
    ImmutableSet<MulticurveMatchDetails> expectedGbpMatches =
        ImmutableSet.of(
            StandardMatchDetails.multicurve(MulticurveFilterTestUtils.GBP_DISCOUNTING),
            StandardMatchDetails.multicurve(MulticurveFilterTestUtils.GBP_OVERNIGHT));
    assertEquals(expectedGbpMatches, gbpMatches);

    CurrencyMulticurveFilter eurFilter = new CurrencyMulticurveFilter(Currency.EUR);
    Set<MulticurveMatchDetails> eurMatches = eurFilter.apply(multicurveId);
    ImmutableSet<MulticurveMatchDetails> expectedEurMatches =
        ImmutableSet.of(StandardMatchDetails.multicurve(MulticurveFilterTestUtils.EUR_LIBOR_6M));
    assertEquals(expectedEurMatches, eurMatches);

    CurrencyMulticurveFilter chfFilter = new CurrencyMulticurveFilter(Currency.CHF);
    Set<MulticurveMatchDetails> chfMatches = chfFilter.apply(multicurveId);
    assertEquals(ImmutableSet.<MulticurveMatchDetails>of(), chfMatches);
  }
}
