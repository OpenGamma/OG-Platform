/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test the DoubleLabelledMatrix3D implementation.
 */
@Test(groups = TestGroup.UNIT)
public class DoubleLabelledMatrix3DTest {

  private static final Double[] SORTED_X_KEYS1 = new Double[] {1., 2., 3., 4., 5. };
  private static final Double[] SORTED_Y_KEYS1 = new Double[] {1., 2., 3., 4. };
  private static final Double[] SORTED_Z_KEYS1 = new Double[] {1., 2., 3., };
  private static final Object[] SORTED_X_LABELS1 = new String[] {"1.0", "2.0", "3.0", "4.0", "5.0" };
  private static final Object[] SORTED_Y_LABELS1 = new String[] {"1.0", "2.0", "3.0", "4.0" };
  private static final Object[] SORTED_Z_LABELS1 = new String[] {"1.0", "2.0", "3.0" };
  private static final Object[] SORTED_X_LABELS2 = new String[] {"A", "B", "C", "D", "E" };
  private static final Object[] SORTED_Y_LABELS2 = new String[] {"A", "B", "C", "D" };
  private static final Object[] SORTED_Z_LABELS2 = new String[] {"A", "B", "C" };
  private static final double[][][] SORTED_VALUES1 = new double[][][] {
      { {32., 31., 33., 35., 34. }, {22., 21., 23., 25., 24. }, {27., 26., 28., 30., 29. }, {37., 36., 38., 40., 39. } },
      { {12., 11., 13., 15., 14. }, {2., 1., 3., 5., 4. }, {7., 6., 8., 10., 9. }, {17., 16., 18., 20., 19. } },
      { {52., 51., 53., 55., 54. }, {42., 41., 43., 45., 44. }, {47., 46., 48., 50., 49. }, {57., 56., 58., 60., 59. } } };
  private static final Double[] X_KEYS1 = new Double[] {2., 1., 3., 5., 4. };
  private static final Double[] Y_KEYS1 = new Double[] {2., 3., 1., 4. };
  private static final Double[] Z_KEYS1 = new Double[] {2., 1., 3. };
  private static final Double[] X_KEYS2 = new Double[] {3., 6. };
  private static final Double[] Y_KEYS2 = new Double[] {4., 7. };
  private static final Double[] Z_KEYS2 = new Double[] {1., 8. };
  private static final Double[] X_KEYS3 = new Double[] {3.1, 6. };
  private static final Double[] Y_KEYS3 = new Double[] {3.9, 7. };
  private static final Double[] Z_KEYS3 = new Double[] {1.1, 8. };
  private static final Object[] X_LABELS2 = new Object[] {"B", "A", "C", "E", "D" };
  private static final Object[] Y_LABELS2 = new Object[] {"B", "C", "A", "D" };
  private static final Object[] Z_LABELS2 = new Object[] {"B", "A", "C" };
  private static final Object[] X_LABELS3 = new Object[] {"C", "F" };
  private static final Object[] Y_LABELS3 = new Object[] {"D", "G" };
  private static final Object[] Z_LABELS3 = new Object[] {"A", "H" };
  private static final double[][][] VALUES1 = new double[][][] { { {1., 2., 3., 4., 5. }, {6., 7., 8., 9., 10. }, {11., 12., 13., 14., 15. }, {16., 17., 18., 19., 20., } },
      { {21., 22., 23., 24., 25. }, {26., 27., 28., 29., 30. }, {31., 32., 33., 34., 35. }, {36., 37., 38., 39., 40., } },
      { {41., 42., 43., 44., 45. }, {46., 47., 48., 49., 50. }, {51., 52., 53., 54., 55. }, {56., 57., 58., 59., 60., } } };
  private static final double[][][] VALUES2 = new double[][][] { { {1., 2. }, {3., 4. } }, { {5., 6. }, {7., 8. } } };
  private static final Object[] LABELS = new Object[] {"X", "Y" };

  @Test
  public void test() {
    DoubleLabelledMatrix3D result = new DoubleLabelledMatrix3D(X_KEYS1, Y_KEYS1, Z_KEYS1, VALUES1);
    testCreationResult(result, SORTED_X_LABELS1, SORTED_Y_LABELS1, SORTED_Z_LABELS1);
    result = new DoubleLabelledMatrix3D(X_KEYS1, X_LABELS2, Y_KEYS1, Y_LABELS2, Z_KEYS1, Z_LABELS2, VALUES1);
    testCreationResult(result, SORTED_X_LABELS2, SORTED_Y_LABELS2, SORTED_Z_LABELS2);
  }

  @Test
  public void testAdd() {
    final DoubleLabelledMatrix3D a = new DoubleLabelledMatrix3D(X_KEYS1, Y_KEYS1, Z_KEYS1, VALUES1);
    final DoubleLabelledMatrix3D b = new DoubleLabelledMatrix3D(X_KEYS2, Y_KEYS2, Z_KEYS2, VALUES2);
    final DoubleLabelledMatrix3D result = a.add(b);
    testAddResult(result);
  }

