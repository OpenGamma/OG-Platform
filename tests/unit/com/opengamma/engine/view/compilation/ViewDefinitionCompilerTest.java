/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.DefaultCachingComputationTargetResolver;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.function.DefaultFunctionResolver;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.livedata.InMemoryLKVSnapshotProvider;
import com.opengamma.engine.position.MockPositionSource;
import com.opengamma.engine.position.PortfolioImpl;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.position.PositionImpl;
import com.opengamma.engine.security.DefaultSecurity;
import com.opengamma.engine.security.MockSecuritySource;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ResultOutputMode;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;

public class ViewDefinitionCompilerTest {
  
  @Test(expected=IllegalArgumentException.class)
  public void testNullDependencyGraphs() {
    new ViewEvaluationModel(null, null);
  }
  
  @Test
  public void testEmptyView() {
    Identifier secIdentifier = Identifier.of("SEC", "1");
    PositionImpl pos = new PositionImpl(new BigDecimal(1), secIdentifier);
    PortfolioNodeImpl pn = new PortfolioNodeImpl("node");
    pn.addPosition(pos);
    PortfolioImpl p = new PortfolioImpl(UniqueIdentifier.of("FOO", "BAR"), "portfolio");
    p.setRootNode(pn);
    
    MockPositionSource positionSource = new MockPositionSource();
    positionSource.addPortfolio(p);
    
    DefaultSecurity defSec = new DefaultSecurity("");
    defSec.addIdentifier(secIdentifier);
    
    MockSecuritySource securitySource = new MockSecuritySource();
    securitySource.addSecurity(defSec);
    
    InMemoryLKVSnapshotProvider snapshotProvider = new InMemoryLKVSnapshotProvider();
    InMemoryFunctionRepository functionRepo = new InMemoryFunctionRepository();
    DefaultFunctionResolver functionResolver = new DefaultFunctionResolver(functionRepo);
    
    DefaultCachingComputationTargetResolver computationTargetResolver = new DefaultCachingComputationTargetResolver(new DefaultComputationTargetResolver(securitySource, positionSource));
    
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    FunctionCompilationContext functionCompilationContext = new FunctionCompilationContext();
    
    ViewCompilationServices vcs = new ViewCompilationServices(snapshotProvider, functionResolver, functionCompilationContext, computationTargetResolver, executorService, securitySource, positionSource);
    
    ViewDefinition viewDefinition = new ViewDefinition("My View", UniqueIdentifier.of("FOO", "BAR"), "kirk");
    
    ViewEvaluationModel vem = ViewDefinitionCompiler.compile(viewDefinition, vcs);
    
    assertTrue(vem.getAllLiveDataRequirements().isEmpty());
    assertTrue(vem.getDependencyGraphsByConfiguration().isEmpty());
    assertEquals(0, vem.getAllComputationTargets().size());
  }
  
