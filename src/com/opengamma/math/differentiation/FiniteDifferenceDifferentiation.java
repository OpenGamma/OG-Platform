/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
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

  public static double getFirstOrder(final Function<Double, Double> f, final Double[] vars, final int index, final double eps, final DifferenceType type) {
    checkInputs(f, vars, index, type);
    final Double[] newVars = Arrays.copyOf(vars, vars.length);
    if (type.equals(DifferenceType.FORWARD)) {
      final double original = f.evaluate(newVars);
      newVars[index] += eps;
      final double up = f.evaluate(newVars);
      return (up - original) / eps;
    } else if (type.equals(DifferenceType.BACKWARD)) {
      final double original = f.evaluate(newVars);
      newVars[index] -= eps;
      final double down = f.evaluate(newVars);
      return (original - down) / eps;
    }
    final double twiceEps = eps * 2;
    newVars[index] += eps;
    final double up = f.evaluate(newVars);
    newVars[index] -= twiceEps;
    final double down = f.evaluate(newVars);
    return (up - down) / twiceEps;
  }

  public static double getMixedSecondOrder(final Function<Double, Double> f, final Double[] vars, final int index1, final int index2, final double eps1, final double eps2) {
    checkInputs(f, vars, new int[] { index1, index2 });
    final Double[] newVars = Arrays.copyOf(vars, vars.length);
    newVars[index1] += eps1;
    newVars[index2] += eps2;
    final double firstTerm = f.evaluate(newVars);
    newVars[index2] -= 2 * eps2;
    final double secondTerm = f.evaluate(newVars);
    newVars[index1] -= 2 * eps1;
    newVars[index2] += 2 * eps2;
    final double thirdTerm = f.evaluate(newVars);
    newVars[index2] -= 2 * eps2;
    final double fourthTerm = f.evaluate(newVars);
    return (firstTerm - secondTerm - thirdTerm + fourthTerm) / (4 * eps1 * eps2);
  }

  public static double getSecondOrder(final Function<Double, Double> f, final Double[] vars, final int index, final double eps) {
    checkInputs(f, vars, new int[] { index });
    final Double[] newVars = Arrays.copyOf(vars, vars.length);
    final double original = f.evaluate(newVars);
    newVars[index] += eps;
    final double up = f.evaluate(newVars);
    newVars[index] -= 2 * eps;
    final double down = f.evaluate(newVars);
    return (up - 2 * original + down) / (eps * eps);
  }

  private static void checkInputs(final Function<Double, Double> f, final Double[] vars, final int index, final DifferenceType type) {
    if (f == null)
      throw new IllegalArgumentException("Function was null");
    if (vars == null)
      throw new IllegalArgumentException("Variable array was null");
    if (vars.length == 0)
      throw new IllegalArgumentException("Variable array was empty");
    if (index < 0)
      throw new IllegalArgumentException("Cannot have a negative index");
    if (type == null)
      throw new IllegalArgumentException("Difference type was null");
  }

  private static void checkInputs(final Function<Double, Double> f, final Double[] vars, final int[] index) {
    if (f == null)
      throw new IllegalArgumentException("Function was null");
    if (vars == null)
      throw new IllegalArgumentException("Variable array was null");
    if (vars.length == 0)
      throw new IllegalArgumentException("Variable array was empty");
    for (final int i : index) {
      if (i < 0)
        throw new IllegalArgumentException("Cannot have a negative index");
    }
  }
}
