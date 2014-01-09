/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.AbstractPortfolioNodeTraversalCallback;
import com.opengamma.core.position.impl.PortfolioNodeTraverser;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.CompiledFunctionRepository;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroup;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroups;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityFilter;
import com.opengamma.engine.marketdata.availability.MarketDataNotSatisfiableException;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetResolverUtils;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Implementation of {@link AvailableOutputs} that scans a function repository to give possible outputs available on a portfolio.
 * <p>
 * The named values aren't guaranteed to be computable for all nodes, positions or security types indicated as the full set of functions may require underlying market data or other information that is
 * not available or computable. Due to the black-box nature of the function definitions additional values not mentioned may be available for a portfolio.
 * <p>
 * For more accurate details, refer to the documentation for the functions installed in the repository.
 */
public class AvailablePortfolioOutputs extends AvailableOutputsImpl {

  private static final Logger s_logger = LoggerFactory.getLogger(AvailablePortfolioOutputs.class);

  private final String _anyValue;

  private static final class SingleItem<T> implements Iterator<T> {

    private T _item;

    public SingleItem(final T item) {
      _item = item;
    }

    @Override
    public boolean hasNext() {
      return _item != null;
    }

    @Override
    public T next() {
      final T item = _item;
      _item = null;
      return item;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

  }

  private static boolean isSatisfied(final ValueRequirement requirement, final ValueSpecification result) {
    return (requirement.getValueName() == result.getValueName()) && requirement.getConstraints().isSatisfiedBy(result.getProperties());
  }

  private static final class TargetCachePopulator extends AbstractPortfolioNodeTraversalCallback {

    private final Map<UniqueId, UniqueIdentifiable> _targetCache = new HashMap<UniqueId, UniqueIdentifiable>();
    private int _work;

    public TargetCachePopulator() {
    }

    public Map<UniqueId, UniqueIdentifiable> getCache() {
      return _targetCache;
    }

    public int getWork() {
      return _work;
    }

    @Override
    public void preOrderOperation(final PortfolioNode portfolioNode) {
      if (portfolioNode.getUniqueId() != null) {
        _work++;
        _targetCache.put(portfolioNode.getUniqueId(), portfolioNode);
      }
    }

    @Override
    public void preOrderOperation(final PortfolioNode parentNode, final Position position) {
      _work++;
      _targetCache.put(position.getUniqueId(), position);
      _targetCache.put(position.getSecurity().getUniqueId(), position.getSecurity());
      for (final Trade trade : position.getTrades()) {
        _targetCache.put(trade.getUniqueId(), trade);
        _targetCache.put(trade.getSecurity().getUniqueId(), trade.getSecurity());
      }
    }

  }

  private final class FunctionApplicator extends AbstractPortfolioNodeTraversalCallback {

    private final FunctionCompilationContext _context;
    private final Collection<CompiledFunctionDefinition> _functions;
    private final Map<ComputationTargetType, CompiledFunctionDefinition[]> _functionsByType = new HashMap<ComputationTargetType, CompiledFunctionDefinition[]>();
    private final FunctionExclusionGroups _functionExclusionGroups;
    private final MarketDataAvailabilityFilter _marketDataAvailabilityFilter;
    private final Map<UniqueId, UniqueIdentifiable> _targetCache;
    private final Map<ComputationTarget, Map<CompiledFunctionDefinition, Set<ValueSpecification>>> _resultsCache = new HashMap<ComputationTarget, Map<CompiledFunctionDefinition, Set<ValueSpecification>>>();
    private final Map<ValueRequirement, List<Pair<List<ValueRequirement>, Set<ValueSpecification>>>> _resolutionCache = new HashMap<ValueRequirement, List<Pair<List<ValueRequirement>, Set<ValueSpecification>>>>();
    private final Integer _totalWork;
    private int _work;

    public FunctionApplicator(final FunctionCompilationContext context, final Collection<CompiledFunctionDefinition> functions, final FunctionExclusionGroups functionExclusionGroups,
        final MarketDataAvailabilityFilter marketDataAvailabilityFilter, final TargetCachePopulator targetCache) {
      _context = context;
      _functions = new ArrayList<CompiledFunctionDefinition>(functions.size());
      for (CompiledFunctionDefinition function : functions) {
        if (function.getTargetType() != null) {
          _functions.add(function);
        }
      }
      _functionExclusionGroups = functionExclusionGroups;
      _marketDataAvailabilityFilter = marketDataAvailabilityFilter;
      _totalWork = targetCache.getWork() * functions.size();
      _targetCache = targetCache.getCache();
    }

