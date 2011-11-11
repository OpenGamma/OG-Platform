/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.server;

import static org.testng.AssertJUnit.assertEquals;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.NumberDeltaComparer;
import com.opengamma.engine.view.ResultOutputMode;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Tests ViewDefinitionBuilder, which is particularly complex.
 */
@Test
public class ViewDefinitionBuilderTest {

  private FudgeContext _fudgeContext;
  
  @BeforeMethod
  public void setup() {
    _fudgeContext = OpenGammaFudgeContext.getInstance();
  }
  
  public void testSerializationCycle() {
    ViewDefinition viewDef = new ViewDefinition("Test View", ObjectId.of("Test Scheme", "Port1"), "someuser");
    viewDef.setMaxDeltaCalculationPeriod(1000L);
    viewDef.setMaxFullCalculationPeriod(60000L);
    viewDef.getResultModelDefinition().setAggregatePositionOutputMode(ResultOutputMode.TERMINAL_OUTPUTS);
    
    ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDef, "Test config");
    calcConfig.addPortfolioRequirementName("SecType", "Req1");
    calcConfig.addPortfolioRequirementName("SecType", "Req2");
    calcConfig.addPortfolioRequirementName("SecType2", "Req1");
    calcConfig.addSpecificRequirement(new ValueRequirement("Req3", ComputationTargetType.PRIMITIVE, UniqueId.of("Scheme2", "USD")));
    calcConfig.getDeltaDefinition().setNumberComparer(new NumberDeltaComparer(2));
    viewDef.addViewCalculationConfiguration(calcConfig);
    
    FudgeMsgEnvelope viewDefMsg = _fudgeContext.toFudgeMsg(viewDef);
    ViewDefinition deserializedViewDef = _fudgeContext.fromFudgeMsg(ViewDefinition.class, viewDefMsg.getMessage());
    
    assertEquals(viewDef, deserializedViewDef);
  }
  
}