  @Test
  public void testAddIgnoreLabels() {
    final DoubleLabelledMatrix3D a = new DoubleLabelledMatrix3D(X_KEYS1, Y_KEYS1, Z_KEYS1, VALUES1);
    final DoubleLabelledMatrix3D b = new DoubleLabelledMatrix3D(X_KEYS2, LABELS, Y_KEYS2, LABELS, Z_KEYS2, LABELS, VALUES2);
    final DoubleLabelledMatrix3D result = a.addIgnoringLabel(b);
    testAddResult(result);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddCheckLabels() {
    final DoubleLabelledMatrix3D a = new DoubleLabelledMatrix3D(X_KEYS1, Y_KEYS1, Z_KEYS1, VALUES1);
    final DoubleLabelledMatrix3D b = new DoubleLabelledMatrix3D(X_KEYS2, LABELS, Y_KEYS2, LABELS, Z_KEYS2, LABELS, VALUES2);
    a.add(b);
  }

  @Test
  public void testAddWithTolerance() {
    final DoubleLabelledMatrix3D a = new DoubleLabelledMatrix3D(X_KEYS1, X_LABELS2, Y_KEYS1, Y_LABELS2, Z_KEYS1, Z_LABELS2, VALUES1);
    final DoubleLabelledMatrix3D b = new DoubleLabelledMatrix3D(X_KEYS3, X_LABELS3, Y_KEYS3, Y_LABELS3, Z_KEYS3, Z_LABELS3, VALUES2);
    final DoubleLabelledMatrix3D result = a.add(b, 0.5, 0.5, 0.5);
    testAddResult(result);
  }

  @Test
  public void testAddWithToleranceIgnoreLabels() {
    final DoubleLabelledMatrix3D a = new DoubleLabelledMatrix3D(X_KEYS1, Y_KEYS1, Z_KEYS1, VALUES1);
    final DoubleLabelledMatrix3D b = new DoubleLabelledMatrix3D(X_KEYS3, LABELS, Y_KEYS3, LABELS, Z_KEYS3, LABELS, VALUES2);
    final DoubleLabelledMatrix3D result = a.addIgnoringLabel(b, 0.5, 0.5, 0.5);
    testAddResult(result);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddWithToleranceCheckLabels() {
    final DoubleLabelledMatrix3D a = new DoubleLabelledMatrix3D(X_KEYS1, Y_KEYS1, Z_KEYS1, VALUES1);
    final DoubleLabelledMatrix3D b = new DoubleLabelledMatrix3D(X_KEYS2, LABELS, Y_KEYS2, LABELS, Z_KEYS2, LABELS, VALUES2);
    a.add(b, 0.5, 0.5, 0.5);
  }

  private void testCreationResult(final DoubleLabelledMatrix3D result, final Object[] xLabels, final Object[] yLabels, final Object[] zLabels) {
    final Double[] xKeysResult = result.getXKeys();
    assertEquals(xKeysResult, SORTED_X_KEYS1);
    final Object[] xLabelsResult = result.getXLabels();
    assertEquals(xLabelsResult, xLabels);
    final Double[] yKeysResult = result.getYKeys();
    assertEquals(yKeysResult, SORTED_Y_KEYS1);
    final Object[] yLabelsResult = result.getYLabels();
    assertEquals(yLabelsResult, yLabels);
    final Double[] zKeysResult = result.getZKeys();
    assertEquals(zKeysResult, SORTED_Z_KEYS1);
    final Object[] zLabelsResult = result.getZLabels();
    assertEquals(zLabelsResult, zLabels);
    final double[][][] values = result.getValues();
    //print(values);
    for (int i = 0; i < values.length; i++) {
      for (int j = 0; j < values[i].length; j++) {
        assertTrue(Arrays.equals(values[i][j], SORTED_VALUES1[i][j]));
      }
    }
  }

  private void testAddResult(final DoubleLabelledMatrix3D result) {
    assertEquals(result.getXKeys(), new Double[] {1., 2., 3., 4., 5., 6. });
    assertEquals(result.getYKeys(), new Double[] {1., 2., 3., 4., 7. });
    assertEquals(result.getZKeys(), new Double[] {1., 2., 3., 8. });
    final double[][][] values = result.getValues();
    assertEquals(values[0][3][2], SORTED_VALUES1[0][3][2] + 1., 0.5);
    assertEquals(values[0][3][5], 2., 0.5);
    assertEquals(values[0][4][2], 3., 0.5);
    assertEquals(values[0][4][5], 4., 0.5);
    assertEquals(values[3][3][2], 5., 0.5);
    assertEquals(values[3][3][5], 6., 0.5);
    assertEquals(values[3][4][2], 7., 0.5);
    assertEquals(values[3][4][5], 8., 0.5);
  }

}
