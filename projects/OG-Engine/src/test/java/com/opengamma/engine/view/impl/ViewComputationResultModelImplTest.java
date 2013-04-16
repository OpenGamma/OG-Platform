/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.engine.view.impl;

import static com.opengamma.engine.view.impl.ViewCalculationResultModelImplTest.COMPUTED_VALUE_RESULT;
import static com.opengamma.engine.view.impl.ViewCalculationResultModelImplTest.SPEC;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import com.google.common.collect.Sets;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.impl.InMemoryViewComputationResultModel;
import com.opengamma.engine.view.impl.InMemoryViewResultModel;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class ViewComputationResultModelImplTest {
  
  public void test() {
    InMemoryViewComputationResultModel model = new InMemoryViewComputationResultModel();
    checkModel(model);
  }

  static void checkModel(InMemoryViewResultModel model) {
    ViewCycleExecutionOptions executionOptions = ViewCycleExecutionOptions.builder()
        .setValuationTime(Instant.ofEpochMilli(400)).create();

    model.setViewCycleExecutionOptions(executionOptions);
    assertEquals(executionOptions, model.getViewCycleExecutionOptions());
    model.setCalculationTime(Instant.ofEpochMilli(500));
    assertEquals(Instant.ofEpochMilli(500), model.getCalculationTime());
    model.setCalculationDuration(Duration.ofMillis(100));
    assertEquals(Duration.ofMillis(100), model.getCalculationDuration());
    
    model.addValue("configName1", COMPUTED_VALUE_RESULT);
    assertEquals(Sets.newHashSet(SPEC), Sets.newHashSet(model.getAllTargets()));
    
    ViewCalculationResultModel calcResult = model.getCalculationResult("configName1");
    assertNotNull(calcResult);
    
    Map<Pair<String, ValueProperties>, ComputedValueResult> targetResults = calcResult.getValues(SPEC);
    assertEquals(1, targetResults.size());
    assertEquals("DATA", targetResults.keySet().iterator().next().getFirst());
    assertEquals(COMPUTED_VALUE_RESULT, targetResults.values().iterator().next());
  }

}
