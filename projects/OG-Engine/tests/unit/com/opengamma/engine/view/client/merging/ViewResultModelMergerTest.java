/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client.merging;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.InMemoryViewComputationResultModel;
import com.opengamma.engine.view.InMemoryViewDeltaResultModel;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.id.UniqueId;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
@Test
public class ViewResultModelMergerTest {

  private static final String CONFIG_1 = "config1";
  private static final String CONFIG_2 = "config2";
  
  public void testDeltaMerger() {
    ViewDeltaResultModelMerger merger = new ViewDeltaResultModelMerger();
    assertNull(merger.getLatestResult());
    
    InMemoryViewDeltaResultModel deltaResult1 = new InMemoryViewDeltaResultModel();
    deltaResult1.addValue(CONFIG_1, getComputedValue("value1", 1));
    deltaResult1.addValue(CONFIG_1, getComputedValue("value2", 2));
    merger.merge(deltaResult1);
    assertResultsEqual(deltaResult1, merger.getLatestResult());
    
    InMemoryViewDeltaResultModel deltaResult2 = new InMemoryViewDeltaResultModel();
    deltaResult2.addValue(CONFIG_1, getComputedValue("value1", 3));
    
    merger.merge(deltaResult1);
    merger.merge(deltaResult2);
    
    InMemoryViewDeltaResultModel expectedMergedResult = new InMemoryViewDeltaResultModel();
    expectedMergedResult.addValue(CONFIG_1, getComputedValue("value1", 3));
    expectedMergedResult.addValue(CONFIG_1, getComputedValue("value2", 2));
    
    assertResultsEqual(expectedMergedResult, merger.getLatestResult());
    
    InMemoryViewDeltaResultModel deltaResult3 = new InMemoryViewDeltaResultModel();
    deltaResult3.addValue(CONFIG_2, getComputedValue("value3", 4));    
    
    merger.merge(deltaResult1);
    merger.merge(deltaResult3);
    
    expectedMergedResult = new InMemoryViewDeltaResultModel();
    expectedMergedResult.addValue(CONFIG_1, getComputedValue("value1", 1));
    expectedMergedResult.addValue(CONFIG_1, getComputedValue("value2", 2));
    expectedMergedResult.addValue(CONFIG_2, getComputedValue("value3", 4));
    
    assertResultsEqual(expectedMergedResult, merger.getLatestResult());
  }
  
  public void testDeltaMergerHandlesPartiallyEmptyModels() {
    ViewDeltaResultModelMerger merger = new ViewDeltaResultModelMerger();
    InMemoryViewDeltaResultModel deltaResult = new InMemoryViewDeltaResultModel();
    merger.merge(deltaResult);
    
    deltaResult = new InMemoryViewDeltaResultModel();
    merger.merge(deltaResult);
    
    deltaResult = new InMemoryViewDeltaResultModel();
    // Tests coping with expanding calculation configurations (e.g. if a new one has been added between computation
    // cycles)
    deltaResult.addValue(CONFIG_1, getComputedValue("value1", 1));
    merger.merge(deltaResult);
  }
  
  public void testDeltaMergerPassesThroughEmptyDelta() {
    ViewDeltaResultModelMerger merger = new ViewDeltaResultModelMerger();
    InMemoryViewDeltaResultModel deltaResult = new InMemoryViewDeltaResultModel();
    merger.merge(deltaResult);
    assertNotNull(merger.getLatestResult());
  }

  //-------------------------------------------------------------------------
  public void testFullMerger() {
    ViewComputationResultModelMerger merger = new ViewComputationResultModelMerger();
    assertNull(merger.getLatestResult());
    
    InMemoryViewComputationResultModel result1 = new InMemoryViewComputationResultModel();
    result1.addValue(CONFIG_1, getComputedValue("value1", 1));
    result1.addValue(CONFIG_1, getComputedValue("value2", 2));
    result1.addMarketData(getComputedValue("vod", 250));
    merger.merge(result1);
    assertResultsEqual(result1, merger.getLatestResult());
    
    InMemoryViewComputationResultModel result2 = new InMemoryViewComputationResultModel();
    result2.addValue(CONFIG_1, getComputedValue("value1", 3));
    result2.addMarketData(getComputedValue("aapl", 400));
    merger.merge(result2);
    
    InMemoryViewComputationResultModel expectedMergedResult = new InMemoryViewComputationResultModel();
    expectedMergedResult.addValue(CONFIG_1, getComputedValue("value1", 3));
    expectedMergedResult.addValue(CONFIG_1, getComputedValue("value2", 2));
    expectedMergedResult.addMarketData(getComputedValue("vod", 250));
    expectedMergedResult.addMarketData(getComputedValue("aapl", 400));
    
    assertResultsEqual(expectedMergedResult, merger.getLatestResult());
    
    InMemoryViewComputationResultModel result3 = new InMemoryViewComputationResultModel();
    result3.addValue(CONFIG_2, getComputedValue("value3", 4));
    result3.addMarketData(getComputedValue("vod", 300));
    
    merger.merge(result1);
    merger.merge(result3);
    
    expectedMergedResult = new InMemoryViewComputationResultModel();
    expectedMergedResult.addValue(CONFIG_1, getComputedValue("value1", 1));
    expectedMergedResult.addValue(CONFIG_1, getComputedValue("value2", 2));
    expectedMergedResult.addValue(CONFIG_2, getComputedValue("value3", 4));
    result3.addMarketData(getComputedValue("vod", 300));
    
    assertResultsEqual(expectedMergedResult, merger.getLatestResult());
  }
  
  //-------------------------------------------------------------------------
  private ComputedValue getComputedValue(String valueName, Object value) {
    UniqueId uniqueId = UniqueId.of("Scheme", valueName);
    ValueRequirement valueRequirement = new ValueRequirement(valueName, ComputationTargetType.PRIMITIVE, uniqueId);
    return new ComputedValue(new ValueSpecification(valueRequirement, "FunctionId"), value);
  }
  
  private void assertResultsEqual(ViewResultModel expected, ViewResultModel actual) {
    assertEquals(expected.getAllTargets(), actual.getAllTargets());
    assertEquals(expected.getCalculationConfigurationNames(), actual.getCalculationConfigurationNames());
    
    for (String calcConfigName : expected.getCalculationConfigurationNames()) {
      ViewCalculationResultModel expectedCalcResult = expected.getCalculationResult(calcConfigName);
      ViewCalculationResultModel actualCalcResult = actual.getCalculationResult(calcConfigName);
      for (ComputationTargetSpecification targetSpec : expected.getAllTargets()) {
        Map<Pair<String, ValueProperties>, ComputedValue> expectedTargetValues = expectedCalcResult.getValues(targetSpec);
        Map<Pair<String, ValueProperties>, ComputedValue> actualTargetValues = actualCalcResult.getValues(targetSpec);
        assertEquals(expectedTargetValues, actualTargetValues);
      }
    }
  }
  
}
