/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.analytics.DoubleLabelledMatrix3D;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link LabelledMatrix3DFudgeBuilder} implementations.
 */
@Test(groups = TestGroup.UNIT)
public class LabelledMatrix3DBuilderTest extends AnalyticsTestBase {

  private static final Object[] X_LABELS = new Object[] {"A", 42d, "C", Short.MAX_VALUE + 1, "E" };
  private static final Object[] Y_LABELS = new Object[] {"A", Currency.USD, SimpleFrequency.ANNUAL, "D" };
  private static final Object[] Z_LABELS = new Object[] {(short) 1, 1, 1L };
  private static final double[][][] VALUES = new double[][][] { { {1., 2., 3., 4., 5. }, {6., 7., 8., 9., 10. }, {11., 12., 13., 14., 15. }, {16., 17., 18., 19., 20., } },
      { {21., 22., 23., 24., 25. }, {26., 27., 28., 29., 30. }, {31., 32., 33., 34., 35. }, {36., 37., 38., 39., 40., } },
      { {41., 42., 43., 44., 45. }, {46., 47., 48., 49., 50. }, {51., 52., 53., 54., 55. }, {56., 57., 58., 59., 60., } } };

  @Test
  public void testDouble() {
    final Double[] X_KEYS = new Double[] {1., 2., 3., 4., 5. };
    final Double[] Y_KEYS = new Double[] {1., 2., 3., 4. };
    final Double[] Z_KEYS = new Double[] {1., 2., 3. };
    final DoubleLabelledMatrix3D matrix = new DoubleLabelledMatrix3D(X_KEYS, X_LABELS, Y_KEYS, Y_LABELS, Z_KEYS, Z_LABELS, VALUES);
    assertEquals(matrix, cycleObject(DoubleLabelledMatrix3D.class, matrix));
  }
}
