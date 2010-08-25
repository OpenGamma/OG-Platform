/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueIdentifier;

/**
 * 
 */
public class CalculationJobResultTest {
  
  @Test
  public void fudge() {
    FudgeContext context = FudgeContext.GLOBAL_DEFAULT;
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
    
    FudgeFieldContainer msg = result.toFudgeMsg(new FudgeSerializationContext(context));
    msg = context.deserialize(context.toByteArray(msg)).getMessage();
    CalculationJobResult outputJob = CalculationJobResult.fromFudgeMsg(new FudgeDeserializationContext(context), msg);
    assertNotNull(outputJob);
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
