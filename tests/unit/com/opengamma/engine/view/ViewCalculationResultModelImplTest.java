/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.HashMap;

import org.junit.Test;

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
public class ViewCalculationResultModelImplTest {
  
  public static final Position POSITION = new PositionImpl(UniqueIdentifier.of("PositionIdentifier", "testPosition"), new BigDecimal(1), new IdentifierBundle());
  public static final ComputationTargetSpecification SPEC = new ComputationTargetSpecification(POSITION);
  public static final ComputedValue COMPUTED_VALUE = new ComputedValue(new ValueSpecification(new ValueRequirement("DATA", SPEC)), "12345");
  public static final PortfolioImpl PORTFOLIO;
  public static final PortfolioNodeImpl PORTFOLIO_ROOT_NODE;
  
  static { 
    PORTFOLIO = new PortfolioImpl("testportfolio");
    PORTFOLIO_ROOT_NODE = new PortfolioNodeImpl(UniqueIdentifier.of("PortfolioIdentifier", "rootNode"), "rootNode");
    PORTFOLIO.setRootNode(PORTFOLIO_ROOT_NODE);
    PORTFOLIO_ROOT_NODE.addPosition(POSITION);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void illegalAddValue() {
    ViewCalculationResultModelImpl calcResult = new ViewCalculationResultModelImpl();
    calcResult.addValue(COMPUTED_VALUE);
  }
  
  @Test
  public void addValue() {
    ViewCalculationResultModelImpl calcResult = new ViewCalculationResultModelImpl();
    assertNull(calcResult.getValues(SPEC));
    assertTrue(calcResult.getAllTargets().isEmpty());
    
    calcResult.recursiveAddPortfolio(PORTFOLIO_ROOT_NODE);
    calcResult.addValue(COMPUTED_VALUE);
    calcResult.addValue(COMPUTED_VALUE);
    
    HashMap<String, ComputedValue> expectedMap = new HashMap<String, ComputedValue>();
    expectedMap.put("DATA", COMPUTED_VALUE);
    assertEquals(expectedMap, calcResult.getValues(SPEC));
    assertEquals(Sets.newHashSet(SPEC, new ComputationTargetSpecification(PORTFOLIO_ROOT_NODE)), Sets.newHashSet(calcResult.getAllTargets()));
    
    assertNull(calcResult.getValues(new ComputationTargetSpecification("nonexistent")));
  }

}
