/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.function.DefaultFunctionResolver;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.function.MockFunction;
import com.opengamma.engine.livedata.InMemoryLKVSnapshotProvider;
import com.opengamma.engine.position.MockPositionSource;
import com.opengamma.engine.position.PortfolioImpl;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.position.PositionImpl;
import com.opengamma.engine.security.DefaultSecurity;
import com.opengamma.engine.security.MockSecuritySource;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;

public class PortfolioEvaluationModelTest {
  
  @Test(expected=IllegalArgumentException.class)
  public void nullPortfolio() {
    new PortfolioEvaluationModel(null);
  }
  
  @Test
  public void initEmptyView() {
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
    
    DefaultComputationTargetResolver computationTargetResolver = new DefaultComputationTargetResolver(securitySource, positionSource);
    
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    FunctionCompilationContext functionCompilationContext = new FunctionCompilationContext();
    
    ViewCompilationServices vcs = new ViewCompilationServices(snapshotProvider, functionResolver, positionSource, securitySource, functionCompilationContext, computationTargetResolver, executorService);
    
    ViewDefinition viewDefinition = new ViewDefinition("My View", UniqueIdentifier.of("FOO", "BAR"), "kirk");
    
    PortfolioEvaluationModel pem = new PortfolioEvaluationModel(p);
    pem.init(vcs, viewDefinition);
    
    assertTrue(pem.getAllLiveDataRequirements().isEmpty());
    assertTrue(pem.getAllDependencyGraphs().isEmpty());
    
    Set<Security> securities = pem.getSecurities();
    assertTrue(securities.contains(defSec));
    assertEquals(1, securities.size());
  }

  @Test
  public void initSingleValueNoLiveData() {
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
    
    ValueRequirement req1 = new ValueRequirement("Req-1", new ComputationTargetSpecification(pn));
    ValueSpecification spec1 = new ValueSpecification(req1);
    ComputedValue value1 = new ComputedValue(spec1, 14.2);
    MockFunction fn1 = new MockFunction(new ComputationTarget(pn),
        Collections.<ValueRequirement>emptySet(),
        Sets.newHashSet(value1));
    
    InMemoryFunctionRepository functionRepo = new InMemoryFunctionRepository();
    functionRepo.addFunction(fn1, fn1);
    DefaultFunctionResolver functionResolver = new DefaultFunctionResolver(functionRepo);
    DefaultComputationTargetResolver computationTargetResolver = new DefaultComputationTargetResolver(securitySource, positionSource);
    
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    FunctionCompilationContext functionCompilationContext = new FunctionCompilationContext();
    functionCompilationContext.setSecuritySource(securitySource);
    
    ViewCompilationServices vcs = new ViewCompilationServices(snapshotProvider, functionResolver, positionSource, securitySource, functionCompilationContext, computationTargetResolver, executorService);
    
    ViewDefinition viewDefinition = new ViewDefinition("My View", UniqueIdentifier.of("FOO", "BAR"), "kirk");
    ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDefinition, "Fibble");
    calcConfig.addPortfolioRequirement("My Sec", "Req-1");
    calcConfig.setPositionOutputsDisabled(true);
    viewDefinition.addViewCalculationConfiguration(calcConfig);
    
    PortfolioEvaluationModel pem = new PortfolioEvaluationModel(p);
    pem.init(vcs, viewDefinition);
    
    assertTrue(pem.getAllLiveDataRequirements().isEmpty());
    assertEquals(1, pem.getAllDependencyGraphs().size());
    DependencyGraph dg = pem.getDependencyGraph("Fibble");
    assertNotNull(dg);
    assertTrue(dg.getAllRequiredLiveData().isEmpty());
    assertEquals(1, dg.getDependencyNodes().size());
    
    Set<Security> securities = pem.getSecurities();
    assertTrue(securities.contains(defSec));
    assertEquals(1, securities.size());
  }

  @Test
  public void initSingleValueExternalDependency() {
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
    DefaultComputationTargetResolver computationTargetResolver = new DefaultComputationTargetResolver(securitySource, positionSource);
    
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    FunctionCompilationContext functionCompilationContext = new FunctionCompilationContext();
    
    ViewCompilationServices vcs = new ViewCompilationServices(snapshotProvider, functionResolver, positionSource, securitySource, functionCompilationContext, computationTargetResolver, executorService);
    
    ViewDefinition viewDefinition = new ViewDefinition("My View", UniqueIdentifier.of("FOO", "BAR"), "kirk");
    ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDefinition, "Fibble");
    calcConfig.addPortfolioRequirement("My Sec", "Req-1");
    calcConfig.setPositionOutputsDisabled(true);
    viewDefinition.addViewCalculationConfiguration(calcConfig);
    PortfolioEvaluationModel pem = new PortfolioEvaluationModel(p);
    pem.init(vcs, viewDefinition);
    
    assertTrue(pem.getAllLiveDataRequirements().isEmpty());
    assertEquals(1, pem.getAllDependencyGraphs().size());
    DependencyGraph dg = pem.getDependencyGraph("Fibble");
    assertNotNull(dg);
    assertTrue(dg.getAllRequiredLiveData().isEmpty());
    assertEquals(2, dg.getDependencyNodes().size());
    
    Set<Security> securities = pem.getSecurities();
    assertTrue(securities.contains(sec1));
    assertEquals(1, securities.size());
  }

}
