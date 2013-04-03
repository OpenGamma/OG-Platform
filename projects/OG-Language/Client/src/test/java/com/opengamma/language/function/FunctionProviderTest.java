/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.function;

import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.language.debug.DebugFunctionLiteral;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class FunctionProviderTest {

  private class ExtFunctionProviderBean extends FunctionProviderBean {

    private int _invocationCount;

    public int getInvocationCount() {
      return _invocationCount;
    }

    @Override
    protected void loadDefinitions(final Collection<MetaFunction> definitions) {
      super.loadDefinitions(definitions);
      _invocationCount++;
    }

  }

  private ExtFunctionProviderBean createBean(PublishedFunction... functions) {
    final ExtFunctionProviderBean bean = new ExtFunctionProviderBean();
    bean.setFunctions(Arrays.asList(functions));
    bean.afterPropertiesSet();
    return bean;
  }

  @Test
  public void testProviderBean() {
    final ExtFunctionProviderBean bean = createBean(new DebugFunctionLiteral("Literal-1", "1"),
        new DebugFunctionLiteral("Literal-2", "2"));
    final Set<MetaFunction> definitions = bean.getDefinitions();
    assertNotNull(definitions);
    assertEquals(2, definitions.size());
    assertEquals(1, bean.getInvocationCount());
    final Set<String> names = Sets.newHashSet("Literal-1", "Literal-2");
    for (MetaFunction function : definitions) {
      names.remove(function.getName());
    }
    assertTrue(names.isEmpty());
  }

  private void assertAggregatingProvider(final boolean cached) {
    final ExtFunctionProviderBean bean1 = createBean(new DebugFunctionLiteral("Literal-1", "1"),
        new DebugFunctionLiteral("Literal-2", "2"));
    final ExtFunctionProviderBean bean2 = createBean(new DebugFunctionLiteral("Literal-3", "3"),
        new DebugFunctionLiteral("Literal-4", "4"));
    final AggregatingFunctionProvider agg = cached ? AggregatingFunctionProvider.cachingInstance()
        : AggregatingFunctionProvider.nonCachingInstance();
    agg.addProvider(bean1);
    agg.addProvider(bean2);
    final Set<MetaFunction> definitions1 = agg.getDefinitions();
    assertEquals(1, bean1.getInvocationCount());
    assertEquals(1, bean2.getInvocationCount());
    final Set<String> names = Sets.newHashSet("Literal-1", "Literal-2", "Literal-3", "Literal-4");
    for (MetaFunction function : definitions1) {
      names.remove(function.getName());
    }
    assertTrue(names.isEmpty());
    final Set<MetaFunction> definitions2 = agg.getDefinitions();
    if (cached) {
      assertSame(definitions1, definitions2);
    } else {
      assertTrue(definitions1 != definitions2);
      assertEquals(definitions1, definitions2);
    }
    assertEquals(1, bean1.getInvocationCount());
    assertEquals(1, bean2.getInvocationCount());
    agg.flush();
    final Set<MetaFunction> definitions3 = agg.getDefinitions();
    assertEquals(definitions1, definitions3);
    assertEquals(2, bean1.getInvocationCount());
    assertEquals(2, bean2.getInvocationCount());
  }

  @Test
  public void testCachedAggregatingProvider() {
    assertAggregatingProvider(true);
  }

  @Test
  public void testNonCachedAggregatingProvider() {
    assertAggregatingProvider(false);
  }

}
