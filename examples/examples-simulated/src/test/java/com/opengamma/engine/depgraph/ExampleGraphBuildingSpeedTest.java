/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
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
import com.opengamma.engine.marketdata.availability.DefaultMarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.OptimisticMarketDataAvailabilityFilter;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphsImpl;
import com.opengamma.engine.view.compilation.ViewCompilationServices;
import com.opengamma.engine.view.compilation.ViewDefinitionCompiler;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.test.TestGroup;

/**
 * Times the building of dependency graphs for all views in the system; that is if they can be built.
 * <p>
 * Although timings can be reported, the repeated attempts to build the graph are more useful to detect faults with the graph building algorithm - for example inconsistent behaviors.
 */
public class ExampleGraphBuildingSpeedTest {

  private static final Logger s_logger = LoggerFactory.getLogger(ExampleGraphBuildingSpeedTest.class);

  private static final int LOOPS = 3;
  private static final int COUNT = 3;

  private ComponentRepository _repo;
  private ConfigSource _configSource;
  private ViewCompilationServices _viewCompilationServices;
  private CacheManager _cacheManager;
  private List<String> _report;

  @BeforeClass(timeOut = 40_000L)
  public void initialise() {
    final ComponentManager manager = new ComponentManager("test");
    manager.start("classpath:/fullstack/fullstack-examplessimulated-test.properties");
    _repo = manager.getRepository();
    _viewCompilationServices = createViewCompilationServices();
    _cacheManager = _repo.getInstance(CacheManager.class, "standard");
    _configSource = _repo.getInstance(ViewProcessor.class, "main").getConfigSource();
    _report = new LinkedList<String>();
  }

  @AfterClass(timeOut = 40_000L)
  public void cleanup() {
    if (_repo != null) {
      _repo.stop();
    }
    for (final String report : _report) {
      s_logger.info("{}", report);
    }
  }

  protected void configureDependencyGraphBuilder(final DependencyGraphBuilderFactory dependencyGraphBuilder) {
    dependencyGraphBuilder.setEnableFailureReporting(false);
    //dependencyGraphBuilder.setRunQueueFactory(RunQueueFactory.getOrdered());
    //dependencyGraphBuilder.setRunQueueFactory(RunQueueFactory.getConcurrentLinkedQueue());
    //dependencyGraphBuilder.setRunQueueFactory(RunQueueFactory.getConcurrentStack());
    //dependencyGraphBuilder.setTargetDigests(new NoTargetDigests());
    //dependencyGraphBuilder.setTargetDigests(new SecurityTypeTargetDigests());
    //dependencyGraphBuilder.setTargetDigests(new FinancialSecurityTargetDigests());
  }

  private ViewCompilationServices createViewCompilationServices() {
    final CompiledFunctionService cfs = _repo.getInstance(CompiledFunctionService.class, "main");
    final FunctionResolver functionResolver = _repo.getInstance(FunctionResolver.class, "main");
    final FunctionExclusionGroups functionExclusionGroups = _repo.getInstance(FunctionExclusionGroups.class, "main");
    final DependencyGraphBuilderFactory dependencyGraphBuilder = new DependencyGraphBuilderFactory();
    dependencyGraphBuilder.setFunctionExclusionGroups(functionExclusionGroups);
    configureDependencyGraphBuilder(dependencyGraphBuilder);
    final MarketDataAvailabilityProvider mdap = new OptimisticMarketDataAvailabilityFilter().withProvider(new DefaultMarketDataAvailabilityProvider());
    return new ViewCompilationServices(mdap, functionResolver, cfs.getFunctionCompilationContext(), cfs.getExecutorService(), dependencyGraphBuilder);
  }

