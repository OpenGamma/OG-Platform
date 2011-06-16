/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Sets;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.PortfolioImpl;
import com.opengamma.core.position.impl.PortfolioNodeImpl;
import com.opengamma.core.position.impl.PositionImpl;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
@Test
public class ViewCalculationResultModelImplTest {

  public static final Position POSITION = new PositionImpl(UniqueIdentifier.of("PositionIdentifier", "testPosition"), new BigDecimal(1), IdentifierBundle.EMPTY);
  public static final ComputationTargetSpecification SPEC = new ComputationTargetSpecification(POSITION);
  public static final ComputedValue COMPUTED_VALUE = new ComputedValue(new ValueSpecification(new ValueRequirement("DATA", SPEC), "mockFunctionId"), "12345");
  public static final PortfolioImpl PORTFOLIO;
  public static final PortfolioNodeImpl PORTFOLIO_ROOT_NODE;

  static {
    PORTFOLIO = new PortfolioImpl("testportfolio");
    PORTFOLIO_ROOT_NODE = new PortfolioNodeImpl(UniqueIdentifier.of("PortfolioIdentifier", "rootNode"), "rootNode");
    PORTFOLIO.setRootNode(PORTFOLIO_ROOT_NODE);
    PORTFOLIO_ROOT_NODE.addPosition(POSITION);
  }

  // REVIEW jonathan 2010-07-06 -- see the TODO on the part this used to test. For the moment, we are allowing any
  // requirement to be added, so this test makes no sense. It might be needed again depending on what happens with the
  // TODO.
  /*
   * @Test(expected=IllegalArgumentException.class)
   * public void illegalAddValue() {
   * ViewCalculationResultModelImpl calcResult = new ViewCalculationResultModelImpl();
   * calcResult.addValue(COMPUTED_VALUE);
   * }
   */

  public void addValue() {
    InMemoryViewResultModel resultModel = new InMemoryViewResultModel() { };
    resultModel.ensureCalculationConfigurationNames(Arrays.asList("Default"));
    ViewCalculationResultModelImpl calcResult = resultModel.getCalculationResultModelImpl("Default");
    assertNotNull(calcResult);
    assertNull(calcResult.getValues(SPEC));
    assertTrue(calcResult.getAllTargets().isEmpty());

    resultModel.setPortfolio(PORTFOLIO);
    resultModel.addValue("Default", COMPUTED_VALUE);
    resultModel.addValue("Default", COMPUTED_VALUE);

    Map<Pair<String, ValueProperties>, ComputedValue> targetResults = calcResult.getValues(SPEC);
    assertEquals(1, targetResults.size());
    assertEquals("DATA", targetResults.keySet().iterator().next().getFirst());
    assertEquals(COMPUTED_VALUE, targetResults.values().iterator().next());
    assertEquals(Sets.newHashSet(SPEC, new ComputationTargetSpecification(PORTFOLIO_ROOT_NODE)), Sets.newHashSet(calcResult.getAllTargets()));

    assertNull(calcResult.getValues(new ComputationTargetSpecification("nonexistent")));
  }

}
