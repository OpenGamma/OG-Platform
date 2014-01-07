/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.MemoryUtils;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.function.blacklist.FunctionBlacklistQuery;
import com.opengamma.engine.target.ComputationTargetResolverUtils;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.ComputationTargetTypeMap;
import com.opengamma.engine.target.ComputationTargetTypeVisitor;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.lambdava.functions.Function2;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;
import com.opengamma.util.tuple.Triple;

/**
 * Default implementation of the compiled function resolver.
 * <p>
 * The aim of the resolution is to find functions that are capable of satisfying a requirement. In addition, a priority mechanism is used to return functions in priority order from highest to lowest.
 * <p>
 * This class is not thread-safe. It is possible to call {@link #resolveFunction} concurrently from multiple threads, the rule manipulation methods require external locking.
 */
public class DefaultCompiledFunctionResolver implements CompiledFunctionResolver {

  private static final Logger s_logger = LoggerFactory.getLogger(DefaultCompiledFunctionResolver.class);

  /**
   * Comparator to give a fixed ordering of functions at the same priority so that we at least have deterministic behavior between runs.
   */
  private static final Comparator<ResolutionRule> RULE_COMPARATOR = new Comparator<ResolutionRule>() {
    @Override
    public int compare(final ResolutionRule o1, final ResolutionRule o2) {
      int c = o1.getParameterizedFunction().getFunction().getFunctionDefinition().getUniqueId().compareTo(o2.getParameterizedFunction().getFunction().getFunctionDefinition().getUniqueId());
      if (c != 0) {
        return c;
      }
      // Have the same function, can try and order the "FunctionParameters" as we know it implements a hash code
      c = o1.getParameterizedFunction().getParameters().hashCode() - o2.getParameterizedFunction().getParameters().hashCode();
      if (c != 0) {
        return c;
      }
      throw new OpenGammaRuntimeException("Rule priority conflict - cannot order " + o1 + " against " + o2);
    }
  };

  /**
   * Holds an arbitrary bundle of rules with mixed priorities. Instances can attach to a "parent" bundle to receive copies of those rules. For example a rule that applies to objects of type A must be
   * present in the bundles for objects of sub-types of A.
   */
  private interface ChainedRuleBundle extends Iterable<Collection<ResolutionRule>> {

    /**
     * Groups the rules into priority levels, sorts these and returns a structure that can iterate over them in descending priority order.
     * 
     * @returns the prioritized rules or null if there are none
     */
    Iterable<Collection<ResolutionRule>> prioritize();

    /**
     * Registers a listening {@link ChainedRuleBundle} with this one. The listener will be immediately notified of any existing rules. When new rules are added, the listener will also be notified.
     */
    void addListener(ChainedRuleBundle listener);

    /**
     * Adds a rule to this bundle and notifies any registered listeners of the rule.
     */
    void addRule(ResolutionRule rule);

  }

  private static final class SimpleChainedRuleBundle implements ChainedRuleBundle {

    /**
     * All of the rules, in an arbitrary order.
     */
    private final LinkedList<ResolutionRule> _rules = new LinkedList<ResolutionRule>();
    /**
     * All of the listeners to be notified when a rule is added.
     */
    private ArrayList<ChainedRuleBundle> _listeners;

    @Override
    public Iterable<Collection<ResolutionRule>> prioritize() {
      if (_rules.isEmpty()) {
        return null;
      } else {
        final Map<Integer, Collection<ResolutionRule>> priorityToRules = new HashMap<Integer, Collection<ResolutionRule>>();
        for (ResolutionRule rule : _rules) {
          Collection<ResolutionRule> rules = priorityToRules.get(rule.getPriority());
          if (rules == null) {
            rules = new LinkedList<ResolutionRule>();
            priorityToRules.put(rule.getPriority(), rules);
          }
          rules.add(rule);
        }
        final Integer[] priorities = priorityToRules.keySet().toArray(new Integer[priorityToRules.size()]);
        Arrays.sort(priorities);
        final Collection<Collection<ResolutionRule>> rules = new ArrayList<Collection<ResolutionRule>>(priorities.length);
        for (int i = priorities.length; --i >= 0;) {
          final List<ResolutionRule> list = new ArrayList<ResolutionRule>(priorityToRules.get(priorities[i]));
          Collections.sort(list, RULE_COMPARATOR);
          rules.add(list);
        }
        return rules;
      }
    }