  @Test(dataProvider = "viewDefinitions", enabled = true, groups = TestGroup.INTEGRATION)
  public void runTimingTest(final ViewDefinition view) {
    if (view == null) {
      s_logger.warn("Skipping - passed null");
      return;
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
        final Instant now = Instant.now();
        final CompiledViewDefinitionWithGraphsImpl compiled = ViewDefinitionCompiler.compile(view, _viewCompilationServices, now.minus(Duration.ofDays(10 + i)),
            VersionCorrection.of(now, now));
        final long tStop = System.nanoTime();
        s_logger.info("Compilation {} of view in {}ms", i, (tStop - tStart) / 1e6);
        final Map<String, String> nodeCounts = new HashMap<String, String>();
        for (final DependencyGraph graph : CompiledViewDefinitionWithGraphsImpl.getDependencyGraphs(compiled)) {
          if (graph.getTerminalOutputs().isEmpty()) {
            s_logger.warn("Didn't compile any terminal output specifications into the graph for {}", graph.getCalculationConfigurationName());
            if (terminalOutputs.get(graph.getCalculationConfigurationName()) == null) {
              nodeCounts.put(graph.getCalculationConfigurationName(), "0, 0");
              terminalOutputs.put(graph.getCalculationConfigurationName(), graph.getTerminalOutputs().keySet());
            } else {
              assertEquals(terminalOutputs.get(graph.getCalculationConfigurationName()).size(), 0);
            }
          } else {
            s_logger.debug("{} graph = {} output specifications from {} nodes", new Object[] {graph.getCalculationConfigurationName(), graph.getTerminalOutputs().size(), graph.getSize() });
            nodeCounts.put(graph.getCalculationConfigurationName(), graph.getTerminalOutputs().size() + ", " + graph.getSize());
            if (terminalOutputs.get(graph.getCalculationConfigurationName()) == null) {
              terminalOutputs.put(graph.getCalculationConfigurationName(), graph.getTerminalOutputs().keySet());
            } else {
              final Set<ValueSpecification> missing = Sets.difference(terminalOutputs.get(graph.getCalculationConfigurationName()), graph.getTerminalOutputs().keySet());
              final Set<ValueSpecification> extra = Sets.difference(graph.getTerminalOutputs().keySet(), terminalOutputs.get(graph.getCalculationConfigurationName()));
              if (!missing.isEmpty()) {
                if (missing.size() < 8) {
                  s_logger.info("Missing = {}", missing);
                } else {
                  for (ValueSpecification vs : missing) {
                    s_logger.info("Missing = {}", vs);
                  }
                }
              }
              if (!extra.isEmpty()) {
                if (extra.size() < 8) {
                  s_logger.info("Extra = {}", extra);
                } else {
                  for (ValueSpecification vs : extra) {
                    s_logger.info("Extra = {}", vs);
                  }
                }
              }
              assertEquals(graph.getTerminalOutputs().size(), terminalOutputs.get(graph.getCalculationConfigurationName()).size());
              assertEquals(missing.size(), extra.size());
              final Collection<ValueSpecification> extraCopy = new LinkedList<ValueSpecification>(extra);
              for (ValueSpecification vs1 : missing) {
                final Iterator<ValueSpecification> itr = extraCopy.iterator();
                while (itr.hasNext()) {
                  final ValueSpecification vs2 = itr.next();
                  if (vs1.getValueName() == vs2.getValueName()) {
                    if (vs1.getTargetSpecification().equals(vs2.getTargetSpecification())) {
                      s_logger.info("Paired {} with {}", vs1, vs2);
                      itr.remove();
                      break;
                    }
                  }
                }
              }
              for (ValueSpecification vs : extraCopy) {
                s_logger.warn("Unpaired extra = {}", vs);
              }
              assertTrue(extraCopy.isEmpty());
            }
          }
        }
        final List<String> configs = new ArrayList<String>(nodeCounts.keySet());
        Collections.sort(configs);
        final StringBuilder sb = new StringBuilder();
        sb.append("Compilation ").append(j).append("/").append(i).append(" of ").append(view.getName()).append(" in ").append((tStop - tStart) / 1e6).append("ms");
        for (String config : configs) {
          sb.append(' ').append(config).append('=').append(nodeCounts.get(config));
        }
        _report.add(sb.toString());
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
    Arrays.sort(viewDefinitions, new Comparator<Object[]>() {
      @Override
      public int compare(Object[] o1, Object[] o2) {
        return ((ViewDefinition) o1[0]).getName().compareTo(((ViewDefinition) o2[0]).getName());
      }
    });
    return viewDefinitions;
  }

}