    private CompiledFunctionDefinition[] getFunctions(final ComputationTargetType targetType) {
      CompiledFunctionDefinition[] functions = _functionsByType.get(targetType);
      if (functions == null) {
        final List<CompiledFunctionDefinition> subset = new ArrayList<CompiledFunctionDefinition>(_functions.size());
        for (CompiledFunctionDefinition function : _functions) {
          if (function.getTargetType().isCompatible(targetType)) {
            subset.add(function);
          }
        }
        functions = subset.toArray(new CompiledFunctionDefinition[subset.size()]);
        _functionsByType.put(targetType, functions);
      }
      _work += _functions.size() - functions.length;
      return functions;
    }

    private Set<ValueSpecification> getCachedResult(final Set<ValueRequirement> visited, final ValueRequirement requirement) {
      final List<Pair<List<ValueRequirement>, Set<ValueSpecification>>> entries = _resolutionCache.get(requirement);
      if (entries == null) {
        return null;
      }
      for (final Pair<List<ValueRequirement>, Set<ValueSpecification>> entry : entries) {
        boolean subset = true;
        for (final ValueRequirement entryKey : entry.getFirst()) {
          if (!visited.contains(entryKey)) {
            subset = false;
            break;
          }
        }
        if (subset) {
          //s_logger.debug("Cache hit on {}", requirement);
          //s_logger.debug("Cached parent = {}", entry.getKey());
          //s_logger.debug("Active parent = {}", visited);
          return entry.getSecond();
        }
      }
      return null;
    }

    private void setCachedResult(final Set<ValueRequirement> visited, final ValueRequirement requirement, final Set<ValueSpecification> results) {
      s_logger.debug("Caching result for {} on {}", requirement, visited);
      List<Pair<List<ValueRequirement>, Set<ValueSpecification>>> entries = _resolutionCache.get(requirement);
      if (entries == null) {
        entries = new LinkedList<Pair<List<ValueRequirement>, Set<ValueSpecification>>>();
        _resolutionCache.put(requirement, entries);
      }
      List<ValueRequirement> list = new ArrayList<ValueRequirement>(visited);
      entries.add(Pairs.of(list, (results != null) ? results : Collections.<ValueSpecification>emptySet()));
    }

    private Set<ValueSpecification> satisfyRequirement(final Set<ValueRequirement> visitedRequirements, final Set<FunctionExclusionGroup> visitedFunctions, final ComputationTarget target,
        final ValueRequirement requirement) {
      Set<ValueSpecification> allResults = getCachedResult(visitedRequirements, requirement);
      if (allResults != null) {
        if (allResults.isEmpty()) {
          s_logger.debug("Cache failure hit on {}", requirement);
          return null;
        } else {
          s_logger.debug("Cache result hit on {}", requirement);
          return allResults;
        }
      }
      Map<CompiledFunctionDefinition, Set<ValueSpecification>> functionResults = _resultsCache.get(target);
      if (functionResults == null) {
        functionResults = new HashMap<CompiledFunctionDefinition, Set<ValueSpecification>>();
        for (final CompiledFunctionDefinition function : getFunctions(target.getType())) {
          try {
            if (function.canApplyTo(_context, target)) {
              final Set<ValueSpecification> results = function.getResults(_context, target);
              if (results != null) {
                functionResults.put(function, results);
              }
            }
          } catch (final Throwable t) {
            s_logger.error("Error applying {} to {}", function, target);
            t.printStackTrace();
            s_logger.info("Exception thrown", t);
          }
        }
        _resultsCache.put(target, functionResults);
      }
      if (!visitedRequirements.add(requirement)) {
        // This shouldn't happen
        throw new IllegalStateException();
      }
      for (final Map.Entry<CompiledFunctionDefinition, Set<ValueSpecification>> functionResult : functionResults.entrySet()) {
        final CompiledFunctionDefinition function = functionResult.getKey();
        for (final ValueSpecification result : functionResult.getValue()) {
          if (isSatisfied(requirement, result)) {
            final FunctionExclusionGroup group = (_functionExclusionGroups == null) ? null : _functionExclusionGroups.getExclusionGroup(function.getFunctionDefinition());
            if ((group == null) || visitedFunctions.add(group)) {
              s_logger.debug("Resolving {} to satisfy {}", result, requirement);
              final Set<ValueSpecification> resolved = resultWithSatisfiedRequirements(visitedRequirements, visitedFunctions, function, target, requirement, result.compose(requirement));
              if (resolved != null) {
                if (allResults == null) {
                  allResults = new HashSet<ValueSpecification>();
                }
                allResults.addAll(resolved);
              }
              if (group != null) {
                visitedFunctions.remove(group);
              }
            }
          }
        }
      }
      visitedRequirements.remove(requirement);
      setCachedResult(visitedRequirements, requirement, allResults);
      return allResults;
    }

