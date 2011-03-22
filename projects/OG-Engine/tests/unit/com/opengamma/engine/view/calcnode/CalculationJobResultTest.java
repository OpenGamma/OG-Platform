/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import java.util.Collections;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import com.google.common.collect.Lists;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.IdentifierMap;
import com.opengamma.engine.view.cache.InMemoryIdentifierMap;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.fudge.OpenGammaFudgeContext;

/**
 * 
 */
@Test
public class CalculationJobResultTest {
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();
  
  public void fudge() {
    IdentifierMap identifierMap = new InMemoryIdentifierMap ();
    CalculationJobSpecification spec = new CalculationJobSpecification("view", "config", 1L, 1L);
    ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueIdentifier.of("Scheme", "Value"));
    
    CalculationJobItem item = new CalculationJobItem(
        "1",
        new EmptyFunctionParameters(),
        targetSpec,
        Collections.<ValueSpecification>emptySet(), 
        Collections.<ValueRequirement>emptySet());
    CalculationJobResultItem item1 = new CalculationJobResultItem(item); 
    CalculationJobResultItem item2 = new CalculationJobResultItem(item, new RuntimeException("failure!"));
    
    CalculationJobResult result = new CalculationJobResult(spec, 
        500, 
        Lists.newArrayList(item1, item2),
        "localhost");
    result.convertInputs(identifierMap);
    FudgeSerializationContext serializationContext = new FudgeSerializationContext(s_fudgeContext);
    MutableFudgeFieldContainer inputMsg = serializationContext.objectToFudgeMsg(result);
    FudgeFieldContainer outputMsg = s_fudgeContext.deserialize(s_fudgeContext.toByteArray(inputMsg)).getMessage();
    
    FudgeDeserializationContext deserializationContext = new FudgeDeserializationContext(s_fudgeContext);
    CalculationJobResult outputJob = deserializationContext.fudgeMsgToObject(CalculationJobResult.class, outputMsg);
    
    assertNotNull(outputJob);
    result.resolveInputs(identifierMap);
    assertEquals(spec, outputJob.getSpecification());
    assertEquals(500, outputJob.getDuration());
    assertEquals("localhost", outputJob.getComputeNodeId());
    assertNotNull(outputJob.getResultItems());
    assertEquals(2, outputJob.getResultItems().size());
    CalculationJobResultItem outputItem1 = outputJob.getResultItems().get(0);
    assertNotNull(outputItem1);
    assertEquals(InvocationResult.SUCCESS, outputItem1.getResult());
    assertNotNull(outputItem1.getItem());
    assertNull(outputItem1.getExceptionClass());
    assertNull(outputItem1.getExceptionMsg());
    assertNull(outputItem1.getStackTrace());
    assertTrue(outputItem1.getMissingInputs().isEmpty());
    
    CalculationJobResultItem outputItem2 = outputJob.getResultItems().get(1);
    assertNotNull(outputItem2);
    assertEquals(InvocationResult.FUNCTION_THREW_EXCEPTION, outputItem2.getResult());
    assertNotNull(outputItem2.getItem());
    assertEquals("java.lang.RuntimeException", outputItem2.getExceptionClass());
    assertEquals("failure!", outputItem2.getExceptionMsg());
    assertNotNull(outputItem2.getStackTrace());
    assertTrue(outputItem2.getMissingInputs().isEmpty());
  }
  

}
