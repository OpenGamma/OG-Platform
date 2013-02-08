/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.lambdava.streams;

import static com.opengamma.util.functional.EqualityUtil.assertEqualRecursive;
import static org.testng.FileAssert.fail;

import java.util.Iterator;

import org.testng.annotations.Test;

import com.opengamma.lambdava.functions.Function1;
import com.opengamma.lambdava.tuple.Pair;

@Test
public class LazyStreamTest {

  public void lazy_map() {
    StreamI<Integer> s = Stream.lazy(new Thunk<Integer>() {
      @Override
      public StreamI<Integer> execute() {
        System.out.println(">> 1");
        return Stream.of(
          1,
          Stream.lazy(new Thunk<Integer>() {
            @Override
            public StreamI<Integer> execute() {
              System.out.println(">> 2");
              return Stream.of(
                2,
                Stream.<Integer>empty()
              );
            }
          })
        );
      }
    });

    StreamI<Integer> m = s.map(new Function1<Integer, Integer>() {
      @Override
      public Integer execute(Integer i) {
        return 4 * i;
      }
    }).asStream();

    assertEqualRecursive(m.head(), 4);
    //assertEqualRecursive(m.head(), Stream.of(8));
    //assertEqualRecursive(s, Stream.of(1, 2));
  }

  public void lazy_stream() {
    StreamI s = Stream.lazy(new Thunk<Integer>() {

      @Override
      public StreamI<Integer> execute() {
        return Stream.of(
          1,
          Stream.lazy(new Thunk<Integer>() {
            @Override
            public StreamI<Integer> execute() {
              fail("The lazy stream is eager!");
              return Stream.of(
                2,
                Stream.<Integer>empty()
              );
            }
          })
        );
      }

    });

    assertEqualRecursive(s.head(), 1);
    s.rest();
  }


  @Test
  public void lazy_iterable() {
    StreamI s = Stream.lazy(new Thunk<Integer>() {

      @Override
      public StreamI<Integer> execute() {
        return Stream.of(
          1,
          Stream.lazy(new Thunk<Integer>() {
            @Override
            public StreamI<Integer> execute() {
              fail("The lazy stream is eager!");
              return Stream.of(
                2,
                Stream.<Integer>empty()
              );
            }
          })
        );
      }

    });

    Iterator i = s.iterator();

    assertEqualRecursive(i.next(), 1);
    i.hasNext();
  }

  public void lazy_take() {
    StreamI s = Stream.lazy(new Thunk<Integer>() {

      @Override
      public StreamI<Integer> execute() {
        return Stream.of(
          1,
          Stream.lazy(new Thunk<Integer>() {
            @Override
            public StreamI<Integer> execute() {
              fail("The lazy stream is eager!");
              return Stream.of(
                2,
                Stream.<Integer>empty()
              );
            }
          })
        );
      }

    });

    Functional t = s.take(1);

    assertEqualRecursive(t.head(), 1);
    t.rest();
  }

  public void lazy_mapping() {
    StreamI s = Stream.lazy(new Thunk<Integer>() {

      @Override
      public StreamI<Integer> execute() {
        return Stream.of(
          1,
          Stream.lazy(new Thunk<Integer>() {
            @Override
            public StreamI<Integer> execute() {
              fail("The lazy stream is eager!");
              return Stream.of(
                2,
                Stream.<Integer>empty()
              );
            }
          })
        );
      }

    });

    Functional t = s.map(new Function1<Integer, Integer>() {
      @Override
      public Integer execute(Integer i) {
        return 4 * i;
      }
    });
    assertEqualRecursive(t.head(), 4);
    t.rest();
  }

  public void lazy_mapping_take() {
    StreamI s = Stream.lazy(new Thunk<Integer>() {

      @Override
      public StreamI<Integer> execute() {
        return Stream.of(
          1,
          Stream.lazy(new Thunk<Integer>() {
            @Override
            public StreamI<Integer> execute() {
              fail("The lazy stream is eager!");
              return Stream.of(
                2,
                Stream.<Integer>empty()
              );
            }
          })
        );
      }

    });

    Functional t = s.map(new Function1<Integer, Integer>() {
      @Override
      public Integer execute(Integer i) {
        return 4 * i;
      }
    }).take(1).doall();
    assertEqualRecursive(t.head(), 4);
    t.rest();
  }

}
