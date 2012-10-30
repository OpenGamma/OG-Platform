/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.engine.view;

import static com.opengamma.engine.view.ViewCalculationResultModelImplTest.COMPUTED_VALUE;
import static com.opengamma.engine.view.ViewCalculationResultModelImplTest.SPEC;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.Map;

import javax.time.Duration;
import javax.time.Instant;

import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
@Test
public class ViewComputationResultModelImplTest {
  
  public void test() {
    InMemoryViewComputationResultModel model = new InMemoryViewComputationResultModel();
    checkModel(model);
  }

  static void checkModel(InMemoryViewResultModel model) {
    model.setValuationTime(Instant.ofEpochMillis(400));
    assertEquals(Instant.ofEpochMillis(400), model.getValuationTime());
    model.setCalculationTime(Instant.ofEpochMillis(500));
    assertEquals(Instant.ofEpochMillis(500), model.getCalculationTime());
    model.setCalculationDuration(Duration.ofMillis(100));
    assertEquals(Duration.ofMillis(100), model.getCalculationDuration());
    
    model.addValue("configName1", COMPUTED_VALUE);
    assertEquals(Sets.newHashSet(SPEC), Sets.newHashSet(model.getAllTargets()));
    
    ViewCalculationResultModel calcResult = model.getCalculationResult("configName1");
    assertNotNull(calcResult);
    
    Map<Pair<String, ValueProperties>, ComputedValue> targetResults = calcResult.getValues(SPEC);
    assertEquals(1, targetResults.size());
    assertEquals("DATA", targetResults.keySet().iterator().next().getFirst());
    assertEquals(COMPUTED_VALUE, targetResults.values().iterator().next());
  }

}
