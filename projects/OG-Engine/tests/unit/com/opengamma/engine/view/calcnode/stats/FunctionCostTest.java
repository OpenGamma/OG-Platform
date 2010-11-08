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

import org.junit.Test;

import com.opengamma.config.ConfigDocument;
import com.opengamma.config.ConfigTypeMaster;
import com.opengamma.config.ConfigHistoryRequest;
import com.opengamma.config.ConfigHistoryResult;
import com.opengamma.config.ConfigSearchRequest;
import com.opengamma.config.ConfigSearchResult;
import com.opengamma.config.memory.InMemoryConfigMaster;
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

  // TODO 2010-09-16 Andrew -- Is there a mock or in-memory config master we can use for quick tests? If not, can we derive one from this?
  private static class MockConfigMaster<T> implements ConfigTypeMaster<T> {

    private final Map<String, ConfigDocument<T>> _data = new HashMap<String, ConfigDocument<T>>();
    private int _addOperations;
    private int _updateOperations;

    @Override
    public ConfigDocument<T> add(ConfigDocument<T> document) {
      _addOperations++;
      ConfigDocument<T> newDocument = new ConfigDocument<T>();
      newDocument.setName(document.getName());
      newDocument.setValue(document.getValue());
      newDocument.setConfigId(UniqueIdentifier.of("Test", "" + _data.size()));
      _data.put(newDocument.getName(), newDocument);
      return newDocument;
    }

    @Override
    public ConfigDocument<T> get(UniqueIdentifier uid) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void remove(UniqueIdentifier uid) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ConfigSearchResult<T> search(ConfigSearchRequest request) {
      final ConfigSearchResult<T> result = new ConfigSearchResult<T>();
      final ConfigDocument<T> doc = _data.get(request.getName());
      if (doc != null) {
        result.getDocuments().add(doc);
      }
      return result;
    }

    @Override
    public ConfigHistoryResult<T> history(ConfigHistoryRequest request) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ConfigDocument<T> update(ConfigDocument<T> document) {
      _updateOperations++;
      ConfigDocument<T> newDocument = new ConfigDocument<T>();
      newDocument.setName(document.getName());
      newDocument.setValue(document.getValue());
      newDocument.setConfigId(document.getConfigId());
      _data.put(newDocument.getName(), newDocument);
      return newDocument;
    }

    public int getUpdateOperations() {
      int updateOperations = _updateOperations;
      _updateOperations = 0;
      return updateOperations;
    }

    public int getAddOperations() {
      int addOperations = _addOperations;
      _addOperations = 0;
      return addOperations;
    }
  }

  @Test
  public void testPersistence() {
    final MockConfigMaster<FunctionInvocationStatistics> testTypedMaster = new MockConfigMaster<FunctionInvocationStatistics>();
    InMemoryConfigMaster cfgMaster = new InMemoryConfigMaster();
    cfgMaster.addTypedMaster(FunctionInvocationStatistics.class, testTypedMaster);
    _cost.setPersistence(cfgMaster);
    FunctionInvocationStatistics stats = _cost.getStatistics("Default", "Foo");
    assertNotNull(stats);
    stats.recordInvocation(1, 1.0, 2.0, 3.0);
    final Runnable writer = _cost.createPersistenceWriter();
    assertNotNull(writer);
    // First run of the writer will write the new function to store (+ the mean document)
    writer.run();
    assertEquals(2, testTypedMaster.getAddOperations());
    assertEquals(0, testTypedMaster.getUpdateOperations());
    // Second run will do nothing as stats and averages haven't changed
    writer.run();
    assertEquals(0, testTypedMaster.getAddOperations());
    assertEquals(0, testTypedMaster.getUpdateOperations());
    // Update stats and check the document updates (and the average)
    stats.recordInvocation(100, 500.0, 600.0, 700.0);
    writer.run();
    assertEquals(0, testTypedMaster.getAddOperations());
    assertEquals(2, testTypedMaster.getUpdateOperations());
    // Create a new repository and check the values were preserved
    _cost = new FunctionCost();
    _cost.setPersistence(cfgMaster);
    stats = _cost.getStatistics("Default", "Foo");
    assertEquals(5.0, stats.getInvocationCost(), 0.05);
    assertEquals(6.0, stats.getDataInputCost(), 0.05);
    assertEquals(7.0, stats.getDataOutputCost(), 0.05);
  }

  @Test
  public void testInitialMean() {
    final MockConfigMaster<FunctionInvocationStatistics> testTypedMaster = new MockConfigMaster<FunctionInvocationStatistics>();
    InMemoryConfigMaster cfgMaster = new InMemoryConfigMaster();
    cfgMaster.addTypedMaster(FunctionInvocationStatistics.class, testTypedMaster);
    _cost.setPersistence(cfgMaster);
    FunctionInvocationStatistics stats = _cost.getStatistics ("Default", "Foo");
    assertEquals (1.0, stats.getInvocationCost (), 1e-5);
    assertEquals (1.0, stats.getDataInputCost (), 1e-5);
    assertEquals (1.0, stats.getDataOutputCost (), 1e-5);
    stats.recordInvocation (1, 2.0, 3.0, 4.0);
    // Nothing will have updated the average
    stats = _cost.getStatistics ("Default", "Bar");
    assertEquals (1.0, stats.getInvocationCost (), 1e-5);
    assertEquals (1.0, stats.getDataInputCost (), 1e-5);
    assertEquals (1.0, stats.getDataOutputCost (), 1e-5);
    final Runnable writer = _cost.createPersistenceWriter();
    writer.run ();
    // Averages will have been set now
    stats = _cost.getStatistics ("Default", "Cow");
    assertEquals (1.3, stats.getInvocationCost (), 0.05);
    assertEquals (1.7, stats.getDataInputCost (), 0.05);
    assertEquals (2.0, stats.getDataOutputCost(), 0.05);
    // Create a new repository and check the average was preserved
    _cost = new FunctionCost ();
    _cost.setPersistence (cfgMaster);
    stats = _cost.getStatistics ("Default", "Man");
    assertEquals (1.3, stats.getInvocationCost (), 0.05);
    assertEquals (1.7, stats.getDataInputCost (), 0.05);
    assertEquals (2.0, stats.getDataOutputCost(), 0.05);
  }

}
