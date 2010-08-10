/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collections;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.junit.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
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
        targetSpec,
        Collections.<ValueSpecification>emptySet(), 
        Collections.<ValueRequirement>emptySet(),
        true);
    CalculationJobResultItem resultItem = new CalculationJobResultItem(item, InvocationResult.SUCCESS); 
    
    CalculationJobResult result = new CalculationJobResult(spec, 500, Collections.singletonList(resultItem));
    
    FudgeFieldContainer msg = result.toFudgeMsg(new FudgeSerializationContext(context));
    msg = context.deserialize(context.toByteArray(msg)).getMessage();
    CalculationJobResult outputJob = CalculationJobResult.fromFudgeMsg(new FudgeDeserializationContext(context), msg);
    assertNotNull(outputJob);
    assertEquals(spec, outputJob.getSpecification());
    assertEquals(500, outputJob.getDuration());
    assertNotNull(outputJob.getResultItems());
    assertEquals(1, outputJob.getResultItems().size());
    CalculationJobResultItem outputItem = outputJob.getResultItems().get(0);
    assertNotNull(outputItem);
    assertEquals(InvocationResult.SUCCESS, outputItem.getResult());
    assertNotNull(outputItem.getItem());
    assertNull(outputItem.getResults());
  }
  

}