    /**
     * Returns an iterator over the rules. The rules are not prioritized and returned as a single chunk.
     */
    @Override
    public Iterator<Collection<ResolutionRule>> iterator() {
      return Collections.<Collection<ResolutionRule>>singleton(_rules).iterator();
    }

    @Override
    public void addListener(final ChainedRuleBundle listener) {
      if (_listeners == null) {
        _listeners = new ArrayList<ChainedRuleBundle>();
      }
      _listeners.add(listener);
      for (ResolutionRule rule : _rules) {
        listener.addRule(rule);
      }
    }

    @Override
    public void addRule(final ResolutionRule rule) {
      _rules.add(rule);
      if (_listeners != null) {
        for (ChainedRuleBundle listener : _listeners) {
          listener.addRule(rule);
        }
      }
    }

  }

  private static final class FoldedChainedRuleBundle implements ChainedRuleBundle {

    private final Collection<ChainedRuleBundle> _bundles;

    private FoldedChainedRuleBundle(final Collection<ChainedRuleBundle> bundles) {
      _bundles = bundles;
    }

    public static ChainedRuleBundle of(final ChainedRuleBundle a, final ChainedRuleBundle b) {
      if (a instanceof FoldedChainedRuleBundle) {
        final Collection<ChainedRuleBundle> bundles = new ArrayList<ChainedRuleBundle>(((FoldedChainedRuleBundle) a)._bundles);
        if (b instanceof FoldedChainedRuleBundle) {
          bundles.addAll(((FoldedChainedRuleBundle) b)._bundles);
        } else {
          bundles.add(b);
        }
        return new FoldedChainedRuleBundle(bundles);
      } else {
        return new FoldedChainedRuleBundle(Arrays.asList(a, b));
      }
    }

    @Override
    public Iterator<Collection<ResolutionRule>> iterator() {
      // Folding should only occur at a "fetch" so we will not be requesting an iterator
      throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Collection<ResolutionRule>> prioritize() {
      // Folding should only occur at a "fetch" so we will not be requesting a prioritization
      throw new UnsupportedOperationException();
    }

    @Override
    public void addListener(final ChainedRuleBundle listener) {
      for (ChainedRuleBundle bundle : _bundles) {
        bundle.addListener(listener);
      }
    }

    @Override
    public void addRule(final ResolutionRule rule) {
      for (ChainedRuleBundle bundle : _bundles) {
        bundle.addRule(rule);
      }
    }

  }

  private static class FoldedCompiledRuleBundle extends ArrayList<Collection<ResolutionRule>> implements Iterable<Collection<ResolutionRule>> {

    private static final long serialVersionUID = 1L;

    public FoldedCompiledRuleBundle(final Iterable<Collection<ResolutionRule>> a, final Iterable<Collection<ResolutionRule>> b) {
      final Iterator<Collection<ResolutionRule>> aItr = a.iterator();
      Collection<ResolutionRule> aRules = aItr.next();
      int aPriority = aRules.iterator().next().getPriority();
      final Iterator<Collection<ResolutionRule>> bItr = b.iterator();
      Collection<ResolutionRule> bRules = bItr.next();
      int bPriority = bRules.iterator().next().getPriority();
      do {
        if (aPriority == bPriority) {
          final Set<ResolutionRule> rules = new HashSet<ResolutionRule>(aRules);
          rules.addAll(bRules);
          final List<ResolutionRule> list = new ArrayList<ResolutionRule>(rules);
          Collections.sort(list, RULE_COMPARATOR);
          add(list);
          aRules = null;
          bRules = null;
        } else if (aPriority > bPriority) {
          add(aRules);
          aRules = null;
        } else {
          add(bRules);
          bRules = null;
        }
        if (aRules == null) {
          if (aItr.hasNext()) {
            aRules = aItr.next();
          }
        }
        if (bRules == null) {
          if (bItr.hasNext()) {
            bRules = bItr.next();
          }
        }
      } while ((aRules != null) && (bRules != null));
      if (aRules != null) {
        do {
          add(aRules);
          if (aItr.hasNext()) {
            aRules = aItr.next();
          } else {
            break;
          }
        } while (true);
      }
      if (bRules != null) {
        do {
          add(bRules);
          if (bItr.hasNext()) {
            bRules = bItr.next();
          } else {
            break;
          }
        } while (true);
      }
    }

  }

