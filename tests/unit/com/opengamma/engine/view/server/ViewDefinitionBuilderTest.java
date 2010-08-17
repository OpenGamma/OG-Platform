/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.server;

import static org.junit.Assert.assertEquals;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.junit.Before;
import org.junit.Test;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.fudgemsg.ViewDefinitionBuilder;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.NumberDeltaComparer;
import com.opengamma.engine.view.ResultOutputMode;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueIdentifier;

/**
 * Tests ViewDefinitionBuilder, which is particularly complex.
 */
public class ViewDefinitionBuilderTest {

  private FudgeContext _fudgeContext;
  
  @Before
  public void setup() {
    _fudgeContext = new FudgeContext();
    _fudgeContext.getObjectDictionary().addBuilder(ViewDefinition.class, new ViewDefinitionBuilder());
  }
  
  @Test
  public void testSerializationCycle() {
    ViewDefinition viewDef = new ViewDefinition("Test View", UniqueIdentifier.of("Test Scheme", "Port1"), "someuser");
    viewDef.setDeltaRecalculationPeriod(1000L);
    viewDef.setFullRecalculationPeriod(60000L);
    viewDef.getResultModelDefinition().setAggregatePositionOutputMode(ResultOutputMode.ALL);
    
    ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDef, "Test config");
    calcConfig.addPortfolioRequirement("SecType", "Req1");
    calcConfig.addPortfolioRequirement("SecType", "Req2");
    calcConfig.addPortfolioRequirement("SecType2", "Req1");
    calcConfig.addSpecificRequirement(new ValueRequirement("Req3", ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("Scheme2", "USD")));
    calcConfig.getDeltaDefinition().setNumberComparer(new NumberDeltaComparer(2));
    viewDef.addViewCalculationConfiguration(calcConfig);
    
    FudgeMsgEnvelope viewDefMsg = _fudgeContext.toFudgeMsg(viewDef);
    ViewDefinition deserializedViewDef = _fudgeContext.fromFudgeMsg(ViewDefinition.class, viewDefMsg.getMessage());
    
    assertEquals(viewDef, deserializedViewDef);
  }
  
}
