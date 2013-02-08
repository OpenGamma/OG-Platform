/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.functional.equality;


import static com.opengamma.util.functional.EqualityUtil.equal;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.lambdava.streams.Stream;

@Test
public class EqualityUtilTest {

  public void two_empty_streams() {
    Object a = Stream.empty();
    Object b = Stream.empty();

    assertTrue(
      equal(a, b)
    );
  }

  public void two_equal_non_empty_streams() {
    Object a = Stream.of(1, 2, 3);
    Object b = Stream.of(1, 2, 3);

    assertTrue(
      equal(a, b)
    );
  }

  public void two_non_equal_streams() {
    Object a = Stream.of(1, 2, 3);
    Object b = Stream.of(2, 3, 1);

    assertFalse(
      equal(a, b)
    );
  }

  public void one_empty_one_non_empty_streams() {
    Object a = Stream.of(1, 2, 3);
    Object b = Stream.empty();

    assertFalse(
      equal(a, b)
    );
  }

  public void two_equal_nested_streams() {
    Object a = Stream.of(1, 3, Stream.of(1, 2, Stream.of(4, 5, 7)), Stream.of(1, 2, 33), 3);
    Object b = Stream.of(1, 3, Stream.of(1, 2, Stream.of(4, 5, 7)), Stream.of(1, 2, 33), 3);

    assertTrue(
      equal(a, b)
    );
  }

  public void two_non_equal_nested_streams() {
    Object a = Stream.of(1, 3, Stream.of(1, 2, Stream.of(4, 5, 7)), Stream.of(1, 2, 33), 3);
    Object b = Stream.of(1, 3, Stream.of(1, 2, Stream.of(4, 5, 7)), Stream.of(8, 2, 33), 3);

    assertFalse(
      equal(a, b)
    );
  }
}