  private static final Function2<Iterable<Collection<ResolutionRule>>, Iterable<Collection<ResolutionRule>>, Iterable<Collection<ResolutionRule>>> s_foldRules = new Function2<Iterable<Collection<ResolutionRule>>, Iterable<Collection<ResolutionRule>>, Iterable<Collection<ResolutionRule>>>() {
    @Override
    public Iterable<Collection<ResolutionRule>> execute(final Iterable<Collection<ResolutionRule>> a, final Iterable<Collection<ResolutionRule>> b) {
      if (a instanceof ChainedRuleBundle) {
        if (b instanceof ChainedRuleBundle) {
          return FoldedChainedRuleBundle.of((ChainedRuleBundle) a, (ChainedRuleBundle) b);
        } else {
          throw new IllegalStateException("Rules have been partially compiled");
        }
      } else {
        if (b instanceof ChainedRuleBundle) {
          throw new IllegalStateException("Rules have been partially compiled");
        } else {
          return new FoldedCompiledRuleBundle(a, b);
        }
      }
    }
  };

  /**
   * The rules by target type. The map values are {@link ChainedRuleBundle} instances during construction, after which return an iterator giving the rules in blocks of descending priority order.
   */
  private final ComputationTargetTypeMap<Iterable<Collection<ResolutionRule>>> _type2Rules = new ComputationTargetTypeMap<Iterable<Collection<ResolutionRule>>>(s_foldRules);

  /**
   * The total number of unique rules.
   */
  private int _ruleCount;

  /**
   * The compilation context.
   */
  private final FunctionCompilationContext _functionCompilationContext;

  /**
   * Cache of targets. The values are weak so that when the function iterators drop out of scope as the requirements on the target are resolved the entry can be dropped.
   */
  private final ConcurrentMap<ComputationTargetSpecification, Pair<ResolutionRule[], Collection<ValueSpecification>[]>> _targetCache = new MapMaker().weakValues().makeMap();

  /**
   * Function definition lookup.
   */
  private final Map<String, CompiledFunctionDefinition> _functions = new HashMap<String, CompiledFunctionDefinition>();

  /**
   * Creates a resolver.
   * 
   * @param functionCompilationContext the context, not null
   */
  public DefaultCompiledFunctionResolver(final FunctionCompilationContext functionCompilationContext) {
    this(functionCompilationContext, Collections.<ResolutionRule>emptyList());
  }

  /**
   * Creates a resolver.
   * 
   * @param functionCompilationContext the context, not null
   * @param resolutionRules the resolution rules, not null
   */
  public DefaultCompiledFunctionResolver(final FunctionCompilationContext functionCompilationContext, Collection<ResolutionRule> resolutionRules) {
    ArgumentChecker.notNull(functionCompilationContext, "functionCompilationContext");
    ArgumentChecker.notNull(resolutionRules, "resolutionRules");
    _functionCompilationContext = functionCompilationContext;
    addRules(resolutionRules);
  }

