/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.impl.RootDiscardingSubgrapher;
import com.opengamma.engine.function.MarketDataAliasingFunction;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.PoolExecutor;

/**
 * Filters a dependency graph to exclude any market data sourcing nodes from a previous data provider that are not valid for the new provider.
 */
/* package */final class InvalidMarketDataDependencyNodeFilter extends RootDiscardingSubgrapher {

  private static final Logger s_logger = LoggerFactory.getLogger(InvalidMarketDataDependencyNodeFilter.class);

  private final ComputationTargetResolver.AtVersionCorrection _targetResolver;
  private final MarketDataAvailabilityProvider _marketData;

  /**
   * The market data specifications from the leaves of the graph. Mapped to {@link Boolean#TRUE} if the value is valid, mapped to {@link Boolean#FALSE} if not.
   */
  private final Map<ValueSpecification, Boolean> _valid = new ConcurrentHashMap<ValueSpecification, Boolean>();

  /**
   * The market data specifications to check. This is a map from an aliased market data value to the underlying market data value values. If the value is used in an unaliased form then the key and
   * value are the same for an entry.
   */
  private Map<ValueSpecification, ValueSpecification> _toCheck = new HashMap<ValueSpecification, ValueSpecification>();

  /**
   * Flag to indicate that there is at least one invalid node.
   */
  private volatile boolean _invalidNodes;

  public InvalidMarketDataDependencyNodeFilter(final ComputationTargetResolver.AtVersionCorrection targetResolver, final MarketDataAvailabilityProvider marketData) {
    _targetResolver = targetResolver;
    _marketData = marketData;
  }

  /**
   * Updates the "to-check" list with market data from the given graph. This must be called for all graph fragments that this sub-grapher will be required to act on before {@link #checkMarketData} is
   * called.
   * 
   * @param node a root node of the graph to consider, not null
   * @param terminalOutputs the graph's terminal outputs, not null
   * @param visited the "visited" buffer, not null
   */
  public void init(final DependencyNode node, final Map<ValueSpecification, ?> terminalOutputs, final Set<DependencyNode> visited) {
    if (!visited.add(node)) {
      return;
    }
    final int count = node.getInputCount();
    if (count == 1) {
      if (MarketDataAliasingFunction.UNIQUE_ID.equals(node.getFunction().getFunctionId())) {
        final ValueSpecification value = node.getInputValue(0);
        if (terminalOutputs.containsKey(value)) {
          // The underlying data is used directly as a terminal-output
          _toCheck.put(value, value);
        }
        final int outputs = node.getOutputCount();
        for (int i = 0; i < outputs; i++) {
          final ValueSpecification aliased = node.getOutputValue(i);
          _toCheck.put(aliased, value);
        }
        return;
      }
    } else if (count == 0) {
      if (MarketDataSourcingFunction.UNIQUE_ID.equals(node.getFunction().getFunctionId())) {
        // The value is consumed other than via a market-data-alias
        assert node.getOutputCount() == 1;
        final ValueSpecification value = node.getOutputValue(0);
        _toCheck.put(value, value);
      }
      return;
    }
    for (int i = 0; i < count; i++) {
      init(node.getInputNode(i), terminalOutputs, visited);
    }
  }

  /**
   * Tests whether the alias used in the graph still resolves to a usable target and the market data provider satisfies the requirement with the same underlying market data.
   * <p>
   * This may be called by multiple threads and will update the {@link #_valid} and {@link #_invalidNodes} properties.
   * 
   * @param alias the alias (might be the same as {@code marketData} if it is used directly), not null
   * @param marketData the previous market data specification returned by the data provider, not null
   */
  private void check(final ValueSpecification alias, final ValueSpecification marketData) {
    final ComputationTarget target = _targetResolver.resolve(alias.getTargetSpecification());
    if (target == null) {
      // This shouldn't normally happen (a default target specification will always be created that gives a stub Primitive instance) unless
      // the target specification cannot be resolved by the target resolver any more.
      s_logger.warn("Couldn't resolve {}", alias.getTargetSpecification());
      _valid.put(alias, Boolean.FALSE);
      _invalidNodes = true;
      return;
    }
    final Object targetValue = target.getValue();
    final ValueRequirement desiredValue = new ValueRequirement(alias.getValueName(), alias.getTargetSpecification(), alias.getProperties().withoutAny(ValuePropertyNames.DATA_PROVIDER));
    final ValueSpecification requiredMarketData = _marketData.getAvailability(alias.getTargetSpecification(), targetValue, desiredValue);
    if (marketData.equals(requiredMarketData)) {
      s_logger.debug("Market data entry {} still available for {}", marketData, desiredValue);
      _valid.put(alias, Boolean.TRUE);
    } else {
      s_logger.debug("New market data {} required for {}", requiredMarketData, desiredValue);
      _valid.put(alias, Boolean.FALSE);
      _invalidNodes = true;
    }
  }

  private final class CheckBatch implements Runnable {

    private final ValueSpecification[] _data;

    public CheckBatch(final Iterator<Map.Entry<ValueSpecification, ValueSpecification>> itr, final int count) {
      _data = new ValueSpecification[count * 2];
      int j = 0;
      for (int i = 0; i < count; i++) {
        final Map.Entry<ValueSpecification, ValueSpecification> e = itr.next();
        _data[j++] = e.getKey();
        _data[j++] = e.getValue();
      }
    }

    @Override
    public void run() {
      int i = 0;
      do {
        final ValueSpecification alias = _data[i++];
        final ValueSpecification marketData = _data[i++];
        check(alias, marketData);
      } while (i < _data.length);
    }

  }

  /**
   * After the "to-check" list is populated by calling {@link #init}, call this to check the market data. Checks are submitted to an executor in batches for parallel operation.
   * 
   * @param executor the executor to use, not null
   * @param batchSize the number of items to check in each batch
   * @return true if at least one market data node is now invalid, false if all are okay
   */
  public boolean checkMarketData(final PoolExecutor executor, final int batchSize) {
    final PoolExecutor.Service<?> service = executor.createService(null);
    Map<ValueSpecification, ValueSpecification> toCheck = _toCheck;
    _toCheck = null;
    int count = toCheck.size();
    final Iterator<Map.Entry<ValueSpecification, ValueSpecification>> itr = toCheck.entrySet().iterator();
    while (count > 0) {
      if (count <= batchSize) {
        while (itr.hasNext()) {
          final Map.Entry<ValueSpecification, ValueSpecification> e = itr.next();
          check(e.getKey(), e.getValue());
        }
        break;
      } else {
        service.execute(new CheckBatch(itr, batchSize));
        count -= batchSize;
      }
    }
    try {
      service.join();
    } catch (InterruptedException e) {
      throw new OpenGammaRuntimeException("Interrupted", e);
    }
    return _invalidNodes;
  }

  // RootDiscardingSubgrapher

  @Override
  public boolean acceptNode(final DependencyNode node) {
    int count = node.getInputCount();
    if (count == 1) {
      if (!MarketDataAliasingFunction.UNIQUE_ID.equals(node.getFunction().getFunctionId())) {
        return true;
      }
      // Only consider the aliasing function
    } else if (count == 0) {
      if (!MarketDataSourcingFunction.UNIQUE_ID.equals(node.getFunction().getFunctionId())) {
        return true;
      }
      // Only consider the sourcing function
    } else {
      return true;
    }
    count = node.getOutputCount();
    for (int i = 0; i < count; i++) {
      if (Boolean.TRUE != _valid.get(node.getOutputValue(i))) {
        return false;
      }
    }
    return true;
  }

}
