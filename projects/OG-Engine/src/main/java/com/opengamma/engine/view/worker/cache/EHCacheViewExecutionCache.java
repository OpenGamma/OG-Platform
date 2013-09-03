/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.collect.MapMaker;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.CompiledFunctionRepository;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphsImpl;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * An EH-Cache based implementation of {@link ViewExecutionCache}.
 */
public class EHCacheViewExecutionCache implements ViewExecutionCache {

  private static final Logger s_logger = LoggerFactory.getLogger(EHCacheViewExecutionCache.class);

  private static final String COMPILED_VIEW_DEFINITIONS = "compiledViewDefinitions";

  private static final Map<Serializable, EHCacheViewExecutionCache> s_instance2identifier = new MapMaker().weakValues().makeMap();

  private static final AtomicInteger s_nextIdentifier = new AtomicInteger(0);

  private final Serializable _identifier;

  private final CacheManager _cacheManager;

  private final ConfigSource _configSource;

  private final CompiledFunctionService _functions;

  private final ConcurrentMap<ViewExecutionCacheKey, CompiledViewDefinitionWithGraphs> _compiledViewDefinitionsFrontCache = new MapMaker().weakValues().makeMap();

  private final Cache _compiledViewDefinitions;

  public EHCacheViewExecutionCache(final CacheManager cacheManager, final ConfigSource configSource, final CompiledFunctionService functions) {
    ArgumentChecker.notNull(cacheManager, "cacheManager");
    ArgumentChecker.notNull(configSource, "configSource");
    ArgumentChecker.notNull(functions, "functions");
    _identifier = s_nextIdentifier.getAndIncrement();
    _cacheManager = cacheManager;
    _configSource = configSource;
    _functions = functions;
    EHCacheUtils.addCache(_cacheManager, COMPILED_VIEW_DEFINITIONS);
    _compiledViewDefinitions = EHCacheUtils.getCacheFromManager(_cacheManager, COMPILED_VIEW_DEFINITIONS);
    s_instance2identifier.put(_identifier, this);
  }

  /**
   * For testing only.
   */
  /* package */void clearFrontCache() {
    _compiledViewDefinitionsFrontCache.clear();
  }

  protected Serializable instance() {
    return _identifier;
  }

  protected static EHCacheViewExecutionCache instance(final Serializable identifier) {
    return s_instance2identifier.get(identifier);
  }

  public ConfigSource getConfigSource() {
    return _configSource;
  }

  public CompiledFunctionService getFunctions() {
    return _functions;
  }

  /* package */static final class DependencyGraphHolder implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String _calcConfig;
    private final Map<ValueSpecification, Set<ValueRequirement>> _terminalOutputs;
    private final ComputationTargetSpecification[] _nodeTargets;
    private final FunctionParameters[] _nodeParameters;
    private final String[] _nodeFunctions;
    private final Collection<ValueSpecification>[] _nodeInputs;
    private final Collection<ValueSpecification>[] _nodeOutputs;

    @SuppressWarnings("unchecked")
    public DependencyGraphHolder(final DependencyGraph graph) {
      _calcConfig = graph.getCalculationConfigurationName();
      final int size = graph.getDependencyNodes().size();
      _nodeTargets = new ComputationTargetSpecification[size];
      _nodeParameters = new FunctionParameters[size];
      _nodeFunctions = new String[size];
      _nodeInputs = new Collection[size];
      _nodeOutputs = new Collection[size];
      int i = 0;
      for (DependencyNode node : graph.getDependencyNodes()) {
        _nodeTargets[i] = node.getComputationTarget();
        _nodeParameters[i] = node.getFunction().getParameters();
        _nodeFunctions[i] = node.getFunction().getFunction().getFunctionDefinition().getUniqueId();
        _nodeInputs[i] = node.getInputValues();
        _nodeOutputs[i] = new ArrayList<ValueSpecification>(node.getOutputValues());
        i++;
      }
      _terminalOutputs = graph.getTerminalOutputs();
    }

