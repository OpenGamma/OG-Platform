/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language;

import static org.testng.AssertJUnit.assertArrayEquals;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests the methods in the {@link DataUtils} class.
 */
@Test(groups = TestGroup.UNIT)
public class DataUtilsTest {

  @Test
  public void testSingle() {
    final Value value = ValueUtils.of(42);
    final Data data = DataUtils.of(value);
    assertNotNull(data);
    assertEquals(value, data.getSingle());
  }

  @Test
  public void testLinear() {
    final Value[] values = new Value[] {ValueUtils.of(1), ValueUtils.of(2)};
    final Data data = DataUtils.of(values);
    assertNotNull(data);
    assertArrayEquals(values, data.getLinear());
  }

  @Test
  public void testMatrix() {
    final Value[][] values = new Value[][] {new Value[] {ValueUtils.of(1), ValueUtils.of(2)},
        new Value[] {ValueUtils.of(3), ValueUtils.of(4)}};
    final Data data = DataUtils.of(values);
    assertNotNull(data);
    assertArrayEquals(values, data.getMatrix());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSingleNull() {
    DataUtils.of((Value) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLinearNull() {
    DataUtils.of((Value[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLinearNullElement() {
    final Value[] values = new Value[] {ValueUtils.of(1), null, ValueUtils.of(2)};
    DataUtils.of(values);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMatrixNull() {
    DataUtils.of((Value[][]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMatrixNullElement() {
    final Value[][] values = new Value[][] {new Value[] {ValueUtils.of(1), null},
        new Value[] {ValueUtils.of(3), ValueUtils.of(4)}};
    DataUtils.of(values);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMatrixNullArray() {
    final Value[][] values = new Value[][] {new Value[] {ValueUtils.of(1), ValueUtils.of(2)}, null};
    DataUtils.of(values);
  }

}
