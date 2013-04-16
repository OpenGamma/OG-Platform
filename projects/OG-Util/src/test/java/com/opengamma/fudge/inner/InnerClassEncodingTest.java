/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge.inner;

import static com.google.common.collect.Lists.newArrayList;
import static com.opengamma.util.fudgemsg.AutoFudgable.autoFudge;
import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding.
 */
@Test(groups = TestGroup.UNIT)
public class InnerClassEncodingTest extends AbstractFudgeBuilderTestCase {

  Random generator = new Random(System.currentTimeMillis());

  public void test_inner_without_context() {
    TestOuterClass inner = new TestOuterClass() {
    };

    

    TestOuterClass cycled = cycleObjectOverBytes(autoFudge(inner)).object();
    for (int i = 0; i < 100; i++) {
      double randomArg = generator.nextDouble();
      assertEquals(inner.eval(randomArg), cycled.eval(randomArg));
    }
  }

  public void test_inner_with_primitive_context() {
    final double some_context = generator.nextDouble();
    TestOuterClass inner = new TestOuterClass() {
      @Override
      public double eval(double arg) {
        return arg * some_context;
      }
    };

    

    TestOuterClass cycled = cycleObjectOverBytes(autoFudge(inner)).object();
    for (int i = 0; i < 100; i++) {
      double randomArg = generator.nextDouble();
      assertEquals(inner.eval(randomArg), cycled.eval(randomArg));
    }
  }
  
  public void test_inner_with_two_primitive_contexts() {
    final double some_context_a = 1.0;
    final double some_context_b = 2.0;
    TestOuterClass inner = new TestOuterClass() {
      @Override
      public double eval(double arg) {
        return arg * some_context_a + some_context_b;
      }
    };
    
    TestOuterClass cycled = cycleObjectOverBytes(autoFudge(inner)).object();
    for (int i = 0; i < 100; i++) {
      double randomArg = generator.nextDouble();
      assertEquals(inner.eval(randomArg), cycled.eval(randomArg));
    }
  }  

  public void test_inner_with_array_of_primitives_context() {

    int count = generator.nextInt(100);
    final double[] some_context = new double[count];

    for (int j = 0; j < count; j++) {
      some_context[j] = generator.nextDouble();
    }

    TestOuterClass inner = new TestOuterClass() {
      @Override
      public double eval(double arg) {
        double sum = arg;
        for (double d : some_context) {
          sum += d;
        }
        return sum;
      }
    };    

    TestOuterClass cycled = cycleObjectOverBytes(autoFudge(inner)).object();    

    for (int i = 0; i < 100; i++) {
      double randomArg = generator.nextDouble();
      assertEquals(inner.eval(randomArg), cycled.eval(randomArg));
    }
  }

  public void test_inner_with_pojo_context() {
    final ContextPOJO some_context = new ContextPOJO();
    some_context.setValue(generator.nextDouble());

    TestOuterClass inner = new TestOuterClass() {
      @Override
      public double eval(double arg) {
        return arg * some_context.getValue();
      }
    };

    TestOuterClass cycled = cycleObjectOverBytes(autoFudge(inner)).object();
    for (int i = 0; i < 100; i++) {
      double randomArg = generator.nextDouble();
      assertEquals(inner.eval(randomArg), cycled.eval(randomArg));
    }
  }

  /**
   * This fails because fudge can't serialize arrays of something other than primitives
   */
  @Test(enabled = false)
  public void test_inner_with_array_of_pojos_context() {
    int count = generator.nextInt(100);
    final ContextPOJO[] some_context = new ContextPOJO[count];

    for (int j = 0; j < count; j++) {
      some_context[j] = new ContextPOJO();
      some_context[j].setValue(generator.nextDouble());
    }

    TestOuterClass inner = new TestOuterClass() {
      @Override
      public double eval(double arg) {
        double sum = arg;
        for (ContextPOJO pojo : some_context) {
          sum += pojo.getValue();
        }
        return sum;
      }
    };

    

    TestOuterClass cycled = cycleObjectOverBytes(autoFudge(inner)).object();

    for (int i = 0; i < 100; i++) {
      double randomArg = generator.nextDouble();
      assertEquals(inner.eval(randomArg), cycled.eval(randomArg));
    }
  }

  public void test_inner_with_list_of_pojos_context() {
    int count = generator.nextInt(100);
    final List<ContextPOJO> some_context = newArrayList();

    for (int j = 0; j < count; j++) {
      ContextPOJO pojo = new ContextPOJO();
      pojo.setValue(generator.nextDouble());
      some_context.add(pojo);
    }

    TestOuterClass inner = new TestOuterClass() {
      @Override
      public double eval(double arg) {
        double sum = arg;
        for (ContextPOJO pojo : some_context) {
          sum += pojo.getValue();
        }
        return sum;
      }
    };

    

    TestOuterClass cycled = cycleObjectOverBytes(autoFudge(inner)).object();

    for (int i = 0; i < 100; i++) {
      double randomArg = generator.nextDouble();
      assertEquals(inner.eval(randomArg), cycled.eval(randomArg));
    }
  }

