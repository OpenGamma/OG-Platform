/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.lambdava.streams;

import static com.opengamma.lambdava.streams.Lambdava.functional;
import static com.opengamma.util.functional.EqualityUtil.assertEqualRecursive;

import org.testng.annotations.Test;

import com.opengamma.lambdava.functions.Function1;

@Test
public class FilterTest {

  @Test
  public void filterAll() {
    Iterable filtered = functional(Stream.of(1, 2, 3, 4, 5)).filter(new Function1<Integer, Boolean>() {
      @Override
      public Boolean execute(Integer _i) {
        return false;
      }
    });

    assertEqualRecursive(filtered, Stream.empty());
  }

  @Test
  public void filterNothing() {
    Iterable filtered = functional(Stream.of(1, 2, 3, 4, 5)).filter(new Function1<Integer, Boolean>() {
      @Override
      public Boolean execute(Integer _i) {
        return true;
      }
    });

    assertEqualRecursive(filtered, Stream.of(1, 2, 3, 4, 5));
  }

  @Test
  public void filterHead() {
    Iterable filtered = functional(Stream.of(1, 2, 3, 4, 5)).filter(new Function1<Integer, Boolean>() {
      @Override
      public Boolean execute(Integer i) {
        return !i.equals(1);
      }
    });

    assertEqualRecursive(filtered, Stream.of(2, 3, 4, 5));
  }
}
