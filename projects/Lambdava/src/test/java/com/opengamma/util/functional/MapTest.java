/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.functional;

import static com.opengamma.util.functional.EqualityUtil.assertEqualRecursive;
import static com.opengamma.lambdava.streams.Lambdava.functional;

import java.util.Arrays;
import java.util.Collection;

import org.testng.annotations.Test;

import com.opengamma.lambdava.functions.Function1;
import com.opengamma.lambdava.functions.Function2;
import com.opengamma.lambdava.streams.Stream;
import com.opengamma.lambdava.streams.StreamI;

@Test
public class MapTest {

  @Test
  public void map_test() {
    StreamI<Integer> stream = Stream.of(1, 2, 3, 4, 5, 6, 7, 8);
    assertEqualRecursive(
      functional(stream).map(new Function1<Integer, Integer>() {
        @Override
        public Integer execute(Integer i) {
          return 2 * i;
        }
      }),
      Stream.of(2, 4, 6, 8, 10, 12, 14, 16)
    );
  }

  @Test
  public void map_test2() {
    Integer[] array = {1, 2, 3, 4, 5, 6, 7, 8};
    Collection<Integer> collection = Arrays.asList(array);
    Iterable<Integer> mapped = functional(collection).map(new Function1<Integer, Integer>() {
      @Override
      public Integer execute(Integer i) {
        return 2 * i;
      }
    });
    assertEqualRecursive(
      mapped,
      Stream.of(2, 4, 6, 8, 10, 12, 14, 16)
    );
  }

  @Test
  public void map_test3() {
    Integer[] array = {1, 2, 3, 4, 5, 6, 7, 8};
    Collection<Integer> collection = Arrays.asList(array);
    StreamI<Integer> mapped = functional(collection).reduce(Stream.<Integer>empty(), new Function2<StreamI<Integer>, Integer, StreamI<Integer>>() {
      @Override
      public StreamI<Integer> execute(StreamI<Integer> acc, Integer e) {
        return acc.append(2 * e);
      }
    });
    assertEqualRecursive(
      mapped,
      Stream.of(2, 4, 6, 8, 10, 12, 14, 16)
    );
  }


}
