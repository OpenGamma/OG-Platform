/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;

@Test(groups = TestGroup.UNIT)
public class DefaultViewComputationCacheTest {

  private DefaultViewComputationCache _viewComputationCache;

  @BeforeMethod
  public void createCache() {
    final IdentifierMap identifierSource = new InMemoryIdentifierMap();
    final FudgeMessageStore privateDataStore = new DefaultFudgeMessageStore(new InMemoryBinaryDataStore(),
        FudgeContext.GLOBAL_DEFAULT);
    final FudgeMessageStore sharedDataStore = new DefaultFudgeMessageStore(new InMemoryBinaryDataStore(),
        FudgeContext.GLOBAL_DEFAULT);
    _viewComputationCache = new DefaultViewComputationCache(identifierSource, privateDataStore, sharedDataStore, FudgeContext.GLOBAL_DEFAULT);
  }

  @Test
  public void testMissingValueSpec() {
    final ValueSpecification valueSpec = new ValueSpecification("missing", ComputationTargetSpecification.NULL,
        ValueProperties.with(ValuePropertyNames.FUNCTION, "mockFunctionId").get());
    assertNull(_viewComputationCache.getValue(valueSpec));
  }

  private void assertPutGetCycle(final Object expected, final int fudgeSize, final CacheSelectHint hint) {
    final ValueSpecification valueSpec = new ValueSpecification("foo", ComputationTargetSpecification.NULL,
        ValueProperties.with(ValuePropertyNames.FUNCTION, "mockFunctionId").get());
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
    assertPutGetCycle(Integer.MAX_VALUE, 4 + 4, CacheSelectHint.allPrivate());
  }

  @Test
  public void testPutGetCycle_primString() {
    final String testString = "Hello World";
    assertPutGetCycle(testString, 4 + 1 + testString.length(), CacheSelectHint.allShared());
  }

  @Test
  public void testPutGetCycle_primDouble() {
    assertPutGetCycle(3.14, 4 + 8, CacheSelectHint.allPrivate());
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
    assertPutGetCycle(bean, 96, CacheSelectHint.allShared());
  }

  @Test
  public void testPutGetCycle_beanList() {
    final List<Bean> list = new ArrayList<Bean>();
    final Bean bean = new Bean();
    bean.setFoo(42.0);
    bean.setBar(-1.0);
    list.add(bean);
    assertPutGetCycle(list, 99, CacheSelectHint.allPrivate());
  }

  private void assertPutValues(int type, final CacheSelectHint correctHint, final CacheSelectHint incorrectHint) {
    final ValueSpecification valueSpecFoo = new ValueSpecification("foo", ComputationTargetSpecification.NULL, ValueProperties.with(ValuePropertyNames.FUNCTION, "mockFunctionId").get());
    final ValueSpecification valueSpecBar = new ValueSpecification("bar", ComputationTargetSpecification.NULL, ValueProperties.with(ValuePropertyNames.FUNCTION, "mockFunctionId").get());
    final ComputedValue valueFoo = new ComputedValue(valueSpecFoo, "Foo");
    final ComputedValue valueBar = new ComputedValue(valueSpecBar, "Bar");
    switch (type) {
      case 0:
        _viewComputationCache.putPrivateValues(Arrays.asList(valueFoo, valueBar));
        break;
      case 1:
        _viewComputationCache.putSharedValues(Arrays.asList(valueFoo, valueBar));
        break;
      case 2:
        _viewComputationCache.putValues(Arrays.asList(valueFoo, valueBar), correctHint);
        break;
    }
    assertEquals(valueFoo.getValue(), _viewComputationCache.getValue(valueSpecFoo));
    assertEquals(valueBar.getValue(), _viewComputationCache.getValue(valueSpecBar));
    assertEquals(valueFoo.getValue(), _viewComputationCache.getValue(valueSpecFoo, correctHint));
    assertEquals(valueBar.getValue(), _viewComputationCache.getValue(valueSpecBar, correctHint));
    assertEquals(null, _viewComputationCache.getValue(valueSpecFoo, incorrectHint));
    assertEquals(null, _viewComputationCache.getValue(valueSpecFoo, incorrectHint));
    Collection<Pair<ValueSpecification, Object>> values = _viewComputationCache.getValues(Arrays.asList(valueSpecFoo, valueSpecBar));
    assertEquals(2, values.size());
    int mask = 0;
    for (Pair<ValueSpecification, Object> value : values) {
      if (value.getFirst().equals(valueSpecFoo)) {
        assertEquals(valueFoo.getValue(), value.getSecond());
        mask |= 1;
      } else if (value.getFirst().equals(valueSpecBar)) {
        assertEquals(valueBar.getValue(), value.getSecond());
        mask |= 2;
      }
    }
    assertEquals(3, mask);
    values = _viewComputationCache.getValues(Arrays.asList(valueSpecFoo, valueSpecBar), correctHint);
    assertEquals(2, values.size());
    mask = 0;
    for (Pair<ValueSpecification, Object> value : values) {
      if (value.getFirst().equals(valueSpecFoo)) {
        assertEquals(valueFoo.getValue(), value.getSecond());
        mask |= 1;
      } else if (value.getFirst().equals(valueSpecBar)) {
        assertEquals(valueBar.getValue(), value.getSecond());
        mask |= 2;
      }
    }
    assertEquals(3, mask);
    values = _viewComputationCache.getValues(Arrays.asList(valueSpecFoo, valueSpecBar), incorrectHint);
    assertEquals(2, values.size());
    for (Pair<ValueSpecification, Object> value : values) {
      if (value.getFirst().equals(valueSpecFoo)) {
        assertNull(value.getSecond());
        mask |= 1;
      } else if (value.getFirst().equals(valueSpecBar)) {
        assertNull(value.getSecond());
        mask |= 2;
      }
    }
    assertEquals(3, mask);
  }

  @Test
  public void testPutValuesPrivate() {
    assertPutValues(0, CacheSelectHint.allPrivate(), CacheSelectHint.allShared());
  }

  @Test
  public void testPutValuesShared() {
    assertPutValues(1, CacheSelectHint.allShared(), CacheSelectHint.allPrivate());
  }

  @Test
  public void testPutValuesMixedPrivate() {
    final ValueSpecification valueSpecFoo = new ValueSpecification("foo", ComputationTargetSpecification.NULL, ValueProperties.with(ValuePropertyNames.FUNCTION, "mockFunctionId").get());
    assertPutValues(2, CacheSelectHint.privateValues(Arrays.asList(valueSpecFoo)), CacheSelectHint.sharedValues(Arrays.asList(valueSpecFoo)));
  }

  @Test
  public void testPutValuesMixedShared() {
    final ValueSpecification valueSpecFoo = new ValueSpecification("foo", ComputationTargetSpecification.NULL, ValueProperties.with(ValuePropertyNames.FUNCTION, "mockFunctionId").get());
    assertPutValues(2, CacheSelectHint.sharedValues(Arrays.asList(valueSpecFoo)), CacheSelectHint.privateValues(Arrays.asList(valueSpecFoo)));
  }

}
