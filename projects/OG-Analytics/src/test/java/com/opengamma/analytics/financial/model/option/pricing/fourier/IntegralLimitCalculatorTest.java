package com.opengamma.analytics.financial.model.option.pricing.fourier;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.ComplexMathUtils;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.number.ComplexNumber;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class IntegralLimitCalculatorTest {
  private static final Function1D<ComplexNumber, ComplexNumber> PSI = new Function1D<ComplexNumber, ComplexNumber>() {

    @Override
    public ComplexNumber evaluate(final ComplexNumber x) {
      return ComplexMathUtils.exp(x);
    }

  };
  private static final double ALPHA = -0.5;
  private static final double TOL = 1e-8;
  private static final IntegralLimitCalculator CALCULATOR = new IntegralLimitCalculator();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPsi() {
    CALCULATOR.solve(null, ALPHA, TOL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZeroAlpha() {
    CALCULATOR.solve(PSI, 0, TOL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAlpha() {
    CALCULATOR.solve(PSI, -1, TOL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeTolerance() {
    CALCULATOR.solve(PSI, ALPHA, -TOL);
  }
}
