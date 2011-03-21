/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Tests the methods in the {@link DataUtil} class.
 */
public class DataUtilTest {

  @Test
  public void testSingle() {
    final Value value = ValueUtil.of(42);
    final Data data = DataUtil.of(value);
    assertNotNull(data);
    assertEquals(value, data.getSingle());
  }

  @Test
  public void testLinear() {
    final Value[] values = new Value[] {ValueUtil.of(1), ValueUtil.of(2)};
    final Data data = DataUtil.of(values);
    assertNotNull(data);
    assertArrayEquals(values, data.getLinear());
  }

  @Test
  public void testMatrix() {
    final Value[][] values = new Value[][] {new Value[] {ValueUtil.of(1), ValueUtil.of(2)},
        new Value[] {ValueUtil.of(3), ValueUtil.of(4)}};
    final Data data = DataUtil.of(values);
    assertNotNull(data);
    assertArrayEquals(values, data.getMatrix());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSingleNull() {
    DataUtil.of((Value) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLinearNull() {
    DataUtil.of((Value[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLinearNullElement() {
    final Value[] values = new Value[] {ValueUtil.of(1), null, ValueUtil.of(2)};
    DataUtil.of(values);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMatrixNull() {
    DataUtil.of((Value[][]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMatrixNullElement() {
    final Value[][] values = new Value[][] {new Value[] {ValueUtil.of(1), null},
        new Value[] {ValueUtil.of(3), ValueUtil.of(4)}};
    DataUtil.of(values);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMatrixNullArray() {
    final Value[][] values = new Value[][] {new Value[] {ValueUtil.of(1), ValueUtil.of(2)}, null};
    DataUtil.of(values);
  }

}
