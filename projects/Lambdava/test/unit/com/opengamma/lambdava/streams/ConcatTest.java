/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.lambdava.streams;

import static com.opengamma.lambdava.streams.FunctionalStream.concat;
import static com.opengamma.util.functional.EqualityUtil.assertEqualRecursive;

import org.testng.annotations.Test;

@Test
public class ConcatTest {

  public void concat_test_1() {
    StreamI<Object> x = concat(Stream.of(Stream.empty(), Stream.empty(), Stream.empty()));

    assertEqualRecursive(x, Stream.empty());
  }

  public void concat_test_2() {
    StreamI<Integer> x = concat(Stream.of(Stream.<Integer>empty(), Stream.of(1), Stream.<Integer>empty()));

    assertEqualRecursive(x, Stream.of(1));
  }

  public void concat_test_3() {
    StreamI<Integer> x = concat(Stream.of(Stream.of(1), Stream.<Integer>empty()));

    assertEqualRecursive(x, Stream.of(1));
  }

  public void concat_test_4() {
    StreamI<Integer> x = concat(Stream.of(Stream.<Integer>empty(), Stream.of(1)));

    assertEqualRecursive(x, Stream.of(1));
  }

}
