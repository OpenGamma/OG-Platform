/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.engine.view;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import org.testng.annotations.Test;
import static com.opengamma.engine.view.ViewCalculationResultModelImplTest.COMPUTED_VALUE;
import static com.opengamma.engine.view.ViewCalculationResultModelImplTest.PORTFOLIO;
import static com.opengamma.engine.view.ViewCalculationResultModelImplTest.PORTFOLIO_ROOT_NODE;
import static com.opengamma.engine.view.ViewCalculationResultModelImplTest.SPEC;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.time.Instant;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTargetSpecification;
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
    model.setResultTimestamp(Instant.ofEpochMillis(500));
    assertEquals(Instant.ofEpochMillis(500), model.getResultTimestamp());
    
    Set<String> calcConfigNames = Sets.newHashSet("configName1", "configName2");
    model.setCalculationConfigurationNames(calcConfigNames);
    assertEquals(calcConfigNames, model.getCalculationConfigurationNames());
    
    model.setPortfolio(PORTFOLIO);
    model.addValue("configName1", COMPUTED_VALUE);
    
    assertEquals(Sets.newHashSet(SPEC, new ComputationTargetSpecification(PORTFOLIO_ROOT_NODE)), Sets.newHashSet(model.getAllTargets()));
    
    ViewCalculationResultModel calcResult = model.getCalculationResult("configName1");
    assertNotNull(calcResult);
    
    Map<Pair<String, ValueProperties>, ComputedValue> targetResults = calcResult.getValues(SPEC);
    assertEquals(1, targetResults.size());
    assertEquals("DATA", targetResults.keySet().iterator().next().getFirst());
    assertEquals(COMPUTED_VALUE, targetResults.values().iterator().next());
  }

}