  @Test
  public void testSingleValueNoLiveData() {
    Identifier secIdentifier = Identifier.of("SEC", "1");
    PositionImpl pos = new PositionImpl(new BigDecimal(1), secIdentifier);
    PortfolioNodeImpl pn = new PortfolioNodeImpl("node");
    pn.addPosition(pos);
    PortfolioImpl p = new PortfolioImpl(UniqueIdentifier.of("FOO", "BAR"), "portfolio");
    p.setRootNode(pn);
    
    MockPositionSource positionSource = new MockPositionSource();
    positionSource.addPortfolio(p);
    
    DefaultSecurity defSec = new DefaultSecurity("My Sec");
    defSec.addIdentifier(secIdentifier);
    
    MockSecuritySource securitySource = new MockSecuritySource();
    securitySource.addSecurity(defSec);
    
    InMemoryLKVSnapshotProvider snapshotProvider = new InMemoryLKVSnapshotProvider();
    
    // This function doesn't actually require anything, so it can compute at the node level without anything else.
    // Hence, the only target will be the node.
    ValueRequirement req1 = new ValueRequirement("Req-1", new ComputationTargetSpecification(pn));
    ValueSpecification spec1 = new ValueSpecification(req1);
    ComputedValue value1 = new ComputedValue(spec1, 14.2);
    MockFunction fn1 = new MockFunction(new ComputationTarget(pn),
        Collections.<ValueRequirement>emptySet(),
        Sets.newHashSet(value1));
    
    InMemoryFunctionRepository functionRepo = new InMemoryFunctionRepository();
    functionRepo.addFunction(fn1, fn1);
    DefaultFunctionResolver functionResolver = new DefaultFunctionResolver(functionRepo);
    DefaultCachingComputationTargetResolver computationTargetResolver = new DefaultCachingComputationTargetResolver(new DefaultComputationTargetResolver(securitySource, positionSource));
    
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    FunctionCompilationContext functionCompilationContext = new FunctionCompilationContext();
    functionCompilationContext.setSecuritySource(securitySource);
    
    ViewCompilationServices vcs = new ViewCompilationServices(snapshotProvider, functionResolver, functionCompilationContext, computationTargetResolver, executorService, securitySource, positionSource);
    
    ViewDefinition viewDefinition = new ViewDefinition("My View", UniqueIdentifier.of("FOO", "BAR"), "kirk");
    
    // We've not provided a function that targets the position level, so we can't ask for it.
    viewDefinition.getResultModelDefinition().setPositionOutputMode(ResultOutputMode.NONE);
    
    ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDefinition, "Fibble");
    calcConfig.addPortfolioRequirement("My Sec", "Req-1");
    viewDefinition.addViewCalculationConfiguration(calcConfig);
    
    ViewEvaluationModel vem = ViewDefinitionCompiler.compile(viewDefinition, vcs);
    
