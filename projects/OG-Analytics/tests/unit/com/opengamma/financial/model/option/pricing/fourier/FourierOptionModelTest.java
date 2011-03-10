package com.opengamma.financial.model.option.pricing.fourier;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.NotImplementedException;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.definition.BlackOptionDataBundle;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.pricing.OptionModel;
import com.opengamma.financial.model.option.pricing.analytic.BlackScholesMertonModel;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.math.surface.ConstantDoublesSurface;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

public class FourierOptionModelTest {
  private static final HashSet<Greek> GREEKS = Sets.newHashSet(Greek.FAIR_PRICE);
  private static final double R = 0.005;
  private static final YieldCurve YIELD_CURVE = new YieldCurve(ConstantDoublesCurve.from(R));
  private static final double BLACK_VOL = 0.34;
  private static final VolatilitySurface VOLATILITY_SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(BLACK_VOL));
  private static final double FORWARD = 100;
  private static final double T = 2;
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2011, 1, 1);
  private static final ZonedDateTime MATURITY = DATE.plusYears((int) T);
  private static final Expiry EXPIRY = new Expiry(MATURITY);
  private static final EuropeanVanillaOptionDefinition ITM_CALL = new EuropeanVanillaOptionDefinition(99, EXPIRY, true);
  private static final EuropeanVanillaOptionDefinition OTM_CALL = new EuropeanVanillaOptionDefinition(101, EXPIRY, true);
  private static final EuropeanVanillaOptionDefinition ITM_PUT = new EuropeanVanillaOptionDefinition(101, EXPIRY, false);
  private static final EuropeanVanillaOptionDefinition OTM_PUT = new EuropeanVanillaOptionDefinition(99, EXPIRY, false);
  private static final CharacteristicExponent1 GAUSSIAN = new GaussianCharacteristicExponent1(BLACK_VOL * BLACK_VOL / 2, BLACK_VOL);
  private static final StandardOptionDataBundle BSM_DATA = new StandardOptionDataBundle(YIELD_CURVE, R, VOLATILITY_SURFACE, Math.exp(-R * T) * FORWARD, DATE);
  private static final BlackOptionDataBundle BLACK_DATA = new BlackOptionDataBundle(FORWARD, YIELD_CURVE, VOLATILITY_SURFACE, DATE);
  private static final OptionModel<EuropeanVanillaOptionDefinition, BlackOptionDataBundle> FOURIER_MODEL1 = new FourierOptionModel(GAUSSIAN);
  private static final OptionModel<EuropeanVanillaOptionDefinition, BlackOptionDataBundle> FOURIER_MODEL2 = new FourierOptionModel(GAUSSIAN, true);
  private static final OptionModel<OptionDefinition, StandardOptionDataBundle> BSM_MODEL = new BlackScholesMertonModel();
  private static final double EPS = 1e-3;

  @Test(expected = IllegalArgumentException.class)
  public void testNullExponent1() {
    new FourierOptionModel(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullExponent2() {
    new FourierOptionModel(null, new RungeKuttaIntegrator1D());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullExponent3() {
    new FourierOptionModel(null, -0.4, 1e-7, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullExponent4() {
    new FourierOptionModel(null, new RungeKuttaIntegrator1D(), -0.4, 1e-6, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullExponent5() {
    new FourierOptionModel(null, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullExponent6() {
    new FourierOptionModel(null, new RungeKuttaIntegrator1D(), false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullIntegrator1() {
    new FourierOptionModel(GAUSSIAN, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullIntegrator2() {
    new FourierOptionModel(GAUSSIAN, null, -0.5, 1e-8, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullIntegrator3() {
    new FourierOptionModel(GAUSSIAN, null, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadAlpha1() {
    new FourierOptionModel(GAUSSIAN, 0, 1e-8, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadAlpha2() {
    new FourierOptionModel(GAUSSIAN, new RungeKuttaIntegrator1D(), -1, 1e-8, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeTolerance1() {
    new FourierOptionModel(GAUSSIAN, -0.5, -1e-8, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeTolerance2() {
    new FourierOptionModel(GAUSSIAN, new RungeKuttaIntegrator1D(), -0.5, -1e-8, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDefinition() {
    FOURIER_MODEL1.getGreeks(null, BLACK_DATA, GREEKS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    FOURIER_MODEL1.getGreeks(ITM_CALL, null, GREEKS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullGreeks() {
    FOURIER_MODEL1.getGreeks(ITM_CALL, BLACK_DATA, null);
  }

  @Test(expected = NotImplementedException.class)
  public void testWrongGreeks() {
    FOURIER_MODEL1.getGreeks(ITM_CALL, BLACK_DATA, Sets.newHashSet(Greek.DELTA, Greek.GAMMA));
  }

  @Test
  public void testPricing() {
    GreekResultCollection fourierPrice = FOURIER_MODEL1.getGreeks(ITM_CALL, BLACK_DATA, GREEKS);
    GreekResultCollection bsmPrice = BSM_MODEL.getGreeks(ITM_CALL, BSM_DATA, GREEKS);
    assertEquals(fourierPrice.size(), 1);
    assertEquals(fourierPrice.get(Greek.FAIR_PRICE), bsmPrice.get(Greek.FAIR_PRICE), EPS);
    fourierPrice = FOURIER_MODEL1.getGreeks(OTM_CALL, BLACK_DATA, GREEKS);
    bsmPrice = BSM_MODEL.getGreeks(OTM_CALL, BSM_DATA, GREEKS);
    assertEquals(fourierPrice.size(), 1);
    assertEquals(fourierPrice.get(Greek.FAIR_PRICE), bsmPrice.get(Greek.FAIR_PRICE), EPS);
    fourierPrice = FOURIER_MODEL1.getGreeks(OTM_PUT, BLACK_DATA, GREEKS);
    bsmPrice = BSM_MODEL.getGreeks(OTM_PUT, BSM_DATA, GREEKS);
    assertEquals(fourierPrice.size(), 1);
    assertEquals(fourierPrice.get(Greek.FAIR_PRICE), bsmPrice.get(Greek.FAIR_PRICE), EPS);
    fourierPrice = FOURIER_MODEL1.getGreeks(ITM_PUT, BLACK_DATA, GREEKS);
    bsmPrice = BSM_MODEL.getGreeks(ITM_PUT, BSM_DATA, GREEKS);
    assertEquals(fourierPrice.size(), 1);
    assertEquals(fourierPrice.get(Greek.FAIR_PRICE), bsmPrice.get(Greek.FAIR_PRICE), EPS);
    fourierPrice = FOURIER_MODEL2.getGreeks(ITM_CALL, BLACK_DATA, GREEKS);
    bsmPrice = BSM_MODEL.getGreeks(ITM_CALL, BSM_DATA, GREEKS);
    assertEquals(fourierPrice.size(), 1);
    assertEquals(fourierPrice.get(Greek.FAIR_PRICE), bsmPrice.get(Greek.FAIR_PRICE), EPS);
    fourierPrice = FOURIER_MODEL2.getGreeks(OTM_CALL, BLACK_DATA, GREEKS);
    bsmPrice = BSM_MODEL.getGreeks(OTM_CALL, BSM_DATA, GREEKS);
    assertEquals(fourierPrice.size(), 1);
    assertEquals(fourierPrice.get(Greek.FAIR_PRICE), bsmPrice.get(Greek.FAIR_PRICE), EPS);
    fourierPrice = FOURIER_MODEL2.getGreeks(OTM_PUT, BLACK_DATA, GREEKS);
    bsmPrice = BSM_MODEL.getGreeks(OTM_PUT, BSM_DATA, GREEKS);
    assertEquals(fourierPrice.size(), 1);
    assertEquals(fourierPrice.get(Greek.FAIR_PRICE), bsmPrice.get(Greek.FAIR_PRICE), EPS);
    fourierPrice = FOURIER_MODEL2.getGreeks(ITM_PUT, BLACK_DATA, GREEKS);
    bsmPrice = BSM_MODEL.getGreeks(ITM_PUT, BSM_DATA, GREEKS);
    assertEquals(fourierPrice.size(), 1);
    assertEquals(fourierPrice.get(Greek.FAIR_PRICE), bsmPrice.get(Greek.FAIR_PRICE), EPS);
  }
}
