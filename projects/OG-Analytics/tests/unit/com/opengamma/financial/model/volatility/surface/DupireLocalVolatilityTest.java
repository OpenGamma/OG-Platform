/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.financial.model.finitedifference.ConvectionDiffusionPDEDataBundle;
import com.opengamma.financial.model.finitedifference.ConvectionDiffusionPDESolver;
import com.opengamma.financial.model.finitedifference.DirichletBoundaryCondition;
import com.opengamma.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.financial.model.finitedifference.HyperbolicMeshing;
import com.opengamma.financial.model.finitedifference.MeshingFunction;
import com.opengamma.financial.model.finitedifference.NeumannBoundaryCondition;
import com.opengamma.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.financial.model.finitedifference.PDEResults1D;
import com.opengamma.financial.model.finitedifference.ThetaMethodFiniteDifference;
import com.opengamma.financial.model.finitedifference.applications.PDEDataBundleProvider;
import com.opengamma.financial.model.finitedifference.applications.PDEUtilityTools;
import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.math.interpolation.FlatExtrapolator1D;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.data.Interpolator1DDoubleQuadraticDataBundle;
import com.opengamma.math.surface.FunctionalDoublesSurface;

/**
 * 
 */
public class DupireLocalVolatilityTest {
  private static final DoubleQuadraticInterpolator1D INTERPOLATOR_1D = new DoubleQuadraticInterpolator1D();
  private static final CombinedInterpolatorExtrapolator EXTRAPOLATOR_1D = new CombinedInterpolatorExtrapolator(INTERPOLATOR_1D, new FlatExtrapolator1D());
  @SuppressWarnings("unused")
  private static final GridInterpolator2D GRID_INTERPOLATOR2D = new GridInterpolator2D(EXTRAPOLATOR_1D, EXTRAPOLATOR_1D);

  private static final DupireLocalVolatilityCalculator DUPIRE = new DupireLocalVolatilityCalculator();
  private static final SABRHaganVolatilityFunction SABR = new SABRHaganVolatilityFunction();
  private static final double SPOT = 0.04;
  private static final double STRIKE = 0.05;
  private static final double EXPIRY = 5.0;
  private static final double ATM_VOL = 0.2;
  private static final double ALPHA;
  private static final double BETA = 0.5;
  private static final double RHO = -0.2;
  private static final double NU = 0.3;
  private static final SABRFormulaData SABR_DATA;
  private static final double RATE = 0.00; //turn back to 5%
  private static final ForwardCurve FORWARD_CURVE = new ForwardCurve(SPOT, RATE);

