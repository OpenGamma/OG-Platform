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
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.junit.Test;

import com.google.common.collect.Sets;
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
    
    List<CalculationJobItem> items = Collections.singletonList(new CalculationJobItem(
        "1", 
        targetSpec,
        Collections.<ValueSpecification>emptySet(), 
        Collections.<ValueRequirement>emptySet(),
        true));
    
    CalculationJob inputJob = new CalculationJob(spec, items, new DummyResultWriter());
    
    FudgeFieldContainer msg = inputJob.toFudgeMsg(new FudgeSerializationContext(context));
    msg = context.deserialize(context.toByteArray(msg)).getMessage();
    CalculationJob outputJob = CalculationJob.fromFudgeMsg(new FudgeDeserializationContext(context), msg);
    assertNotNull(outputJob);
    assertEquals(inputJob.getSpecification(), outputJob.getSpecification());
    assertNotNull(outputJob.getJobItems());
    assertEquals(1, outputJob.getJobItems().size());
    CalculationJobItem outputItem = outputJob.getJobItems().get(0);
    assertNotNull(outputItem);
    assertNotNull(outputItem.getInputs());
    assertTrue(outputItem.getInputs().isEmpty());
    assertNotNull(outputItem.getDesiredValues());
    assertTrue(outputItem.getDesiredValues().isEmpty());
    assertEquals(targetSpec, outputItem.getComputationTargetSpecification());
    assertEquals("1", outputItem.getFunctionUniqueIdentifier());
  }

  @Test
  public void fudgeEncodingOneInputOneOutput() {
    FudgeContext context = FudgeContext.GLOBAL_DEFAULT;
    CalculationJobSpecification spec = new CalculationJobSpecification("view", "config", 1L, 1L);
    ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueIdentifier.of("Scheme", "Value"));
    
    ValueRequirement desiredValue = new ValueRequirement("Foo", ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("Scheme", "Value2"));
    ValueSpecification inputSpec = new ValueSpecification(new ValueRequirement("Foo", ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("Scheme", "Value3")));
    
    List<CalculationJobItem> items = Collections.singletonList(new CalculationJobItem(
        "1", 
        targetSpec,
        Sets.newHashSet(inputSpec),
        Sets.newHashSet(desiredValue),
        true));
    
    CalculationJob inputJob = new CalculationJob(spec, items, new DummyResultWriter());
    
    FudgeFieldContainer msg = inputJob.toFudgeMsg(new FudgeSerializationContext(context));
    msg = context.deserialize(context.toByteArray(msg)).getMessage();
    CalculationJob outputJob = CalculationJob.fromFudgeMsg(new FudgeDeserializationContext(context), msg);
    assertNotNull(outputJob);
    assertEquals(inputJob.getSpecification(), outputJob.getSpecification());
    
    assertNotNull(outputJob.getJobItems());
    assertEquals(1, outputJob.getJobItems().size());
    CalculationJobItem outputItem = outputJob.getJobItems().get(0);
    assertNotNull(outputItem);
    
    assertEquals(1, outputItem.getInputs().size());
    assertTrue(outputItem.getInputs().contains(inputSpec));
    
    assertEquals(1, outputItem.getDesiredValues().size());
    assertTrue(outputItem.getDesiredValues().contains(desiredValue));
  }

}
