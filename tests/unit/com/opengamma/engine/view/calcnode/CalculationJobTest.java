/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
public class CalculationJobTest {
  
  @Test
  public void fudgeEncodingNoInputsOutputs() {
    FudgeContext context = FudgeContext.GLOBAL_DEFAULT;
    CalculationJobSpecification spec = new CalculationJobSpecification("view", "config", 1L, 1L);
    ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueIdentifier.of("Scheme", "Value"));
    CalculationJob inputJob = new CalculationJob(spec, "1", targetSpec, Collections.<ValueSpecification>emptySet(), Collections.<ValueRequirement>emptySet());
    
    FudgeFieldContainer msg = inputJob.toFudgeMsg(new FudgeSerializationContext(context));
    msg = context.deserialize(context.toByteArray(msg)).getMessage();
    CalculationJob outputJob = CalculationJob.fromFudgeMsg(new FudgeDeserializationContext(context), msg);
    assertNotNull(outputJob);
    assertEquals(inputJob.getSpecification(), outputJob.getSpecification());
    assertNotNull(outputJob.getInputs());
    assertTrue(outputJob.getInputs().isEmpty());
    assertNotNull(outputJob.getDesiredValues());
    assertTrue(outputJob.getDesiredValues().isEmpty());
    assertEquals(targetSpec, outputJob.getComputationTargetSpecification());
    assertEquals("1", outputJob.getFunctionUniqueIdentifier());
  }

}
