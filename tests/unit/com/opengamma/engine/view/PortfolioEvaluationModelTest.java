/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.function.DefaultFunctionResolver;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.livedata.InMemoryLKVSnapshotProvider;
import com.opengamma.engine.position.InMemoryPositionMaster;
import com.opengamma.engine.position.PortfolioImpl;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.position.PositionImpl;
import com.opengamma.engine.security.DefaultSecurity;
import com.opengamma.engine.security.InMemorySecurityMaster;
import com.opengamma.engine.security.Security;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;

public class PortfolioEvaluationModelTest {
  
  @Test(expected=NullPointerException.class)
  public void nullPortfolio() {
    new PortfolioEvaluationModel(null);
  }
  
  @Test
  public void testInitializationEmptyView() {
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

}
