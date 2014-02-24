/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker.cache;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import net.sf.ehcache.CacheManager;

import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableMap;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.builder.TestDependencyGraphBuilder;
import com.opengamma.engine.depgraph.builder.TestDependencyGraphBuilder.NodeBuilder;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfigurationImpl;
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
@Test(groups = TestGroup.UNIT)
public class EHCacheViewExecutionCacheTest {

  private final Instant _now = Instant.now();
  private CacheManager _cacheManager;

  @BeforeClass
  public void setUpClass() {
    _cacheManager = EHCacheUtils.createTestCacheManager(EHCacheViewExecutionCache.class);
  }

  @AfterClass
  public void tearDownClass() {
    EHCacheUtils.shutdownQuiet(_cacheManager);
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
    final TestDependencyGraphBuilder gb = new TestDependencyGraphBuilder("Default");
    final NodeBuilder n1 = gb.addNode("Foo", ComputationTargetSpecification.NULL);
    n1.addTerminalOutput("Foo");
    final NodeBuilder n2 = gb.addNode("Bar", ComputationTargetSpecification.NULL);
    n1.addInput(n2.addOutput("Bar"));
    final NodeBuilder n3 = gb.addNode("Cow", ComputationTargetSpecification.NULL);
    n2.addInput(n3.addOutput("Cow"));
    return gb.buildGraph();
  }

  private CompiledViewDefinitionWithGraphs createCompiledViewDefinitionWithGraphs() {
    final Portfolio portfolio = createPortfolio();
    final ViewDefinition viewDefinition = createViewDefinition();
    final ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDefinition, "Default");
    viewDefinition.addViewCalculationConfiguration(calcConfig);
    final DependencyGraph graph = createDependencyGraph();
    final Collection<DependencyGraph> graphs = Collections.singleton(graph);
    final Collection<CompiledViewCalculationConfiguration> calcConfigs = Collections.<CompiledViewCalculationConfiguration>singleton(CompiledViewCalculationConfigurationImpl.of(graph));
    final Map<ComputationTargetReference, UniqueId> resolutions = ImmutableMap.<ComputationTargetReference, UniqueId>of(new ComputationTargetRequirement(ComputationTargetType.SECURITY,
        ExternalId.of("Security", "Foo")), UniqueId.of("Sec", "0"));
    return new CompiledViewDefinitionWithGraphsImpl(VersionCorrection.of(_now, _now), "", viewDefinition, graphs, resolutions, portfolio, 0, calcConfigs, null, null);
  }

  private EHCacheViewExecutionCache createCache() {
    final ComputationTargetResolver targetResolver = Mockito.mock(ComputationTargetResolver.class);
    Mockito.when(targetResolver.resolve(new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO, UniqueId.of("Portfolio", "0", "V")), VersionCorrection.of(_now, _now)))
        .thenReturn(new ComputationTarget(ComputationTargetType.PORTFOLIO, createPortfolio()));
    Mockito.when(targetResolver.resolve(new ComputationTargetSpecification(ComputationTargetType.of(ViewDefinition.class), UniqueId.of("View", "0", "V")), VersionCorrection.LATEST))
        .thenReturn(new ComputationTarget(ComputationTargetType.of(ViewDefinition.class), createViewDefinition()));
    Mockito.when(targetResolver.atVersionCorrection(VersionCorrection.of(_now, _now))).thenReturn(Mockito.mock(ComputationTargetResolver.AtVersionCorrection.class));
    return new EHCacheViewExecutionCache(_cacheManager, targetResolver);
  }

  public void testCompiledViewDefinitionWithGraphs_serialization() throws Exception {
    final EHCacheViewExecutionCache cache = createCache();
    final CompiledViewDefinitionWithGraphs object = createCompiledViewDefinitionWithGraphs();
    final CompiledViewDefinitionWithGraphsHolder holder = cache.new CompiledViewDefinitionWithGraphsHolder(object);
    assertSame(holder.get(), object);
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(holder);
    oos.close();
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

  public void testCompiledViewDefinitionWithGraphs_caching() {
    final EHCacheViewExecutionCache cache = createCache();
    final CompiledViewDefinitionWithGraphs object = createCompiledViewDefinitionWithGraphs();
    final ViewExecutionCacheKey key = new ViewExecutionCacheKey(UniqueId.of("Key", "1"), "Foo", "No-op");
    // Miss
    assertNull(cache.getCompiledViewDefinitionWithGraphs(key));
    // Store
    cache.setCompiledViewDefinitionWithGraphs(key, object);
    // Hit the front cache
    assertSame(cache.getCompiledViewDefinitionWithGraphs(key), object);
    // Hit the EH Cache
    cache.clearFrontCache();
    final CompiledViewDefinitionWithGraphs cachedObject = cache.getCompiledViewDefinitionWithGraphs(key);
    assertNotNull(cachedObject);
    // Hit the front cache
    assertSame(cache.getCompiledViewDefinitionWithGraphs(key), cachedObject);
    // Replacement
    final CompiledViewDefinitionWithGraphs newObject = createCompiledViewDefinitionWithGraphs();
    assertNotSame(newObject, object);
    assertNotSame(newObject, cachedObject);
    cache.setCompiledViewDefinitionWithGraphs(key, newObject);
    assertSame(cache.getCompiledViewDefinitionWithGraphs(key), newObject);
  }

}
