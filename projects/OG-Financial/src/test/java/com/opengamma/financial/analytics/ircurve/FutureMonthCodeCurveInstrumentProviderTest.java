/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.financial.analytics.ircurve;

import static org.testng.Assert.*;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

@Test(groups = TestGroup.UNIT)
public class FutureMonthCodeCurveInstrumentProviderTest {

  private static final LocalDate NOW = LocalDate.of(2014, 1, 1);

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNonFuture1() throws Exception {
    FutureMonthCodeCurveInstrumentProvider provider = new FutureMonthCodeCurveInstrumentProvider("AB", "CD",
        ExternalSchemes.OG_SYNTHETIC_TICKER);
    provider.getInstrument(NOW, Tenor.ONE_YEAR);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNonFuture2() throws Exception {
    FutureMonthCodeCurveInstrumentProvider provider = new FutureMonthCodeCurveInstrumentProvider("AB", "CD",
        ExternalSchemes.OG_SYNTHETIC_TICKER);
    provider.getInstrument(NOW, Tenor.ONE_YEAR, 4, true);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testNonFuture3() throws Exception {
    FutureMonthCodeCurveInstrumentProvider provider = new FutureMonthCodeCurveInstrumentProvider("AB", "CD",
        ExternalSchemes.OG_SYNTHETIC_TICKER);
    provider.getInstrument(NOW, Tenor.ONE_YEAR, Tenor.ONE_YEAR, IndexType.Libor);
  }

  public void testQuarterlyFuture() throws Exception {
    FutureMonthCodeCurveInstrumentProvider provider = new FutureMonthCodeCurveInstrumentProvider("AB", "CD",
        ExternalSchemes.OG_SYNTHETIC_TICKER);
    ExternalId returnedId = provider.getInstrument(NOW, Tenor.ONE_DAY, Tenor.THREE_MONTHS, 2);
    assertEquals(returnedId, ExternalSchemes.syntheticSecurityId("ABM4CD"));
    ExternalId returnedId2 = provider.getInstrument(NOW, Tenor.ONE_DAY, Tenor.THREE_MONTHS, 3);
    assertEquals(returnedId2, ExternalSchemes.syntheticSecurityId("ABU4CD"));
  }

  public void testMonthlyFuture() throws Exception {
    FutureMonthCodeCurveInstrumentProvider provider = new FutureMonthCodeCurveInstrumentProvider("AB", "CD",
        ExternalSchemes.OG_SYNTHETIC_TICKER);
    ExternalId returnedId = provider.getInstrument(NOW, Tenor.ONE_DAY, Tenor.ONE_MONTH, 2);
    assertEquals(returnedId, ExternalSchemes.syntheticSecurityId("ABG4CD"));
    ExternalId returnedId2 = provider.getInstrument(NOW, Tenor.ONE_DAY, Tenor.ONE_MONTH, 3);
    assertEquals(returnedId2, ExternalSchemes.syntheticSecurityId("ABH4CD"));
  }

  public void testGetInstrumentSpaces() throws Exception {
    FutureMonthCodeCurveInstrumentProvider provider = new FutureMonthCodeCurveInstrumentProvider("AB", " CD",
        ExternalSchemes.OG_SYNTHETIC_TICKER);
    ExternalId returnedId = provider.getInstrument(NOW, Tenor.ONE_DAY, Tenor.THREE_MONTHS, 2);
    assertEquals(ExternalSchemes.syntheticSecurityId("ABM4 CD"), returnedId);
  }

}