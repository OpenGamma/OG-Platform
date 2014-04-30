/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference.applications;

import org.apache.commons.lang.Validate;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDE1DCoefficients;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDE1DStandardCoefficients;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDESolver;
import com.opengamma.analytics.financial.model.finitedifference.DirichletBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.analytics.financial.model.finitedifference.MeshingFunction;
import com.opengamma.analytics.financial.model.finitedifference.PDE1DDataBundle;
import com.opengamma.analytics.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.finitedifference.ThetaMethodFiniteDifference;
import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class VariableCoefficientWaveEquationTest {

  private static final Surface<Double, Double, Double> A = ConstantDoublesSurface.from(-0.01);
  private static final Surface<Double, Double, Double> B;
  private static final Surface<Double, Double, Double> C = ConstantDoublesSurface.from(0);
  private static final ConvectionDiffusionPDE1DCoefficients COEFF;
  private static BoundaryCondition LOWER;
  private static BoundaryCondition UPPER;
  private static PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> DATA;

  static {
    final Function<Double, Double> b = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tx) {
        Validate.isTrue(tx.length == 2);
        final double x = tx[1];
        return 0.2 + FunctionUtils.square(Math.sin(x - 1));
      }
    };

    final Function1D<Double, Double> initial = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        return Math.exp(-100 * (x - 1) * (x - 1));
      }
    };

    B = FunctionalDoublesSurface.from(b);
    COEFF = new ConvectionDiffusionPDE1DStandardCoefficients(A, B, C);

    //Trefethen uses periodic boundary conditions
    LOWER = new DirichletBoundaryCondition(0, 0);
    UPPER = new DirichletBoundaryCondition(0, 2 * Math.PI);

    final int spaceSteps = 100;
    final double h = 2 * Math.PI / spaceSteps;
    final double dt = h / 2;
    final double tMax = 8;
    final int timeSteps = (int) (tMax / dt);

    final MeshingFunction timeMesh = new ExponentialMeshing(0, tMax, timeSteps + 1, 0);
    final MeshingFunction spaceMesh = new ExponentialMeshing(LOWER.getLevel(), UPPER.getLevel(), spaceSteps + 1, 0);//new HyperbolicMeshing(LOWER.getLevel(), UPPER.getLevel(), 1.0, spaceSteps + 1, 1.0);
    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);

    DATA = new PDE1DDataBundle<>(COEFF, initial, LOWER, UPPER, grid);
  }

  @Test
      (enabled = false)
      public void test() {

    final ConvectionDiffusionPDESolver solver = new ThetaMethodFiniteDifference(1.0, true);
    final PDEFullResults1D res = (PDEFullResults1D) solver.solve(DATA);
    PDEUtilityTools.printSurface("wave", res);
  }
}