    assertTrue(vem.getAllLiveDataRequirements().isEmpty());
    assertEquals(1, vem.getAllDependencyGraphs().size());
    assertNotNull(vem.getDependencyGraph("Fibble"));
    assertTargets(vem, pn.getUniqueIdentifier());
  }

  @Test
  public void testSingleValueExternalDependency() {
    Identifier secIdentifier1 = Identifier.of("SEC", "1");
    Identifier secIdentifier2 = Identifier.of("SEC", "2");
    PositionImpl pos = new PositionImpl(new BigDecimal(1), secIdentifier1);
    PortfolioNodeImpl pn = new PortfolioNodeImpl("node");
    pn.addPosition(pos);
    PortfolioImpl p = new PortfolioImpl(UniqueIdentifier.of("FOO", "BAR"), "portfolio");
    p.setRootNode(pn);
    
    MockPositionSource positionSource = new MockPositionSource();
    positionSource.addPortfolio(p);
    
    DefaultSecurity sec1 = new DefaultSecurity("My Sec");
    sec1.addIdentifier(secIdentifier1);
    
    DefaultSecurity sec2 = new DefaultSecurity("Your Sec");
    sec2.addIdentifier(secIdentifier2);
    
    MockSecuritySource securitySource = new MockSecuritySource();
    securitySource.addSecurity(sec1);
    securitySource.addSecurity(sec2);
    
    InMemoryLKVSnapshotProvider snapshotProvider = new InMemoryLKVSnapshotProvider();
    
    ValueRequirement req1 = new ValueRequirement("Req-1", new ComputationTargetSpecification(pn));
    ValueSpecification spec1 = new ValueSpecification(req1);
    ComputedValue value1 = new ComputedValue(spec1, 14.2);
    ValueRequirement req2 = new ValueRequirement("Req-1", new ComputationTargetSpecification(sec2));
    ValueSpecification spec2 = new ValueSpecification(req2);
    ComputedValue value2 = new ComputedValue(spec2, 14.2);
    
    MockFunction fn1 = new MockFunction(new ComputationTarget(pn),
        Sets.newHashSet(req2),
        Sets.newHashSet(value1));
    
    MockFunction fn2 = new MockFunction(new ComputationTarget(sec2),
        Collections.<ValueRequirement>emptySet(),
        Sets.newHashSet(value2));
    
    InMemoryFunctionRepository functionRepo = new InMemoryFunctionRepository();
    functionRepo.addFunction(fn1, fn1);
    functionRepo.addFunction(fn2, fn2);
    DefaultFunctionResolver functionResolver = new DefaultFunctionResolver(functionRepo);
    DefaultCachingComputationTargetResolver computationTargetResolver = new DefaultCachingComputationTargetResolver(new DefaultComputationTargetResolver(securitySource, positionSource));
    
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    FunctionCompilationContext functionCompilationContext = new FunctionCompilationContext();
    
    ViewCompilationServices vcs = new ViewCompilationServices(snapshotProvider, functionResolver, functionCompilationContext, computationTargetResolver, executorService, securitySource, positionSource);
    
    ViewDefinition viewDefinition = new ViewDefinition("My View", UniqueIdentifier.of("FOO", "BAR"), "kirk");
    viewDefinition.getResultModelDefinition().setPositionOutputMode(ResultOutputMode.NONE);
    ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDefinition, "Fibble");
    calcConfig.addPortfolioRequirement("My Sec", "Req-1");
    viewDefinition.addViewCalculationConfiguration(calcConfig);
    ViewEvaluationModel vem = ViewDefinitionCompiler.compile(viewDefinition, vcs);
    
    assertTrue(vem.getAllLiveDataRequirements().isEmpty());
    assertEquals(1, vem.getAllDependencyGraphs().size());
    DependencyGraph dg = vem.getDependencyGraph("Fibble");
    assertNotNull(dg);
    assertTrue(dg.getAllRequiredLiveData().isEmpty());
    assertEquals(2, dg.getDependencyNodes().size());
    
    // Expect the node and the security, since we've turned off position-level outputs and not actually provided a
    // function that can produce them
    assertTargets(vem, sec2.getUniqueIdentifier(), pn.getUniqueIdentifier());
  }
  
  @Test
  public void testPrimitivesOnlyNoPortfolioReference() {
    ViewDefinition viewDefinition = new ViewDefinition("Test", "jonathan");
    ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDefinition, "Config1");
    viewDefinition.addViewCalculationConfiguration(calcConfig);
    
    UniqueIdentifier t1 = UniqueIdentifier.of("TestScheme", "t1");
    ValueRequirement r1 = new ValueRequirement("Req-1", new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, t1));
    ComputedValue v1 = new ComputedValue(new ValueSpecification(r1), 42);
    
    InMemoryLKVSnapshotProvider snapshotProvider = new InMemoryLKVSnapshotProvider();
    InMemoryFunctionRepository functionRepo = new InMemoryFunctionRepository();
    DefaultFunctionResolver functionResolver = new DefaultFunctionResolver(functionRepo);
    DefaultCachingComputationTargetResolver computationTargetResolver = new DefaultCachingComputationTargetResolver(new DefaultComputationTargetResolver());
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    FunctionCompilationContext compilationContext = new FunctionCompilationContext();
    ViewCompilationServices compilationServices = new ViewCompilationServices(snapshotProvider, functionResolver, compilationContext, computationTargetResolver, executorService);
    
    MockFunction f1 = new MockFunction(new ComputationTarget(ComputationTargetType.PRIMITIVE, t1), Collections.<ValueRequirement>emptySet(), Sets.newHashSet(v1));
    functionRepo.addFunction(f1, f1);
    
    // We'll require r1 which can be satisfied by f1
    calcConfig.addSpecificRequirement(r1);
    
    ViewEvaluationModel vem = ViewDefinitionCompiler.compile(viewDefinition, compilationServices);
    
    assertTrue(vem.getAllLiveDataRequirements().isEmpty());
    assertEquals(1, vem.getAllDependencyGraphs().size());
    assertNotNull(vem.getDependencyGraph("Config1"));
    assertTargets(vem, t1);
  }
  
  @Test
  public void testPrimitivesAndSecuritiesNoPortfolioReference() {
    ViewDefinition viewDefinition = new ViewDefinition("Test", "jonathan");
    ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDefinition, "Config1");
    viewDefinition.addViewCalculationConfiguration(calcConfig);
    
    Identifier secIdentifier1 = Identifier.of("SEC", "1");
    DefaultSecurity sec1 = new DefaultSecurity("My Sec");
    sec1.addIdentifier(secIdentifier1);
    MockSecuritySource securitySource = new MockSecuritySource();
    securitySource.addSecurity(sec1);
    
    UniqueIdentifier t1 = UniqueIdentifier.of("TestScheme", "t1");
    ValueRequirement r1 = new ValueRequirement("Req-1", new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, t1));
    ComputedValue v1 = new ComputedValue(new ValueSpecification(r1), 42);
    ValueRequirement r2 = new ValueRequirement("Req-2", new ComputationTargetSpecification(ComputationTargetType.SECURITY, sec1.getUniqueIdentifier()));
    ComputedValue v2 = new ComputedValue(new ValueSpecification(r2), 60);
    
    InMemoryLKVSnapshotProvider snapshotProvider = new InMemoryLKVSnapshotProvider();
    InMemoryFunctionRepository functionRepo = new InMemoryFunctionRepository();
    DefaultFunctionResolver functionResolver = new DefaultFunctionResolver(functionRepo);
    DefaultCachingComputationTargetResolver computationTargetResolver = new DefaultCachingComputationTargetResolver(new DefaultComputationTargetResolver(securitySource));
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    FunctionCompilationContext compilationContext = new FunctionCompilationContext();
    ViewCompilationServices compilationServices = new ViewCompilationServices(snapshotProvider, functionResolver, compilationContext, computationTargetResolver, executorService);
    
    MockFunction f1 = new MockFunction(new ComputationTarget(ComputationTargetType.PRIMITIVE, t1), Collections.<ValueRequirement>emptySet(), Sets.newHashSet(v1));
    MockFunction f2 = new MockFunction(new ComputationTarget(ComputationTargetType.SECURITY, sec1), Sets.newHashSet(r1), Sets.newHashSet(v2));
    functionRepo.addFunction(f1, f1);
    functionRepo.addFunction(f2, f2);
    
    // We'll require r2 which can be satisfied by f2, which in turn requires the output of f1
    // Additionally, the security should be resolved through the ComputationTargetResolver, which only has a security
    // source.
    calcConfig.addSpecificRequirement(r2);
    
    ViewEvaluationModel vem = ViewDefinitionCompiler.compile(viewDefinition, compilationServices);
    assertTrue(vem.getAllLiveDataRequirements().isEmpty());
    assertEquals(1, vem.getAllDependencyGraphs().size());
    assertNotNull(vem.getDependencyGraph("Config1"));
    assertTargets(vem, sec1.getUniqueIdentifier(), t1);
    
    // Turning off primitive outputs should not affect the dep graph since the primitive is needed for the security
    viewDefinition.getResultModelDefinition().setPrimitiveOutputMode(ResultOutputMode.NONE);
    vem = ViewDefinitionCompiler.compile(viewDefinition, compilationServices);
    assertTargets(vem, sec1.getUniqueIdentifier(), t1);
    
    // Turning off security outputs, even if all primitive outputs are enabled, should allow the dep graph to be
    // pruned completely, since the only *terminal* output is the security output.
    viewDefinition.getResultModelDefinition().setPrimitiveOutputMode(ResultOutputMode.ALL);
    viewDefinition.getResultModelDefinition().setSecurityOutputMode(ResultOutputMode.NONE);
    vem = ViewDefinitionCompiler.compile(viewDefinition, compilationServices);
    assertTargets(vem);
  }

  private void assertTargets(ViewEvaluationModel vem, UniqueIdentifier... targets){
    Set<UniqueIdentifier> expectedTargets = new HashSet<UniqueIdentifier>(Arrays.asList(targets));
    Set<ComputationTargetSpecification> actualTargets = vem.getAllComputationTargets();
    assertEquals(expectedTargets.size(), actualTargets.size());
    for (ComputationTargetSpecification actualTarget : actualTargets) {
      assertTrue(expectedTargets.contains(actualTarget.getUniqueIdentifier()));
    }
  }

}