    public boolean isAvailable(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement requirement) {
      try {
        return _marketDataAvailabilityFilter.isAvailable(targetSpec, target, requirement);
      } catch (final MarketDataNotSatisfiableException e) {
        return false;
      }
    }

    // Note: This is the logic from GetFunctionsStep, modified to omit the intersection against the satisfying properties
    private ValueProperties satisfyingProperties(final ValueRequirement requirement, final String functionName) {
      final ValueProperties constraints = requirement.getConstraints();
      final Set<String> functionNames = constraints.getValues(ValuePropertyNames.FUNCTION);
      if (functionNames == null) {
        final Set<String> allProperties = constraints.getProperties();
        if (allProperties == null) {
          // Requirement made no constraints
          return ValueProperties.with(ValuePropertyNames.FUNCTION, functionName).get();
        } else if (!allProperties.isEmpty()) {
          // Requirement made no constraint on function identifier
          return constraints.copy().with(ValuePropertyNames.FUNCTION, functionName).get();
        } else {
          // Requirement used a nearly infinite property bundle that omitted a function identifier
          return constraints.copy().withAny(ValuePropertyNames.FUNCTION).get();
        }
      } else {
        if (functionNames.isEmpty()) {
          final Set<String> allProperties = constraints.getProperties();
          if (allProperties.isEmpty()) {
            // Requirement is for an infinite or nearly infinite property bundle. This is valid but may be indicative of an error
            return constraints;
          } else {
            // Requirement had a wild card for the function but is otherwise finite
            return constraints.copy().withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, functionName).get();
          }
        } else if (functionNames.size() == 1) {
          // Requirement is fully specified
          return constraints;
        } else {
          // Requirement allowed a choice of function - pick one
          return constraints.copy().withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, functionNames.iterator().next()).get();
        }
      }
    }

    private ValueSpecification satisfyingSpecification(final ValueRequirement requirement, final ComputationTargetSpecification targetSpec, final String functionName) {
      return new ValueSpecification(requirement.getValueName(), targetSpec, satisfyingProperties(requirement, functionName));
    }

