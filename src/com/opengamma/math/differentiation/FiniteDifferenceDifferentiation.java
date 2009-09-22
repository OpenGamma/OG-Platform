package com.opengamma.math.differentiation;

import java.util.Arrays;

import com.opengamma.math.function.Function;

/**
 * 
 * @author emcleod
 * 
 */

public class FiniteDifferenceDifferentiation {
  public enum DifferenceType {
    FORWARD, CENTRAL, BACKWARD
  }

  public static double getFirstOrder(Function<Double, Double> f, Double[] vars, int index, double eps, DifferenceType type) {
    Double[] newVars = Arrays.copyOf(vars, vars.length);
    if (type.equals(DifferenceType.FORWARD)) {
      double original = f.evaluate(newVars);
      newVars[index] += eps;
      double up = f.evaluate(newVars);
      return (up - original) / eps;
    } else if (type.equals(DifferenceType.BACKWARD)) {
      double original = f.evaluate(newVars);
      newVars[index] -= eps;
      double down = f.evaluate(newVars);
      return (original - down) / eps;
    }
    double twiceEps = eps * 2;
    newVars[index] += eps;
    Double up = f.evaluate(vars);
    newVars[index] -= twiceEps;
    Double down = f.evaluate(vars);
    return (up - down) / twiceEps;
  }

  public static double getMixedFirstOrder(Function<Double, Double> f, Double[] vars, int index1, int index2, double eps1, double eps2) {
    Double[] newVars = Arrays.copyOf(vars, vars.length);
    newVars[index1] += eps1;
    newVars[index2] += eps2;
    double firstTerm = f.evaluate(newVars);
    newVars[index2] -= 2 * eps2;
    double secondTerm = f.evaluate(newVars);
    newVars[index1] -= 2 * eps1;
    newVars[index2] += 2 * eps2;
    double thirdTerm = f.evaluate(newVars);
    newVars[index2] -= 2 * eps2;
    double fourthTerm = f.evaluate(newVars);
    return (firstTerm - secondTerm - thirdTerm + fourthTerm) / (4 * eps1 * eps2);
  }

  public static double getSecondOrder(Function<Double, Double> f, Double[] vars, int index, double eps) {
    Double[] newVars = Arrays.copyOf(vars, vars.length);
    double original = f.evaluate(newVars);
    newVars[index] += eps;
    double up = f.evaluate(newVars);
    newVars[index] -= 2 * eps;
    double down = f.evaluate(newVars);
    return (up - 2 * original + down) / (eps * eps);
  }

  public static double getMixedSecondOrder(Function<Double, Double> f, Double[] vars, int index1, int index2, double eps1, double eps2) {
    // TODO
    return 0;
  }

  public static double getThirdOrder(Function<Double, Double> f, Double[] vars, int index, double eps) {
    // TODO
    return 0;
  }
}
