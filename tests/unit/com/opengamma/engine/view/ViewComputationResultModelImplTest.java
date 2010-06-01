/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */

package com.opengamma.engine.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.position.PortfolioImpl;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionImpl;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * 
 */
public class ViewComputationResultModelImplTest {
  
  private static final Position POSITION = new PositionImpl(UniqueIdentifier.of("PositionIdentifier", "testPosition"), new BigDecimal(1), new IdentifierBundle());
  private static final ComputationTargetSpecification SPEC = new ComputationTargetSpecification(POSITION);
  private static final ComputedValue COMPUTED_VALUE = new ComputedValue(new ValueSpecification(new ValueRequirement("DATA", SPEC)), "12345");
  
  @Test
  public void test() {
    ViewComputationResultModelImpl model = new ViewComputationResultModelImpl();
    
    model.setInputDataTimestamp(400);
    assertEquals(400, model.getInputDataTimestamp());
    model.setResultTimestamp(500);
    assertEquals(500, model.getResultTimestamp());
    
    Set<String> calcConfigNames = Sets.newHashSet("configName1", "configName2");
    model.setCalculationConfigurationNames(calcConfigNames);
    assertEquals(calcConfigNames, model.getCalculationConfigurationNames());
    
    PortfolioImpl portfolio = new PortfolioImpl("testportfolio");
    PortfolioNodeImpl rootNode = new PortfolioNodeImpl(UniqueIdentifier.of("PortfolioIdentifier", "rootNode"), "rootNode");
    portfolio.setRootNode(rootNode);
    rootNode.addPosition(POSITION);
    
    model.setPortfolio(portfolio);
    model.addValue("configName1", COMPUTED_VALUE);
    
    assertEquals(Sets.newHashSet(SPEC, new ComputationTargetSpecification(rootNode)), Sets.newHashSet(model.getAllTargets()));
    
    ViewCalculationResultModel calcResult = model.getCalculationResult("configName1");
    assertNotNull(calcResult);
    
    HashMap<String, ComputedValue> expectedMap = new HashMap<String, ComputedValue>();
    expectedMap.put("DATA", COMPUTED_VALUE);
    assertEquals(expectedMap, Maps.newHashMap(calcResult.getValues(SPEC)));
    
  }

}