    private Set<ValueSpecification> resultWithSatisfiedRequirements(final Set<ValueRequirement> visitedRequirements, final Set<FunctionExclusionGroup> visitedFunctions,
        final CompiledFunctionDefinition function, final ComputationTarget target, final ValueRequirement requiredOutputValue, final ValueSpecification resolvedOutputValue) {
      final Set<ValueRequirement> requirements;
      try {
        requirements = function.getRequirements(_context, target, requiredOutputValue);
        if (requirements == null) {
          s_logger.debug("Unsatisfiable requirement {} for {}", requiredOutputValue, function);
          return null;
        }
      } catch (final Throwable t) {
        s_logger.error("Error applying {} to {}", function, target);
        s_logger.info("Exception thrown", t);
        return null;
      }
      if (requirements.isEmpty()) {
        return Collections.singleton(resolvedOutputValue);
      }
      for (final ValueRequirement requirement : requirements) {
        if (visitedRequirements.contains(requirement)) {
          return null;
        }
      }
      final Map<Iterator<ValueSpecification>, ValueRequirement> inputs = new HashMap<Iterator<ValueSpecification>, ValueRequirement>();
      for (final ValueRequirement requirement : requirements) {
        final ComputationTargetReference targetRef = requirement.getTargetReference();
        if (targetRef instanceof ComputationTargetSpecification) {
          final ComputationTargetSpecification targetSpec = targetRef.getSpecification();
          final UniqueIdentifiable requirementTarget = _targetCache.get(targetSpec.getUniqueId());
          if (isAvailable(targetSpec, requirementTarget, requirement)) {
            s_logger.debug("Requirement {} can be satisfied by market data", requirement);
            inputs.put(new SingleItem<ValueSpecification>(satisfyingSpecification(requirement, targetSpec, MarketDataSourcingFunction.UNIQUE_ID)), requirement);
          } else {
            if (requirementTarget != null) {
              s_logger.debug("Resolving {} for function {}", requirement, function);
              final Set<ValueSpecification> satisfied = satisfyRequirement(visitedRequirements, visitedFunctions,
                  ComputationTargetResolverUtils.createResolvedTarget(targetSpec, requirementTarget), requirement);
              if (satisfied == null) {
                s_logger.debug("Can't satisfy {} for function {}", requirement, function);
                if (!function.canHandleMissingRequirements()) {
                  return null;
                }
              } else {
                s_logger.debug("Resolved {} to {}", requirement, satisfied);
                inputs.put(satisfied.iterator(), requirement);
              }
            } else {
              s_logger.debug("No target cached for {}, assuming ok", targetSpec);
              inputs.put(new SingleItem<ValueSpecification>(satisfyingSpecification(requirement, targetSpec, "UncachedTarget")), requirement);
            }
          }
        } else {
          s_logger.debug("Externally referenced entity {}, assuming ok", targetRef);
          final ExternalId eid = targetRef.getRequirement().getIdentifiers().iterator().next();
          final UniqueId uid = UniqueId.of(eid.getScheme().getName(), eid.getValue());
          inputs.put(new SingleItem<ValueSpecification>(satisfyingSpecification(requirement, new ComputationTargetSpecification(targetRef.getType(), uid), "ExternalEntity")), requirement);
        }
      }
      final Set<ValueSpecification> outputs = new HashSet<ValueSpecification>();
      final Map<ValueSpecification, ValueRequirement> inputSet = new HashMap<ValueSpecification, ValueRequirement>();
      do {
        for (final Map.Entry<Iterator<ValueSpecification>, ValueRequirement> input : inputs.entrySet()) {
          if (!input.getKey().hasNext()) {
            inputSet.clear();
            break;
          }
          inputSet.put(input.getKey().next(), input.getValue());
        }
        if (inputSet.isEmpty()) {
          break;
        } else {
          try {
            final Set<ValueSpecification> results = function.getResults(_context, target, inputSet);
            if (results != null) {
              for (final ValueSpecification result : results) {
                if ((resolvedOutputValue == result) || isSatisfied(requiredOutputValue, result)) {
                  outputs.add(result.compose(requiredOutputValue));
                }
              }
            }
          } catch (final Throwable t) {
            s_logger.error("Error applying {} to {}", function, target);
            s_logger.info("Exception thrown", t);
          }
          inputSet.clear();
        }
      } while (true);
      if (outputs.isEmpty()) {
        s_logger.debug("Provisional result {} not in results after late resolution", resolvedOutputValue);
        return null;
      } else {
        return outputs;
      }
    }

    private final String _watch = null; // Don't check in with != null