    public DependencyGraph get(final CompiledFunctionRepository functions) {
      final DependencyGraph graph = new DependencyGraph(_calcConfig);
      for (int i = 0; i < _nodeTargets.length; i++) {
        final DependencyNode node = new DependencyNode(_nodeTargets[i]);
        for (ValueSpecification input : _nodeInputs[i]) {
          node.addInputValue(input);
        }
        node.addOutputValues(_nodeOutputs[i]);
        node.setFunction(new ParameterizedFunction(functions.getDefinition(_nodeFunctions[i]), _nodeParameters[i]));
        graph.addDependencyNode(node);
      }
      for (DependencyNode node : graph.getDependencyNodes()) {
        for (ValueSpecification inputValue : node.getInputValues()) {
          final DependencyNode inputNode = graph.getNodeProducing(inputValue);
          if (inputNode != null) {
            node.addInputNode(inputNode);
          }
        }
      }
      graph.addTerminalOutputs(_terminalOutputs);
      return graph;
    }

  }

  /* package */static final class CompiledViewDefinitionWithGraphsReader implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Serializable _parent;
    private final VersionCorrection _versionCorrection;
    private final String _compilationId;
    private final Instant _compilationTime;
    private final UniqueId _viewDefinition;
    private final Collection<DependencyGraphHolder> _graphs;
    private final Map<ComputationTargetReference, UniqueId> _resolutions;
    private final UniqueId _portfolio;
    private final long _functionInitId;
    private final Collection<CompiledViewCalculationConfiguration> _calcConfigs;

    public CompiledViewDefinitionWithGraphsReader(EHCacheViewExecutionCache parent, CompiledViewDefinitionWithGraphs viewDef) {
      _parent = parent.instance();
      _versionCorrection = viewDef.getResolverVersionCorrection();
      _compilationId = viewDef.getCompilationIdentifier();
      if (viewDef.getValidFrom() == null) {
        if (viewDef.getValidTo() == null) {
          _compilationTime = Instant.now();
        } else {
          _compilationTime = viewDef.getValidTo();
        }
      } else {
        if (viewDef.getValidTo() == null) {
          _compilationTime = viewDef.getValidFrom();
        } else {
          _compilationTime = Instant.ofEpochSecond((viewDef.getValidFrom().getEpochSecond() + viewDef.getValidTo().getEpochSecond()) >> 1);
        }
      }
      _viewDefinition = viewDef.getViewDefinition().getUniqueId();
      final Collection<DependencyGraphExplorer> graphs = viewDef.getDependencyGraphExplorers();
      _graphs = new ArrayList<>(graphs.size());
      for (DependencyGraphExplorer explorer : graphs) {
        _graphs.add(new DependencyGraphHolder(explorer.getWholeGraph()));
      }
      _resolutions = viewDef.getResolvedIdentifiers();
      _portfolio = viewDef.getPortfolio().getUniqueId();
      _functionInitId = ((CompiledViewDefinitionWithGraphsImpl) viewDef).getFunctionInitId();
      _calcConfigs = viewDef.getCompiledCalculationConfigurations();
    }

    private Object readResolve() {
      final EHCacheViewExecutionCache parent = instance(_parent);
      final ViewDefinition viewDefinition = parent.getConfigSource().getConfig(ViewDefinition.class, _viewDefinition);
      final Collection<DependencyGraph> graphs = new ArrayList<DependencyGraph>(_graphs.size());
      final CompiledFunctionRepository functions = parent.getFunctions().compileFunctionRepository(_compilationTime);
      for (DependencyGraphHolder graph : _graphs) {
        graphs.add(graph.get(functions));
      }
      final Portfolio portfolio = (Portfolio) parent.getFunctions().getFunctionCompilationContext().getRawComputationTargetResolver()
          .resolve(new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO, _portfolio), _versionCorrection).getValue();
      CompiledViewDefinitionWithGraphsImpl compiledViewDef =
          new CompiledViewDefinitionWithGraphsImpl(_versionCorrection, _compilationId, viewDefinition, graphs,
              _resolutions, portfolio, _functionInitId, _calcConfigs);
      return parent.new CompiledViewDefinitionWithGraphsHolder(compiledViewDef);
    }
  }

  /* package */final class CompiledViewDefinitionWithGraphsHolder implements Serializable {

    private static final long serialVersionUID = 1L;

    private CompiledViewDefinitionWithGraphs _viewDefinition;

    public CompiledViewDefinitionWithGraphsHolder(final CompiledViewDefinitionWithGraphs viewDefinition) {
      _viewDefinition = viewDefinition;
    }

    public CompiledViewDefinitionWithGraphs get() {
      return _viewDefinition;
    }

    private Object writeReplace() {
      return new CompiledViewDefinitionWithGraphsReader(EHCacheViewExecutionCache.this, _viewDefinition);
    }

  }

  @Override
  public CompiledViewDefinitionWithGraphs getCompiledViewDefinitionWithGraphs(ViewExecutionCacheKey key) {
    CompiledViewDefinitionWithGraphs graphs = _compiledViewDefinitionsFrontCache.get(key);
    if (graphs != null) {
      s_logger.debug("Front cache hit CompiledViewDefinitionWithGraphs for {}", key);
      return graphs;
    }
    final Element element = _compiledViewDefinitions.get(key);
    if (element != null) {
      s_logger.debug("EHCache hit CompiledViewDefinitionWithGraphs for {}", key);
      graphs = ((CompiledViewDefinitionWithGraphsHolder) element.getObjectValue()).get();
      final CompiledViewDefinitionWithGraphs existing = _compiledViewDefinitionsFrontCache.putIfAbsent(key, graphs);
      if (existing != null) {
        graphs = existing;
      }
    } else {
      s_logger.debug("EHCache miss CompiledViewDefinitionWithGraphs for {}", key);
    }
    return graphs;
  }

  @Override
  public void setCompiledViewDefinitionWithGraphs(ViewExecutionCacheKey key, CompiledViewDefinitionWithGraphs viewDefinition) {
    CompiledViewDefinitionWithGraphs existing = _compiledViewDefinitionsFrontCache.put(key, viewDefinition);
    if (existing != null) {
      if (existing == viewDefinition) {
        return;
      }
    }
    s_logger.info("Storing CompiledViewDefinitionWithGraphs for {}", key);
    _compiledViewDefinitions.put(new Element(key, new CompiledViewDefinitionWithGraphsHolder(viewDefinition)));
  }

}
