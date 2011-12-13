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
import com.opengamma.financial.analytics.volatility.cube.fitting.FittedSmileDataPoints;
import com.opengamma.financial.analytics.volatility.fittedresults.HestonFittedSurfaces;
import com.opengamma.financial.analytics.volatility.fittedresults.SABRFittedSurfaces;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.id.ExternalId;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class SmileFittedSurfaceBuilderTest extends AnalyticsTestBase {

  @Test
  public void testSABR() {
    final SABRInterestRateParameters sabrResults = TestsDataSets.createSABR1();
    final InterpolatedDoublesSurface alphaSurface = sabrResults.getAlphaSurface();
    final InterpolatedDoublesSurface betaSurface = sabrResults.getBetaSurface();
    final InterpolatedDoublesSurface nuSurface = sabrResults.getNuSurface();
    final InterpolatedDoublesSurface rhoSurface = sabrResults.getRhoSurface();
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
    final InterpolatedDoublesSurface kappaSurface = sabrResults.getAlphaSurface();
    final InterpolatedDoublesSurface thetaSurface = sabrResults.getBetaSurface();
    final InterpolatedDoublesSurface vol0Surface = sabrResults.getNuSurface();
    final InterpolatedDoublesSurface omegaSurface = sabrResults.getRhoSurface();
    final InterpolatedDoublesSurface rhoSurface = TestsDataSets.createSABR1AlphaBumped().getRhoSurface();
    final Currency currency = Currency.AUD;
    final Map<DoublesPair, DoubleMatrix2D> inverseJacobians = new HashMap<DoublesPair, DoubleMatrix2D>();
    inverseJacobians.put(new DoublesPair(0, 1), new DoubleMatrix2D(new double[][] { {1, 2}, {3, 4}}));
    inverseJacobians.put(new DoublesPair(2, 1), new DoubleMatrix2D(new double[][] { {10, 20}, {30, 40}}));
    final HestonFittedSurfaces fits = new HestonFittedSurfaces(kappaSurface, thetaSurface, vol0Surface, omegaSurface, rhoSurface, inverseJacobians, currency);
    assertEquals(fits, cycleObject(HestonFittedSurfaces.class, fits));
  }

  @Test
  public void testSABRFittedDataPoints() {
    final Map<Pair<Tenor, Tenor>, ExternalId[]> externalIds = new HashMap<Pair<Tenor, Tenor>, ExternalId[]>();
    final Map<Pair<Tenor, Tenor>, Double[]> relativeStrikes = new HashMap<Pair<Tenor, Tenor>, Double[]>();
    externalIds.put(Pair.of(Tenor.ONE_YEAR, Tenor.ONE_YEAR),
        new ExternalId[] {ExternalId.of("TEST", "USSV0101A"), ExternalId.of("TEST", "USSV0101B"), ExternalId.of("TEST", "USSV0101C"), ExternalId.of("TEST", "USSV0101D")});
    relativeStrikes.put(Pair.of(Tenor.ONE_YEAR, Tenor.ONE_YEAR), new Double[] {-100., -50., 0., 50.});
    externalIds.put(Pair.of(Tenor.ONE_YEAR, Tenor.ofYears(5)),
        new ExternalId[] {ExternalId.of("TEST", "USSV0105A"), ExternalId.of("TEST", "USSV0105B"), ExternalId.of("TEST", "USSV0105C"), ExternalId.of("TEST", "USSV0105D")});
    relativeStrikes.put(Pair.of(Tenor.ONE_YEAR, Tenor.ofYears(5)), new Double[] {-50., 0., 50., 100.});
    externalIds.put(Pair.of(Tenor.ofYears(5), Tenor.ONE_YEAR),
        new ExternalId[] {ExternalId.of("TEST", "USSV0501A"), ExternalId.of("TEST", "USSV0501B"), ExternalId.of("TEST", "USSV0501C"), ExternalId.of("TEST", "USSV0501D")});
    relativeStrikes.put(Pair.of(Tenor.ofYears(5), Tenor.ONE_YEAR), new Double[] {-50., 0., 50., 100.});
    externalIds.put(Pair.of(Tenor.ofYears(7), Tenor.ofYears(3)), new ExternalId[] {ExternalId.of("TEST", "USSV0703A"), ExternalId.of("TEST", "USSV0703B"), ExternalId.of("TEST", "USSV0703C"),
        ExternalId.of("TEST", "USSV0703D")});
    relativeStrikes.put(Pair.of(Tenor.ofYears(7), Tenor.ofYears(3)), new Double[] {-100., 0., 50., 100.});
    final FittedSmileDataPoints object = new FittedSmileDataPoints(externalIds, relativeStrikes);
    assertEquals(object, cycleObject(FittedSmileDataPoints.class, object));
  }
}
