/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.lambdava.streams;

import static com.opengamma.util.functional.EqualityUtil.assertEqualRecursive;

import org.testng.annotations.Test;

@Test
public class AppendTest {

  @Test
  public void simple_append() {
    StreamI<Integer> s1 = Stream.of(1, 2, 3);
    StreamI<Integer> s2 = Stream.of(4, 5, 6);

    assertEqualRecursive(
      s2.append(s1),
      Stream.of(4, 5, 6, 1, 2, 3)
    );

  }

  @Test
  public void simple_append_single() {
    StreamI<Integer> s = Stream.of(1, 2, 3);

    assertEqualRecursive(
      s.append(8),
      Stream.of(1, 2, 3, 8)
    );

  }

  @Test
  public void append_to_empty() {
    StreamI<Integer> s = Stream.empty();
    StreamI<Integer> s1 = Stream.of(4, 5, 6);

    assertEqualRecursive(
      s.append(s1),
      Stream.of(4, 5, 6)
    );
  }

  @Test
  public void append_to_single() {
    StreamI<Integer> s = Stream.of(1);
    StreamI<Integer> s1 = Stream.of(4, 5, 6);

    assertEqualRecursive(
      s.append(s1),
      Stream.of(1, 4, 5, 6)
    );
  }

  @Test
  public void append_single_to_empty() {
    StreamI<Integer> s = Stream.empty();

    assertEqualRecursive(
      s.append(8),
      Stream.of(8)
    );

  }
}
