/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.minimization;

import static com.opengamma.analytics.math.minimization.MinimizationTestFunctions.COUPLED_ROSENBROCK;
import static com.opengamma.analytics.math.minimization.MinimizationTestFunctions.COUPLED_ROSENBROCK_GRAD;
import static com.opengamma.analytics.math.minimization.MinimizationTestFunctions.ROSENBROCK;
import static com.opengamma.analytics.math.minimization.MinimizationTestFunctions.ROSENBROCK_GRAD;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class MultidimensionalMinimizerSpeedTest {
  private static final Logger s_logger = LoggerFactory.getLogger(MultidimensionalMinimizerSpeedTest.class);
  private static final int HOTSPOT_WARMUP_CYCLES = 0;
  private static final int BENCHMARK_CYCLES = 1;

  private static double EPS = 1e-8;

  private static ScalarMinimizer LINE_MINIMIZER = new BrentMinimizer1D();
  private final static NelderMeadDownhillSimplexMinimizer SIMPLEX_MINIMIZER = new NelderMeadDownhillSimplexMinimizer();
  private static ConjugateDirectionVectorMinimizer CONJUGATE_DIRECTION_MINIMIZER = new ConjugateDirectionVectorMinimizer(LINE_MINIMIZER,
      EPS, 10000);
  private static ConjugateGradientVectorMinimizer CONJUGATE_GRADIENT_MINIMIZER = new ConjugateGradientVectorMinimizer(LINE_MINIMIZER,
      EPS, 500);
  private static QuasiNewtonVectorMinimizer QUASI_NEWTON_MINIMISER = new QuasiNewtonVectorMinimizer();

  @Test
  public void testWithoutGradientInfo1() {
    final DoubleMatrix1D start = new DoubleMatrix1D(new double[] {-1.0, 1.0});
    doHotSpot(SIMPLEX_MINIMIZER, "Simplex - rosenbrock ", ROSENBROCK, null, start);
    doHotSpot(CONJUGATE_DIRECTION_MINIMIZER, "Conjugate direction - rosenbrock ", ROSENBROCK, null, start);
    doHotSpot(CONJUGATE_GRADIENT_MINIMIZER, "Conjugate Gradient - rosenbrock (no grad info)", ROSENBROCK, null, start);

    //Quasi Newton not working without gradient
    //   doHotSpot(QUASI_NEWTON_MINIMISER, "Quasi Newton - rosenbrock (no grad info)", ROSENBROCK, null, start);
  }

  @Test
  public void testWithGradientInfo1() {
    final DoubleMatrix1D start = new DoubleMatrix1D(new double[] {-1.0, 1.0});
    doHotSpot(CONJUGATE_GRADIENT_MINIMIZER, "Conjugate Gradient - rosenbrock", ROSENBROCK, ROSENBROCK_GRAD, start);
    doHotSpot(QUASI_NEWTON_MINIMISER, "Quasi Newton - rosenbrock", ROSENBROCK, ROSENBROCK_GRAD, start);
  }

  @Test
  public void testWithoutGradientInfo2() {
    final DoubleMatrix1D start = new DoubleMatrix1D(new double[] {-1.0, 1.0, -1.0, 1.0, -1.0, 1.0, 1.0});
    doHotSpot(SIMPLEX_MINIMIZER, "Simplex - coupled rosenbrock ", COUPLED_ROSENBROCK, null, start);

    //Conjugate direction takes too long
    //doHotSpot(CONJUGATE_DIRECTION_MINIMIZER, "Conjugate direction - coupled rosenbrock", COUPLED_ROSENBROCK, null,
    //    start);
    doHotSpot(CONJUGATE_GRADIENT_MINIMIZER, "Conjugate Gradient - coupled rosenbrock (no grad info)",
        COUPLED_ROSENBROCK, null, start);

    //Quasi Newton not working without gradient
    //   doHotSpot(QUASI_NEWTON_MINIMISER, "Quasi Newton - rosenbrock (no grad info)",  COUPLED_ROSENBROCK, null, start);
  }

  @Test
  public void testWithGradientInfo2() {
    final DoubleMatrix1D start = new DoubleMatrix1D(new double[] {-1.0, 1.0});
    doHotSpot(CONJUGATE_GRADIENT_MINIMIZER, "Conjugate Gradient - coupled rosenbrock", COUPLED_ROSENBROCK,
        COUPLED_ROSENBROCK_GRAD, start);
    doHotSpot(QUASI_NEWTON_MINIMISER, "Quasi Newton - coupled rosenbrock", COUPLED_ROSENBROCK, COUPLED_ROSENBROCK_GRAD,
        start);
  }

  private void doHotSpot(final Minimizer<Function1D<DoubleMatrix1D, Double>, DoubleMatrix1D> minimizer, final String name,
      final Function1D<DoubleMatrix1D, Double> function, final Function1D<DoubleMatrix1D, DoubleMatrix1D> grad,
      final DoubleMatrix1D startPosition) {
    for (int i = 0; i < HOTSPOT_WARMUP_CYCLES; i++) {
      doTest(minimizer, function, grad, startPosition);
    }
    if (BENCHMARK_CYCLES > 0) {
      final OperationTimer timer = new OperationTimer(s_logger, "processing {} cycles on " + name, BENCHMARK_CYCLES);
      for (int i = 0; i < BENCHMARK_CYCLES; i++) {
        doTest(minimizer, function, grad, startPosition);
      }
      timer.finished();
    }
  }

  private void doTest(final Minimizer<Function1D<DoubleMatrix1D, Double>, DoubleMatrix1D> minimizer, final Function1D<DoubleMatrix1D, Double> function,
      final Function1D<DoubleMatrix1D, DoubleMatrix1D> grad, final DoubleMatrix1D startPosition) {
    if (grad == null || !(minimizer instanceof MinimizerWithGradient)) {
      minimizer.minimize(function, startPosition);
    } else {
      @SuppressWarnings("unchecked")
      final MinimizerWithGradient<Function1D<DoubleMatrix1D, Double>, Function1D<DoubleMatrix1D, DoubleMatrix1D>, DoubleMatrix1D> minwithGrad = (MinimizerWithGradient<Function1D<DoubleMatrix1D, Double>, Function1D<DoubleMatrix1D, DoubleMatrix1D>, DoubleMatrix1D>) minimizer;
      minwithGrad.minimize(function, grad, startPosition);
    }
  }
}
