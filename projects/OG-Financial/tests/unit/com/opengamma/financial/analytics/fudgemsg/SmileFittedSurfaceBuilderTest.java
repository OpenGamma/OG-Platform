/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.financial.analytics.TestsDataSets;
import com.opengamma.financial.analytics.volatility.heston.HestonFittedSurfaces;
import com.opengamma.financial.analytics.volatility.sabr.SABRFittedSurfaces;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class SmileFittedSurfaceBuilderTest extends AnalyticsTestBase {

  @Test
  public void testSABR() {
    final SABRInterestRateParameters sabrResults = TestsDataSets.createSABR1();
    final VolatilitySurface alphaSurface = sabrResults.getAlphaSurface();
    final VolatilitySurface betaSurface = sabrResults.getBetaSurface();
    final VolatilitySurface nuSurface = sabrResults.getNuSurface();
    final VolatilitySurface rhoSurface = sabrResults.getRhoSurface();
    final Currency currency = Currency.AUD;
    final DayCount dayCount = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    final Map<DoublesPair, DoubleMatrix2D> inverseJacobians = new HashMap<DoublesPair, DoubleMatrix2D>();
    inverseJacobians.put(new DoublesPair(0, 1), new DoubleMatrix2D(new double[][] { {1, 2}, {3, 4}}));
    inverseJacobians.put(new DoublesPair(2, 1), new DoubleMatrix2D(new double[][] { {10, 20}, {30, 40}}));
    final SABRFittedSurfaces fits = new SABRFittedSurfaces(alphaSurface, betaSurface, nuSurface, rhoSurface, inverseJacobians, currency, dayCount);
    assertEquals(fits, cycleObject(SABRFittedSurfaces.class, fits));
  }

  @Test
  public void testHeston() {
    final SABRInterestRateParameters sabrResults = TestsDataSets.createSABR1();
    final VolatilitySurface kappaSurface = sabrResults.getAlphaSurface();
    final VolatilitySurface thetaSurface = sabrResults.getBetaSurface();
    final VolatilitySurface vol0Surface = sabrResults.getNuSurface();
    final VolatilitySurface omegaSurface = sabrResults.getRhoSurface();
    final VolatilitySurface rhoSurface = TestsDataSets.createSABR1AlphaBumped().getRhoSurface();
    final Currency currency = Currency.AUD;
    final Map<DoublesPair, DoubleMatrix2D> inverseJacobians = new HashMap<DoublesPair, DoubleMatrix2D>();
    inverseJacobians.put(new DoublesPair(0, 1), new DoubleMatrix2D(new double[][] { {1, 2}, {3, 4}}));
    inverseJacobians.put(new DoublesPair(2, 1), new DoubleMatrix2D(new double[][] { {10, 20}, {30, 40}}));
    final HestonFittedSurfaces fits = new HestonFittedSurfaces(kappaSurface, thetaSurface, vol0Surface, omegaSurface, rhoSurface, inverseJacobians, currency);
    assertEquals(fits, cycleObject(HestonFittedSurfaces.class, fits));
  }
}
