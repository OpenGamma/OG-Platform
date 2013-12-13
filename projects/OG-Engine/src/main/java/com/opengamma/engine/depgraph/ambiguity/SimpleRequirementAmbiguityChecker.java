/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph.ambiguity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.google.common.collect.MapMaker;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.MarketDataSourcingFunction;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroup;
import com.opengamma.engine.function.exclusion.FunctionExclusionGroups;
import com.opengamma.engine.function.resolver.ComputationTargetResults;
import com.opengamma.engine.function.resolver.ResolutionRule;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Basic implementation of {@link RequirementAmbiguityChecker} that operates in a single (the caller's) thread.
 */
public class SimpleRequirementAmbiguityChecker implements RequirementAmbiguityChecker {

  private static final Logger s_logger = LoggerFactory.getLogger(SimpleRequirementAmbiguityChecker.class);
  private static final ParameterizedFunction MARKET_DATA_SOURCING_FUNCTION = createParameterizedFunction(MarketDataSourcingFunction.INSTANCE);

  private static ParameterizedFunction createParameterizedFunction(final CompiledFunctionDefinition function) {
    return new ParameterizedFunction(function, function.getFunctionDefinition().getDefaultParameters());
  }

  private final MarketDataAvailabilityProvider _mdap;
  private final FunctionExclusionGroups _exclusions;
  private final FunctionCompilationContext _compilationContext;
  private final ResolutionRule[][] _rules;
  private final ConcurrentMap<ComputationTargetType, ResolutionRule[][]> _rulesByType = new ConcurrentHashMap<ComputationTargetType, ResolutionRule[][]>();
  private boolean _greedyCaching;
  private ConcurrentMap<?, FullRequirementResolution> _sharedCaching;

  public SimpleRequirementAmbiguityChecker(final AmbiguityCheckerContext context, final Instant valuationTime, VersionCorrection resolverVersionCorrection) {
    ArgumentChecker.notNull(context, "context");
    ArgumentChecker.notNull(valuationTime, "valuationTime");
    resolverVersionCorrection = ArgumentChecker.notNull(resolverVersionCorrection, "resolverVersionCorrection").withLatestFixed(Instant.now());
    _mdap = context.getMarketDataAvailabilityProvider();
    _exclusions = context.getFunctionExclusionGroups();
    _compilationContext = context.getFunctionCompilationContext().clone();
    _compilationContext.setComputationTargetResolver(_compilationContext.getRawComputationTargetResolver().atVersionCorrection(resolverVersionCorrection));
    Collection<ResolutionRule> rules = context.getFunctionResolver().compile(valuationTime).getAllResolutionRules();
    _compilationContext.setComputationTargetResults(new ComputationTargetResults(rules));
    _rules = buildRules(rules);
  }

  public SimpleRequirementAmbiguityChecker(final AmbiguityCheckerContext context, final Instant valuationTime, VersionCorrection resolverVersionCorrection,
      final ViewCalculationConfiguration calcConfig) {
    ArgumentChecker.notNull(context, "context");
    ArgumentChecker.notNull(valuationTime, "valuationTime");
    resolverVersionCorrection = ArgumentChecker.notNull(resolverVersionCorrection, "resolverVersionCorrection").withLatestFixed(Instant.now());
    ArgumentChecker.notNull(calcConfig, "calcConfig");
    _mdap = context.getMarketDataAvailabilityProvider();
    _exclusions = context.getFunctionExclusionGroups();
    _compilationContext = context.getFunctionCompilationContext().clone();
    _compilationContext.setComputationTargetResolver(_compilationContext.getRawComputationTargetResolver().atVersionCorrection(resolverVersionCorrection));
    Collection<ResolutionRule> rules = context.getFunctionResolver().compile(valuationTime).getAllResolutionRules();
    rules = calcConfig.getResolutionRuleTransform().transform(rules);
    _compilationContext.setComputationTargetResults(new ComputationTargetResults(rules));
    _compilationContext.setViewCalculationConfiguration(calcConfig);
    _rules = buildRules(rules);
    final UniqueId portfolioId = calcConfig.getViewDefinition().getPortfolioId();
    if (portfolioId != null) {
      s_logger.info("Resolving portflio {} for view definition", portfolioId);
      ComputationTargetSpecification portfolioSpec = new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO, portfolioId);
      if (portfolioId.isLatest()) {
        _compilationContext.getComputationTargetResolver().getSpecificationResolver().getTargetSpecification(portfolioSpec);
      }
      final ComputationTarget target = _compilationContext.getComputationTargetResolver().resolve(portfolioSpec);
      if (target != null) {
        _compilationContext.setPortfolio(target.getValue(ComputationTargetType.PORTFOLIO));
      } else {
        s_logger.error("Couldn't resolve portfolio {}", portfolioId);
      }
    }
  }

  private ResolutionRule[][] buildRules(final Collection<ResolutionRule> rules) {
    final Map<Integer, Collection<ResolutionRule>> byPriority = new HashMap<Integer, Collection<ResolutionRule>>();
    for (ResolutionRule rule : rules) {
      Collection<ResolutionRule> priorityGroup = byPriority.get(rule.getPriority());
      if (priorityGroup == null) {
        priorityGroup = new ArrayList<ResolutionRule>();
        byPriority.put(rule.getPriority(), priorityGroup);
      }
      priorityGroup.add(rule);
    }
    final List<Integer> priorities = new ArrayList<Integer>(byPriority.keySet());
    Collections.sort(priorities);
    final ResolutionRule[][] result = new ResolutionRule[priorities.size()][];
    int i = result.length;
    for (Integer priority : priorities) {
      final Collection<ResolutionRule> priorityGroup = byPriority.get(priority);
      result[--i] = priorityGroup.toArray(new ResolutionRule[priorityGroup.size()]);
    }
    return result;
  }

  public MarketDataAvailabilityProvider getMarketDataAvailabilityProvider() {
    return _mdap;
  }

  public FunctionExclusionGroups getExclusions() {
    return _exclusions;
  }

  public FunctionCompilationContext getCompilationContext() {
    return _compilationContext;
  }

  private ResolutionRule[][] getRules() {
    return _rules;
  }

  private ResolutionRule[][] getRules(final ComputationTargetType type) {
    ResolutionRule[][] rules = _rulesByType.get(type);
    if (rules == null) {
      final List<ResolutionRule[]> rulesList = new ArrayList<ResolutionRule[]>(getRules().length);
      for (ResolutionRule[] originalRules : getRules()) {
        final List<ResolutionRule> filteredRules = new ArrayList<ResolutionRule>(originalRules.length);
        for (ResolutionRule originalRule : originalRules) {
          if (originalRule.getParameterizedFunction().getFunction().getTargetType().isCompatible(type)) {
            filteredRules.add(originalRule);
          }
        }
        if (!filteredRules.isEmpty()) {
          rulesList.add(filteredRules.toArray(new ResolutionRule[filteredRules.size()]));
        }
      }
      rules = rulesList.toArray(new ResolutionRule[rulesList.size()][]);
      final ResolutionRule[][] existing = _rulesByType.putIfAbsent(type, rules);
      if (existing != null) {
        rules = existing;
      }
    }
    return rules;
  }

  public void setGreedyCaching(final boolean greedyCaching) {
    _greedyCaching = greedyCaching;
  }

  public boolean isGreedyCaching() {
    return _greedyCaching;
  }

  public void setSharedCaching(final boolean sharedCaching) {
    //_sharedCaching = sharedCaching ? new ConcurrentHashMap<Object, FullRequirementResolution>() : null;
    _sharedCaching = sharedCaching ? new MapMaker().softValues().<Object, FullRequirementResolution>makeMap() : null;
  }

  public boolean isSharedCaching() {
    return _sharedCaching != null;
  }

  private ValueSpecification alias(ValueSpecification marketDataSpec, final ComputationTargetSpecification targetSpec, final ValueRequirement requirement) {
    if (!marketDataSpec.getValueName().equals(requirement.getValueName())) {
      marketDataSpec = new ValueSpecification(requirement.getValueName(), marketDataSpec.getTargetSpecification(), marketDataSpec.getProperties());
    }
    if (!marketDataSpec.getTargetSpecification().equals(targetSpec)) {
      marketDataSpec = new ValueSpecification(marketDataSpec.getValueName(), targetSpec, marketDataSpec.getProperties());
    }
    if (!requirement.getConstraints().isSatisfiedBy(marketDataSpec.getProperties())) {
      final String function = requirement.getConstraints().getSingleValue(ValuePropertyNames.FUNCTION);
      final ValueProperties a, b;
      if (function != null) {
        a = marketDataSpec.getProperties().copy().withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, function).get();
        b = requirement.getConstraints().withoutAny(ValuePropertyNames.FUNCTION);
      } else {
        a = marketDataSpec.getProperties();
        b = requirement.getConstraints();
      }
      marketDataSpec = new ValueSpecification(marketDataSpec.getValueName(), marketDataSpec.getTargetSpecification(), a.union(b));
    }
    return marketDataSpec;
  }

  private boolean isExcluded(final Collection<FunctionExclusionGroup> exclusions, final ResolutionRule rule) {
    if (exclusions != null) {
      final FunctionExclusionGroups util = getExclusions();
      final FunctionExclusionGroup exclusion = util.getExclusionGroup(rule.getParameterizedFunction().getFunction().getFunctionDefinition());
      if ((exclusion != null) && util.isExcluded(exclusion, exclusions)) {
        s_logger.debug("Ignoring {} from exclusion group {}", rule, exclusion);
        return true;
      }
    }
    return false;
  }

  private Collection<FunctionExclusionGroup> getFunctionExclusion(final Collection<FunctionExclusionGroup> parentExclusion, final ResolutionRule rule) {
    final FunctionExclusionGroups groups = getExclusions();
    if (groups == null) {
      return null;
    }
    final FunctionExclusionGroup functionExclusion = groups.getExclusionGroup(rule.getParameterizedFunction().getFunction().getFunctionDefinition());
    if (functionExclusion == null) {
      return parentExclusion;
    }
    if (parentExclusion != null) {
      return groups.withExclusion(parentExclusion, functionExclusion);
    } else {
      return Collections.singleton(functionExclusion);
    }
  }

  private Collection<FullRequirementResolution> resolve(final CheckingCache cache, final Collection<FunctionExclusionGroup> parentExclusion, final ComputationTarget target,
      final ValueRequirement desiredValue, final ResolutionRule rule, final Set<ValueRequirement> inputs) {
    String functionExclusionValueName = desiredValue.getValueName();
    Collection<FunctionExclusionGroup> functionExclusion = null;
    final Collection<FullRequirementResolution> resolvedInputs = new ArrayList<FullRequirementResolution>(inputs.size());
    for (ValueRequirement input : inputs) {
      final FullRequirementResolution resolvedInput;
      if ((input.getValueName() == functionExclusionValueName) && input.getTargetReference().equals(target.toSpecification())) {
        if (functionExclusion == null) {
          functionExclusion = getFunctionExclusion(parentExclusion, rule);
          if (functionExclusion == null) {
            functionExclusionValueName = null;
          }
        }
        resolvedInput = resolve(cache, functionExclusion, input);
      } else {
        resolvedInput = resolve(cache, null, input);
      }
      if ((resolvedInput != null) && resolvedInput.isResolved()) {
        resolvedInputs.add(resolvedInput);
      }
    }
    return resolvedInputs;
  }

  private void getResolvedInputs(final Collection<FullRequirementResolution> resolvedInputs, final ValueRequirement[] inputArray, final Iterator<Collection<RequirementResolution>>[] itrResolvedInputs) {
    int i = 0;
    for (FullRequirementResolution resolvedInput : resolvedInputs) {
      inputArray[i] = resolvedInput.getRequirement();
      itrResolvedInputs[i++] = resolvedInput.getResolutions().iterator();
    }
  }

  private boolean getResolvedInputs(final int j, final ValueRequirement[] inputArray, final RequirementResolution[][] resolvedInputsSlice, final Map<ValueSpecification, ValueRequirement> inputMap) {
    inputMap.clear();
    int base = 1;
    boolean success = true;
    for (int i = 0; i < resolvedInputsSlice.length; i++) {
      final int size = resolvedInputsSlice[i].length;
      final RequirementResolution resolvedInput = resolvedInputsSlice[i][(j / base) % size];
      base *= size;
      if (resolvedInput == null) {
        success = false;
      } else {
        inputMap.put(resolvedInput.getSpecification(), inputArray[i]);
      }
    }
    return success;
  }

  protected FullRequirementResolution resolve(final CheckingCache cache, final Collection<FunctionExclusionGroup> exclusions, final ValueRequirement requirement) {
    if (!cache.begin(requirement)) {
      // Recursive requirement; abort
      s_logger.debug("Recursive requirement on {}", requirement);
      return null;
    }
    FullRequirementResolution resolved = cache.get(requirement);
    if (resolved != null) {
      s_logger.debug("Cached resolution {}", resolved);
      cache.end(requirement);
      return resolved;
    }
    s_logger.debug("Resolving {}", requirement);
    resolved = new FullRequirementResolution(requirement);
    final ComputationTargetResolver.AtVersionCorrection resolver = getCompilationContext().getComputationTargetResolver();
    final ComputationTargetSpecification targetSpec = resolver.getSpecificationResolver().getTargetSpecification(requirement.getTargetReference());
    if (targetSpec != null) {
      final ComputationTarget target = resolver.resolve(targetSpec);
      ValueSpecification marketData = getMarketDataAvailabilityProvider().getAvailability(targetSpec, (target != null) ? target.getValue() : null, requirement);
      if (marketData != null) {
        s_logger.debug("Market data satisfies {} with {}", requirement, marketData);
        marketData = alias(marketData, targetSpec, requirement);
        resolved.addResolutions(Collections.singleton(new RequirementResolution(marketData, MARKET_DATA_SOURCING_FUNCTION, Collections.<FullRequirementResolution>emptySet())));
      } else {
        if (target != null) {
          final List<Collection<RequirementResolution>> resolutions = new ArrayList<Collection<RequirementResolution>>();
          final Map<ComputationTargetType, ComputationTarget> targetCache = new HashMap<ComputationTargetType, ComputationTarget>();
          final Map<ValueSpecification, ValueRequirement> inputMap = new HashMap<ValueSpecification, ValueRequirement>();
          for (ResolutionRule[] rules : getRules(target.toSpecification().getType())) {
            for (ResolutionRule rule : rules) {
              try {
                if (isExcluded(exclusions, rule)) {
                  continue;
                }
                final ComputationTarget adjustedTarget = rule.adjustTarget(targetCache, target);
                final ValueSpecification nominalResult = rule.getResult(requirement.getValueName(), adjustedTarget, requirement.getConstraints(), getCompilationContext());
                if (nominalResult != null) {
                  s_logger.debug("Possible resolution of {} to {}", requirement, nominalResult);
                  Set<ValueRequirement> inputs = null;
                  try {
                    inputs = rule.getParameterizedFunction().getFunction().getRequirements(getCompilationContext(), adjustedTarget, requirement);
                  } catch (Throwable t) {
                    s_logger.debug("Exception thrown by getRequirements", t);
                  }
                  if (inputs != null) {
                    final Collection<FullRequirementResolution> resolvedInputs = resolve(cache, exclusions, target, requirement, rule, inputs);
                    if (resolvedInputs.size() != inputs.size()) {
                      if (!rule.getParameterizedFunction().getFunction().canHandleMissingRequirements()) {
                        s_logger.debug("Couldn't resolve inputs for {}", rule);
                        continue;
                      }
                    }
                    final ValueRequirement[] inputArray = new ValueRequirement[resolvedInputs.size()];
                    @SuppressWarnings("unchecked")
                    final Iterator<Collection<RequirementResolution>>[] itrResolvedInputs = new Iterator[resolvedInputs.size()];
                    getResolvedInputs(resolvedInputs, inputArray, itrResolvedInputs);
                    final RequirementResolution[][] resolvedInputsSlice = new RequirementResolution[resolvedInputs.size()][];
                    int resolutionIndex = 0;
                    do {
                      int ambiguous = 1;
                      boolean hasNext = false;
                      for (int i = 0; i < resolvedInputsSlice.length; i++) {
                        if (itrResolvedInputs[i].hasNext()) {
                          final Collection<RequirementResolution> value = itrResolvedInputs[i].next();
                          resolvedInputsSlice[i] = value.toArray(new RequirementResolution[value.size()]);
                          hasNext = true;
                        }
                        ambiguous *= resolvedInputsSlice[i].length;
                        if (ambiguous <= 0) {
                          // The cross product can be bad enough, but this is *really* bad
                          throw new IllegalStateException("Overflow");
                        }
                      }
                      if (!hasNext) {
                        break;
                      }
                      if (ambiguous > 1) {
                        s_logger.info("{} ambiguous input states discovered for {}", ambiguous, requirement);
                      }
                      boolean failed = false;
                      boolean succeeded = false;
                      for (int j = 0; j < ambiguous; j++) {
                        if (!getResolvedInputs(j, inputArray, resolvedInputsSlice, inputMap)) {
                          if (!rule.getParameterizedFunction().getFunction().canHandleMissingRequirements()) {
                            failed = true;
                            continue;
                          }
                        }
                        Set<ValueSpecification> results = null;
                        try {
                          results = rule.getParameterizedFunction().getFunction().getResults(getCompilationContext(), adjustedTarget, inputMap);
                        } catch (Throwable t) {
                          s_logger.debug("Exception thrown by getResults", t);
                        }
                        if (results != null) {
                          ValueSpecification finalResult = null;
                          for (ValueSpecification result : results) {
                            if (requirement.getValueName().equals(result.getValueName())) {
                              if (requirement.getConstraints().isSatisfiedBy(result.getProperties())) {
                                finalResult = result;
                                break;
                              }
                            }
                          }
                          if (finalResult != null) {
                            final Set<ValueRequirement> additionalRequirements = rule.getParameterizedFunction().getFunction()
                                .getAdditionalRequirements(getCompilationContext(), adjustedTarget, inputMap.keySet(), results);
                            if (additionalRequirements != null) {
                              if (additionalRequirements.isEmpty()) {
                                s_logger.debug("Resolved {} to {}", requirement, finalResult);
                                if (resolutionIndex >= resolutions.size()) {
                                  resolutions.add(new HashSet<RequirementResolution>());
                                }
                                resolutions.get(resolutionIndex).add(new RequirementResolution(finalResult, rule.getParameterizedFunction(), resolvedInputs));
                                succeeded = true;
                              } else {
                                final Collection<FullRequirementResolution> additionalResolvedRequirements = resolve(cache, exclusions, target, requirement, rule, additionalRequirements);
                                if ((additionalResolvedRequirements.size() == additionalRequirements.size()) || rule.getParameterizedFunction().getFunction().canHandleMissingRequirements()) {
                                  resolvedInputs.addAll(additionalResolvedRequirements);
                                  s_logger.debug("Resolved {} to {}", requirement, finalResult);
                                  if (resolutionIndex >= resolutions.size()) {
                                    resolutions.add(new HashSet<RequirementResolution>());
                                  }
                                  resolutions.get(resolutionIndex).add(new RequirementResolution(finalResult, rule.getParameterizedFunction(), resolvedInputs));
                                  succeeded = true;
                                } else {
                                  failed = true;
                                }
                              }
                            } else {
                              failed = true;
                            }
                          } else {
                            failed = true;
                          }
                        } else {
                          failed = true;
                        }
                      }
                      if (succeeded) {
                        if (failed) {
                          // Not all combinations are successful; treat as ambiguous
                          resolutions.get(resolutionIndex).add(null);
                        }
                        resolutionIndex++;
                      }
                    } while (true);
                  }
                }
              } catch (Throwable t) {
                s_logger.error("Exception thrown by {} when handling {}", rule, requirement);
                s_logger.warn("Exception", t);
              }
            }
            if (!resolutions.isEmpty()) {
              for (Collection<RequirementResolution> resolution : resolutions) {
                if (resolution.size() > 1) {
                  s_logger.info("Got ambiguous resolution of {} to {}", requirement, resolutions);
                } else {
                  s_logger.debug("Unambiguous resolution of {} to {}", requirement, resolutions);
                }
                resolved.addResolutions(resolution);
              }
              resolutions.clear();
            }
          }
          if (resolved.isResolved()) {
            s_logger.info("Resolved {}", requirement);
          } else {
            s_logger.debug("No resolutions found for {}", requirement);
          }
        } else {
          s_logger.warn("Couldn't resolve target for {}", requirement);
        }
      }
    } else {
      s_logger.warn("Couldn't resolve target specification for {}", requirement);
    }
    cache.end(requirement);
    return cache.put(resolved);
  }

  // RequirementAmbiguityChecker

  @Override
  public FullRequirementResolution resolve(final ValueRequirement requirement) {
    return resolve(new CheckingCache(isGreedyCaching(), _sharedCaching), null, requirement);
  }

}