  private static final Function2<Iterable<Collection<ResolutionRule>>, Iterable<Collection<ResolutionRule>>, Iterable<Collection<ResolutionRule>>> s_combineChainedRuleBundle = new Function2<Iterable<Collection<ResolutionRule>>, Iterable<Collection<ResolutionRule>>, Iterable<Collection<ResolutionRule>>>() {
    @Override
    public Iterable<Collection<ResolutionRule>> execute(final Iterable<Collection<ResolutionRule>> a, final Iterable<Collection<ResolutionRule>> b) {
      if (!(a instanceof ChainedRuleBundle)) {
        throw new IllegalStateException("Rules have already been compiled - can't add new ones");
      }
      ((ChainedRuleBundle) a).addListener((ChainedRuleBundle) b);
      return b;
    }
  };

  private static final ComputationTargetTypeVisitor<DefaultCompiledFunctionResolver, Void> s_createChainedRuleBundle = new ComputationTargetTypeVisitor<DefaultCompiledFunctionResolver, Void>() {

    @Override
    public Void visitMultipleComputationTargetTypes(final Set<ComputationTargetType> types, final DefaultCompiledFunctionResolver self) {
      for (ComputationTargetType type : types) {
        type.accept(this, self);
      }
      return null;
    }

    @Override
    public Void visitNestedComputationTargetTypes(final List<ComputationTargetType> types, final DefaultCompiledFunctionResolver self) {
      return types.get(types.size() - 1).accept(this, self);
    }

    @Override
    public Void visitNullComputationTargetType(final DefaultCompiledFunctionResolver self) {
      if (self._type2Rules.getDirect(ComputationTargetType.NULL) == null) {
        self._type2Rules.put(ComputationTargetType.NULL, new SimpleChainedRuleBundle());
      }
      return null;
    }

    private void insertBundle(final Class<?> clazz, final DefaultCompiledFunctionResolver self) {
      if ((clazz != null) && UniqueIdentifiable.class.isAssignableFrom(clazz)) {
        @SuppressWarnings("unchecked")
        final ComputationTargetType type = ComputationTargetType.of((Class<? extends UniqueIdentifiable>) clazz);
        if (self._type2Rules.getDirect(type) == null) {
          for (Class<?> superClazz : clazz.getInterfaces()) {
            insertBundle(superClazz, self);
          }
          insertBundle(clazz.getSuperclass(), self);
          self._type2Rules.put(type, new SimpleChainedRuleBundle(), s_combineChainedRuleBundle);
        }
      }
    }

    @Override
    public Void visitClassComputationTargetType(final Class<? extends UniqueIdentifiable> type, final DefaultCompiledFunctionResolver self) {
      insertBundle(type, self);
      return null;
    }

  };

  /**
   * Adds a single rule to the resolver. Rules must be added before calling {@link #compileRules} to pre-process them into the data structures used for resolution.
   * 
   * @param resolutionRule the rule to add, not null
   */
  public void addRule(ResolutionRule resolutionRule) {
    final ComputationTargetType type = resolutionRule.getParameterizedFunction().getFunction().getTargetType();
    type.accept(s_createChainedRuleBundle, this);
    final Iterable<Collection<ResolutionRule>> rules = _type2Rules.getDirect(type);
    assert rules != null; // s_createChainedRuleBundle should have done this
    final ChainedRuleBundle bundle;
    if (rules instanceof ChainedRuleBundle) {
      bundle = (ChainedRuleBundle) rules;
    } else {
      throw new IllegalStateException("Rules have already been compiled");
    }
    bundle.addRule(resolutionRule);
    _functions.put(resolutionRule.getParameterizedFunction().getFunctionId(), resolutionRule.getParameterizedFunction().getFunction());
  }

  /**
   * Adds rules to the resolver. Rules must be added before calling {@link #compileRules} to pre-process them into the data structures used for resolution.
   * 
   * @param resolutionRules the rules to add, no nulls, not null
   */
  public void addRules(Iterable<ResolutionRule> resolutionRules) {
    for (ResolutionRule resolutionRule : resolutionRules) {
      addRule(resolutionRule);
    }
  }

