package com.opengamma.financial.model.finiteDifference;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;
import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.pricing.analytic.formula.CEVPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.math.cube.Cube;
import com.opengamma.math.cube.FunctionalDoublesCube;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.function.Function;
import com.opengamma.math.surface.FunctionalDoublesSurface;
import com.opengamma.math.surface.Surface;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

public class ExplicitFiniteDifference2DTest {
  
  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();

  private static BoundaryCondition A_LOWER;
  private static BoundaryCondition A_UPPER;
  private static BoundaryCondition B_LOWER;
  private static BoundaryCondition B_UPPER;

  private static final double SPOT_A = 100;
  private static final double SPOT_B = 100;
  
  private static final double T = 1.0;
  private static final double RATE = 0.0;
  private static final YieldAndDiscountCurve YIELD_CURVE = new YieldCurve(ConstantDoublesCurve.from(RATE));
  private static final double VOL_A = 0.20;
  private static final double VOL_B = 0.30;
  private static final double RHO = 0.50;
  

  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 7, 1);
  private static final OptionDefinition OPTION;
  private static final ConvectionDiffusion2DPDEDataBundle DATA;
  
  private static Cube<Double, Double, Double, Double> A;
  private static Cube<Double, Double, Double, Double> B;
  private static Cube<Double, Double, Double, Double> C;
  private static Cube<Double, Double, Double, Double> D;
  private static Cube<Double, Double, Double, Double> E;
  private static Cube<Double, Double, Double, Double> F;
 
  static {
    
    A_LOWER = new FixedValueBoundaryCondition(0.0, 0.0);
    A_UPPER = new FixedValueBoundaryCondition(0.0, 5*SPOT_A);
    B_LOWER = new FixedValueBoundaryCondition(0.0, 0.0);
    B_UPPER = new FixedValueBoundaryCondition(0.0, 5*SPOT_B);
    

    OPTION = new EuropeanVanillaOptionDefinition(1.0, new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, T)), true);

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
        return x*RATE;
      }
    };
    B = FunctionalDoublesCube.from(b);
    
    final Function<Double, Double> c = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... txy) {
        Validate.isTrue(txy.length == 3);
        double x = txy[1];
        return -RATE;
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
        
        return -x * y * VOL_A * VOL_B *RHO;
      }
    };
    E = FunctionalDoublesCube.from(e);
    
    final Function<Double, Double> f = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... txy) {
        Validate.isTrue(txy.length == 3);
        double y = txy[2];
        return y*RATE;
      }
    };
    F = FunctionalDoublesCube.from(f);
    
    final Function<Double, Double> payoff = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... xy) {
        Validate.isTrue(xy.length == 2);
       double x = xy[0];
         double y = xy[1];
        return Math.max(x-y, 0);
      }
    };
  
   
    
    DATA = new ConvectionDiffusion2DPDEDataBundle(A, B, C, D, E, F, FunctionalDoublesSurface.from(payoff));
  }    
    
  
  @Test
  public void testBlackScholesEquation() {
    double df = YIELD_CURVE.getDiscountFactor(T);
    int timeSteps = 100;
    int xSteps = 10;
    int ySteps = 10;
    
    ExplicitFiniteDifference2D solver = new ExplicitFiniteDifference2D();
    final EuropeanVanillaOption option = new EuropeanVanillaOption(1.0, T, true);

    double[][] res = solver.solve(DATA,timeSteps,xSteps,ySteps,T,A_LOWER, A_UPPER, B_LOWER, B_UPPER, null);
    
    for(int i=0; i <= xSteps; i++){
      for(int j=0; j <= ySteps; j++){
        System.out.print(res[i][j] + "\t");
      }
    //  System.out.print("\n");
    }
    
//    int n = res.length;
//    for (int i = 20; i < n - 100; i++) {
//      double spot = lowerBound + i * (upperBound - lowerBound) / priceSteps;
//      BlackFunctionData data = new BlackFunctionData(spot / df, df, 0.0);
//      double impVol;
//      try {
//        impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, option, res[i]);
//      } catch (Exception e) {
//        impVol = 0.0;
//      }
//      // System.out.println(spot + "\t" + res[i] + "\t" + impVol);
//      assertEquals(ATM_VOL, impVol, 1e-3);
//    }
  }
    


  
}
