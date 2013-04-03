/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker.cache;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.worker.cache.ViewExecutionCacheKey;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link ViewExecutionCacheKey} class.
 */
@Test(groups = TestGroup.UNIT)
public class ViewExecutionCacheKeyTest {

  private ViewDefinition viewDefinition1() {
    return new ViewDefinition(UniqueId.of("View", "123"), "Test 1", "User");
  }

  private ViewDefinition viewDefinition2() {
    return new ViewDefinition(UniqueId.of("View", "456"), "Test 2", "User");
  }

  private MarketDataAvailabilityProvider marketDataProvider(final String key) {
    final MarketDataAvailabilityProvider mock = Mockito.mock(MarketDataAvailabilityProvider.class);
    Mockito.when(mock.getAvailabilityHintKey()).thenReturn(key);
    return mock;
  }

  private MarketDataAvailabilityProvider[] marketDataProviders1() {
    final MarketDataAvailabilityProvider[] result = new MarketDataAvailabilityProvider[3];
    result[0] = marketDataProvider("0");
    result[1] = marketDataProvider("1");
    result[2] = marketDataProvider("2");
    return result;
  }

  private MarketDataAvailabilityProvider[] marketDataProviders2() {
    final MarketDataAvailabilityProvider[] result = new MarketDataAvailabilityProvider[5];
    result[0] = marketDataProvider("2");
    result[1] = marketDataProvider("1");
    result[2] = marketDataProvider("0");
    result[3] = marketDataProvider("3");
    result[4] = marketDataProvider("4");
    return result;
  }

  public void ofIterable() {
    final ViewExecutionCacheKey key = ViewExecutionCacheKey.of(viewDefinition1(), Arrays.asList(marketDataProviders1()));
    assertEquals(key.toString(), "ViewExecution[View~123, 0, 1, 2]");
    assertEquals(key.hashCode(), 44715799);
    assertTrue(key.equals(key));
    assertTrue(key.equals(ViewExecutionCacheKey.of(viewDefinition1(), Arrays.asList(marketDataProviders1()))));
    assertFalse(key.equals(ViewExecutionCacheKey.of(viewDefinition1(), Arrays.asList(marketDataProviders2()))));
    assertFalse(key.equals(ViewExecutionCacheKey.of(viewDefinition2(), Arrays.asList(marketDataProviders1()))));
  }

  public void ofSingle() {
    final ViewExecutionCacheKey key = ViewExecutionCacheKey.of(viewDefinition1(), marketDataProvider("Foo"));
    assertEquals(key.toString(), "ViewExecution[View~123, Foo]");
    assertEquals(key.hashCode(), 44709164);
    assertTrue(key.equals(key));
    assertTrue(key.equals(ViewExecutionCacheKey.of(viewDefinition1(), marketDataProvider("Foo"))));
    assertFalse(key.equals(ViewExecutionCacheKey.of(viewDefinition1(), marketDataProvider("Bar"))));
    assertFalse(key.equals(ViewExecutionCacheKey.of(viewDefinition2(), marketDataProvider("Foo"))));
  }

  public void ofArray() {
    final ViewExecutionCacheKey key = ViewExecutionCacheKey.of(viewDefinition1(), marketDataProviders1());
    assertEquals(key.toString(), "ViewExecution[View~123, 0, 1, 2]");
    assertEquals(key.hashCode(), 44715799);
    assertTrue(key.equals(key));
    assertTrue(key.equals(ViewExecutionCacheKey.of(viewDefinition1(), marketDataProviders1())));
    assertFalse(key.equals(ViewExecutionCacheKey.of(viewDefinition1(), marketDataProviders2())));
    assertFalse(key.equals(ViewExecutionCacheKey.of(viewDefinition2(), marketDataProviders1())));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void ofEmpty() {
    ViewExecutionCacheKey.of(viewDefinition1(), Collections.<MarketDataAvailabilityProvider>emptySet());
  }

}
