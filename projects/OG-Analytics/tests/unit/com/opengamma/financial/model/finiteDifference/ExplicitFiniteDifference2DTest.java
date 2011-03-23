package com.opengamma.financial.model.finiteDifference;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import org.apache.commons.lang.Validate;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.cube.Cube;
import com.opengamma.math.cube.FunctionalDoublesCube;
import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.surface.FunctionalDoublesSurface;

public class ExplicitFiniteDifference2DTest {

  private static BoundaryCondition A_LOWER;
  private static BoundaryCondition A_UPPER;
  private static BoundaryCondition B_LOWER;
  private static BoundaryCondition B_UPPER;

  private static final double SPOT_A = 100;
  private static final double SPOT_B = 100;

  private static final double T = 1.0;
  private static final double RATE = 0.0;
  // private static final YieldAndDiscountCurve YIELD_CURVE = new YieldCurve(ConstantDoublesCurve.from(RATE));
  private static final double VOL_A = 0.20;
  private static final double VOL_B = 0.30;
  private static final double RHO = -0.50;

  // private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 7, 1);
  // private static final OptionDefinition OPTION;
  private static final ConvectionDiffusion2DPDEDataBundle DATA;

  private static Cube<Double, Double, Double, Double> A;
  private static Cube<Double, Double, Double, Double> B;
  private static Cube<Double, Double, Double, Double> C;
  private static Cube<Double, Double, Double, Double> D;
  private static Cube<Double, Double, Double, Double> E;
  private static Cube<Double, Double, Double, Double> F;

  static {

    A_LOWER = new DirichletBoundaryCondition(0.0, 0.0);
    A_UPPER = new FixedSecondDerivativeBoundaryCondition(0.0, 5 * SPOT_A);
    B_LOWER = new FixedSecondDerivativeBoundaryCondition(0.0, 0.0);
    B_UPPER = new FixedSecondDerivativeBoundaryCondition(0.0, 5 * SPOT_B);

    // OPTION = new EuropeanVanillaOptionDefinition(1.0, new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, T)), true);

    final Function<Double, Double> a = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... txy) {
        Validate.isTrue(txy.length == 3);
        double x = txy[1];
        return -x * x * VOL_A * VOL_A / 2;
      }
    };
    A = FunctionalDoublesCube.from(a);

    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... txy) {
        Validate.isTrue(txy.length == 3);
        double x = txy[1];
        return -x * RATE;
      }
    };
    B = FunctionalDoublesCube.from(b);

    final Function<Double, Double> c = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... txy) {
        Validate.isTrue(txy.length == 3);
        return RATE;
      }
    };
    C = FunctionalDoublesCube.from(c);

    final Function<Double, Double> d = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... txy) {
        Validate.isTrue(txy.length == 3);
        double y = txy[2];
        return -y * y * VOL_B * VOL_B / 2;
      }
    };
    D = FunctionalDoublesCube.from(d);

    final Function<Double, Double> e = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... txy) {
        Validate.isTrue(txy.length == 3);
        double x = txy[1];
        double y = txy[2];

        return -x * y * VOL_A * VOL_B * RHO;
      }
    };
    E = FunctionalDoublesCube.from(e);

    final Function<Double, Double> f = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... txy) {
        Validate.isTrue(txy.length == 3);
        double y = txy[2];
        return -y * RATE;
      }
    };
    F = FunctionalDoublesCube.from(f);

    final Function<Double, Double> payoff = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... xy) {
        Validate.isTrue(xy.length == 2);
        double x = xy[0];
        double y = xy[1];
        // return Math.max(x-STRIKE,0);
        return Math.max(x - y, 0);
      }
    };

    DATA = new ConvectionDiffusion2DPDEDataBundle(A, B, C, D, E, F, FunctionalDoublesSurface.from(payoff));
  }

  @Test
  public void testBlackScholesEquation() {

    int timeSteps = 5000;
    int xSteps = 100;
    int ySteps = 100;

    ExplicitFiniteDifference2D solver = new ExplicitFiniteDifference2D();

    double[][] res = solver.solve(DATA, timeSteps, xSteps, ySteps, T, A_LOWER, A_UPPER, B_LOWER, B_UPPER, null);

    // for(int i=0; i <= xSteps; i++){
    // for(int j=0; j <= ySteps; j++){
    // System.out.print(res[i][j] + "\t");
    // }
    // System.out.print("\n");
    // }

    double vol = Math.sqrt(VOL_A * VOL_A + VOL_B * VOL_B - 2 * RHO * VOL_A * VOL_B);
    double forward = SPOT_A / SPOT_B;
    double strike = 1.0;
    BlackFunctionData data = new BlackFunctionData(forward, SPOT_B, vol);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, T, true);
    BlackPriceFunction pricer = new BlackPriceFunction();
    Function1D<BlackFunctionData, Double> func = pricer.getPriceFunction(option);
    double price = func.evaluate(data);

    double pdfPrice = res[(int) (SPOT_A * xSteps / (A_UPPER.getLevel() - A_LOWER.getLevel()))][(int) (SPOT_B * ySteps / (B_UPPER.getLevel() - B_LOWER.getLevel()))];

    // System.out.println(price+"\t"+pdfPrice);

    assertEquals(price, pdfPrice, 1e-1);

  }

}
