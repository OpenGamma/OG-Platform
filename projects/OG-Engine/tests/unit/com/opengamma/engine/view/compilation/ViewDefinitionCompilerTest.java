/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.time.Instant;

import org.testng.annotations.Test;

import com.opengamma.core.position.impl.MockPositionSource;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.DefaultCachingComputationTargetResolver;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.function.CachingFunctionRepositoryCompiler;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.function.resolver.DefaultFunctionResolver;
import com.opengamma.engine.marketdata.InMemoryLKVMarketDataProvider;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.test.MockSecurity;
import com.opengamma.engine.test.MockSecuritySource;
import com.opengamma.engine.view.ResultOutputMode;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ehcache.EHCacheUtils;

@Test
public class ViewDefinitionCompilerTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDependencyGraphs() {
    new CompiledViewDefinitionWithGraphsImpl(null, null, null, 0);
  }

  public void testEmptyView() {
    ExternalId secIdentifier = ExternalId.of("SEC", "1");
    SimplePosition pos = new SimplePosition(new BigDecimal(1), secIdentifier);
    SimplePortfolioNode pn = new SimplePortfolioNode("node");
    pn.addPosition(pos);
    SimplePortfolio p = new SimplePortfolio(UniqueId.of("FOO", "BAR"), "portfolio");
    p.setRootNode(pn);

    MockPositionSource positionSource = new MockPositionSource();
    positionSource.addPortfolio(p);

    MockSecurity defSec = new MockSecurity("");
    defSec.addIdentifier(secIdentifier);

    MockSecuritySource securitySource = new MockSecuritySource();
    securitySource.addSecurity(defSec);

    InMemoryLKVMarketDataProvider snapshotProvider = new InMemoryLKVMarketDataProvider();
    InMemoryFunctionRepository functionRepo = new InMemoryFunctionRepository();
    FunctionCompilationContext functionCompilationContext = new FunctionCompilationContext();
    functionCompilationContext.setFunctionInitId(123);
    final CompiledFunctionService cfs = new CompiledFunctionService(functionRepo, new CachingFunctionRepositoryCompiler(), functionCompilationContext);
    cfs.initialize();
    DefaultFunctionResolver functionResolver = new DefaultFunctionResolver(cfs);

    DefaultCachingComputationTargetResolver computationTargetResolver = new DefaultCachingComputationTargetResolver(new DefaultComputationTargetResolver(securitySource, positionSource), EHCacheUtils
        .createCacheManager());

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    ViewCompilationServices vcs = new ViewCompilationServices(snapshotProvider, functionResolver, functionCompilationContext, computationTargetResolver, executorService, securitySource,
        positionSource);

    ViewDefinition viewDefinition = new ViewDefinition("My View", ObjectId.of("FOO", "BAR"), "kirk");

    CompiledViewDefinitionWithGraphsImpl compiledViewDefinition = ViewDefinitionCompiler.compile(viewDefinition, vcs, Instant.now(), VersionCorrection.LATEST);

    assertTrue(compiledViewDefinition.getMarketDataRequirements().isEmpty());
    assertTrue(compiledViewDefinition.getDependencyGraphsByConfiguration().isEmpty());
    assertEquals(0, compiledViewDefinition.getComputationTargets().size());
  }

  public void testSingleValueNoLiveData() {
    ExternalId secIdentifier = ExternalId.of("SEC", "1");
    SimplePosition pos = new SimplePosition(new BigDecimal(1), secIdentifier);
    SimplePortfolioNode pn = new SimplePortfolioNode("node");
    pn.addPosition(pos);
    SimplePortfolio p = new SimplePortfolio(UniqueId.of("FOO", "BAR"), "portfolio");
    p.setRootNode(pn);

    MockPositionSource positionSource = new MockPositionSource();
    positionSource.addPortfolio(p);

    MockSecurity defSec = new MockSecurity("My Sec");
    defSec.addIdentifier(secIdentifier);

    MockSecuritySource securitySource = new MockSecuritySource();
    securitySource.addSecurity(defSec);

    InMemoryLKVMarketDataProvider snapshotProvider = new InMemoryLKVMarketDataProvider();

    // This function doesn't actually require anything, so it can compute at the node level without anything else.
    // Hence, the only target will be the node.
    MockFunction fn1 = MockFunction.getMockFunction(new ComputationTarget(pn), 14.2);

    InMemoryFunctionRepository functionRepo = new InMemoryFunctionRepository();
    functionRepo.addFunction(fn1);
    FunctionCompilationContext functionCompilationContext = new FunctionCompilationContext();
    functionCompilationContext.setFunctionInitId(123);
    functionCompilationContext.setSecuritySource(securitySource);

    final CompiledFunctionService cfs = new CompiledFunctionService(functionRepo, new CachingFunctionRepositoryCompiler(), functionCompilationContext);
    cfs.initialize();
    DefaultFunctionResolver functionResolver = new DefaultFunctionResolver(cfs);
    DefaultCachingComputationTargetResolver computationTargetResolver = new DefaultCachingComputationTargetResolver(new DefaultComputationTargetResolver(securitySource, positionSource), EHCacheUtils
        .createCacheManager());

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    ViewCompilationServices vcs = new ViewCompilationServices(snapshotProvider, functionResolver, functionCompilationContext, computationTargetResolver, executorService, securitySource,
        positionSource);

    ViewDefinition viewDefinition = new ViewDefinition("My View", ObjectId.of("FOO", "BAR"), "kirk");

    // We've not provided a function that targets the position level, so we can't ask for it.
    viewDefinition.getResultModelDefinition().setPositionOutputMode(ResultOutputMode.NONE);

    ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDefinition, "Fibble");
    calcConfig.addPortfolioRequirementName("My Sec", "OUTPUT");
    viewDefinition.addViewCalculationConfiguration(calcConfig);

    CompiledViewDefinitionWithGraphsImpl compiledViewDefinition = ViewDefinitionCompiler.compile(viewDefinition, vcs, Instant.now(), VersionCorrection.LATEST);

    assertTrue(compiledViewDefinition.getMarketDataRequirements().isEmpty());
    assertEquals(1, compiledViewDefinition.getAllDependencyGraphs().size());
    assertNotNull(compiledViewDefinition.getDependencyGraph("Fibble"));
    assertTargets(compiledViewDefinition, pn.getUniqueId());
  }

  public void testSingleValueExternalDependency() {
    ExternalId secIdentifier1 = ExternalId.of("SEC", "1");
    ExternalId secIdentifier2 = ExternalId.of("SEC", "2");
    SimplePosition pos = new SimplePosition(new BigDecimal(1), secIdentifier1);
    SimplePortfolioNode pn = new SimplePortfolioNode("node");
    pn.addPosition(pos);
    SimplePortfolio p = new SimplePortfolio(UniqueId.of("FOO", "BAR"), "portfolio");
    p.setRootNode(pn);

    MockPositionSource positionSource = new MockPositionSource();
    positionSource.addPortfolio(p);

    MockSecurity sec1 = new MockSecurity("My Sec");
    sec1.addIdentifier(secIdentifier1);

    MockSecurity sec2 = new MockSecurity("Your Sec");
    sec2.addIdentifier(secIdentifier2);

    MockSecuritySource securitySource = new MockSecuritySource();
    securitySource.addSecurity(sec1);
    securitySource.addSecurity(sec2);

    InMemoryLKVMarketDataProvider snapshotProvider = new InMemoryLKVMarketDataProvider();

    MockFunction fn2 = MockFunction.getMockFunction("fn2", new ComputationTarget(sec2), 14.2);
    MockFunction fn1 = MockFunction.getMockFunction("fn1", new ComputationTarget(pn), 14.2, fn2);

    InMemoryFunctionRepository functionRepo = new InMemoryFunctionRepository();
    functionRepo.addFunction(fn1);
    functionRepo.addFunction(fn2);
    FunctionCompilationContext functionCompilationContext = new FunctionCompilationContext();
    functionCompilationContext.setFunctionInitId(123);
    final CompiledFunctionService cfs = new CompiledFunctionService(functionRepo, new CachingFunctionRepositoryCompiler(), functionCompilationContext);
    cfs.initialize();
    DefaultFunctionResolver functionResolver = new DefaultFunctionResolver(cfs);
    DefaultCachingComputationTargetResolver computationTargetResolver = new DefaultCachingComputationTargetResolver(new DefaultComputationTargetResolver(securitySource, positionSource), EHCacheUtils
        .createCacheManager());

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    ViewCompilationServices vcs = new ViewCompilationServices(snapshotProvider, functionResolver, functionCompilationContext, computationTargetResolver, executorService, securitySource,
        positionSource);

    ViewDefinition viewDefinition = new ViewDefinition("My View", ObjectId.of("FOO", "BAR"), "kirk");
    viewDefinition.getResultModelDefinition().setPositionOutputMode(ResultOutputMode.NONE);
    ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDefinition, "Fibble");
    calcConfig.addPortfolioRequirementName("My Sec", "OUTPUT");
    viewDefinition.addViewCalculationConfiguration(calcConfig);
    
    CompiledViewDefinitionWithGraphsImpl compiledViewDefinition = ViewDefinitionCompiler.compile(viewDefinition, vcs, Instant.now(), VersionCorrection.LATEST);

    assertTrue(compiledViewDefinition.getMarketDataRequirements().isEmpty());
    assertEquals(1, compiledViewDefinition.getAllDependencyGraphs().size());
    DependencyGraph dg = compiledViewDefinition.getDependencyGraph("Fibble");
    assertNotNull(dg);
    assertTrue(dg.getAllRequiredMarketData().isEmpty());
    assertEquals(2, dg.getDependencyNodes().size());

    // Expect the node and the security, since we've turned off position-level outputs and not actually provided a
    // function that can produce them
    assertTargets(compiledViewDefinition, sec2.getUniqueId(), pn.getUniqueId());
  }

  public void testPrimitivesOnlyNoPortfolioReference() {
    ViewDefinition viewDefinition = new ViewDefinition("Test", "jonathan");
    ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDefinition, "Config1");
    viewDefinition.addViewCalculationConfiguration(calcConfig);

    UniqueId t1 = UniqueId.of("TestScheme", "t1");

    InMemoryLKVMarketDataProvider snapshotProvider = new InMemoryLKVMarketDataProvider();

    InMemoryFunctionRepository functionRepo = new InMemoryFunctionRepository();
    MockFunction f1 = MockFunction.getMockFunction(new ComputationTarget(ComputationTargetType.PRIMITIVE, t1), 42);
    functionRepo.addFunction(f1);

    FunctionCompilationContext compilationContext = new FunctionCompilationContext();
    compilationContext.setFunctionInitId(123);
    final CompiledFunctionService cfs = new CompiledFunctionService(functionRepo, new CachingFunctionRepositoryCompiler(), compilationContext);
    cfs.initialize();
    DefaultFunctionResolver functionResolver = new DefaultFunctionResolver(cfs);
    DefaultCachingComputationTargetResolver computationTargetResolver = new DefaultCachingComputationTargetResolver(new DefaultComputationTargetResolver(), EHCacheUtils.createCacheManager());
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    ViewCompilationServices compilationServices = new ViewCompilationServices(snapshotProvider, functionResolver, compilationContext, computationTargetResolver, executorService);

    // We'll require r1 which can be satisfied by f1
    calcConfig.addSpecificRequirement(f1.getResultSpec().toRequirementSpecification());

    CompiledViewDefinitionWithGraphsImpl compiledViewDefinition = ViewDefinitionCompiler.compile(viewDefinition, compilationServices, Instant.now(), VersionCorrection.LATEST);

    assertTrue(compiledViewDefinition.getMarketDataRequirements().isEmpty());
    assertEquals(1, compiledViewDefinition.getAllDependencyGraphs().size());
    assertNotNull(compiledViewDefinition.getDependencyGraph("Config1"));
    assertTargets(compiledViewDefinition, t1);
  }

  public void testPrimitivesAndSecuritiesNoPortfolioReference() {
    ViewDefinition viewDefinition = new ViewDefinition("Test", "jonathan");
    ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDefinition, "Config1");
    viewDefinition.addViewCalculationConfiguration(calcConfig);

    ExternalId secIdentifier1 = ExternalId.of("SEC", "1");
    MockSecurity sec1 = new MockSecurity("My Sec");
    sec1.addIdentifier(secIdentifier1);
    MockSecuritySource securitySource = new MockSecuritySource();
    securitySource.addSecurity(sec1);

    UniqueId t1 = UniqueId.of("TestScheme", "t1");

    InMemoryLKVMarketDataProvider snapshotProvider = new InMemoryLKVMarketDataProvider();

    InMemoryFunctionRepository functionRepo = new InMemoryFunctionRepository();
    MockFunction f1 = MockFunction.getMockFunction("f1", new ComputationTarget(ComputationTargetType.PRIMITIVE, t1), 42);
    MockFunction f2 = MockFunction.getMockFunction("f2", new ComputationTarget(ComputationTargetType.SECURITY, sec1), 60, f1);
    functionRepo.addFunction(f1);
    functionRepo.addFunction(f2);

    FunctionCompilationContext compilationContext = new FunctionCompilationContext();
    compilationContext.setFunctionInitId(123);
    final CompiledFunctionService cfs = new CompiledFunctionService(functionRepo, new CachingFunctionRepositoryCompiler(), compilationContext);
    cfs.initialize();
    DefaultFunctionResolver functionResolver = new DefaultFunctionResolver(cfs);
    DefaultCachingComputationTargetResolver computationTargetResolver = new DefaultCachingComputationTargetResolver(new DefaultComputationTargetResolver(securitySource), EHCacheUtils
        .createCacheManager());
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    ViewCompilationServices compilationServices = new ViewCompilationServices(snapshotProvider, functionResolver, compilationContext, computationTargetResolver, executorService);

    // We'll require r2 which can be satisfied by f2, which in turn requires the output of f1
    // Additionally, the security should be resolved through the ComputationTargetResolver, which only has a security
    // source.
    calcConfig.addSpecificRequirement(f2.getResultSpec().toRequirementSpecification());

    CompiledViewDefinitionWithGraphsImpl compiledViewDefinition = ViewDefinitionCompiler.compile(viewDefinition, compilationServices, Instant.now(), VersionCorrection.LATEST);
    assertTrue(compiledViewDefinition.getMarketDataRequirements().isEmpty());
    assertEquals(1, compiledViewDefinition.getAllDependencyGraphs().size());
    assertNotNull(compiledViewDefinition.getDependencyGraph("Config1"));
    assertTargets(compiledViewDefinition, sec1.getUniqueId(), t1);

    // Turning off primitive outputs should not affect the dep graph since the primitive is needed for the security
    viewDefinition.getResultModelDefinition().setPrimitiveOutputMode(ResultOutputMode.NONE);
    compiledViewDefinition = ViewDefinitionCompiler.compile(viewDefinition, compilationServices, Instant.now(), VersionCorrection.LATEST);
    assertTargets(compiledViewDefinition, sec1.getUniqueId(), t1);

    // Turning off security outputs, even if all primitive outputs are enabled, should allow the dep graph to be
    // pruned completely, since the only *terminal* output is the security output.
    viewDefinition.getResultModelDefinition().setPrimitiveOutputMode(ResultOutputMode.TERMINAL_OUTPUTS);
    viewDefinition.getResultModelDefinition().setSecurityOutputMode(ResultOutputMode.NONE);
    compiledViewDefinition = ViewDefinitionCompiler.compile(viewDefinition, compilationServices, Instant.now(), VersionCorrection.LATEST);
    assertTargets(compiledViewDefinition);
  }

  private void assertTargets(CompiledViewDefinitionWithGraphsImpl compiledViewDefinition, UniqueId... targets) {
    Set<UniqueId> expectedTargets = new HashSet<UniqueId>(Arrays.asList(targets));
    Set<ComputationTarget> actualTargets = compiledViewDefinition.getComputationTargets();
    assertEquals(expectedTargets.size(), actualTargets.size());
    for (ComputationTarget actualTarget : actualTargets) {
      assertTrue(expectedTargets.contains(actualTarget.getUniqueId()));
    }
  }

}
