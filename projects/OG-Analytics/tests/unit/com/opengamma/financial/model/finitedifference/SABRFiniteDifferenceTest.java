/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.model.finitedifference.applications.PDEDataBundleProvider;
import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.financial.model.volatility.surface.AbsoluteLocalVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.DupireLocalVolatilityCalculator;
import com.opengamma.financial.model.volatility.surface.PriceSurface;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.surface.FunctionalDoublesSurface;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
@Test
@SuppressWarnings("unused")
public class SABRFiniteDifferenceTest {
  
  private static final PDEDataBundleProvider PDE_DATA_PROVIDER = new PDEDataBundleProvider();

  private static final BlackPriceFunction BLACK_PRICE_FUNCTION = new BlackPriceFunction();
  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();
  private static final SABRHaganVolatilityFunction SABR = new SABRHaganVolatilityFunction();
  private static final double SPOT = 0.04;
  private static final double STRIKE;
  private static final double ATM_VOL = 0.2;
  private static final double ALPHA;
  private static final double BETA = 0.5;
  private static final double RHO = -0.6;
  private static final double NU = 0.3;
  private static final double RATE = 0.00;
  private static final double T = 5.0;
  private static final ForwardCurve FORWARD = new ForwardCurve(SPOT);
  private static final YieldAndDiscountCurve YIELD_CURVE = new YieldCurve(ConstantDoublesCurve.from(RATE));
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 7, 1);
  private static final EuropeanVanillaOption OPTION;
  private static final ConvectionDiffusionPDEDataBundle DATA;

  private static BoundaryCondition LOWER;
  private static BoundaryCondition UPPER;


  private static final PriceSurface SABR_PRICE_SURFACE;
  private static final BlackVolatilitySurface SABR_VOL_SURFACE;
  private static final AbsoluteLocalVolatilitySurface SABR_LOCAL_VOL;
  /**
   * 
   */
  static {

    ALPHA = ATM_VOL * Math.pow(SPOT, 1 - BETA);

    final Function<Double, Double> sabrSurface = new Function<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... x) {
        final double t = x[0];
        final double k = x[1];
        final SABRFormulaData sabrdata = new SABRFormulaData( ALPHA, BETA, RHO, NU);
        final EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);
        final Function1D<SABRFormulaData, Double> func = SABR.getVolatilityFunction(option,SPOT * Math.exp(RATE * t));
        return func.evaluate(sabrdata);
      }
    };

    SABR_VOL_SURFACE = new BlackVolatilitySurface(FunctionalDoublesSurface.from(sabrSurface));

    final Function<Double, Double> priceSurface = new Function<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... x) {
        final double t = x[0];
        final double k = x[1];
        final double sigma = sabrSurface.evaluate(x);
        final double df = YIELD_CURVE.getDiscountFactor(t);
        final BlackFunctionData data = new BlackFunctionData(SPOT / df, df, sigma);
        final EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);
        final Function1D<BlackFunctionData, Double> pfunc = BLACK_PRICE_FUNCTION.getPriceFunction(option);
        final double price = pfunc.evaluate(data);
        return price;
      }
    };

    SABR_PRICE_SURFACE = new PriceSurface(FunctionalDoublesSurface.from(priceSurface));

    final DupireLocalVolatilityCalculator cal = new DupireLocalVolatilityCalculator();
    SABR_LOCAL_VOL = cal.getAbsoluteLocalVolatilitySurface(SABR_VOL_SURFACE, SPOT, RATE);

    STRIKE = SPOT / YIELD_CURVE.getDiscountFactor(T);

    OPTION = new EuropeanVanillaOption(STRIKE, T, true);

    LOWER = new DirichletBoundaryCondition(0, 0.0);
    UPPER = new FixedSecondDerivativeBoundaryCondition(0.0, 5.0 * SPOT, false);


    DATA = PDE_DATA_PROVIDER.getBackwardsLocalVol(FORWARD, STRIKE, T, 0.0, true, SABR_LOCAL_VOL);
  }

  public void test() {
    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(0.5, false);
    final int tNodes = 20;
    final int xNodes = 101;
    final PDEGrid1D grid = new PDEGrid1D(tNodes, xNodes, T, LOWER.getLevel(), UPPER.getLevel());
    final PDEResults1D res = solver.solve(DATA, grid, LOWER, UPPER);

    final int i = (int) (xNodes * SPOT / UPPER.getLevel());
    final double spot = res.getSpaceValue(i);
    final double price = res.getFunctionValue(i);
    final double df = YIELD_CURVE.getDiscountFactor(T);

    assertEquals(SPOT, spot, 1e-9);
    assertEquals(SABR_PRICE_SURFACE.getPrice(T, STRIKE), price, price * 2e-3);

    final BlackFunctionData data = new BlackFunctionData(spot / df, df, 0.0);

    double impVol;
    try {
      impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, OPTION, price);
    } catch (final Exception e) {
      impVol = 0.0;
    }
    assertEquals(SABR_VOL_SURFACE.getVolatility(T, STRIKE), impVol, 1e-3);

  }
}