    @Override
    public void postOrderOperation(final PortfolioNode portfolioNode) {
      if (portfolioNode.getUniqueId() == null) {
        // Anonymous node in the portfolio means it cannot be referenced so no results can be produced on it.
        // Being presented with a portfolio like this almost certainly implies a temporary portfolio for which
        // node-level results are not required.
        s_logger.debug("Ignoring portfolio node with no unique ID: {}", portfolioNode);
        return;
      }
      final ComputationTarget target = new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, portfolioNode);
      final Set<ValueRequirement> visitedRequirements = new HashSet<ValueRequirement>();
      final Set<FunctionExclusionGroup> visitedFunctions = new HashSet<FunctionExclusionGroup>();
      for (final CompiledFunctionDefinition function : getFunctions(ComputationTargetType.PORTFOLIO_NODE)) {
        try {
          if (function.canApplyTo(_context, target)) {
            final Set<ValueSpecification> results = function.getResults(_context, target);
            for (final ValueSpecification result : results) {
              visitedRequirements.clear();
              visitedFunctions.clear();
              final FunctionExclusionGroup group = (_functionExclusionGroups == null) ? null : _functionExclusionGroups.getExclusionGroup(function.getFunctionDefinition());
              if (group != null) {
                visitedFunctions.add(group);
              }
              if (_watch != null) {
                if (_watch.equals(result.getValueName())) {
                  System.out.println(function.getFunctionDefinition().getUniqueId());
                } else {
                  continue;
                }
              }
              final Set<ValueSpecification> resolved = resultWithSatisfiedRequirements(visitedRequirements, visitedFunctions, function, target, new ValueRequirement(result.getValueName(),
                  result.getTargetSpecification()), result);
              if (resolved != null) {
                s_logger.info("Resolved {} on {}", result.getValueName(), portfolioNode);
                for (final ValueSpecification resolvedItem : resolved) {
                  portfolioNodeOutput(resolvedItem.getValueName(), resolvedItem.getProperties());
                }
              } else {
                s_logger.info("Did not resolve {} on {}", result.getValueName(), portfolioNode);
              }
              if ((_watch != null) && _watch.equals(result.getValueName())) {
                System.out.println();
              }
            }
          }
        } catch (final Throwable t) {
          s_logger.error("Error applying {} to {}", function, target);
          s_logger.info("Exception thrown", t);
        }
        if (s_logger.isDebugEnabled()) {
          _work++;
          s_logger.debug("Completed {} steps of {}", _work, _totalWork);
        }
      }
    }

    @Override
    public void preOrderOperation(final PortfolioNode parentNode, final Position position) {
      final ComputationTarget target = new ComputationTarget(ComputationTargetType.POSITION, position);
      final Set<ValueRequirement> visitedRequirements = new HashSet<ValueRequirement>();
      final Set<FunctionExclusionGroup> visitedFunctions = new HashSet<FunctionExclusionGroup>();
      for (final CompiledFunctionDefinition function : getFunctions(ComputationTargetType.POSITION)) {
        try {
          if (function.canApplyTo(_context, target)) {
            final Set<ValueSpecification> results = function.getResults(_context, target);
            for (final ValueSpecification result : results) {
              visitedRequirements.clear();
              visitedFunctions.clear();
              final FunctionExclusionGroup group = (_functionExclusionGroups == null) ? null : _functionExclusionGroups.getExclusionGroup(function.getFunctionDefinition());
              if (group != null) {
                visitedFunctions.add(group);
              }
              if (_watch != null) {
                if (_watch.equals(result.getValueName())) {
                  System.out.println(function.getFunctionDefinition().getUniqueId() + " on " + position.getSecurity());
                } else {
                  continue;
                }
              }
              final Set<ValueSpecification> resolved = resultWithSatisfiedRequirements(visitedRequirements, visitedFunctions, function, target, new ValueRequirement(result.getValueName(),
                  result.getTargetSpecification()), result);
              if (resolved != null) {
                s_logger.info("Resolved {} on {}", result.getValueName(), position);
                for (final ValueSpecification resolvedItem : resolved) {
                  positionOutput(resolvedItem.getValueName(), position.getSecurity().getSecurityType(), resolvedItem.getProperties());
                }
              } else {
                s_logger.info("Did not resolve {} on {}", result.getValueName(), position);
              }
              if ((_watch != null) && _watch.equals(result.getValueName())) {
                System.out.println();
              }
            }
          }
        } catch (final Throwable t) {
          s_logger.error("Error applying {} to {}", function, target);
          s_logger.info("Exception thrown", t);
        }
        if (s_logger.isDebugEnabled()) {
          _work++;
          s_logger.debug("Completed {} steps of {}", _work, _totalWork);
        }
      }
    }

  }

  /**
   * Constructs a new output set.
   * 
   * @param portfolio the portfolio (must be resolved), not null
   * @param functionRepository the functions, not null
   * @param functionExclusionGroups the function exclusion groups
   * @param marketDataAvailability the market data availability, not null
   * @param anyValue value to use when composing a wild-card with a finite set of property values, or null to not compose
   */
  public AvailablePortfolioOutputs(final Portfolio portfolio, final CompiledFunctionRepository functionRepository, final FunctionExclusionGroups functionExclusionGroups,
      final MarketDataAvailabilityFilter marketDataAvailability, final String anyValue) {
    ArgumentChecker.notNull(portfolio, "portfolio");
    ArgumentChecker.notNull(functionRepository, "functions");
    ArgumentChecker.notNull(marketDataAvailability, "marketDataAvailability");
    _anyValue = anyValue;
    final Collection<CompiledFunctionDefinition> functions = functionRepository.getAllFunctions();
    final TargetCachePopulator targetCachePopulator = new TargetCachePopulator();
    PortfolioNodeTraverser.depthFirst(targetCachePopulator).traverse(portfolio.getRootNode());
    PortfolioNodeTraverser.depthFirst(new FunctionApplicator(functionRepository.getCompilationContext(), functions, functionExclusionGroups, marketDataAvailability, targetCachePopulator))
        .traverse(portfolio.getRootNode());
  }

  @Override
  protected AvailableOutputImpl createOutput(final String valueName) {
    return new AvailableOutputImpl(valueName, _anyValue);
  }

}