  /**
   * Processes the rules into data structures used for resolution. After calling this method, no further rules must be added using {@link #addRule} or {@link #addRules}.
   */
  public void compileRules() {
    final Iterator<Map.Entry<ComputationTargetType, Iterable<Collection<ResolutionRule>>>> itr = _type2Rules.entries().iterator();
    int count = 0;
    while (itr.hasNext()) {
      final Map.Entry<ComputationTargetType, Iterable<Collection<ResolutionRule>>> e = itr.next();
      final Iterable<Collection<ResolutionRule>> v = e.getValue();
      if (v instanceof ChainedRuleBundle) {
        final Iterable<Collection<ResolutionRule>> rules = ((ChainedRuleBundle) v).prioritize();
        if (rules != null) {
          e.setValue(rules);
          for (Collection<ResolutionRule> rule : rules) {
            count += rule.size();
          }
        } else {
          itr.remove();
        }
      }
    }
    _ruleCount = count;
  }

  @Override
  public Collection<ResolutionRule> getAllResolutionRules() {
    final Set<ResolutionRule> rules = new HashSet<ResolutionRule>();
    for (Iterable<Collection<ResolutionRule>> typeRules : _type2Rules.values()) {
      for (Collection<ResolutionRule> priorityRules : typeRules) {
        rules.addAll(priorityRules);
      }
    }
    return rules;
  }

  /**
   * Gets the function compilation context.
   * 
   * @return the context, not null
   */
  protected FunctionCompilationContext getFunctionCompilationContext() {
    return _functionCompilationContext;
  }

  private static ValueSpecification reduceMemory(final ValueSpecification valueSpec, final ComputationTargetResolver.AtVersionCorrection resolver) {
    final ComputationTargetSpecification oldTargetSpec = valueSpec.getTargetSpecification();
    final ComputationTargetSpecification newTargetSpec = ComputationTargetResolverUtils.simplifyType(oldTargetSpec, resolver);
    if (newTargetSpec == oldTargetSpec) {
      return MemoryUtils.instance(valueSpec);
    } else {
      return MemoryUtils.instance(new ValueSpecification(valueSpec.getValueName(), newTargetSpec, valueSpec.getProperties()));
    }
  }

