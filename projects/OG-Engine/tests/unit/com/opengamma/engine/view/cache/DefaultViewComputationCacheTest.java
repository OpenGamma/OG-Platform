/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.junit.Before;
import org.junit.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.Pair;

public class DefaultViewComputationCacheTest {

  private DefaultViewComputationCache _viewComputationCache;

  @Before
  public void createCache() {
    final IdentifierMap identifierSource = new InMemoryIdentifierMap();
    final BinaryDataStore privateDataStore = new InMemoryBinaryDataStore();
    final BinaryDataStore sharedDataStore = new InMemoryBinaryDataStore();
    _viewComputationCache = new DefaultViewComputationCache(identifierSource, privateDataStore, sharedDataStore, FudgeContext.GLOBAL_DEFAULT);
  }

  @Test
  public void testMissingValueSpec() {
    final ValueRequirement valueReq = new ValueRequirement("missing", new ComputationTargetSpecification(null));
    final ValueSpecification valueSpec = new ValueSpecification(valueReq, "mockFunctionId");
    assertNull(_viewComputationCache.getValue(valueSpec));
  }

  private void testPutGetCycle(final Object expected, final int fudgeSize, final CacheSelectHint hint) {
    final ValueRequirement valueReq = new ValueRequirement("foo", new ComputationTargetSpecification(null));
    final ValueSpecification valueSpec = new ValueSpecification(valueReq, "mockFunctionId");
    final ComputedValue value = new ComputedValue(valueSpec, expected);
    _viewComputationCache.putValue(value, hint);
    // Try with the default behaviors
    Object obj = _viewComputationCache.getValue(valueSpec);
    assertNotNull(obj);
    assertEquals(expected, obj);
    assertEquals((Integer) fudgeSize, _viewComputationCache.estimateValueSize(value));
    // Repeat with the hint
    obj = _viewComputationCache.getValue(valueSpec, hint);
    assertNotNull(obj);
    assertEquals(expected, obj);
    assertEquals((Integer) fudgeSize, _viewComputationCache.estimateValueSize(value));
  }

  @Test
  public void testPutGetCycle_primInt() {
    testPutGetCycle(Integer.MAX_VALUE, 12 + 4, CacheSelectHint.allPrivate());
  }

  @Test
  public void testPutGetCycle_primString() {
    testPutGetCycle("Hello World", 13 + 11, CacheSelectHint.allShared());
  }

  @Test
  public void testPutGetCycle_primDouble() {
    testPutGetCycle(3.14, 12 + 8, CacheSelectHint.allPrivate());
  }

  public static final class Bean {
    private double _foo;
    private double _bar;

    public Bean() {
    }

    public void setFoo(final double foo) {
      _foo = foo;
    }

    public double getFoo() {
      return _foo;
    }

    public void setBar(final double bar) {
      _bar = bar;
    }

    public double getBar() {
      return _bar;
    }

    @Override
    public boolean equals(final Object o) {
      if (!(o instanceof Bean))
        return false;
      Bean other = (Bean) o;
      return (other._foo == _foo) && (other._bar == _bar);
    }

    @Override
    public String toString() {
      return "Foo=" + _foo + ", Bar=" + _bar;
    }
  }

  @Test
  public void testPutGetCycle_bean() {
    final Bean bean = new Bean();
    bean.setFoo(42.0);
    bean.setBar(-1.0);
    testPutGetCycle(bean, 109, CacheSelectHint.allShared());
  }

  @Test
  public void testPutGetCycle_beanList() {
    final List<Bean> list = new ArrayList<Bean>();
    final Bean bean = new Bean();
    bean.setFoo(42.0);
    bean.setBar(-1.0);
    list.add(bean);
    testPutGetCycle(list, 112, CacheSelectHint.allPrivate());
  }
  