  private static final PriceSurface PRICE_SURFACE;
  private static final BlackVolatilitySurface SABR_SURFACE;
  private static AbsoluteLocalVolatilitySurface ABS_LOCAL_VOL;
  private static LocalVolatilitySurface LOCAL_VOL;
  /**
   * 
   */
  static {
    ALPHA = ATM_VOL * Math.pow(SPOT, 1 - BETA);
    SABR_DATA = new SABRFormulaData(ALPHA, BETA, RHO, NU);

    final Function<Double, Double> sabrSurface = new Function<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double... x) {
        final double t = x[0];
        final double k = x[1];
        final SABRFormulaData sabrdata = new SABRFormulaData(ALPHA, BETA, RHO, NU);
        final EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);
        final Function1D<SABRFormulaData, Double> func = SABR.getVolatilityFunction(option, SPOT * Math.exp(RATE * t));
        return func.evaluate(sabrdata);
      }
    };

    SABR_SURFACE = new BlackVolatilitySurface(FunctionalDoublesSurface.from(sabrSurface));

    final BlackPriceFunction func = new BlackPriceFunction();

    final Function<Double, Double> priceSurface = new Function<Double, Double>() {

      @Override
      public Double evaluate(final Double... x) {
        final double t = x[0];
        final double k = x[1];
        final double sigma = sabrSurface.evaluate(x);
        final double df = Math.exp(-RATE * t);
        final BlackFunctionData data = new BlackFunctionData(SPOT / df, df, sigma);
        final EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);
        final Function1D<BlackFunctionData, Double> pfunc = func.getPriceFunction(option);
        final double price = pfunc.evaluate(data);
        if (Double.isNaN(price)) {
          System.out.println("fuck");
        }
        return price;
      }
    };

    PRICE_SURFACE = new PriceSurface(FunctionalDoublesSurface.from(priceSurface));

    LOCAL_VOL = DUPIRE.getLocalVolatility(SABR_SURFACE, FORWARD_CURVE);
    ABS_LOCAL_VOL = DUPIRE.getAbsoluteLocalVolatilitySurface(SABR_SURFACE, SPOT, RATE);

  }

  @SuppressWarnings("deprecation")
  @Test
  public void debugTest() {
    final double t = 3.0;
    final double f = 0.04;
    DUPIRE.debug(PRICE_SURFACE, SABR_SURFACE, SPOT, RATE, t, f);
  }

  @Test(enabled = false)
  public void printSurfaces() {
    //    SABRHaganVolatilityFunction sabr = new SABRHaganVolatilityFunction();
    //    double k = 0.01;
    //    for (int i = 0; i < 10; i++) {
    //      double t = 0.5 + i * 20 / 9.;
    //      double vol1 = SABR_SURFACE.getVolatility(t, k);
    //      double vol2 = sabr.getVolatility(SPOT, k, t, ALPHA, BETA, RHO, NU);
    //      System.out.println(t + "\t" + vol1 + "\t" + vol2);
    // }
    PDEUtilityTools.printSurface("Imp Vol", SABR_SURFACE.getSurface(), 0., 5., 0.1 * SPOT, 3 * SPOT);
    PDEUtilityTools.printSurface("Loc Vol", LOCAL_VOL.getSurface(), 0., 5., 0.1 * SPOT, 3 * SPOT);
    PDEUtilityTools.printSurface("ABs Loc Vol", ABS_LOCAL_VOL.getSurface(), 0., 5., 0.1 * SPOT, 3 * SPOT);
  }

  //TODO move test
  @SuppressWarnings("deprecation")
  @Test(enabled =false)
  public void pdePriceTest() {
    final double shift = 1e-3 * SPOT;
    PDEDataBundleProvider provider = new PDEDataBundleProvider();
    ConvectionDiffusionPDEDataBundle db = provider.getForwardLocalVol(SPOT, true, LOCAL_VOL);
    LocalVolatilitySurface lvUp = DUPIRE.getLocalVolatility(SABR_SURFACE, SPOT + shift, RATE);
    LocalVolatilitySurface lvDown = DUPIRE.getLocalVolatility(SABR_SURFACE, SPOT - shift, RATE);
    ConvectionDiffusionPDEDataBundle dbUp = provider.getForwardLocalVol(SPOT + shift, true, lvUp);
    ConvectionDiffusionPDEDataBundle dbDown = provider.getForwardLocalVol(SPOT - shift, true, lvDown);
    //ConvectionDiffusionPDEDataBundle db = provider.getBackwardsLocalVol(FORWARD_CURVE, 1.1 * SPOT, 5.0, 0.0, true, ABS_LOCAL_VOL2);
    ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(0.5, true);

    int nStrikeNodes = 100;
    int nTimeNodes = 50;
    double upperLevel = 3.5 * SPOT;
    BoundaryCondition lower = new DirichletBoundaryCondition(SPOT, 0);
    BoundaryCondition lowerUp = new DirichletBoundaryCondition(SPOT + shift, 0);
    BoundaryCondition lowerDown = new DirichletBoundaryCondition(SPOT - shift, 0);
    BoundaryCondition upper = new NeumannBoundaryCondition(0.0, upperLevel, false);
    MeshingFunction timeMesh = new ExponentialMeshing(0.0, EXPIRY, nTimeNodes, 3.0);
    MeshingFunction spaceMesh = new ExponentialMeshing(0.0, upperLevel, nStrikeNodes, 0.1);
    PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    PDEFullResults1D res = (PDEFullResults1D) solver.solve(db, grid, lower, upper);
    PDEFullResults1D resUp = (PDEFullResults1D) solver.solve(dbUp, grid, lowerUp, upper);
    PDEFullResults1D resDown = (PDEFullResults1D) solver.solve(dbDown, grid, lowerDown, upper);
    //  PDEUtilityTools.printSurface("pde results", res);

    //  Map<DoublesPair, Double> vol = PDEUtilityTools.priceToImpliedVol(FORWARD_CURVE, res, 0, 5.0, 0.4 * SPOT, 2.2 * SPOT);

    SABRHaganVolatilityFunction sabr = new SABRHaganVolatilityFunction();
    double[] d1 = new double[5];
    double[][] d2 = new double[2][2];

    int tIndex = nTimeNodes - 1;
    double t = grid.getTimeNode(tIndex);
    final int n = grid.getNumSpaceNodes();
    for (int i = 0; i < n; i++) {
      double k = grid.getSpaceNode(i);
      if (k >= 0.2 * SPOT && k < 3.0 * SPOT) {
        EuropeanVanillaOption option = new EuropeanVanillaOption(k, t, true);

        double sabrVol = SABR_SURFACE.getVolatility(t, k);
        double price = res.getFunctionValue(i, tIndex);
        double modelVol = BlackFormulaRepository.impliedVolatility(price, SPOT, k, t, true);
        double sabrUp = sabr.getVolatility(option, SPOT + shift, SABR_DATA);
        double sabrDown = sabr.getVolatility(option, SPOT - shift, SABR_DATA);
        double priceUp = BlackFormulaRepository.price(SPOT + shift, k, t, sabrUp, true);
        double priceDown = BlackFormulaRepository.price(SPOT - shift, k, t, sabrDown, true);
        // assertEquals(sabrVol, modelVol, 2e-4); //2bps error

        double bsDelta = BlackFormulaRepository.delta(SPOT, k, t, sabrVol, true);
        double bsVega = BlackFormulaRepository.vega(SPOT, k, t, sabrVol);
        double bsGamma = BlackFormulaRepository.gamma(SPOT, k, t, sabrVol);
        double bsVanna = BlackFormulaRepository.vanna(SPOT, k, t, sabrVol);
        sabr.getVolatilityAdjoint2(new EuropeanVanillaOption(k, t, true), SPOT, SABR_DATA, d1, d2);
        double delta = bsDelta + bsVega * d1[0];
        @SuppressWarnings("unused")
        double gamma = bsGamma + 2 * bsVanna * d1[0] + bsVega * d2[0][0];

        double deltaFD = (priceUp - priceDown) / 2 / shift;
        double deltaPDE = (resUp.getFunctionValue(i, tIndex) - resDown.getFunctionValue(i, tIndex)) / 2 / shift;
        //    assertEquals(deltaFD, delta, 2e-4);
        System.out.println(k + "\t" + sabrVol + "\t" + modelVol + "\t" + bsDelta + "\t" + delta + "\t" + deltaPDE + "\t" + deltaFD);
      }
    }
  }

  @Test
  public void pdeGreekTest() {
    PDEDataBundleProvider provider = new PDEDataBundleProvider();
    ConvectionDiffusionPDEDataBundle db = provider.getBackwardsLocalVol(STRIKE, EXPIRY, true, LOCAL_VOL);
    ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(0.5, false);
    final double forward = FORWARD_CURVE.getForward(EXPIRY);

    final int nTimeNodes = 50;
    final int nSpotNodes = 100;
    final double upperLevel = 3.5 * SPOT;

    BoundaryCondition lower = new DirichletBoundaryCondition(0, 0);
    BoundaryCondition upper = new NeumannBoundaryCondition(1.0, upperLevel, false);
    MeshingFunction timeMesh = new ExponentialMeshing(0.0, EXPIRY, nTimeNodes, 6.0);
    MeshingFunction spaceMesh = new HyperbolicMeshing(0, upperLevel, STRIKE, nSpotNodes, 0.05);
    PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    PDEResults1D res = solver.solve(db, grid, lower, upper);

    int spotIndex = grid.getLowerBoundIndexForSpace(SPOT);
    double[] spot = new double[4];
    double[] vol = new double[4];
    for (int i = 0; i < 4; i++) {
      spot[i] = grid.getSpaceNode(i + spotIndex - 1);
      double price = res.getFunctionValue(i + spotIndex - 1);
      vol[i] = BlackFormulaRepository.impliedVolatility(price, forward, STRIKE, EXPIRY, true);
    }
    Interpolator1DDoubleQuadraticDataBundle idb = INTERPOLATOR_1D.getDataBundle(spot, vol);

    double sabrVol = SABR_SURFACE.getVolatility(EXPIRY, STRIKE);
    double modelVol = INTERPOLATOR_1D.interpolate(idb, SPOT);
    assertEquals("Volatility test", sabrVol, modelVol, 1e-4); //1bps error

    double spotValue = grid.getSpaceNode(spotIndex);
    SABRHaganVolatilityFunction sabr = new SABRHaganVolatilityFunction();
    double[] volAdjoint = sabr.getVolatilityAdjoint(new EuropeanVanillaOption(STRIKE, EXPIRY, true), spotValue, SABR_DATA);

    sabrVol = volAdjoint[0];

    double bsDelta = BlackFormulaRepository.delta(spotValue, STRIKE, EXPIRY, sabrVol, true);
    double bsVega = BlackFormulaRepository.vega(spotValue, STRIKE, EXPIRY, sabrVol);

    double delta = bsDelta + bsVega * volAdjoint[1];
    double pdeDelta = res.getFirstSpatialDerivative(spotIndex);
    //    System.out.println(spotValue + "\t" + grid.getSpaceNode(spotIndex) + "\t" + bsDelta + "\t" + delta + "\t" + pdeDelta);
    assertEquals("Delta test", delta, pdeDelta, 1e-2);

    double bsVanna = BlackFormulaRepository.vanna(spotValue, STRIKE, EXPIRY, sabrVol);
    double bsGamma = BlackFormulaRepository.gamma(spotValue, STRIKE, EXPIRY, sabrVol);

    double[] volD1 = new double[5];
    double[][] volD2 = new double[2][2];
    sabr.getVolatilityAdjoint2(new EuropeanVanillaOption(STRIKE, EXPIRY, true), spotValue, SABR_DATA, volD1, volD2);
    double d2Sigmad2Fwd = volD2[0][0];

    double gamma = bsGamma + 2 * bsVanna * volAdjoint[1] + bsVega * d2Sigmad2Fwd;
    double pdeGamma = res.getSecondSpatialDerivative(spotIndex);
    //TODO The gamma resolution is poor
    assertEquals("Gamma test", gamma, pdeGamma, 3e-2 * gamma);
  }

  @Test(enabled = false)
  public void printPriceTest() {

    double t;
    double k;
    double price;

    for (int j = 0; j < 101; j++) {
      t = 0.01 + 5.0 * j / 100.0;
      System.out.print("\t" + t);
    }
    System.out.print("\n");

    for (int i = 0; i < 101; i++) {
      k = 0.001 + 0.15 * i / 100.0;
      System.out.print(k);
      for (int j = 0; j < 101; j++) {
        t = 0.01 + 5.0 * j / 100.0;
        price = PRICE_SURFACE.getPrice(t, k);
        System.out.print("\t" + price);
      }
      System.out.print("\n");
    }
  }

  @Test(enabled = false)
  public void priceTest() {
    final DupireLocalVolatilityCalculator cal = new DupireLocalVolatilityCalculator();
    final LocalVolatilitySurface locVol = cal.getLocalVolatility(PRICE_SURFACE, SPOT, RATE, 0.0);
    double t;
    double f;
    double vol;

    for (int j = 0; j < 101; j++) {
      t = 0.01 + 5.0 * j / 100.0;
      System.out.print("\t" + t);
    }
    System.out.print("\n");

    for (int i = 0; i < 101; i++) {
      f = 0.001 + 0.15 * i / 100.0;
      System.out.print(f);
      for (int j = 0; j < 101; j++) {
        t = 0.01 + 5.0 * j / 100.0;
        vol = locVol.getVolatility(t, f);
        System.out.print("\t" + vol);
      }
      System.out.print("\n");
    }
  }

  @SuppressWarnings("deprecation")
  @Test(enabled = false)
  public void volTest() {
    final DupireLocalVolatilityCalculator cal = new DupireLocalVolatilityCalculator();
    final LocalVolatilitySurface locVol = cal.getLocalVolatility(SABR_SURFACE, SPOT, RATE);
    double t;
    double f;
    double vol;

    for (int j = 0; j < 101; j++) {
      t = 0.01 + 5.0 * j / 100.0;
      System.out.print("\t" + t);
    }
    System.out.print("\n");

    for (int i = 0; i < 101; i++) {
      f = 0.001 + 0.15 * i / 100.0;
      System.out.print(f);
      for (int j = 0; j < 101; j++) {
        t = 0.01 + 5.0 * j / 100.0;
        vol = locVol.getVolatility(t, f);
        System.out.print("\t" + vol);
      }
      System.out.print("\n");
    }
  }

}
