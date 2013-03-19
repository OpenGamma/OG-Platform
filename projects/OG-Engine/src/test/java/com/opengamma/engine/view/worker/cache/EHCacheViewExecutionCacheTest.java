/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker.cache;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import net.sf.ehcache.CacheManager;

import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.function.LazyFunctionRepositoryCompiler;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphsImpl;
import com.opengamma.engine.view.worker.cache.EHCacheViewExecutionCache.CompiledViewDefinitionWithGraphsHolder;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link EHCacheViewExecutionCache} class.
 */
@Test(groups = TestGroup.UNIT_DB)
public class EHCacheViewExecutionCacheTest {

  private CacheManager _cacheManager;

  @BeforeClass
  public void setUpClass() {
    _cacheManager = EHCacheUtils.createTestCacheManager(EHCacheViewExecutionCache.class);
  }

  @AfterClass
  public void tearDownClass() {
    EHCacheUtils.shutdownQuiet(_cacheManager);
  }

  private Security createSecurity(final UniqueId uid, final ExternalId eid) {
    final Security security = Mockito.mock(Security.class);
    Mockito.when(security.getUniqueId()).thenReturn(uid);
    Mockito.when(security.getExternalIdBundle()).thenReturn(eid.toBundle());
    return security;
  }

  private Portfolio createPortfolio() {
    return new SimplePortfolio(UniqueId.of("Portfolio", "0", "V"), "Portfolio");
  }

  private ViewDefinition createViewDefinition() {
    final ViewDefinition viewDefinition = new ViewDefinition("TestView", UniqueId.of("Portfolio", "0"), "TestUser");
    viewDefinition.setUniqueId(UniqueId.of("View", "0", "V"));
    return viewDefinition;
  }

  private DependencyGraph createDependencyGraph() {
    final DependencyGraph graph = new DependencyGraph("Default");
    final ValueProperties properties = ValueProperties.with(ValuePropertyNames.FUNCTION, "Mock").get();
    final ComputationTarget t1 = new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, new SimplePortfolioNode(UniqueId.of("Node", "0"), "node"));
    final DependencyNode n1 = new DependencyNode(t1.toSpecification());
    n1.setFunction(new MockFunction("F1", t1));
    n1.addOutputValue(new ValueSpecification("Foo", t1.toSpecification(), properties));
    n1.addTerminalOutputValue(new ValueSpecification("Foo", t1.toSpecification(), properties));
    graph.addDependencyNode(n1);
    final ComputationTarget t2 = new ComputationTarget(ComputationTargetType.POSITION, new SimplePosition(UniqueId.of("Pos", "0"), BigDecimal.ONE, ExternalId.of("Security", "Foo")));
    final DependencyNode n2 = new DependencyNode(t2.toSpecification());
    n2.setFunction(new MockFunction("F2", t1));
    n2.addOutputValue(new ValueSpecification("Foo", t2.toSpecification(), properties));
    n1.addInputValue(new ValueSpecification("Foo", t2.toSpecification(), properties));
    n1.addInputNode(n2);
    graph.addDependencyNode(n2);
    final ComputationTarget t3 = new ComputationTarget(ComputationTargetType.SECURITY, createSecurity(UniqueId.of("Pos", "0"), ExternalId.of("Security", "Foo")));
    final DependencyNode n3 = new DependencyNode(t3.toSpecification());
    n3.setFunction(new MockFunction("F3", t1));
    n3.addOutputValue(new ValueSpecification("Foo", t3.toSpecification(), properties));
    n2.addInputValue(new ValueSpecification("Foo", t3.toSpecification(), properties));
    n2.addInputNode(n3);
    graph.addDependencyNode(n3);
    graph.addTerminalOutput(new ValueRequirement("Foo", t1.toSpecification()), new ValueSpecification("Foo", t1.toSpecification(), properties));
    return graph;
  }

  private FunctionCompilationContext createFunctionCompilationContext() {
    return new FunctionCompilationContext();
  }

  private FunctionRepository createFunctionRepository() {
    final InMemoryFunctionRepository functions = new InMemoryFunctionRepository();
    functions.addFunction(new MockFunction("F1", new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, new SimplePortfolioNode(UniqueId.of("Node", "0"), "node"))));
    functions.addFunction(new MockFunction("F2", new ComputationTarget(ComputationTargetType.POSITION, new SimplePosition(UniqueId.of("Pos", "0"), BigDecimal.ONE, ExternalId.of("Security", "Foo")))));
    functions.addFunction(new MockFunction("F3", new ComputationTarget(ComputationTargetType.SECURITY, createSecurity(UniqueId.of("Pos", "0"), ExternalId.of("Security", "Foo")))));
    return functions;
  }

  private CompiledViewDefinitionWithGraphs createCompiledViewDefinitionWithGraphs() {
    final Portfolio portfolio = createPortfolio();
    final ViewDefinition viewDefinition = createViewDefinition();
    final ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDefinition, "Default");
    viewDefinition.addViewCalculationConfiguration(calcConfig);
    final DependencyGraph graph = createDependencyGraph();
    final Collection<DependencyGraph> graphs = Collections.singleton(graph);
    final Map<ComputationTargetReference, UniqueId> resolutions = ImmutableMap.<ComputationTargetReference, UniqueId>of(
        new ComputationTargetRequirement(ComputationTargetType.SECURITY, ExternalId.of("Security", "Foo")), UniqueId.of("Sec", "0"));
    return new CompiledViewDefinitionWithGraphsImpl(VersionCorrection.LATEST, viewDefinition, graphs, resolutions, portfolio, 0);
  }

  public void testCompiledViewDefinitionWithGraphsSerialization() throws Exception {
    final ConfigSource configSource = Mockito.mock(ConfigSource.class);
    final CompiledViewDefinitionWithGraphs object = createCompiledViewDefinitionWithGraphs();
    Mockito.when(configSource.getConfig(ViewDefinition.class, UniqueId.of("View", "0", "V"))).thenReturn(createViewDefinition());
    final CompiledFunctionService functions = new CompiledFunctionService(createFunctionRepository(), new LazyFunctionRepositoryCompiler(), createFunctionCompilationContext());
    functions.initialize();
    final EHCacheViewExecutionCache cache = new EHCacheViewExecutionCache(_cacheManager, configSource, functions);
    final CompiledViewDefinitionWithGraphsHolder holder = cache.new CompiledViewDefinitionWithGraphsHolder(object);
    assertSame(holder.get(), object);
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(holder);
    final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    final ObjectInputStream ois = new ObjectInputStream(bais);
    final Object newHolder = ois.readObject();
    assertEquals(newHolder.getClass(), CompiledViewDefinitionWithGraphsHolder.class);
    final CompiledViewDefinitionWithGraphs newObject = ((CompiledViewDefinitionWithGraphsHolder) newHolder).get();
    assertEquals(newObject.getCompiledCalculationConfigurations(), object.getCompiledCalculationConfigurations());
    assertEquals(newObject.getComputationTargets(), object.getComputationTargets());
    assertEquals(newObject.getMarketDataRequirements(), object.getMarketDataRequirements());
    assertEquals(newObject.getPortfolio(), object.getPortfolio());
    assertEquals(newObject.getResolvedIdentifiers(), object.getResolvedIdentifiers());
    assertEquals(newObject.getResolverVersionCorrection(), object.getResolverVersionCorrection());
  }
}
