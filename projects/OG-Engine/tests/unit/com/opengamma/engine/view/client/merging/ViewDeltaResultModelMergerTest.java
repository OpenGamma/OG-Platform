/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client.merging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModelImpl;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.id.UniqueIdentifier;

/**
 * 
 */
public class ViewDeltaResultModelMergerTest {

  private static final String CONFIG_1 = "config1";
  private static final String CONFIG_2 = "config2";
  
  @Test
  public void testMerger() {
    ViewDeltaResultModelMerger merger = new ViewDeltaResultModelMerger();
    assertNull(merger.consume());
    
    ViewDeltaResultModelImpl deltaResult1 = new ViewDeltaResultModelImpl();
    deltaResult1.setCalculationConfigurationNames(Collections.singleton(CONFIG_1));
    deltaResult1.addValue(CONFIG_1, getComputedValue("value1", 1));
    deltaResult1.addValue(CONFIG_1, getComputedValue("value2", 2));
    merger.merge(deltaResult1);
    assertResultsEqual(deltaResult1, merger.consume());
    assertNull(merger.consume());
    
    ViewDeltaResultModelImpl deltaResult2 = new ViewDeltaResultModelImpl();
    deltaResult2.setCalculationConfigurationNames(Collections.singleton(CONFIG_1));
    deltaResult2.addValue(CONFIG_1, getComputedValue("value1", 3));
    
    merger.merge(deltaResult1);
    merger.merge(deltaResult2);
    
    ViewDeltaResultModelImpl expectedMergedResult = new ViewDeltaResultModelImpl();
    expectedMergedResult.setCalculationConfigurationNames(Collections.singleton(CONFIG_1));
    expectedMergedResult.addValue(CONFIG_1, getComputedValue("value1", 3));
    expectedMergedResult.addValue(CONFIG_1, getComputedValue("value2", 2));
    
    assertResultsEqual(expectedMergedResult, merger.consume());
    assertNull(merger.consume());
    
    ViewDeltaResultModelImpl deltaResult3 = new ViewDeltaResultModelImpl();
    deltaResult3.setCalculationConfigurationNames(Collections.singleton(CONFIG_2));
    deltaResult3.addValue(CONFIG_2, getComputedValue("value3", 4));    
    
    merger.merge(deltaResult1);
    merger.merge(deltaResult3);
    
    expectedMergedResult = new ViewDeltaResultModelImpl();
    expectedMergedResult.setCalculationConfigurationNames(Arrays.asList(CONFIG_1, CONFIG_2));
    expectedMergedResult.addValue(CONFIG_1, getComputedValue("value1", 1));
    expectedMergedResult.addValue(CONFIG_1, getComputedValue("value2", 2));
    expectedMergedResult.addValue(CONFIG_2, getComputedValue("value3", 4));
    
    assertResultsEqual(expectedMergedResult, merger.consume());
    assertNull(merger.consume());
  }
  
  @Test
  public void testHandlesPartiallyEmptyModels() {
    ViewDeltaResultModelMerger merger = new ViewDeltaResultModelMerger();
    ViewDeltaResultModelImpl deltaResult = new ViewDeltaResultModelImpl();
    merger.merge(deltaResult);
    
    deltaResult = new ViewDeltaResultModelImpl();
    deltaResult.setCalculationConfigurationNames(Collections.singleton(CONFIG_1));
    merger.merge(deltaResult);
    
    deltaResult = new ViewDeltaResultModelImpl();
    // Tests coping with expanding calculation configurations (e.g. if a new one has been added between computation
    // cycles)
    deltaResult.setCalculationConfigurationNames(Arrays.asList(CONFIG_1, CONFIG_2));
    deltaResult.addValue(CONFIG_1, getComputedValue("value1", 1));
    merger.merge(deltaResult);
  }

  private ComputedValue getComputedValue(String valueName, Object value) {
    UniqueIdentifier uniqueId = UniqueIdentifier.of("Scheme", valueName);
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
        Map<String, ComputedValue> expectedTargetValues = expectedCalcResult.getValues(targetSpec);
        Map<String, ComputedValue> actualTargetValues = actualCalcResult.getValues(targetSpec);
        assertEquals(expectedTargetValues, actualTargetValues);
      }
    }
  }
  
}
