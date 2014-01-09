package com.opengamma.analytics.financial.model.option.pricing.fourier;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashSet;

import org.apache.commons.lang.NotImplementedException;
import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.BlackOptionDataBundle;
import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.OptionModel;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BlackScholesMertonModel;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class FFTOptionModelTest {
  private static final HashSet<Greek> GREEKS = Sets.newHashSet(Greek.FAIR_PRICE);
  private static final double R = 0.005;
  private static final YieldCurve YIELD_CURVE = YieldCurve.from(ConstantDoublesCurve.from(R));
  private static final double BLACK_VOL = 0.34;
  private static final VolatilitySurface VOLATILITY_SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(BLACK_VOL));
  private static final double FORWARD = 100;
  private static final double T = 2;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2011, 1, 1);
  private static final ZonedDateTime MATURITY = DATE.plusYears((int) T);
  private static final Expiry EXPIRY = new Expiry(MATURITY);
  private static final EuropeanVanillaOptionDefinition ITM_CALL = new EuropeanVanillaOptionDefinition(99, EXPIRY, true);
  private static final EuropeanVanillaOptionDefinition OTM_CALL = new EuropeanVanillaOptionDefinition(101, EXPIRY, true);
  private static final EuropeanVanillaOptionDefinition ITM_PUT = new EuropeanVanillaOptionDefinition(101, EXPIRY, false);
  private static final EuropeanVanillaOptionDefinition OTM_PUT = new EuropeanVanillaOptionDefinition(99, EXPIRY, false);
  private static final MartingaleCharacteristicExponent GAUSSIAN = new GaussianMartingaleCharacteristicExponent(BLACK_VOL);
  private static final StandardOptionDataBundle BSM_DATA = new StandardOptionDataBundle(YIELD_CURVE, R, VOLATILITY_SURFACE, Math.exp(-R * T) * FORWARD, DATE);
  private static final BlackOptionDataBundle BLACK_DATA = new BlackOptionDataBundle(FORWARD, YIELD_CURVE, VOLATILITY_SURFACE, DATE);
  private static final OptionModel<EuropeanVanillaOptionDefinition, BlackOptionDataBundle> FFT_MODEL = new FFTOptionModel(GAUSSIAN);
  private static final OptionModel<OptionDefinition, StandardOptionDataBundle> BSM_MODEL = new BlackScholesMertonModel();
  private static final double EPS = 1e-2;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCharacteristicExponent1() {
    new FFTOptionModel(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCharacteristicExponent2() {
    new FFTOptionModel(null, 100, 10, -0.5, 1e-8);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeNStrikes() {
    new FFTOptionModel(GAUSSIAN, -100, 10, -0.5, 1e-8);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeDelta() {
    new FFTOptionModel(GAUSSIAN, 100, -10, -0.5, 1e-8);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZeroAlpha() {
    new FFTOptionModel(GAUSSIAN, 100, 10, 0, 1e-8);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAlpha() {
    new FFTOptionModel(GAUSSIAN, 100, 10, -1, 1e-8);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeTolerance() {
    new FFTOptionModel(GAUSSIAN, 100, 10, -1, 1e-8);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDefinition() {
    FFT_MODEL.getGreeks(null, BLACK_DATA, GREEKS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    FFT_MODEL.getGreeks(ITM_CALL, null, GREEKS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullGreeks() {
    FFT_MODEL.getGreeks(ITM_CALL, BLACK_DATA, null);
  }

  @Test(expectedExceptions = NotImplementedException.class)
  public void testWrongGreeks() {
    FFT_MODEL.getGreeks(ITM_CALL, BLACK_DATA, Sets.newHashSet(Greek.DELTA, Greek.GAMMA));
  }

  @Test
  public void testPricing() {
    GreekResultCollection fftPrice = FFT_MODEL.getGreeks(ITM_CALL, BLACK_DATA, GREEKS);
    GreekResultCollection bsmPrice = BSM_MODEL.getGreeks(ITM_CALL, BSM_DATA, GREEKS);
    assertEquals(fftPrice.size(), 1);
    assertEquals(fftPrice.get(Greek.FAIR_PRICE), bsmPrice.get(Greek.FAIR_PRICE), EPS);
    fftPrice = FFT_MODEL.getGreeks(OTM_CALL, BLACK_DATA, GREEKS);
    bsmPrice = BSM_MODEL.getGreeks(OTM_CALL, BSM_DATA, GREEKS);
    assertEquals(fftPrice.size(), 1);
    assertEquals(fftPrice.get(Greek.FAIR_PRICE), bsmPrice.get(Greek.FAIR_PRICE), EPS);
    fftPrice = FFT_MODEL.getGreeks(OTM_PUT, BLACK_DATA, GREEKS);
    bsmPrice = BSM_MODEL.getGreeks(OTM_PUT, BSM_DATA, GREEKS);
    assertEquals(fftPrice.size(), 1);
    assertEquals(fftPrice.get(Greek.FAIR_PRICE), bsmPrice.get(Greek.FAIR_PRICE), EPS);
    fftPrice = FFT_MODEL.getGreeks(ITM_PUT, BLACK_DATA, GREEKS);
    bsmPrice = BSM_MODEL.getGreeks(ITM_PUT, BSM_DATA, GREEKS);
    assertEquals(fftPrice.size(), 1);
    assertEquals(fftPrice.get(Greek.FAIR_PRICE), bsmPrice.get(Greek.FAIR_PRICE), EPS);
  }

}
