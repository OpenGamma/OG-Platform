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
import com.opengamma.engine.depgraph.DependencyGraphModel;
import com.opengamma.engine.function.DefaultFunctionResolver;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.function.MockFunction;
import com.opengamma.engine.livedata.InMemoryLKVSnapshotProvider;
import com.opengamma.engine.position.InMemoryPositionMaster;
import com.opengamma.engine.position.PortfolioImpl;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.position.PositionImpl;
import com.opengamma.engine.security.DefaultSecurity;
import com.opengamma.engine.security.InMemorySecurityMaster;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;

public class PortfolioEvaluationModelTest {
  
  @Test(expected=NullPointerException.class)
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
    
    InMemoryPositionMaster positionMaster = new InMemoryPositionMaster();
    positionMaster.addPortfolio(p);
    
    DefaultSecurity defSec = new DefaultSecurity();
    defSec.addIdentifier(secIdentifier);
    
    InMemorySecurityMaster secMaster = new InMemorySecurityMaster();
    secMaster.addSecurity(defSec);
    
    InMemoryLKVSnapshotProvider snapshotProvider = new InMemoryLKVSnapshotProvider();
    InMemoryFunctionRepository functionRepo = new InMemoryFunctionRepository();
    DefaultFunctionResolver functionResolver = new DefaultFunctionResolver(functionRepo);
    
    DefaultComputationTargetResolver computationTargetResolver = new DefaultComputationTargetResolver(secMaster, positionMaster);
    
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    FunctionCompilationContext functionCompilationContext = new FunctionCompilationContext();
    
    ViewCompilationServices vcs = new ViewCompilationServices(snapshotProvider, functionResolver, positionMaster, secMaster, functionCompilationContext, computationTargetResolver, executorService);
    
    ViewDefinition viewDefinition = new ViewDefinition("My View", UniqueIdentifier.of("FOO", "BAR"), "kirk");
    
    PortfolioEvaluationModel pem = new PortfolioEvaluationModel(p);
    pem.init(vcs, viewDefinition);
    
    assertTrue(pem.getAllLiveDataRequirements().isEmpty());
    assertTrue(pem.getAllDependencyGraphModels().isEmpty());
    
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
    
    InMemoryPositionMaster positionMaster = new InMemoryPositionMaster();
    positionMaster.addPortfolio(p);
    
    DefaultSecurity defSec = new DefaultSecurity();
    defSec.addIdentifier(secIdentifier);
    defSec.setSecurityType("My Sec");
    
    InMemorySecurityMaster secMaster = new InMemorySecurityMaster();
    secMaster.addSecurity(defSec);
    
    InMemoryLKVSnapshotProvider snapshotProvider = new InMemoryLKVSnapshotProvider();
    
    ValueRequirement req1 = new ValueRequirement("Req-1", new ComputationTargetSpecification(pn));
    ValueSpecification spec1 = new ValueSpecification(req1);
    ComputedValue value1 = new ComputedValue(spec1, 14.2);
    MockFunction fn1 = new MockFunction(new ComputationTarget(pn),
        Collections.<ValueRequirement>emptySet(),
        Sets.newHashSet(value1));
    
    /*ValueRequirement req2 = new ValueRequirement("Req-1", new ComputationTargetSpecification(pos));
    ValueSpecification spec2 = new ValueSpecification(req2);
    ComputedValue value2 = new ComputedValue(spec2, 14.2);
    MockFunction fn2 = new MockFunction(new ComputationTarget(pos),
        Collections.<ValueRequirement>emptySet(),
        Sets.newHashSet(value2));
        */
    
    InMemoryFunctionRepository functionRepo = new InMemoryFunctionRepository();
    functionRepo.addFunction(fn1, fn1);
    //functionRepo.addFunction(fn2, fn2);
    DefaultFunctionResolver functionResolver = new DefaultFunctionResolver(functionRepo);
    DefaultComputationTargetResolver computationTargetResolver = new DefaultComputationTargetResolver(secMaster, positionMaster);
    
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    FunctionCompilationContext functionCompilationContext = new FunctionCompilationContext();
    
    ViewCompilationServices vcs = new ViewCompilationServices(snapshotProvider, functionResolver, positionMaster, secMaster, functionCompilationContext, computationTargetResolver, executorService);
    
    ViewDefinition viewDefinition = new ViewDefinition("My View", UniqueIdentifier.of("FOO", "BAR"), "kirk");
    viewDefinition.addValueDefinition("Fibble", "My Sec", "Req-1");
    viewDefinition.setComputePositionNodeCalculations(false);
    
    PortfolioEvaluationModel pem = new PortfolioEvaluationModel(p);
    pem.init(vcs, viewDefinition);
    
    assertTrue(pem.getAllLiveDataRequirements().isEmpty());
    assertEquals(1, pem.getAllDependencyGraphModels().size());
    DependencyGraphModel dgm = pem.getDependencyGraphModel("Fibble");
    assertNotNull(dgm);
    assertTrue(dgm.getAllRequiredLiveData().isEmpty());
    assertEquals(1, dgm.getAllDependencyGraphs().size());
    
    Set<Security> securities = pem.getSecurities();
    assertTrue(securities.contains(defSec));
    assertEquals(1, securities.size());
  }

}