  private static Collection<ValueSpecification> reduceMemory(final Set<ValueSpecification> specifications, final ComputationTargetResolver.AtVersionCorrection resolver) {
    if (specifications.size() == 1) {
      final ValueSpecification specification = specifications.iterator().next();
      final ValueSpecification reducedSpecification = reduceMemory(specification, resolver);
      if (specification == reducedSpecification) {
        return specifications;
      } else {
        return Collections.singleton(reducedSpecification);
      }
    } else {
      final Collection<ValueSpecification> result = new ArrayList<ValueSpecification>(specifications.size());
      for (ValueSpecification specification : specifications) {
        result.add(reduceMemory(specification, resolver));
      }
      return result;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Iterator<Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>>> resolveFunction(final String valueName, final ComputationTarget target,
      final ValueProperties constraints) {
    final ComputationTargetResolver.AtVersionCorrection resolver = getFunctionCompilationContext().getComputationTargetResolver();
    // TODO [PLAT-2286] Don't key the cache by target specification as the contexts may vary. E.g. the (PORTFOLIO_NODE/POSITION, node0, pos0) target
    // will have considered all the rules for (POSITION, pos0). We want to share this, not duplicate the effort (and the storage)
    final ComputationTargetSpecification targetSpecification = MemoryUtils.instance(ComputationTargetResolverUtils.simplifyType(target.toSpecification(), resolver));
    Pair<ResolutionRule[], Collection<ValueSpecification>[]> cached = _targetCache.get(targetSpecification);
    if (cached == null) {
      int resolutions = 0;
      ResolutionRule[] resolutionRules = new ResolutionRule[_ruleCount];
      Collection<ValueSpecification>[] resolutionResults = new Collection[_ruleCount];
      final Iterable<Collection<ResolutionRule>> typeRules = _type2Rules.get(target.getType());
      if (typeRules != null) {
        try {
          final Map<ComputationTargetType, ComputationTarget> adjusted = new HashMap<ComputationTargetType, ComputationTarget>();
          for (Collection<ResolutionRule> rules : typeRules) {
            assert resolutions + rules.size() <= resolutionRules.length;
            for (ResolutionRule rule : rules) {
              final ComputationTarget adjustedTarget = rule.adjustTarget(adjusted, target);
              if (adjustedTarget != null) {
                final Set<ValueSpecification> results = rule.getResults(adjustedTarget, getFunctionCompilationContext());
                if ((results != null) && !results.isEmpty()) {
                  resolutionRules[resolutions] = rule;
                  resolutionResults[resolutions] = reduceMemory(results, resolver);
                  resolutions++;
                }
              }
            }
          }
        } catch (RuntimeException e) {
          s_logger.error("Couldn't process rules for {}: {}", target, e.getMessage());
          s_logger.info("Caught exception", e);
          // Now have an incomplete rule set for the target, possibly even an empty one
        }
      } else {
        s_logger.warn("No rules for target type {}", target);
      }
      // TODO: the array of rules is probably getting duplicated for each similar target (e.g. all swaps probably use the same rules)
      if (resolutions != resolutionRules.length) {
        resolutionRules = Arrays.copyOf(resolutionRules, resolutions);
        resolutionResults = Arrays.copyOf(resolutionResults, resolutions);
      }
      cached = (Pair<ResolutionRule[], Collection<ValueSpecification>[]>) (Pair<?, ?>) Pairs.of(resolutionRules, resolutionResults);
      final Pair<ResolutionRule[], Collection<ValueSpecification>[]> existing = _targetCache.putIfAbsent(targetSpecification, cached);
      if (existing != null) {
        cached = existing;
      }
    }
    return new It(valueName, targetSpecification, constraints, target, getFunctionCompilationContext(), cached);
  }

  /**
   * Iterator of functions and specifications from a dependency node.
   */
  private static final class It implements Iterator<Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>>> {

    private final FunctionCompilationContext _context;
    private final ComputationTargetSpecification _target;
    private final String _valueName;
    private final ValueProperties _constraints;
    private final Pair<ResolutionRule[], Collection<ValueSpecification>[]> _values;
    private int _itr;
    private Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>> _next;

    private It(final String valueName, final ComputationTargetSpecification targetSpecification, final ValueProperties constraints, final ComputationTarget target,
        final FunctionCompilationContext context, final Pair<ResolutionRule[], Collection<ValueSpecification>[]> values) {
      _context = context;
      _target = targetSpecification;
      _valueName = valueName;
      _constraints = constraints;
      _values = values;
      findNext(target);
    }

    private void findNext(final ComputationTarget target) {
      final ResolutionRule[] rules = _values.getFirst();
      final Collection<ValueSpecification>[] resultSets = _values.getSecond();
      final FunctionBlacklistQuery blacklist = _context.getGraphBuildingBlacklist();
      while (_itr < rules.length) {
        final ResolutionRule rule = rules[_itr];
        if (!blacklist.isBlacklisted(rule.getParameterizedFunction(), _target)) {
          final ComputationTarget adjustedTarget = rule.adjustTarget(target);
          if (adjustedTarget != null) {
            final Collection<ValueSpecification> resultSet = resultSets[_itr];
            final ValueSpecification result = rule.getResult(_valueName, adjustedTarget, _constraints, resultSet);
            if (result != null) {
              _next = Triple.of(rule.getParameterizedFunction(), result, resultSet);
              _itr++;
              return;
            }
          }
        }
        _itr++;
      }
      _next = null;
    }

    @Override
    public boolean hasNext() {
      if (_next == null) {
        findNext(_context.getComputationTargetResolver().resolve(_target));
      }
      return _next != null;
    }

    @Override
    public Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>> next() {
      if (_next == null) {
        findNext(_context.getComputationTargetResolver().resolve(_target));
      }
      Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>> next = _next;
      _next = null;
      return next;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public CompiledFunctionDefinition getFunction(final String uniqueId) {
    return _functions.get(uniqueId);
  }

}