  public void test_inner_with_context_copied_from_enclosing_class() {
    final double some_context = some_outer_context;
    TestOuterClass inner = new TestOuterClass() {
      @Override
      public double eval(double arg) {
        return arg * some_context;
      }
    };

    

    TestOuterClass cycled = cycleObjectOverBytes(autoFudge(inner)).object();
    for (int i = 0; i < 100; i++) {
      double randomArg = generator.nextDouble();
      assertEquals(inner.eval(randomArg), cycled.eval(randomArg));
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  public void test_inner_implementing_iface_without_context() {
    TestOuterInterface inner = new TestOuterInterface() {
      @Override
      public double eval(double arg) {
        return arg;
      }
    };

    TestOuterInterface cycled = cycleObjectOverBytes(autoFudge(inner)).object();
    for (int i = 0; i < 100; i++) {
      double randomArg = generator.nextDouble();
      assertEquals(inner.eval(randomArg), cycled.eval(randomArg));
    }
  }

  public void test_inner_implementing_iface_with_primitive_context() {
    final double some_context = generator.nextDouble();
    TestOuterInterface inner = new TestOuterInterface() {
      @Override
      public double eval(double arg) {
        return arg * some_context;
      }
    };

    TestOuterInterface cycled = cycleObjectOverBytes(autoFudge(inner)).object();
    for (int i = 0; i < 100; i++) {
      double randomArg = generator.nextDouble();
      assertEquals(inner.eval(randomArg), cycled.eval(randomArg));
    }
  }


  public void test_inner_implementing_iface_with_array_of_primitives_context() {

    int count = generator.nextInt(100);
    final double[] some_context = new double[count];

    for (int j = 0; j < count; j++) {
      some_context[j] = generator.nextDouble();
    }

    TestOuterInterface inner = new TestOuterInterface() {
      @Override
      public double eval(double arg) {
        double sum = arg;
        for (double d : some_context) {
          sum += d;
        }
        return sum;
      }
    };

    TestOuterInterface cycled = cycleObjectOverBytes(autoFudge(inner)).object();

    for (int i = 0; i < 100; i++) {
      double randomArg = generator.nextDouble();
      assertEquals(inner.eval(randomArg), cycled.eval(randomArg));
    }
  }

  public void test_inner_implementing_iface_with_pojo_context() {
    final ContextPOJO some_context = new ContextPOJO();
    some_context.setValue(generator.nextDouble());

    TestOuterInterface inner = new TestOuterInterface() {
      @Override
      public double eval(double arg) {
        return arg * some_context.getValue();
      }
    };

    TestOuterInterface cycled = cycleObjectOverBytes(autoFudge(inner)).object();
    for (int i = 0; i < 100; i++) {
      double randomArg = generator.nextDouble();
      assertEquals(inner.eval(randomArg), cycled.eval(randomArg));
    }
  }

  /**
   * This fails because fudge can't serialize arrays of something other than primitives
   */
  @Test(enabled = false)
  public void test_inner_implementing_iface_with_array_of_pojos_context() {
    int count = generator.nextInt(100);
    final ContextPOJO[] some_context = new ContextPOJO[count];

    for (int j = 0; j < count; j++) {
      some_context[j] = new ContextPOJO();
      some_context[j].setValue(generator.nextDouble());
    }

    TestOuterInterface inner = new TestOuterInterface() {
      @Override
      public double eval(double arg) {
        double sum = arg;
        for (ContextPOJO pojo : some_context) {
          sum += pojo.getValue();
        }
        return sum;
      }
    };

    TestOuterInterface cycled = cycleObjectOverBytes(autoFudge(inner)).object();

    for (int i = 0; i < 100; i++) {
      double randomArg = generator.nextDouble();
      assertEquals(inner.eval(randomArg), cycled.eval(randomArg));
    }
  }

  public void test_inner_implementing_iface_with_list_of_pojos_context() {
    int count = generator.nextInt(100);
    final List<ContextPOJO> some_context = newArrayList();

    for (int j = 0; j < count; j++) {
      ContextPOJO pojo = new ContextPOJO();
      pojo.setValue(generator.nextDouble());
      some_context.add(pojo);
    }

    TestOuterInterface inner = new TestOuterInterface() {
      @Override
      public double eval(double arg) {
        double sum = arg;
        for (ContextPOJO pojo : some_context) {
          sum += pojo.getValue();
        }
        return sum;
      }
    };

    TestOuterInterface cycled = cycleObjectOverBytes(autoFudge(inner)).object();

    for (int i = 0; i < 100; i++) {
      double randomArg = generator.nextDouble();
      assertEquals(inner.eval(randomArg), cycled.eval(randomArg));
    }
  }

  double some_outer_context = generator.nextDouble();

  public void test_inner_implementing_iface_with_context_copied_from_enclosing_class() {
    final double some_context = some_outer_context;
    TestOuterInterface inner = new TestOuterInterface() {
      @Override
      public double eval(double arg) {
        return arg * some_context;
      }
    };

    TestOuterInterface cycled = cycleObjectOverBytes(autoFudge(inner)).object();
    for (int i = 0; i < 100; i++) {
      double randomArg = generator.nextDouble();
      assertEquals(inner.eval(randomArg), cycled.eval(randomArg));
    }
  }

  public void test_a_collection_which_is_inner_class() {
    Map<Byte, Byte> map = Collections.unmodifiableMap(new HashMap<Byte, Byte>() {
      private static final long serialVersionUID = 1L;
      {
      this.put((byte) 1, (byte) 2);
      }
    });
    @SuppressWarnings("rawtypes")
    Map cycled = cycleObjectOverBytes(map);

    assertEquals(cycled.get((byte)1), (byte)2);
  }

}
