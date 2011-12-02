/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import java.math.BigDecimal;
import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
@Test
public class ViewCalculationResultModelImplTest {

  public static final Position POSITION = new SimplePosition(UniqueId.of("PositionIdentifier", "testPosition"), new BigDecimal(1), ExternalIdBundle.EMPTY);
  public static final ComputationTargetSpecification SPEC = new ComputationTargetSpecification(POSITION);
  public static final ComputedValue COMPUTED_VALUE = new ComputedValue(new ValueSpecification(new ValueRequirement("DATA", SPEC), "mockFunctionId"), "12345");
  public static final SimplePortfolio PORTFOLIO;
  public static final SimplePortfolioNode PORTFOLIO_ROOT_NODE;

  static {
    PORTFOLIO = new SimplePortfolio("testportfolio");
    PORTFOLIO_ROOT_NODE = new SimplePortfolioNode(UniqueId.of("PortfolioIdentifier", "rootNode"), "rootNode");
    PORTFOLIO.setRootNode(PORTFOLIO_ROOT_NODE);
    PORTFOLIO_ROOT_NODE.addPosition(POSITION);
  }

  public void addValue() {
    InMemoryViewResultModel resultModel = new InMemoryViewResultModel() {
      private static final long serialVersionUID = 1L;
    };
    ViewCalculationResultModelImpl calcResult = resultModel.getCalculationResultModelImpl("Default");
    assertNull(calcResult);

    resultModel.addValue("Default", COMPUTED_VALUE);
    resultModel.addValue("Default", COMPUTED_VALUE);

    calcResult = resultModel.getCalculationResultModelImpl("Default");
    assertNotNull(calcResult);
    Map<Pair<String, ValueProperties>, ComputedValue> targetResults = calcResult.getValues(SPEC);
    assertEquals(1, targetResults.size());
    assertEquals("DATA", targetResults.keySet().iterator().next().getFirst());
    assertEquals(COMPUTED_VALUE, targetResults.values().iterator().next());
    assertEquals(Sets.newHashSet(SPEC), Sets.newHashSet(calcResult.getAllTargets()));

    assertNull(calcResult.getValues(new ComputationTargetSpecification("nonexistent")));
  }

}
