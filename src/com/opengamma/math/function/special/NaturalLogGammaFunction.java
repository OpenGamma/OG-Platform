package com.opengamma.math.function.special;

import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 * 
 */

public class NaturalLogGammaFunction extends Function1D<Double, Double, MathException> {
  private static final double[] COEFFICIENTS = new double[] { 57.1562356658629235, -59.5979603554754912, 14.1360979747417471, -0.491913816097620199, 0.339946499848118887e-4,
      0.465236289270485756e-4, -0.983744753048795646e-4, 0.158088703224912494e-3, -0.210264441724104883e-3, 0.217439618115212643e-3, -0.164318106536763890e-3,
      0.844182239838527433e-4, -0.261908384015814087e-4, 0.368991826595316234e-5 };

  @Override
  public Double evaluate(Double x) throws MathException {
    if (x < 0)
      throw new MathException("x must be greater than zero");
    double y = x;
    double ser = 0.999999999999997092;
    double temp;
    temp = x + 5.24218750;
    temp = (x + 0.5) * Math.log(temp) - temp;
    for (int j = 0; j < 14; j++) {
      ser += COEFFICIENTS[j] / ++y;
    }
    return temp + Math.log(2.5066282746310005 * ser / x);
  }
}
