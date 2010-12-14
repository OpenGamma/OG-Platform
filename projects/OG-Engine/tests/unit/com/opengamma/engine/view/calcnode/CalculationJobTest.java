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
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.CacheSelectHint;
import com.opengamma.engine.view.cache.IdentifierMap;
import com.opengamma.engine.view.cache.InMemoryIdentifierMap;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.fudge.OpenGammaFudgeContext;

/**
 * 
 */
public class CalculationJobTest {
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();

  @Test
  public void fudgeEncodingNoInputsOutputs() {
    IdentifierMap identifierMap = new InMemoryIdentifierMap();
    CalculationJobSpecification spec = new CalculationJobSpecification("view", "config", 1L, 1L);
    ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueIdentifier.of("Scheme", "Value"));

    List<CalculationJobItem> items = Collections.singletonList(new CalculationJobItem("1", new EmptyFunctionParameters(), targetSpec, Collections.<ValueSpecification> emptySet(), Collections
        .<ValueRequirement> emptySet()));

    CalculationJob inputJob = new CalculationJob(spec, 123L, null, items, CacheSelectHint.allShared());
    inputJob.convertInputs(identifierMap);

    FudgeSerializationContext serContext = new FudgeSerializationContext(s_fudgeContext);
    MutableFudgeFieldContainer inputMsg = serContext.objectToFudgeMsg(inputJob);
    
    FudgeFieldContainer outputMsg = s_fudgeContext.deserialize(s_fudgeContext.toByteArray(inputMsg)).getMessage();
    FudgeDeserializationContext deserializationContext = new FudgeDeserializationContext(s_fudgeContext);
    CalculationJob outputJob = deserializationContext.fudgeMsgToObject(CalculationJob.class, outputMsg);
    assertNotNull(outputJob);
    outputJob.resolveInputs(identifierMap);
    assertEquals(inputJob.getSpecification(), outputJob.getSpecification());
    assertEquals (inputJob.getFunctionInitializationTimestamp(), outputJob.getFunctionInitializationTimestamp());
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
    IdentifierMap identifierMap = new InMemoryIdentifierMap();
    CalculationJobSpecification spec = new CalculationJobSpecification("view", "config", 1L, 1L);
    ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueIdentifier.of("Scheme", "Value"));

    ValueRequirement desiredValue = new ValueRequirement("Foo", ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("Scheme", "Value2"));
    ValueSpecification inputSpec = new ValueSpecification(new ValueRequirement("Foo", ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("Scheme", "Value3")), "mockFunctionId");

    List<CalculationJobItem> items = Collections.singletonList(new CalculationJobItem("1", new EmptyFunctionParameters(), targetSpec, Sets.newHashSet(inputSpec), Sets.newHashSet(desiredValue)));

    CalculationJob inputJob = new CalculationJob(spec, Long.MAX_VALUE, null, items, CacheSelectHint.allShared());
    inputJob.convertInputs(identifierMap);

    FudgeSerializationContext serializationContext = new FudgeSerializationContext(s_fudgeContext);
    MutableFudgeFieldContainer inputMsg = serializationContext.objectToFudgeMsg(inputJob);
    FudgeFieldContainer outputMsg = s_fudgeContext.deserialize(s_fudgeContext.toByteArray(inputMsg)).getMessage();
    FudgeDeserializationContext deserializationContext = new FudgeDeserializationContext(s_fudgeContext);
    CalculationJob outputJob = deserializationContext.fudgeMsgToObject(CalculationJob.class, outputMsg);
    
    assertNotNull(outputJob);
    outputJob.resolveInputs(identifierMap);
    assertEquals(inputJob.getSpecification(), outputJob.getSpecification());
    assertEquals (inputJob.getFunctionInitializationTimestamp(), outputJob.getFunctionInitializationTimestamp());

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
