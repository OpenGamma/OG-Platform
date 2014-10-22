/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.datasets.CalendarUSD;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.NormalDataSets;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.test.TestGroup;

/**
 * Test of provider with normal implied volatility expiry and tenor dependent.
 */
@Test(groups = TestGroup.UNIT)
public class NormalSwaptionExpiryTenorProviderTest {
  
  /** Data */
  private static final MulticurveProviderDiscount MULTICURVE = 
      MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final InterpolatedDoublesSurface NORMAL_SURFACE =
      NormalDataSets.normalSurfaceSwaptionExpiryTenor();
  /** Conventions. */
  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_FIXED_IBOR_MASTER = 
      GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = 
      GENERATOR_SWAP_FIXED_IBOR_MASTER.getGenerator(GeneratorSwapFixedIborMaster.USD6MLIBOR3M, NYC);
  /** Provider. */
  private static final NormalSwaptionExpiryTenorProvider MULTICURVE_BACHELIER_SWAPTION =
      new NormalSwaptionExpiryTenorProvider(MULTICURVE, NORMAL_SURFACE, USD6MLIBOR3M);
  
  private static final double TOLERANCE_VOL = 1.0E-8;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullMulticurve() {
    new NormalSwaptionExpiryTenorProvider(null, NORMAL_SURFACE, USD6MLIBOR3M);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullSurface() {
    new NormalSwaptionExpiryTenorProvider(MULTICURVE, null, USD6MLIBOR3M);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullGenerator() {
    new NormalSwaptionExpiryTenorProvider(MULTICURVE, NORMAL_SURFACE, null);
  }
  
  @Test
  public void getter() {
    assertEquals("NormalSwaptionExpiryTenorProvider: getter", 
        MULTICURVE_BACHELIER_SWAPTION.getMulticurveProvider(), MULTICURVE);
    assertEquals("NormalSwaptionExpiryTenorProvider: getter", 
        MULTICURVE_BACHELIER_SWAPTION.getGeneratorSwap(), USD6MLIBOR3M);
  }
  

  @Test
  public void volatility() {
    double expiry = 1.234;
    double tenor = 7.0;
    double volatilityExpected = NORMAL_SURFACE.getZValue(expiry, tenor);
    double volatilityComputed = MULTICURVE_BACHELIER_SWAPTION.getVolatility(expiry, tenor, 0.0, 0.0);
    double volatilityComputed2 = MULTICURVE_BACHELIER_SWAPTION.getVolatility(expiry, tenor, 0.10, -0.10);
    assertEquals("NormalSwaptionExpiryTenorProvider: volatility", 
        volatilityExpected, volatilityComputed, TOLERANCE_VOL);
    assertEquals("NormalSwaptionExpiryTenorProvider: volatility", 
        volatilityExpected, volatilityComputed2, TOLERANCE_VOL);
  }
  
}
