/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.ehcache.CacheManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import com.google.common.collect.Sets;
import com.opengamma.component.ComponentManager;
import com.opengamma.component.ComponentRepository;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroups;
import com.opengamma.engine.function.resolver.FunctionResolver;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.OptimisticMarketDataAvailabilityProvider;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphsImpl;
import com.opengamma.engine.view.compilation.ViewCompilationServices;
import com.opengamma.engine.view.compilation.ViewDefinitionCompiler;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * Times the building of dependency graphs for all views in the system; that is if they can be built.
 * <p>
 * Although timings can be reported, the repeated attempts to build the graph are more useful to detect faults with the graph building algorithm - for example inconsistent behaviors.
 */
public class GraphBuildingSpeedTest {

  private static final Logger s_logger = LoggerFactory.getLogger(GraphBuildingSpeedTest.class);

  private static final int LOOPS = 3;
  private static final int COUNT = 3;

  private ComponentRepository _repo;
  private ConfigSource _configSource;
  private ViewCompilationServices _viewCompilationServices;
  private CacheManager _cacheManager;
  private List<String> _report;

  @BeforeClass
  public void initialise() {
    final ComponentManager manager = new ComponentManager("test");
    manager.start("classpath:fullstack/fullstack-example-dev.properties");
    _repo = manager.getRepository();
    _viewCompilationServices = createViewCompilationServices();
    _cacheManager = _repo.getInstance(CacheManager.class, "standard");
    _configSource = _repo.getInstance(ViewProcessor.class, "main").getConfigSource();
    _report = new LinkedList<String>();
  }

  @AfterClass
  public void cleanup() {
    if (_repo != null) {
      _repo.stop();
    }
    for (final String report : _report) {
      s_logger.info("{}", report);
    }
  }

  private ViewCompilationServices createViewCompilationServices() {
    final CompiledFunctionService cfs = _repo.getInstance(CompiledFunctionService.class, "main");
    final FunctionResolver functionResolver = _repo.getInstance(FunctionResolver.class, "main");
    final FunctionExclusionGroups functionExclusionGroups = _repo.getInstance(FunctionExclusionGroups.class, "main");
    final DependencyGraphBuilderFactory dependencyGraphBuilder = new DependencyGraphBuilderFactory();
    dependencyGraphBuilder.setFunctionExclusionGroups(functionExclusionGroups);
    dependencyGraphBuilder.setEnableFailureReporting(false);
    dependencyGraphBuilder.setRunQueueFactory(RunQueueFactory.getOrdered());
    //dependencyGraphBuilder.setRunQueueFactory(RunQueueFactory.getConcurrentLinkedQueue());
    //dependencyGraphBuilder.setRunQueueFactory(RunQueueFactory.getConcurrentStack());
    final MarketDataAvailabilityProvider mdap = new OptimisticMarketDataAvailabilityProvider();
    return new ViewCompilationServices(mdap, functionResolver, cfs.getFunctionCompilationContext(), cfs.getExecutorService(), dependencyGraphBuilder);
  }

  @Test(dataProvider = "viewDefinitions", enabled = true)
  public void runTimingTest(final ViewDefinition view) {
    if (view == null) {
      s_logger.warn("Skipping - passed null");
    }
    s_logger.info("Testing view {}", view.getName());
    final Map<String, Set<ValueSpecification>> terminalOutputs = new HashMap<String, Set<ValueSpecification>>();
    for (int j = 0; j < LOOPS; j++) {
      if (j > 0) {
        s_logger.info("Clearing caches");
        for (final String cacheName : _cacheManager.getCacheNames()) {
          s_logger.debug("Clearing cache {}", cacheName);
          EHCacheUtils.getCacheFromManager(_cacheManager, cacheName).removeAll();
        }
      }
      for (int i = 0; i < COUNT; i++) {
        final long tStart = System.nanoTime();
        final CompiledViewDefinitionWithGraphsImpl compiled = ViewDefinitionCompiler.compile(view, _viewCompilationServices, Instant.now().minus(Duration.ofDays(10 + i)),
            VersionCorrection.LATEST);
        final long tStop = System.nanoTime();
        s_logger.info("Compilation {} of view in {}ms", i, (tStop - tStart) / 1e6);
        _report.add("Compilation " + j + "/" + i + " of " + view.getName() + " in " + ((tStop - tStart) / 1e6) + "ms");
        for (final DependencyGraph graph : compiled.getAllDependencyGraphs()) {
          if (graph.getTerminalOutputSpecifications().isEmpty()) {
            s_logger.warn("Didn't compile any terminal output specifications into the graph for {}", graph.getCalculationConfigurationName());
            fail();
          } else {
            s_logger.debug("{} graph = {} output specifications from {} nodes", new Object[] {graph.getCalculationConfigurationName(), graph.getTerminalOutputSpecifications().size(),
                graph.getDependencyNodes().size() });
            if (terminalOutputs.get(graph.getCalculationConfigurationName()) == null) {
              terminalOutputs.put(graph.getCalculationConfigurationName(), graph.getTerminalOutputSpecifications());
            } else {
              final Set<ValueSpecification> missing = Sets.difference(terminalOutputs.get(graph.getCalculationConfigurationName()), graph.getTerminalOutputSpecifications());
              final Set<ValueSpecification> extra = Sets.difference(graph.getTerminalOutputSpecifications(), terminalOutputs.get(graph.getCalculationConfigurationName()));
              if (!missing.isEmpty()) {
                s_logger.info("Missing = {}", missing);
              }
              if (!extra.isEmpty()) {
                s_logger.info("Extra = {}", extra);
              }
              assertEquals(missing, Collections.emptySet());
              assertEquals(extra, Collections.emptySet());
            }
          }
        }
      }
    }
  }

  @DataProvider(name = "viewDefinitions")
  public Object[][] viewDefinitionsProvider() {
    final Collection<ConfigItem<ViewDefinition>> items = _configSource.getAll(ViewDefinition.class, VersionCorrection.LATEST);
    final Object[][] viewDefinitions = new Object[items.size()][1];
    int i = 0;
    for (final ConfigItem<ViewDefinition> item : items) {
      viewDefinitions[i++][0] = item.getValue();
    }
    return viewDefinitions;
  }

}
