/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.junit.Before;
import org.junit.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

public class StandardViewComputationCacheTest {

  private DefaultViewComputationCache _viewComputationCache;
  
  @Before
  public void createCache () {
    final IdentifierMap identifierSource = new InMemoryIdentifierMap ();
    final BinaryDataStore dataStore = new InMemoryBinaryDataStore ();
    _viewComputationCache = new DefaultViewComputationCache (identifierSource, dataStore, dataStore, FudgeContext.GLOBAL_DEFAULT);
  }
  
  @Test
  public void testMissingValueSpec () {
    final ValueRequirement valueReq = new ValueRequirement("missing", new ComputationTargetSpecification (null));
    final ValueSpecification valueSpec = new ValueSpecification(valueReq, "mockFunctionId");
    assertNull (_viewComputationCache.getValue(valueSpec));
  }
  
  private void testPutGetCycle (final Object expected) {
    final ValueRequirement valueReq = new ValueRequirement("foo", new ComputationTargetSpecification (null));
    final ValueSpecification valueSpec = new ValueSpecification(valueReq, "mockFunctionId");
    final ComputedValue value = new ComputedValue (valueSpec, expected);
    _viewComputationCache.putSharedValue(value);
    final Object obj = _viewComputationCache.getValue (valueSpec);
    assertNotNull (obj);
    assertEquals (expected, obj);
  }
  
  @Test
  public void testPutGetCycle_primInt () {
    testPutGetCycle (Integer.MAX_VALUE);
  }
  
  @Test
  public void testPutGetCycle_primString () {
    testPutGetCycle ("Hello World");
  }
  
  @Test
  public void testPutGetCycle_primDouble () {
    testPutGetCycle (3.14);
  }
  
  public static final class Bean {
    private double _foo;
    private double _bar;
    public Bean () {
    }
    public void setFoo (final double foo) {
      _foo = foo;
    }
    public double getFoo () {
      return _foo;
    }
    public void setBar (final double bar) {
      _bar = bar;
    }
    public double getBar () {
      return _bar;
    }
    @Override
    public boolean equals (final Object o) {
      if (!(o instanceof Bean)) return false;
      Bean other = (Bean)o;
      return (other._foo == _foo) && (other._bar == _bar);
    }
    @Override
    public String toString () {
      return "Foo=" + _foo + ", Bar=" + _bar;
    }
  }
  
  @Test
  public void testPutGetCycle_bean () {
    final Bean bean = new Bean ();
    bean.setFoo (42.0);
    bean.setBar (-1.0);
    testPutGetCycle (bean);
  }
  
  @Test
  public void testPutGetCycle_beanList () {
    final List<Bean> list = new ArrayList<Bean> ();
    final Bean bean = new Bean ();
    bean.setFoo (42.0);
    bean.setBar (-1.0);
    list.add (bean);
    testPutGetCycle (list);
  }
  
}