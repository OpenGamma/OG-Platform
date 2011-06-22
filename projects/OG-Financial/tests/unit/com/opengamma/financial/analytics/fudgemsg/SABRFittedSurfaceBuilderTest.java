/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.analytics.TestsDataSets;
import com.opengamma.financial.analytics.volatility.sabr.SABRFittedSurfaces;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class SABRFittedSurfaceBuilderTest extends AnalyticsTestBase {

  @Test
  public void test() {
    final SABRInterestRateParameters sabrResults = TestsDataSets.createSABR1();
    final VolatilitySurface alphaSurface = sabrResults.getAlphaSurface();
    final VolatilitySurface betaSurface = sabrResults.getBetaSurface();
    final VolatilitySurface nuSurface = sabrResults.getNuSurface();
    final VolatilitySurface rhoSurface = sabrResults.getRhoSurface();
    final Currency currency = Currency.AUD;
    final DayCount dayCount = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    final SABRFittedSurfaces fits = new SABRFittedSurfaces(alphaSurface, betaSurface, nuSurface, rhoSurface, currency, dayCount);
    assertEquals(fits, cycleObject(SABRFittedSurfaces.class, fits));
  }
}