  private void testPutValues (int type, final CacheSelectHint correctHint, final CacheSelectHint incorrectHint) {
    final ValueSpecification valueSpecFoo = new ValueSpecification(new ValueRequirement("foo", new ComputationTargetSpecification(null)), "mockFunctionId");
    final ValueSpecification valueSpecBar = new ValueSpecification(new ValueRequirement("bar", new ComputationTargetSpecification(null)), "mockFunctionId");
    final ComputedValue valueFoo = new ComputedValue (valueSpecFoo, "Foo");
    final ComputedValue valueBar = new ComputedValue (valueSpecBar, "Bar");
    switch (type) {
      case 0 :
        _viewComputationCache.putPrivateValues(Arrays.asList (valueFoo, valueBar));
        break;
      case 1 :
        _viewComputationCache.putSharedValues(Arrays.asList(valueFoo, valueBar));
        break;
      case 2 :
        _viewComputationCache.putValues (Arrays.asList (valueFoo, valueBar), correctHint);
        break;
    }
    assertEquals (valueFoo.getValue (), _viewComputationCache.getValue (valueSpecFoo));
    assertEquals (valueBar.getValue (), _viewComputationCache.getValue (valueSpecBar));
    assertEquals (valueFoo.getValue (), _viewComputationCache.getValue (valueSpecFoo, correctHint));
    assertEquals (valueBar.getValue (), _viewComputationCache.getValue (valueSpecBar, correctHint));
    assertEquals (null, _viewComputationCache.getValue (valueSpecFoo, incorrectHint));
    assertEquals (null, _viewComputationCache.getValue (valueSpecFoo, incorrectHint));
    Collection<Pair<ValueSpecification,Object>> values = _viewComputationCache.getValues (Arrays.asList (valueSpecFoo, valueSpecBar));
    assertEquals (2, values.size ());
    int mask = 0;
    for (Pair<ValueSpecification,Object> value : values) {
      if (value.getFirst ().equals (valueSpecFoo)) {
        assertEquals (valueFoo.getValue (), value.getSecond ());
        mask |= 1;
      } else if (value.getFirst ().equals (valueSpecBar)) {
        assertEquals (valueBar.getValue (), value.getSecond ());
        mask |= 2;
      }
    }
    assertEquals (3, mask);
    values = _viewComputationCache.getValues (Arrays.asList (valueSpecFoo, valueSpecBar), correctHint);
    assertEquals (2, values.size ());
    mask = 0;
    for (Pair<ValueSpecification,Object> value : values) {
      if (value.getFirst ().equals (valueSpecFoo)) {
        assertEquals (valueFoo.getValue (), value.getSecond ());
        mask |= 1;
      } else if (value.getFirst ().equals (valueSpecBar)) {
        assertEquals (valueBar.getValue (), value.getSecond ());
        mask |= 2;
      }
    }
    assertEquals (3, mask);
    values = _viewComputationCache.getValues (Arrays.asList (valueSpecFoo, valueSpecBar), incorrectHint);
    assertEquals (2, values.size ());
    for (Pair<ValueSpecification,Object> value : values) {
      if (value.getFirst ().equals (valueSpecFoo)) {
        assertNull (value.getSecond ());
        mask |= 1;
      } else if (value.getFirst ().equals (valueSpecBar)) {
        assertNull (value.getSecond ());
        mask |= 2;
      }
    }
    assertEquals (3, mask);
  }
  
  @Test
  public void testPutValuesPrivate() {
    testPutValues (0, CacheSelectHint.allPrivate (), CacheSelectHint.allShared ());
  }

  @Test
  public void testPutValuesShared() {
    testPutValues (1, CacheSelectHint.allShared (), CacheSelectHint.allPrivate ());
  }

  @Test
  public void testPutValuesMixedPrivate() {
    final ValueSpecification valueSpecFoo = new ValueSpecification(new ValueRequirement("foo", new ComputationTargetSpecification(null)), "mockFunctionId");
    testPutValues (2, CacheSelectHint.privateValues(Arrays.asList (valueSpecFoo)), CacheSelectHint.sharedValues(Arrays.asList(valueSpecFoo)));
  }

  @Test
  public void testPutValuesMixedShared() {
    final ValueSpecification valueSpecFoo = new ValueSpecification(new ValueRequirement("foo", new ComputationTargetSpecification(null)), "mockFunctionId");
    testPutValues (2, CacheSelectHint.sharedValues(Arrays.asList (valueSpecFoo)), CacheSelectHint.privateValues(Arrays.asList(valueSpecFoo)));
  }

}
