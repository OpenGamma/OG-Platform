/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class StringLabelledMatrix1DTest {

  private static final String[] NAMES1 = new String[] {"A", "D", "C", "B"};
  private static final double[] VALUES1 = new double[] {1, 4, 3, 2};
  private static final String[] NAMES2 = new String[] {"A", "B", "C", "D"};
  private static final double[] VALUES2 = new double[] {1, 2, 3, 4};
  private static final String[] NAMES3 = new String[] {"A", "D", "C", "B"};
  private static final double[] VALUES3 = new double[] {10, 40, 30, 20};
  private static final String[] NAMES4 = new String[] {"E", "G", "F", "H"};
  private static final double[] VALUES4 = new double[] {100, 300, 200, 400};
  private static final String[] NAMES5 = new String[] {"E", "D", "F", "A"};
  private static final double[] VALUES5 = new double[] {100, 300, 200, 400};
  private static final StringLabelledMatrix1D M = new StringLabelledMatrix1D(NAMES1, VALUES1);

  @Test
  public void testSorting() {
    assertArrayEquals(M.getKeys(), NAMES2);
    assertArrayEquals(M.getValues(), VALUES2, 0);
    Object[] labels = M.getLabels();
    for (int i = 0; i < 4; i++) {
      assertEquals(labels[i], NAMES2[i]);
    }
  }

  @Test
  public void testAddDifferentLabels() {
    String[] keys = new String[] {"A", "B", "C", "D", "E", "F", "G", "H"};
    Object[] labels = new Object[] {"A", "B", "C", "D", "E", "F", "G", "H"};
    double[] values = new double[] {1, 2, 3, 4, 100, 200, 300, 400};
    LabelledMatrix1D<String, String> sum = M.add(new StringLabelledMatrix1D(NAMES4, VALUES4));
    assertEquals(sum, new StringLabelledMatrix1D(keys, values));
    assertArrayEquals(labels, sum.getLabels());
    sum = M.addIgnoringLabel(new StringLabelledMatrix1D(NAMES4, VALUES4));
    assertEquals(sum, new StringLabelledMatrix1D(keys, values));
    assertArrayEquals(labels, sum.getLabels());
    sum = M.addIgnoringLabel(new StringLabelledMatrix1D(NAMES4, VALUES4), "E");
    assertEquals(sum, new StringLabelledMatrix1D(keys, values));
    assertArrayEquals(labels, sum.getLabels());
    sum = M.add(new StringLabelledMatrix1D(NAMES4, VALUES4), "E");
    assertEquals(sum, new StringLabelledMatrix1D(keys, values));
    assertArrayEquals(labels, sum.getLabels());
  }

  @Test
  public void testAddSameLabels() {
    String[] keys = new String[] {"A", "B", "C", "D"};
    Object[] labels = new Object[] {"A", "B", "C", "D"};
    double[] values = new double[] {11, 22, 33, 44};
    LabelledMatrix1D<String, String> sum = M.add(new StringLabelledMatrix1D(NAMES3, VALUES3));
    assertEquals(sum, new StringLabelledMatrix1D(keys, values));
    assertArrayEquals(labels, sum.getLabels());
    sum = M.addIgnoringLabel(new StringLabelledMatrix1D(NAMES3, VALUES3));
    assertEquals(sum, new StringLabelledMatrix1D(keys, values));
    assertArrayEquals(labels, sum.getLabels());
    sum = M.addIgnoringLabel(new StringLabelledMatrix1D(NAMES3, VALUES3), "L");
    assertEquals(sum, new StringLabelledMatrix1D(keys, values));
    assertArrayEquals(labels, sum.getLabels());
    sum = M.add(new StringLabelledMatrix1D(NAMES3, VALUES3), "L");
    assertEquals(sum, new StringLabelledMatrix1D(keys, values));
    assertArrayEquals(labels, sum.getLabels());
  }

  @Test
  public void testAdd() {
    String[] keys = new String[] {"A", "B", "C", "D", "E", "F"};
    Object[] labels = new Object[] {"A", "B", "C", "D", "E", "F"};
    double[] values = new double[] {401, 2, 3, 304, 100, 200};
    LabelledMatrix1D<String, String> sum = M.add(new StringLabelledMatrix1D(NAMES5, VALUES5));
    assertEquals(sum, new StringLabelledMatrix1D(keys, values));
    assertArrayEquals(labels, sum.getLabels());
    sum = M.addIgnoringLabel(new StringLabelledMatrix1D(NAMES5, VALUES5), "P");
    assertEquals(sum, new StringLabelledMatrix1D(keys, values));
    assertArrayEquals(labels, sum.getLabels());
    sum = M.add(new StringLabelledMatrix1D(NAMES5, VALUES5), "P");
    assertEquals(sum, new StringLabelledMatrix1D(keys, values));
    assertArrayEquals(labels, sum.getLabels());
  }
}
