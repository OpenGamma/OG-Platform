/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.compilation;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;

import net.sf.ehcache.CacheManager;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.core.position.impl.MockPositionSource;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.security.impl.SimpleSecurity;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.DefaultCachingComputationTargetResolver;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.InMemorySecuritySource;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphBuilderFactory;
import com.opengamma.engine.depgraph.impl.DependencyGraphImpl;
import com.opengamma.engine.function.CachingFunctionRepositoryCompiler;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.function.resolver.DefaultFunctionResolver;
import com.opengamma.engine.marketdata.availability.FixedMarketDataAvailabilityProvider;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.view.ResultOutputMode;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.TestLifecycle;

@Test(groups = {TestGroup.UNIT, "ehcache" })
public class ViewDefinitionCompilerTest {

  private CacheManager _cacheManager;

  @BeforeClass
  public void setUpClass() {
    _cacheManager = EHCacheUtils.createTestCacheManager(getClass());
  }

  @AfterClass
  public void tearDownClass() {
    EHCacheUtils.shutdownQuiet(_cacheManager);
  }

  @BeforeMethod
  public void setUp() {
    EHCacheUtils.clear(_cacheManager);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDependencyGraphs() {
    new CompiledViewDefinitionWithGraphsImpl(null, null, null, null, null, null, 0, null, null, null);
  }

  public void testEmptyView() {
    TestLifecycle.begin();
    try {
      final ExternalId secIdentifier = ExternalId.of("SEC", "1");
      final SimplePosition pos = new SimplePosition(new BigDecimal(1), secIdentifier);
      final SimplePortfolioNode pn = new SimplePortfolioNode("node");
      pn.addPosition(pos);
      final SimplePortfolio p = new SimplePortfolio(UniqueId.of("FOO", "BAR"), "portfolio");
      p.setRootNode(pn);
      final MockPositionSource positionSource = new MockPositionSource();
      positionSource.addPortfolio(p);
      final SimpleSecurity defSec = new SimpleSecurity("");
      defSec.addExternalId(secIdentifier);
      final InMemorySecuritySource securitySource = new InMemorySecuritySource();
      securitySource.addSecurity(defSec);
      final InMemoryFunctionRepository functionRepo = new InMemoryFunctionRepository();
      final FunctionCompilationContext functionCompilationContext = new FunctionCompilationContext();
      functionCompilationContext.setFunctionInitId(123);
      functionCompilationContext.setRawComputationTargetResolver(new DefaultCachingComputationTargetResolver(new DefaultComputationTargetResolver(securitySource, positionSource),
          _cacheManager));
      final CompiledFunctionService cfs = new CompiledFunctionService(functionRepo, new CachingFunctionRepositoryCompiler(), functionCompilationContext);
      TestLifecycle.register(cfs);
      cfs.initialize();
      final DefaultFunctionResolver functionResolver = new DefaultFunctionResolver(cfs);
      final ViewCompilationServices vcs = new ViewCompilationServices(new FixedMarketDataAvailabilityProvider(), functionResolver, functionCompilationContext, cfs.getExecutorService(),
          new DependencyGraphBuilderFactory());
      final ViewDefinition viewDefinition = new ViewDefinition("My View", UniqueId.of("FOO", "BAR"), "kirk");
      final Instant now = Instant.now();
      final CompiledViewDefinitionWithGraphsImpl compiledViewDefinition = ViewDefinitionCompiler.compile(viewDefinition, vcs, now, VersionCorrection.of(now, now));
      assertTrue(compiledViewDefinition.getMarketDataRequirements().isEmpty());
      assertTrue(compiledViewDefinition.getDependencyGraphExplorers().isEmpty());
      assertEquals(0, compiledViewDefinition.getComputationTargets().size());
    } finally {
      TestLifecycle.end();
    }
  }

  public void testSingleValueNoLiveData() {
    TestLifecycle.begin();
    try {
      final ExternalId secIdentifier = ExternalId.of("SEC", "1");
      final SimplePosition pos = new SimplePosition(new BigDecimal(1), secIdentifier);
      final SimplePortfolioNode pn = new SimplePortfolioNode("node");
      pn.addPosition(pos);
      final SimplePortfolio p = new SimplePortfolio(UniqueId.of("FOO", "BAR"), "portfolio");
      p.setRootNode(pn);
      final MockPositionSource positionSource = new MockPositionSource();
      positionSource.addPortfolio(p);
      final SimpleSecurity defSec = new SimpleSecurity("My Sec");
      defSec.addExternalId(secIdentifier);
      final InMemorySecuritySource securitySource = new InMemorySecuritySource();
      securitySource.addSecurity(defSec);
      // This function doesn't actually require anything, so it can compute at the node level without anything else.
      // Hence, the only target will be the node.
      final MockFunction fn1 = MockFunction.getMockFunction(new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, pn), 14.2);
      final InMemoryFunctionRepository functionRepo = new InMemoryFunctionRepository();
      functionRepo.addFunction(fn1);
      final FunctionCompilationContext functionCompilationContext = new FunctionCompilationContext();
      functionCompilationContext.setFunctionInitId(123);
      functionCompilationContext.setSecuritySource(securitySource);
      final CompiledFunctionService cfs = new CompiledFunctionService(functionRepo, new CachingFunctionRepositoryCompiler(), functionCompilationContext);
      TestLifecycle.register(cfs);
      cfs.initialize();
      final DefaultFunctionResolver functionResolver = new DefaultFunctionResolver(cfs);
      final DefaultCachingComputationTargetResolver computationTargetResolver = new DefaultCachingComputationTargetResolver(new DefaultComputationTargetResolver(securitySource,
          positionSource), _cacheManager);
      functionCompilationContext.setRawComputationTargetResolver(computationTargetResolver);
      final ViewCompilationServices vcs = new ViewCompilationServices(new FixedMarketDataAvailabilityProvider(), functionResolver, functionCompilationContext, cfs.getExecutorService(),
          new DependencyGraphBuilderFactory());
      final ViewDefinition viewDefinition = new ViewDefinition("My View", UniqueId.of("FOO", "BAR"), "kirk");
      // We've not provided a function that targets the position level, so we can't ask for it.
      viewDefinition.getResultModelDefinition().setPositionOutputMode(ResultOutputMode.NONE);
      final ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDefinition, "Fibble");
      calcConfig.addPortfolioRequirementName("My Sec", "OUTPUT");
      viewDefinition.addViewCalculationConfiguration(calcConfig);
      final Instant now = Instant.now();
      final CompiledViewDefinitionWithGraphsImpl compiledViewDefinition = ViewDefinitionCompiler.compile(viewDefinition, vcs, now, VersionCorrection.of(now, now));
      assertTrue(compiledViewDefinition.getMarketDataRequirements().isEmpty());
      assertEquals(1, compiledViewDefinition.getDependencyGraphExplorers().size());
      assertNotNull(compiledViewDefinition.getDependencyGraphExplorer("Fibble"));
      assertTargets(compiledViewDefinition, pn.getUniqueId());
    } finally {
      TestLifecycle.end();
    }
  }

  public void testSingleValueExternalDependency() {
    TestLifecycle.begin();
    try {
      final ExternalId secIdentifier1 = ExternalId.of("SEC", "1");
      final ExternalId secIdentifier2 = ExternalId.of("SEC", "2");
      final SimplePosition pos = new SimplePosition(new BigDecimal(1), secIdentifier1);
      final SimplePortfolioNode pn = new SimplePortfolioNode("node");
      pn.addPosition(pos);
      final SimplePortfolio p = new SimplePortfolio(UniqueId.of("FOO", "BAR"), "portfolio");
      p.setRootNode(pn);
      final MockPositionSource positionSource = new MockPositionSource();
      positionSource.addPortfolio(p);
      final SimpleSecurity sec1 = new SimpleSecurity("My Sec");
      sec1.addExternalId(secIdentifier1);
      final SimpleSecurity sec2 = new SimpleSecurity("Your Sec");
      sec2.addExternalId(secIdentifier2);
      final InMemorySecuritySource securitySource = new InMemorySecuritySource();
      securitySource.addSecurity(sec1);
      securitySource.addSecurity(sec2);
      final MockFunction fn2 = MockFunction.getMockFunction("fn2", new ComputationTarget(ComputationTargetType.SECURITY, sec2), 14.2);
      final MockFunction fn1 = MockFunction.getMockFunction("fn1", new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, pn), 14.2, fn2);
      final InMemoryFunctionRepository functionRepo = new InMemoryFunctionRepository();
      functionRepo.addFunction(fn1);
      functionRepo.addFunction(fn2);
      final FunctionCompilationContext functionCompilationContext = new FunctionCompilationContext();
      functionCompilationContext.setFunctionInitId(123);
      final CompiledFunctionService cfs = new CompiledFunctionService(functionRepo, new CachingFunctionRepositoryCompiler(), functionCompilationContext);
      TestLifecycle.register(cfs);
      cfs.initialize();
      final DefaultFunctionResolver functionResolver = new DefaultFunctionResolver(cfs);
      final DefaultCachingComputationTargetResolver computationTargetResolver = new DefaultCachingComputationTargetResolver(new DefaultComputationTargetResolver(securitySource,
          positionSource), _cacheManager);
      functionCompilationContext.setRawComputationTargetResolver(computationTargetResolver);
      final ViewCompilationServices vcs = new ViewCompilationServices(new FixedMarketDataAvailabilityProvider(), functionResolver, functionCompilationContext, cfs.getExecutorService(),
          new DependencyGraphBuilderFactory());
      final ViewDefinition viewDefinition = new ViewDefinition("My View", UniqueId.of("FOO", "BAR"), "kirk");
      viewDefinition.getResultModelDefinition().setPositionOutputMode(ResultOutputMode.NONE);
      final ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDefinition, "Fibble");
      calcConfig.addPortfolioRequirementName("My Sec", "OUTPUT");
      viewDefinition.addViewCalculationConfiguration(calcConfig);
      final Instant now = Instant.now();
      final CompiledViewDefinitionWithGraphsImpl compiledViewDefinition = ViewDefinitionCompiler.compile(viewDefinition, vcs, now, VersionCorrection.of(now, now));
      assertTrue(compiledViewDefinition.getMarketDataRequirements().isEmpty());
      assertEquals(1, compiledViewDefinition.getDependencyGraphExplorers().size());
      final DependencyGraph dg = compiledViewDefinition.getDependencyGraphExplorer("Fibble").getWholeGraph();
      assertNotNull(dg);
      assertTrue(DependencyGraphImpl.getMarketData(dg).isEmpty());
      assertEquals(2, dg.getSize());
      // Expect the node and the security, since we've turned off position-level outputs and not actually provided a
      // function that can produce them
      assertTargets(compiledViewDefinition, sec2.getUniqueId(), pn.getUniqueId());
    } finally {
      TestLifecycle.end();
    }
  }

  public void testPrimitivesOnlyNoPortfolioReference() {
    TestLifecycle.begin();
    try {
      final ViewDefinition viewDefinition = new ViewDefinition("Test", "jonathan");
      final ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDefinition, "Config1");
      viewDefinition.addViewCalculationConfiguration(calcConfig);
      final UniqueId t1 = UniqueId.of("TestScheme", "t1");
      final InMemoryFunctionRepository functionRepo = new InMemoryFunctionRepository();
      final MockFunction f1 = MockFunction.getMockFunction(new ComputationTarget(ComputationTargetType.PRIMITIVE, t1), 42);
      functionRepo.addFunction(f1);
      final FunctionCompilationContext compilationContext = new FunctionCompilationContext();
      compilationContext.setFunctionInitId(123);
      final CompiledFunctionService cfs = new CompiledFunctionService(functionRepo, new CachingFunctionRepositoryCompiler(), compilationContext);
      TestLifecycle.register(cfs);
      cfs.initialize();
      final DefaultFunctionResolver functionResolver = new DefaultFunctionResolver(cfs);
      final DefaultCachingComputationTargetResolver computationTargetResolver = new DefaultCachingComputationTargetResolver(new DefaultComputationTargetResolver(), _cacheManager);
      compilationContext.setRawComputationTargetResolver(computationTargetResolver);
      final ViewCompilationServices compilationServices = new ViewCompilationServices(new FixedMarketDataAvailabilityProvider(), functionResolver, compilationContext,
          cfs.getExecutorService(), new DependencyGraphBuilderFactory());
      // We'll require r1 which can be satisfied by f1
      calcConfig.addSpecificRequirement(f1.getResultSpec().toRequirementSpecification());
      final Instant now = Instant.now();
      final CompiledViewDefinitionWithGraphsImpl compiledViewDefinition = ViewDefinitionCompiler.compile(viewDefinition, compilationServices, now, VersionCorrection.of(now, now));
      assertTrue(compiledViewDefinition.getMarketDataRequirements().isEmpty());
      assertEquals(1, compiledViewDefinition.getDependencyGraphExplorers().size());
      assertNotNull(compiledViewDefinition.getDependencyGraphExplorer("Config1"));
      assertTargets(compiledViewDefinition, t1);
    } finally {
      TestLifecycle.end();
    }
  }

  public void testPrimitivesAndSecuritiesNoPortfolioReference() {
    TestLifecycle.begin();
    try {
      final ViewDefinition viewDefinition = new ViewDefinition("Test", "jonathan");
      final ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDefinition, "Config1");
      viewDefinition.addViewCalculationConfiguration(calcConfig);
      final ExternalId secIdentifier1 = ExternalId.of("SEC", "1");
      final SimpleSecurity sec1 = new SimpleSecurity("My Sec");
      sec1.addExternalId(secIdentifier1);
      final InMemorySecuritySource securitySource = new InMemorySecuritySource();
      securitySource.addSecurity(sec1);
      final UniqueId t1 = UniqueId.of("TestScheme", "t1");
      final InMemoryFunctionRepository functionRepo = new InMemoryFunctionRepository();
      final MockFunction f1 = MockFunction.getMockFunction("f1", new ComputationTarget(ComputationTargetType.PRIMITIVE, t1), 42);
      final MockFunction f2 = MockFunction.getMockFunction("f2", new ComputationTarget(ComputationTargetType.SECURITY, sec1), 60, f1);
      functionRepo.addFunction(f1);
      functionRepo.addFunction(f2);
      final FunctionCompilationContext compilationContext = new FunctionCompilationContext();
      compilationContext.setFunctionInitId(123);
      final CompiledFunctionService cfs = new CompiledFunctionService(functionRepo, new CachingFunctionRepositoryCompiler(), compilationContext);
      TestLifecycle.register(cfs);
      cfs.initialize();
      final DefaultFunctionResolver functionResolver = new DefaultFunctionResolver(cfs);
      final DefaultCachingComputationTargetResolver computationTargetResolver = new DefaultCachingComputationTargetResolver(new DefaultComputationTargetResolver(securitySource),
          _cacheManager);
      compilationContext.setRawComputationTargetResolver(computationTargetResolver);
      final ViewCompilationServices compilationServices = new ViewCompilationServices(new FixedMarketDataAvailabilityProvider(), functionResolver, compilationContext,
          cfs.getExecutorService(), new DependencyGraphBuilderFactory());
      // We'll require r2 which can be satisfied by f2, which in turn requires the output of f1
      // Additionally, the security should be resolved through the ComputationTargetResolver, which only has a security
      // source.
      calcConfig.addSpecificRequirement(f2.getResultSpec().toRequirementSpecification());
      final Instant now = Instant.now();
      CompiledViewDefinitionWithGraphsImpl compiledViewDefinition = ViewDefinitionCompiler.compile(viewDefinition, compilationServices, now, VersionCorrection.of(now, now));
      assertTrue(compiledViewDefinition.getMarketDataRequirements().isEmpty());
      assertEquals(1, compiledViewDefinition.getDependencyGraphExplorers().size());
      assertNotNull(compiledViewDefinition.getDependencyGraphExplorer("Config1"));
      assertTargets(compiledViewDefinition, sec1.getUniqueId(), t1);
      // Turning off primitive outputs should not affect the dep graph since the primitive is needed for the security
      viewDefinition.getResultModelDefinition().setPrimitiveOutputMode(ResultOutputMode.NONE);
      compiledViewDefinition = ViewDefinitionCompiler.compile(viewDefinition, compilationServices, now, VersionCorrection.of(now, now));
      assertTargets(compiledViewDefinition, sec1.getUniqueId(), t1);
      // Turning off security outputs, even if all primitive outputs are enabled, should allow the dep graph to be
      // pruned completely, since the only *terminal* output is the security output.
      viewDefinition.getResultModelDefinition().setPrimitiveOutputMode(ResultOutputMode.TERMINAL_OUTPUTS);
      viewDefinition.getResultModelDefinition().setSecurityOutputMode(ResultOutputMode.NONE);
      compiledViewDefinition = ViewDefinitionCompiler.compile(viewDefinition, compilationServices, now, VersionCorrection.of(now, now));
      assertTargets(compiledViewDefinition);
    } finally {
      TestLifecycle.end();
    }
  }

  public void testCancel() throws Exception {
    TestLifecycle.begin();
    try {
      final ViewDefinition viewDefinition = new ViewDefinition("Test", "jonathan");
      final ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDefinition, "Config1");
      viewDefinition.addViewCalculationConfiguration(calcConfig);
      final FunctionRepository functionRepo = new InMemoryFunctionRepository();
      final FunctionCompilationContext compilationContext = new FunctionCompilationContext();
      final CompiledFunctionService cfs = new CompiledFunctionService(functionRepo, new CachingFunctionRepositoryCompiler(), compilationContext);
      TestLifecycle.register(cfs);
      cfs.initialize();
      final DefaultFunctionResolver functionResolver = new DefaultFunctionResolver(cfs);
      final SecuritySource securitySource = new InMemorySecuritySource();
      final DefaultCachingComputationTargetResolver computationTargetResolver = new DefaultCachingComputationTargetResolver(new DefaultComputationTargetResolver(securitySource),
          _cacheManager);
      compilationContext.setRawComputationTargetResolver(computationTargetResolver);
      final Future<CompiledViewDefinitionWithGraphsImpl> future = ViewDefinitionCompiler.fullCompileTask(viewDefinition, new ViewCompilationServices(
          new FixedMarketDataAvailabilityProvider(), functionResolver, compilationContext, cfs.getExecutorService(), new DependencyGraphBuilderFactory()), Instant.now(),
          VersionCorrection.LATEST);
      assertFalse(future.isDone());
      assertFalse(future.isCancelled());
      assertTrue(future.cancel(true));
      try {
        future.get();
        fail();
      } catch (final CancellationException e) {
        // expected
      }
      assertTrue(future.isCancelled());
    } finally {
      TestLifecycle.end();
    }
  }

  private void assertTargets(final CompiledViewDefinitionWithGraphsImpl compiledViewDefinition, final UniqueId... targets) {
    final Set<UniqueId> expectedTargets = new HashSet<UniqueId>(Arrays.asList(targets));
    final Set<ComputationTargetSpecification> actualTargets = compiledViewDefinition.getComputationTargets();
    assertEquals(expectedTargets.size(), actualTargets.size());
    for (final ComputationTargetSpecification actualTarget : actualTargets) {
      assertTrue(expectedTargets.contains(actualTarget.getUniqueId()));
    }
  }

}
