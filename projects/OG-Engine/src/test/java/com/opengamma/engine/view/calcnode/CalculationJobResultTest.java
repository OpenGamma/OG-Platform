/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.Instant;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.engine.view.cache.AbstractIdentifierMap;
import com.opengamma.engine.view.cache.IdentifierMap;
import com.opengamma.engine.view.cache.InMemoryIdentifierMap;
import com.opengamma.id.UniqueId;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * 
 */
@Test
public class CalculationJobResultTest {
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();
  
  public void fudge() {
    IdentifierMap identifierMap = new InMemoryIdentifierMap ();
    CalculationJobSpecification spec = new CalculationJobSpecification(UniqueId.of("Test", "ViewCycle"), "config", Instant.now(), 1L);
    
    CalculationJobResultItem item1 = CalculationJobResultItem.success();
    
    MutableExecutionLog executionLog = new MutableExecutionLog(ExecutionLogMode.INDICATORS);
    CalculationJobResultItem item2 = CalculationJobResultItemBuilder.of(executionLog).withException(new RuntimeException("failure!")).toResultItem();
    
    CalculationJobResult result = new CalculationJobResult(spec, 
        500, 
        Lists.newArrayList(item1, item2),
        "localhost");
    AbstractIdentifierMap.convertIdentifiers(identifierMap, result);
    FudgeSerializer serializationContext = new FudgeSerializer(s_fudgeContext);
    MutableFudgeMsg inputMsg = serializationContext.objectToFudgeMsg(result);
    FudgeMsg outputMsg = s_fudgeContext.deserialize(s_fudgeContext.toByteArray(inputMsg)).getMessage();
    
    FudgeDeserializer deserializationContext = new FudgeDeserializer(s_fudgeContext);
    CalculationJobResult outputJob = deserializationContext.fudgeMsgToObject(CalculationJobResult.class, outputMsg);
    
    assertNotNull(outputJob);
    AbstractIdentifierMap.resolveIdentifiers(identifierMap, outputJob);
    assertEquals(spec, outputJob.getSpecification());
    assertEquals(500, outputJob.getDuration());
    assertEquals("localhost", outputJob.getComputeNodeId());
    assertNotNull(outputJob.getResultItems());
    assertEquals(2, outputJob.getResultItems().size());
    CalculationJobResultItem outputItem1 = outputJob.getResultItems().get(0);
    assertNotNull(outputItem1);
    assertEquals(InvocationResult.SUCCESS, outputItem1.getResult());
    assertFalse(outputItem1.getExecutionLog().hasError());
    assertTrue(outputItem1.getMissingInputs().isEmpty());
    
    CalculationJobResultItem outputItem2 = outputJob.getResultItems().get(1);
    assertNotNull(outputItem2);
    assertEquals(InvocationResult.FUNCTION_THREW_EXCEPTION, outputItem2.getResult());
    assertEquals("java.lang.RuntimeException", outputItem2.getExecutionLog().getExceptionClass());
    assertEquals("failure!", outputItem2.getExecutionLog().getExceptionMessage());
    assertNotNull(outputItem2.getExecutionLog().getExceptionStackTrace());
    assertTrue(outputItem2.getMissingInputs().isEmpty());
  }
  

}
