/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode.stats;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.opengamma.config.ConfigDocument;
import com.opengamma.config.ConfigMaster;
import com.opengamma.config.ConfigSearchHistoricRequest;
import com.opengamma.config.ConfigSearchHistoricResult;
import com.opengamma.config.ConfigSearchRequest;
import com.opengamma.config.ConfigSearchResult;
import com.opengamma.config.DefaultConfigDocument;
import com.opengamma.id.UniqueIdentifier;

/**
 * Tests the function statistics gatherer.
 */
public class FunctionCostTest {

  private FunctionCost _cost = new FunctionCost();

  @Test
  public void testBasicBehaviour() {
    FunctionInvocationStatistics stats = _cost.getStatistics("Default", "Foo");
    assertNotNull(stats);
    // Initial values
    assertEquals(1.0, stats.getInvocationCost(), 1e-5);
    assertEquals(1.0, stats.getDataInputCost(), 1e-5);
    assertEquals(1.0, stats.getDataOutputCost(), 1e-5);
    _cost.functionInvoked("Default", "Foo", 1, 2.0, 3.0, 4.0);
    // First sample
    assertEquals(2.0, stats.getInvocationCost(), 1e-5);
    assertEquals(3.0, stats.getDataInputCost(), 1e-5);
    assertEquals(4.0, stats.getDataOutputCost(), 1e-5);
    _cost.functionInvoked("Default", "Foo", 99, 99.0 * 3.0, 99.0 * 4.0, 99.0 * 5.0);
    assertEquals(2.0, stats.getInvocationCost(), 1e-5);
    assertEquals(3.0, stats.getDataInputCost(), 1e-5);
    assertEquals(4.0, stats.getDataOutputCost(), 1e-5);
    _cost.functionInvoked("Default", "Foo", 1, 3.0, 4.0, 5.0);
    // Updated sample
    assertEquals(2.991, stats.getInvocationCost(), 0.0005);
    assertEquals(3.991, stats.getDataInputCost(), 0.0005);
    assertEquals(4.991, stats.getDataOutputCost(), 0.0005);
    _cost.functionInvoked("Default", "Foo", 100, 100.0 * 3.0, 100.0 * 4.0, 100.0 * 5.0);
    // Older data less relevant
    assertEquals(2.996, stats.getInvocationCost(), 0.0005);
    assertEquals(3.996, stats.getDataInputCost(), 0.0005);
    assertEquals(4.996, stats.getDataOutputCost(), 0.0005);
  }

  @Test
  public void testMaps() {
    assertSame(_cost.getStatistics("A", "1"), _cost.getStatistics("A", "1"));
    assertNotSame(_cost.getStatistics("A", "2"), _cost.getStatistics("B", "2"));
    assertNotSame(_cost.getStatistics("B", "1"), _cost.getStatistics("A", "1"));
  }

  @Test
  public void testPersistence() {
    final AtomicInteger add = new AtomicInteger();
    final AtomicInteger update = new AtomicInteger();
    // TODO 2010-09-16 Andrew -- Is there a mock or in-memory config master we can use for quick tests? If not, can we derive one from this?
    final ConfigMaster<FunctionInvocationStatistics> testConfigMaster = new ConfigMaster<FunctionInvocationStatistics>() {

      private final Map<String, ConfigDocument<FunctionInvocationStatistics>> _data = new HashMap<String, ConfigDocument<FunctionInvocationStatistics>>();

      @Override
      public ConfigDocument<FunctionInvocationStatistics> add(ConfigDocument<FunctionInvocationStatistics> document) {
        add.incrementAndGet();
        DefaultConfigDocument<FunctionInvocationStatistics> newDocument = new DefaultConfigDocument<FunctionInvocationStatistics>();
        newDocument.setName(document.getName());
        newDocument.setValue(document.getValue());
        newDocument.setUniqueIdentifier(UniqueIdentifier.of("Test", "" + _data.size()));
        _data.put(newDocument.getName(), newDocument);
        return newDocument;
      }

      @Override
      public ConfigDocument<FunctionInvocationStatistics> get(UniqueIdentifier uid) {
        throw new UnsupportedOperationException();
      }

      @Override
      public void remove(UniqueIdentifier uid) {
        throw new UnsupportedOperationException();
      }

      @Override
      public ConfigSearchResult<FunctionInvocationStatistics> search(ConfigSearchRequest request) {
        final ConfigSearchResult<FunctionInvocationStatistics> result = new ConfigSearchResult<FunctionInvocationStatistics>();
        final ConfigDocument<FunctionInvocationStatistics> doc = _data.get(request.getName());
        if (doc != null) {
          result.getDocuments().add(doc);
        }
        return result;
      }

      @Override
      public ConfigSearchHistoricResult<FunctionInvocationStatistics> searchHistoric(ConfigSearchHistoricRequest request) {
        throw new UnsupportedOperationException();
      }

      @Override
      public ConfigDocument<FunctionInvocationStatistics> update(ConfigDocument<FunctionInvocationStatistics> document) {
        update.incrementAndGet();
        DefaultConfigDocument<FunctionInvocationStatistics> newDocument = new DefaultConfigDocument<FunctionInvocationStatistics>();
        newDocument.setName(document.getName());
        newDocument.setValue(document.getValue());
        newDocument.setUniqueIdentifier(document.getUniqueIdentifier());
        _data.put(newDocument.getName(), newDocument);
        return newDocument;
      }

    };
    _cost.setPersistence(testConfigMaster);
    FunctionInvocationStatistics stats = _cost.getStatistics("Default", "Foo");
    assertNotNull(stats);
    stats.recordInvocation(1, 1.0, 2.0, 3.0);
    final Runnable writer = _cost.createPersistenceWriter();
    assertNotNull(writer);
    // First run of the writer will write the new function to store
    writer.run();
    assertEquals(1, add.get());
    assertEquals(0, update.get());
    // Second run will do nothing as stats haven't changed
    writer.run();
    assertEquals(1, add.get());
    assertEquals(0, update.get());
    // Update stats and check the document updates
    stats.recordInvocation(100, 500.0, 600.0, 700.0);
    writer.run();
    assertEquals(1, add.get());
    assertEquals(1, update.get());
    // Create a new repository and check the values were preserved
    _cost = new FunctionCost();
    _cost.setPersistence(testConfigMaster);
    stats = _cost.getStatistics("Default", "Foo");
    assertEquals(5.0, stats.getInvocationCost(), 0.05);
    assertEquals(6.0, stats.getDataInputCost(), 0.05);
    assertEquals(7.0, stats.getDataOutputCost(), 0.05);
  }

}
