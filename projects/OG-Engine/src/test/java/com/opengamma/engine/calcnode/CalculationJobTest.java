/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.cache.AbstractIdentifierMap;
import com.opengamma.engine.cache.CacheSelectHint;
import com.opengamma.engine.cache.IdentifierMap;
import com.opengamma.engine.cache.InMemoryIdentifierMap;
import com.opengamma.engine.function.EmptyFunctionParameters;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class CalculationJobTest extends AbstractFudgeBuilderTestCase {

  public void fudgeEncodingNoInputsOutputs() {
    final IdentifierMap identifierMap = new InMemoryIdentifierMap();
    final CalculationJobSpecification spec = new CalculationJobSpecification(UniqueId.of("Test", "ViewCycle"), "config", Instant.now(), 1L);
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Scheme", "Value"));
    final List<CalculationJobItem> items = Collections.singletonList(new CalculationJobItem("1", new EmptyFunctionParameters(), targetSpec, Collections.<ValueSpecification>emptySet(), Collections
        .<ValueSpecification>emptySet(), ExecutionLogMode.INDICATORS));
    final CalculationJob inputJob = new CalculationJob(spec, 123L, VersionCorrection.LATEST, null, items, CacheSelectHint.allShared());
    AbstractIdentifierMap.convertIdentifiers(identifierMap, inputJob);
    final CalculationJob outputJob = cycleObject(CalculationJob.class, inputJob);
    assertNotNull(outputJob);
    AbstractIdentifierMap.resolveIdentifiers(identifierMap, outputJob);
    assertEquals(inputJob.getSpecification(), outputJob.getSpecification());
    assertEquals(inputJob.getFunctionInitializationIdentifier(), outputJob.getFunctionInitializationIdentifier());
    assertNotNull(outputJob.getJobItems());
    assertEquals(1, outputJob.getJobItems().size());
    final CalculationJobItem outputItem = outputJob.getJobItems().get(0);
    assertNotNull(outputItem);
    assertNotNull(outputItem.getInputs());
    assertTrue(outputItem.getInputs().length == 0);
    assertNotNull(outputItem.getOutputs());
    assertTrue(outputItem.getOutputs().length == 0);
    assertEquals(targetSpec, outputItem.getComputationTargetSpecification());
    assertEquals("1", outputItem.getFunctionUniqueIdentifier());
  }

  public void fudgeEncodingOneInputOneOutput() {
    final IdentifierMap identifierMap = new InMemoryIdentifierMap();
    final CalculationJobSpecification spec = new CalculationJobSpecification(UniqueId.of("Test", "ViewCycle"), "config", Instant.now(), 1L);
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Scheme", "Value"));
    final ValueSpecification outputSpec = ValueSpecification.of("Foo", ComputationTargetType.PRIMITIVE, UniqueId.of("Scheme", "Value2"),
        ValueProperties.with(ValuePropertyNames.FUNCTION, "mockFunctionId")
            .get());
    final ValueSpecification inputSpec = ValueSpecification.of("Foo", ComputationTargetType.PRIMITIVE, UniqueId.of("Scheme", "Value3"),
        ValueProperties.with(ValuePropertyNames.FUNCTION, "mockFunctionId")
            .get());
    final List<CalculationJobItem> items = Collections.singletonList(new CalculationJobItem("1", new EmptyFunctionParameters(), targetSpec, Sets.newHashSet(inputSpec), Sets.newHashSet(outputSpec),
        ExecutionLogMode.INDICATORS));
    final CalculationJob inputJob = new CalculationJob(spec, Long.MAX_VALUE, VersionCorrection.LATEST, null, items, CacheSelectHint.allShared());
    AbstractIdentifierMap.convertIdentifiers(identifierMap, inputJob);
    final CalculationJob outputJob = cycleObject(CalculationJob.class, inputJob);
    assertNotNull(outputJob);
    AbstractIdentifierMap.resolveIdentifiers(identifierMap, outputJob);
    assertEquals(inputJob.getSpecification(), outputJob.getSpecification());
    assertEquals(inputJob.getFunctionInitializationIdentifier(), outputJob.getFunctionInitializationIdentifier());
    assertNotNull(outputJob.getJobItems());
    assertEquals(1, outputJob.getJobItems().size());
    final CalculationJobItem outputItem = outputJob.getJobItems().get(0);
    assertNotNull(outputItem);
    assertEquals(1, outputItem.getInputs().length);
    assertEquals(inputSpec, outputItem.getInputs()[0]);
    assertEquals(1, outputItem.getOutputs().length);
    assertEquals(outputSpec, outputItem.getOutputs()[0]);
  }

  public void fudgeEncodingComputationTarget() {
    final CalculationJobSpecification jobSpec = new CalculationJobSpecification(UniqueId.of("Test", "ViewCycle"), "config", Instant.now(), 1L);
    final ComputationTargetSpecification target1 = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Scheme", "1"));
    final ComputationTargetSpecification target2 = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Scheme", "2"));
    CalculationJob job = new CalculationJob(jobSpec, Long.MAX_VALUE, VersionCorrection.LATEST, null, Arrays.asList(
        new CalculationJobItem("Foo", new EmptyFunctionParameters(), target1, Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification>emptySet(), ExecutionLogMode.INDICATORS),
        new CalculationJobItem("Bar", new EmptyFunctionParameters(), target1, Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification>emptySet(), ExecutionLogMode.INDICATORS),
        new CalculationJobItem("Cow", new EmptyFunctionParameters(), target2, Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification>emptySet(), ExecutionLogMode.INDICATORS)),
        CacheSelectHint.allShared());
    AbstractIdentifierMap.convertIdentifiers(new InMemoryIdentifierMap(), job);
    job = cycleObject(CalculationJob.class, job);
    assertNotNull(job);
    assertEquals(target1, job.getJobItems().get(0).getComputationTargetSpecification());
    assertEquals(target1, job.getJobItems().get(1).getComputationTargetSpecification());
    assertSame(job.getJobItems().get(0).getComputationTargetSpecification(), job.getJobItems().get(1).getComputationTargetSpecification());
    assertEquals(target2, job.getJobItems().get(2).getComputationTargetSpecification());
  }

  public void fudgeEncodingParameterizedFunction() {
    final CalculationJobSpecification jobSpec = new CalculationJobSpecification(UniqueId.of("Test", "ViewCycle"), "config", Instant.now(), 1L);
    final ComputationTargetSpecification target1 = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Scheme", "1"));
    final ComputationTargetSpecification target2 = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Scheme", "2"));
    CalculationJob job = new CalculationJob(jobSpec, Long.MAX_VALUE, VersionCorrection.LATEST, null, Arrays.asList(
        new CalculationJobItem("Foo", new EmptyFunctionParameters(), target1, Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification>emptySet(), ExecutionLogMode.INDICATORS),
        new CalculationJobItem("Foo", new EmptyFunctionParameters(), target1, Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification>emptySet(), ExecutionLogMode.INDICATORS),
        new CalculationJobItem("Bar", new EmptyFunctionParameters(), target2, Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification>emptySet(), ExecutionLogMode.INDICATORS)),
        CacheSelectHint.allShared());
    AbstractIdentifierMap.convertIdentifiers(new InMemoryIdentifierMap(), job);
    job = cycleObject(CalculationJob.class, job);
    assertNotNull(job);
    assertEquals("Foo", job.getJobItems().get(0).getFunctionUniqueIdentifier());
    assertEquals("Foo", job.getJobItems().get(1).getFunctionUniqueIdentifier());
    assertSame(job.getJobItems().get(0).getFunctionUniqueIdentifier(), job.getJobItems().get(1).getFunctionUniqueIdentifier());
    assertEquals("Bar", job.getJobItems().get(2).getFunctionUniqueIdentifier());
  }

}
