/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker.cache;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Collections;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.MarketDataPointSelector;
import com.opengamma.engine.marketdata.manipulator.MarketDataSelectionGraphManipulator;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.ExternalId;
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

  public void ofSingle() {
    final ViewExecutionCacheKey key = ViewExecutionCacheKey.of(viewDefinition1(), marketDataProvider("Foo"), null);
    assertEquals(key.toString(), "ViewExecution[View~123, Foo, No-op]");
    assertEquals(key.hashCode(), 835443498);
    assertTrue(key.equals(key));
    assertTrue(key.equals(ViewExecutionCacheKey.of(viewDefinition1(), marketDataProvider("Foo"), null)));
    assertFalse(key.equals(ViewExecutionCacheKey.of(viewDefinition1(), marketDataProvider("Bar"), null)));
    assertFalse(key.equals(ViewExecutionCacheKey.of(viewDefinition2(), marketDataProvider("Foo"), null)));
  }

  public void ofManipulated() {
    final ViewExecutionCacheKey key = ViewExecutionCacheKey
        .of(viewDefinition1(),
            marketDataProvider("Foo"),
            new MarketDataSelectionGraphManipulator(MarketDataPointSelector.of(ExternalId.of("Test", "Foo")), Collections
                .<String, Map<DistinctMarketDataSelector, FunctionParameters>>emptyMap()));
    assertEquals(key.toString(), "ViewExecution[View~123, Foo, Test~Foo & {}]");
    assertEquals(key.hashCode(), 838623242);
    assertFalse(key.equals(ViewExecutionCacheKey.of(viewDefinition1(), marketDataProvider("Foo"), null)));
    assertTrue(key
        .equals(ViewExecutionCacheKey.of(viewDefinition1(), marketDataProvider("Foo"), new MarketDataSelectionGraphManipulator(MarketDataPointSelector.of(ExternalId.of("Test", "Foo")),
            Collections.<String, Map<DistinctMarketDataSelector, FunctionParameters>>emptyMap()))));
    assertFalse(key
        .equals(ViewExecutionCacheKey.of(viewDefinition1(), marketDataProvider("Foo"), new MarketDataSelectionGraphManipulator(MarketDataPointSelector.of(ExternalId.of("Test", "Bar")),
            Collections.<String, Map<DistinctMarketDataSelector, FunctionParameters>>emptyMap()))));
  }
}
