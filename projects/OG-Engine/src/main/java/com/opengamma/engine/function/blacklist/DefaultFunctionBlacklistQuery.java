/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Basic implementation of a blacklist based on a set of indexed rules.
 */
public class DefaultFunctionBlacklistQuery extends AbstractFunctionBlacklistQuery {

  private static final Logger s_logger = LoggerFactory.getLogger(DefaultFunctionBlacklistQuery.class);

  /**
   * Field used for the pivot in the tree used to index the rules.
   */
  /* package */enum PivotField {

    FUNCTION_IDENTIFIER(FUNCTION_IDENTIFIER_MASK),
    FUNCTION_PARAMETERS(FUNCTION_PARAMETERS_MASK),
    TARGET(TARGET_MASK),
    INPUTS(INPUTS_MASK),
    OUTPUTS(OUTPUTS_MASK);

    private int _mask;

    private PivotField(final int mask) {
      _mask = mask;
    }

    public int mask() {
      return _mask;
    }

  }

  private static final int FUNCTION_IDENTIFIER_MASK = 1;
  private static final int FUNCTION_PARAMETERS_MASK = 2;
  private static final int TARGET_MASK = 4;
  private static final int INPUTS_MASK = 8;
  private static final int OUTPUTS_MASK = 16;
  private static final int ALL_PIVOT_FIELDS = 31;

  private static PivotField nextPivotField(final int mask) {
    if ((mask & FUNCTION_IDENTIFIER_MASK) != 0) {
      return PivotField.FUNCTION_IDENTIFIER;
    } else if ((mask & FUNCTION_PARAMETERS_MASK) != 0) {
      return PivotField.FUNCTION_PARAMETERS;
    } else if ((mask & TARGET_MASK) != 0) {
      return PivotField.TARGET;
    } else if ((mask & INPUTS_MASK) != 0) {
      return PivotField.INPUTS;
    } else if ((mask & OUTPUTS_MASK) != 0) {
      return PivotField.OUTPUTS;
    } else {
      throw new IllegalStateException();
    }
  }

  // TODO: Partial/exact matching of inputs and outputs is not done properly here. We always do an exact match but warn when
  // a partial match rule is added to the query

  /* package */abstract static class TreeEntry {

    private final PivotField _pivot;

    public TreeEntry(final PivotField pivot) {
      _pivot = pivot;
    }

    protected PivotField getPivot() {
      return _pivot;
    }

    protected abstract boolean isWildcardBlacklisted(String functionIdentifier, FunctionParameters functionParameters);

    protected abstract boolean isFunctionIdentifierBlacklisted(String functionIdentifier, FunctionParameters functionParameters);

    protected abstract boolean isFunctionParametersBlacklisted(String functionIdentifier, FunctionParameters functionParameters);

    public static boolean isBlacklisted(final TreeEntry node, final String functionIdentifier, final FunctionParameters functionParameters) {
      return (node != null) && node.isBlacklisted(functionIdentifier, functionParameters);
    }

    protected boolean isBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters) {
      if (isWildcardBlacklisted(functionIdentifier, functionParameters)) {
        return true;
      }
      switch (getPivot()) {
        case FUNCTION_IDENTIFIER:
          return isFunctionIdentifierBlacklisted(functionIdentifier, functionParameters);
        case FUNCTION_PARAMETERS:
          return isFunctionParametersBlacklisted(functionIdentifier, functionParameters);
        default:
          return false;
      }
    }

    protected abstract boolean isWildcardBlacklisted(ComputationTargetSpecification target);

    protected abstract boolean isTargetBlacklisted(ComputationTargetSpecification target);

    public static boolean isBlacklisted(final TreeEntry node, final ComputationTargetSpecification target) {
      return (node != null) && node.isBlacklisted(target);
    }

    protected boolean isBlacklisted(final ComputationTargetSpecification target) {
      if (isWildcardBlacklisted(target)) {
        return true;
      }
      if (getPivot() == PivotField.TARGET) {
        return isTargetBlacklisted(target);
      }
      return false;
    }

    protected abstract boolean isWildcardBlacklisted(String functionIdentifier, FunctionParameters functionParameters, ComputationTargetSpecification target);

    protected abstract boolean isFunctionIdentifierBlacklisted(String functionIdentifier, FunctionParameters functionParameters, ComputationTargetSpecification target);

    protected abstract boolean isFunctionParametersBlacklisted(String functionIdentifier, FunctionParameters functionParameters, ComputationTargetSpecification target);

    protected abstract boolean isTargetBlacklisted(String functionIdentifier, FunctionParameters functionParameters, ComputationTargetSpecification target);

    public static boolean isBlacklisted(final TreeEntry node, final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target) {
      return (node != null) && node.isBlacklisted(functionIdentifier, functionParameters, target);
    }

    protected boolean isBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target) {
      if (isWildcardBlacklisted(functionIdentifier, functionParameters, target)) {
        return true;
      }
      switch (getPivot()) {
        case FUNCTION_IDENTIFIER:
          return isFunctionIdentifierBlacklisted(functionIdentifier, functionParameters, target);
        case FUNCTION_PARAMETERS:
          return isFunctionParametersBlacklisted(functionIdentifier, functionParameters, target);
        case TARGET:
          return isTargetBlacklisted(functionIdentifier, functionParameters, target);
        default:
          return false;
      }
    }

    protected abstract boolean isWildcardBlacklisted(String functionIdentifier, FunctionParameters functionParameters, ComputationTargetSpecification target, ValueSpecification[] inputs,
        ValueSpecification[] outputs);

    protected abstract boolean isWildcardBlacklisted(String functionIdentifier, FunctionParameters functionParameters, ComputationTargetSpecification target, Collection<ValueSpecification> inputs,
        Collection<ValueSpecification> outputs);

    protected abstract boolean isFunctionIdentifierBlacklisted(String functionIdentifier, FunctionParameters functionParameters, ComputationTargetSpecification target,
        ValueSpecification[] inputs, ValueSpecification[] outputs);

    protected abstract boolean isFunctionIdentifierBlacklisted(String functionIdentifier, FunctionParameters functionParameters, ComputationTargetSpecification target,
        Collection<ValueSpecification> inputs, Collection<ValueSpecification> outputs);

    protected abstract boolean isFunctionParametersBlacklisted(String functionIdentifier, FunctionParameters functionParameters, ComputationTargetSpecification target,
        ValueSpecification[] inputs, ValueSpecification[] outputs);

    protected abstract boolean isFunctionParametersBlacklisted(String functionIdentifier, FunctionParameters functionParameters, ComputationTargetSpecification target,
        Collection<ValueSpecification> inputs, Collection<ValueSpecification> outputs);

    protected abstract boolean isTargetBlacklisted(String functionIdentifier, FunctionParameters functionParameters, ComputationTargetSpecification target, ValueSpecification[] inputs,
        ValueSpecification[] outputs);

    protected abstract boolean isTargetBlacklisted(String functionIdentifier, FunctionParameters functionParameters, ComputationTargetSpecification target, Collection<ValueSpecification> inputs,
        Collection<ValueSpecification> outputs);

    protected abstract boolean isInputsBlacklisted(String functionIdentifier, FunctionParameters functionParameters, ComputationTargetSpecification target, ValueSpecification[] inputs,
        ValueSpecification[] outputs);

    protected abstract boolean isInputsBlacklisted(String functionIdentifier, FunctionParameters functionParameters, ComputationTargetSpecification target, Collection<ValueSpecification> inputs,
        Collection<ValueSpecification> outputs);

    protected abstract boolean isOutputsBlacklisted(String functionIdentifier, FunctionParameters functionParameters, ComputationTargetSpecification target, ValueSpecification[] inputs,
        ValueSpecification[] outputs);

    protected abstract boolean isOutputsBlacklisted(String functionIdentifier, FunctionParameters functionParameters, ComputationTargetSpecification target, Collection<ValueSpecification> inputs,
        Collection<ValueSpecification> outputs);

    public static boolean isBlacklisted(final TreeEntry node, final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target,
        final ValueSpecification[] inputs, final ValueSpecification[] outputs) {
      return (node != null) && node.isBlacklisted(functionIdentifier, functionParameters, target, inputs, outputs);
    }

    public static boolean isBlacklisted(final TreeEntry node, final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target,
        final Collection<ValueSpecification> inputs, final Collection<ValueSpecification> outputs) {
      return (node != null) && node.isBlacklisted(functionIdentifier, functionParameters, target, inputs, outputs);
    }

    protected boolean isBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target,
        final ValueSpecification[] inputs, final ValueSpecification[] outputs) {
      if (isWildcardBlacklisted(functionIdentifier, functionParameters, target, inputs, outputs)) {
        return true;
      }
      switch (getPivot()) {
        case FUNCTION_IDENTIFIER:
          return isFunctionIdentifierBlacklisted(functionIdentifier, functionParameters, target, inputs, outputs);
        case FUNCTION_PARAMETERS:
          return isFunctionParametersBlacklisted(functionIdentifier, functionParameters, target, inputs, outputs);
        case TARGET:
          return isTargetBlacklisted(functionIdentifier, functionParameters, target, inputs, outputs);
        case INPUTS:
          return isInputsBlacklisted(functionIdentifier, functionParameters, target, inputs, outputs);
        case OUTPUTS:
          return isOutputsBlacklisted(functionIdentifier, functionParameters, target, inputs, outputs);
        default:
          throw new IllegalStateException();
      }
    }

    protected boolean isBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target,
        final Collection<ValueSpecification> inputs, final Collection<ValueSpecification> outputs) {
      if (isWildcardBlacklisted(functionIdentifier, functionParameters, target, inputs, outputs)) {
        return true;
      }
      switch (getPivot()) {
        case FUNCTION_IDENTIFIER:
          return isFunctionIdentifierBlacklisted(functionIdentifier, functionParameters, target, inputs, outputs);
        case FUNCTION_PARAMETERS:
          return isFunctionParametersBlacklisted(functionIdentifier, functionParameters, target, inputs, outputs);
        case TARGET:
          return isTargetBlacklisted(functionIdentifier, functionParameters, target, inputs, outputs);
        case INPUTS:
          return isInputsBlacklisted(functionIdentifier, functionParameters, target, inputs, outputs);
        case OUTPUTS:
          return isOutputsBlacklisted(functionIdentifier, functionParameters, target, inputs, outputs);
        default:
          throw new IllegalStateException();
      }
    }

    protected abstract void addEntry(FunctionBlacklistRule rule, Object key, int unpivoted);

    protected abstract void addWildcard(FunctionBlacklistRule rule, int unpivoted);

    public void add(final FunctionBlacklistRule rule, final int unpivoted) {
      assert (unpivoted & getPivot().mask()) != 0;
      switch (getPivot()) {
        case FUNCTION_IDENTIFIER:
          if (rule.getFunctionIdentifier() != null) {
            addEntry(rule, rule.getFunctionIdentifier(), unpivoted ^ FUNCTION_IDENTIFIER_MASK);
          } else {
            addWildcard(rule, unpivoted ^ FUNCTION_IDENTIFIER_MASK);
          }
          break;
        case FUNCTION_PARAMETERS:
          if (rule.getFunctionParameters() != null) {
            addEntry(rule, rule.getFunctionParameters(), unpivoted ^ FUNCTION_PARAMETERS_MASK);
          } else {
            addWildcard(rule, unpivoted ^ FUNCTION_PARAMETERS_MASK);
          }
          break;
        case TARGET:
          if (rule.getTarget() != null) {
            addEntry(rule, rule.getTarget(), unpivoted ^ TARGET_MASK);
          } else {
            addWildcard(rule, unpivoted ^ TARGET_MASK);
          }
          break;
        case INPUTS:
          if (rule.getInputs() != null) {
            addEntry(rule, rule.getInputs(), unpivoted ^ INPUTS_MASK);
          } else {
            addWildcard(rule, unpivoted ^ INPUTS_MASK);
          }
          break;
        case OUTPUTS:
          if (rule.getOutputs() != null) {
            addEntry(rule, rule.getOutputs(), unpivoted ^ OUTPUTS_MASK);
          } else {
            addWildcard(rule, unpivoted ^ OUTPUTS_MASK);
          }
          break;
        default:
          throw new IllegalStateException();
      }
    }

    protected abstract boolean removeEntry(FunctionBlacklistRule rule, Object key);

    protected abstract boolean removeWildcard(FunctionBlacklistRule rule);

    /**
     * Removes the rule from the tree.
     * 
     * @param rule the rule to remove
     * @return true if the tree is non-empty, false if the tree fragment is empty after the removal
     */
    public boolean remove(final FunctionBlacklistRule rule) {
      switch (getPivot()) {
        case FUNCTION_IDENTIFIER:
          if (rule.getFunctionIdentifier() != null) {
            return removeEntry(rule, rule.getFunctionIdentifier());
          } else {
            return removeWildcard(rule);
          }
        case FUNCTION_PARAMETERS:
          if (rule.getFunctionParameters() != null) {
            return removeEntry(rule, rule.getFunctionParameters());
          } else {
            return removeWildcard(rule);
          }
        case TARGET:
          if (rule.getTarget() != null) {
            return removeEntry(rule, rule.getTarget());
          } else {
            return removeWildcard(rule);
          }
        case INPUTS:
          if (rule.getInputs() != null) {
            return removeEntry(rule, rule.getInputs());
          } else {
            return removeWildcard(rule);
          }
        case OUTPUTS:
          if (rule.getOutputs() != null) {
            return removeEntry(rule, rule.getOutputs());
          } else {
            return removeWildcard(rule);
          }
        default:
          throw new IllegalStateException();
      }
    }

  }

  /* package */static class MidTreeEntry extends TreeEntry {

    private volatile TreeEntry _wildcard;
    private volatile Map<Object, TreeEntry> _values;

    public MidTreeEntry(final PivotField pivot) {
      super(pivot);
    }

    @Override
    protected boolean isWildcardBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters) {
      return isBlacklisted(_wildcard, functionIdentifier, functionParameters);
    }

    @Override
    protected boolean isFunctionIdentifierBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters) {
      final Map<Object, TreeEntry> values = _values;
      return (values != null) && isBlacklisted(values.get(functionIdentifier), functionIdentifier, functionParameters);
    }

    @Override
    protected boolean isFunctionParametersBlacklisted(String functionIdentifier, final FunctionParameters functionParameters) {
      final Map<Object, TreeEntry> values = _values;
      return (values != null) && isBlacklisted(values.get(functionParameters), functionIdentifier, functionParameters);
    }

    @Override
    protected boolean isWildcardBlacklisted(final ComputationTargetSpecification target) {
      return isBlacklisted(_wildcard, target);
    }

    @Override
    protected boolean isTargetBlacklisted(final ComputationTargetSpecification target) {
      final Map<Object, TreeEntry> values = _values;
      return (values != null) && isBlacklisted(values.get(target), target);
    }

    @Override
    protected boolean isWildcardBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target) {
      return isBlacklisted(_wildcard, functionIdentifier, functionParameters, target);
    }

    @Override
    protected boolean isFunctionIdentifierBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target) {
      final Map<Object, TreeEntry> values = _values;
      return (values != null) && isBlacklisted(values.get(functionIdentifier), functionIdentifier, functionParameters, target);
    }

    @Override
    protected boolean isFunctionParametersBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target) {
      final Map<Object, TreeEntry> values = _values;
      return (values != null) && isBlacklisted(values.get(functionParameters), functionIdentifier, functionParameters, target);
    }

    @Override
    protected boolean isTargetBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target) {
      final Map<Object, TreeEntry> values = _values;
      return (values != null) && isBlacklisted(values.get(target), functionIdentifier, functionParameters, target);
    }

    @Override
    protected boolean isWildcardBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target,
        final ValueSpecification[] inputs, final ValueSpecification[] outputs) {
      return isBlacklisted(_wildcard, functionIdentifier, functionParameters, target, inputs, outputs);
    }

    @Override
    protected boolean isWildcardBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target,
        final Collection<ValueSpecification> inputs, final Collection<ValueSpecification> outputs) {
      return isBlacklisted(_wildcard, functionIdentifier, functionParameters, target, inputs, outputs);
    }

    @Override
    protected boolean isFunctionIdentifierBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target,
        final ValueSpecification[] inputs, final ValueSpecification[] outputs) {
      final Map<Object, TreeEntry> values = _values;
      return (values != null) && isBlacklisted(values.get(functionIdentifier), functionIdentifier, functionParameters, target, inputs, outputs);
    }

    @Override
    protected boolean isFunctionIdentifierBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target,
        final Collection<ValueSpecification> inputs, final Collection<ValueSpecification> outputs) {
      final Map<Object, TreeEntry> values = _values;
      return (values != null) && isBlacklisted(values.get(functionIdentifier), functionIdentifier, functionParameters, target, inputs, outputs);
    }

    @Override
    protected boolean isFunctionParametersBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target,
        final ValueSpecification[] inputs, final ValueSpecification[] outputs) {
      final Map<Object, TreeEntry> values = _values;
      return (values != null) && isBlacklisted(values.get(functionParameters), functionIdentifier, functionParameters, target, inputs, outputs);
    }

    @Override
    protected boolean isFunctionParametersBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target,
        final Collection<ValueSpecification> inputs, final Collection<ValueSpecification> outputs) {
      final Map<Object, TreeEntry> values = _values;
      return (values != null) && isBlacklisted(values.get(functionParameters), functionIdentifier, functionParameters, target, inputs, outputs);
    }

    @Override
    protected boolean isTargetBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target,
        final ValueSpecification[] inputs, final ValueSpecification[] outputs) {
      final Map<Object, TreeEntry> values = _values;
      return (values != null) && isBlacklisted(values.get(target), functionIdentifier, functionParameters, target, inputs, outputs);
    }

    @Override
    protected boolean isTargetBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target,
        final Collection<ValueSpecification> inputs, final Collection<ValueSpecification> outputs) {
      final Map<Object, TreeEntry> values = _values;
      return (values != null) && isBlacklisted(values.get(target), functionIdentifier, functionParameters, target, inputs, outputs);
    }

    @Override
    protected boolean isInputsBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target,
        final ValueSpecification[] inputs, final ValueSpecification[] outputs) {
      final Map<Object, TreeEntry> values = _values;
      return (values != null) && isBlacklisted(values.get(inputs), functionIdentifier, functionParameters, target, inputs, outputs);
    }

    @Override
    protected boolean isInputsBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target,
        final Collection<ValueSpecification> inputs, final Collection<ValueSpecification> outputs) {
      final Map<Object, TreeEntry> values = _values;
      return (values != null) && isBlacklisted(values.get(inputs), functionIdentifier, functionParameters, target, inputs, outputs);
    }

    @Override
    protected boolean isOutputsBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target,
        final ValueSpecification[] inputs, final ValueSpecification[] outputs) {
      final Map<Object, TreeEntry> values = _values;
      return (values != null) && isBlacklisted(values.get(outputs), functionIdentifier, functionParameters, target, inputs, outputs);
    }

    @Override
    protected boolean isOutputsBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target,
        final Collection<ValueSpecification> inputs, final Collection<ValueSpecification> outputs) {
      final Map<Object, TreeEntry> values = _values;
      return (values != null) && isBlacklisted(values.get(outputs), functionIdentifier, functionParameters, target, inputs, outputs);
    }

    private TreeEntry createTreeEntry(final int unpivoted) {
      final PivotField pivot = nextPivotField(unpivoted);
      if (unpivoted == pivot.mask()) {
        return new LeafTreeEntry(pivot);
      } else {
        return new MidTreeEntry(pivot);
      }
    }

    @Override
    protected void addEntry(final FunctionBlacklistRule rule, final Object key, final int unpivoted) {
      Map<Object, TreeEntry> values = _values;
      TreeEntry next;
      if (values == null) {
        next = createTreeEntry(unpivoted);
        _values = Collections.singletonMap(key, next);
      } else {
        next = values.get(key);
        if (next == null) {
          next = createTreeEntry(unpivoted);
          values = new HashMap<Object, TreeEntry>(values);
          values.put(key, next);
          _values = values;
        }
      }
      next.add(rule, unpivoted);
    }

    @Override
    protected void addWildcard(final FunctionBlacklistRule rule, final int unpivoted) {
      TreeEntry wildcard = _wildcard;
      if (wildcard == null) {
        wildcard = createTreeEntry(unpivoted);
        _wildcard = wildcard;
      }
      wildcard.add(rule, unpivoted);
    }

    @Override
    protected boolean removeEntry(final FunctionBlacklistRule rule, final Object key) {
      Map<Object, TreeEntry> values = _values;
      if (values != null) {
        TreeEntry next = values.get(key);
        if (next != null) {
          if (next.remove(rule)) {
            return true;
          } else {
            values = new HashMap<Object, TreeEntry>(values);
            values.remove(key);
            _values = values;
            return (_wildcard != null) || !values.isEmpty();
          }
        } else {
          throw new IllegalStateException("Rule " + rule + " not in the collection");
        }
      } else {
        throw new IllegalStateException("Rule " + rule + " not in the collection");
      }
    }

    @Override
    protected boolean removeWildcard(final FunctionBlacklistRule rule) {
      final TreeEntry wildcard = _wildcard;
      if (wildcard != null) {
        if (wildcard.remove(rule)) {
          return true;
        } else {
          _wildcard = null;
          final Map<Object, TreeEntry> values = _values;
          return (values != null) && !values.isEmpty();
        }
      } else {
        throw new IllegalStateException("Rule " + rule + " not in the collection");
      }
    }

  }

  /* package */static class LeafTreeEntry extends TreeEntry {

    private volatile int _wildcard;
    private volatile Set<Object> _values;

    public LeafTreeEntry(final PivotField pivot) {
      super(pivot);
    }

    @Override
    protected boolean isWildcardBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters) {
      return _wildcard > 0;
    }

    @Override
    protected boolean isFunctionIdentifierBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters) {
      final Set<Object> values = _values;
      return (values != null) && values.contains(functionIdentifier);
    }

    @Override
    protected boolean isFunctionParametersBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters) {
      final Set<Object> values = _values;
      return (values != null) && values.contains(functionParameters);
    }

    @Override
    protected boolean isWildcardBlacklisted(final ComputationTargetSpecification target) {
      return _wildcard > 0;
    }

    @Override
    protected boolean isTargetBlacklisted(final ComputationTargetSpecification target) {
      final Set<Object> values = _values;
      return (values != null) && values.contains(target);
    }

    @Override
    protected boolean isWildcardBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target) {
      return _wildcard > 0;
    }

    @Override
    protected boolean isFunctionIdentifierBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target) {
      final Set<Object> values = _values;
      return (values != null) && values.contains(functionIdentifier);
    }

    @Override
    protected boolean isFunctionParametersBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target) {
      final Set<Object> values = _values;
      return (values != null) && values.contains(functionParameters);
    }

    @Override
    protected boolean isTargetBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target) {
      final Set<Object> values = _values;
      return (values != null) && values.contains(target);
    }

    @Override
    protected boolean isWildcardBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target,
        final ValueSpecification[] inputs, final ValueSpecification[] outputs) {
      return _wildcard > 0;
    }

    @Override
    protected boolean isWildcardBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target,
        final Collection<ValueSpecification> inputs, final Collection<ValueSpecification> outputs) {
      return _wildcard > 0;
    }

    @Override
    protected boolean isFunctionIdentifierBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target,
        final ValueSpecification[] inputs, final ValueSpecification[] outputs) {
      final Set<Object> values = _values;
      return (values != null) && values.contains(functionIdentifier);
    }

    @Override
    protected boolean isFunctionIdentifierBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target,
        final Collection<ValueSpecification> inputs, final Collection<ValueSpecification> outputs) {
      final Set<Object> values = _values;
      return (values != null) && values.contains(functionIdentifier);
    }

    @Override
    protected boolean isFunctionParametersBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target,
        final ValueSpecification[] inputs, final ValueSpecification[] outputs) {
      final Set<Object> values = _values;
      return (values != null) && values.contains(functionParameters);
    }

    @Override
    protected boolean isFunctionParametersBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target,
        final Collection<ValueSpecification> inputs, final Collection<ValueSpecification> outputs) {
      final Set<Object> values = _values;
      return (values != null) && values.contains(functionParameters);
    }

    @Override
    protected boolean isTargetBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target,
        final ValueSpecification[] inputs, final ValueSpecification[] outputs) {
      final Set<Object> values = _values;
      return (values != null) && values.contains(target);
    }

    @Override
    protected boolean isTargetBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target,
        final Collection<ValueSpecification> inputs, final Collection<ValueSpecification> outputs) {
      final Set<Object> values = _values;
      return (values != null) && values.contains(target);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean isInputsBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target,
        final ValueSpecification[] inputs, final ValueSpecification[] outputs) {
      final Set<Object> values = _values;
      if (values == null) {
        return false;
      }
      for (Object value : values) {
        final Collection<ValueSpecification> valueCollection = (Collection<ValueSpecification>) value;
        if (valueCollection.size() == inputs.length) {
          boolean match = true;
          for (ValueSpecification input : inputs) {
            if (!((Collection<ValueSpecification>) value).contains(input)) {
              match = false;
              break;
            }
          }
          if (match) {
            return true;
          }
        }
      }
      return false;
    }

    @Override
    protected boolean isInputsBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target,
        final Collection<ValueSpecification> inputs, final Collection<ValueSpecification> outputs) {
      final Set<Object> values = _values;
      return (values != null) && values.contains(inputs);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean isOutputsBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target,
        final ValueSpecification[] inputs, final ValueSpecification[] outputs) {
      final Set<Object> values = _values;
      if (values == null) {
        return false;
      }
      for (Object value : values) {
        final Collection<ValueSpecification> valueCollection = (Collection<ValueSpecification>) value;
        if (valueCollection.size() == outputs.length) {
          boolean match = true;
          for (ValueSpecification output : outputs) {
            if (!((Collection<ValueSpecification>) value).contains(output)) {
              match = false;
              break;
            }
          }
          if (match) {
            return true;
          }
        }
      }
      return false;
    }

    @Override
    protected boolean isOutputsBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target,
        final Collection<ValueSpecification> inputs, final Collection<ValueSpecification> outputs) {
      final Set<Object> values = _values;
      return (values != null) && values.contains(outputs);
    }

    @Override
    protected void addEntry(final FunctionBlacklistRule rule, final Object key, final int unpivoted) {
      Set<Object> values = _values;
      if (values == null) {
        values = Collections.singleton(key);
        _values = values;
      } else {
        values = new HashSet<Object>(values);
        values.add(key);
        _values = values;
      }
    }

    @Override
    protected void addWildcard(final FunctionBlacklistRule rule, final int unpivoted) {
      _wildcard++;
    }

    @Override
    protected boolean removeEntry(final FunctionBlacklistRule rule, final Object key) {
      Set<Object> values = _values;
      if (values != null) {
        values = new HashSet<Object>(values);
        if (values.remove(key)) {
          _values = values;
          return (_wildcard > 0) || !values.isEmpty();
        } else {
          throw new IllegalStateException("Rule " + rule + " not in the collection");
        }
      } else {
        throw new IllegalStateException("Rule " + rule + " not in the collection");
      }
    }

    @Override
    protected boolean removeWildcard(final FunctionBlacklistRule rule) {
      final int wildcard = _wildcard;
      if (wildcard > 0) {
        _wildcard = wildcard - 1;
        final Set<Object> values = _values;
        return (wildcard > 1) || ((values != null) && !values.isEmpty());
      } else {
        throw new IllegalStateException("Rule " + rule + " not in the collection");
      }
    }

  }

  private static final class Listener extends AbstractFunctionBlacklistRuleListener {

    private final WeakReference<DefaultFunctionBlacklistQuery> _ref;

    public Listener(final FunctionBlacklist blacklist, final DefaultFunctionBlacklistQuery ref) {
      super(blacklist);
      _ref = new WeakReference<DefaultFunctionBlacklistQuery>(ref);
    }

    @Override
    protected void init() {
      super.init();
      getBlacklist().addRuleListener(this);
      refresh();
    }

    private DefaultFunctionBlacklistQuery getReference() {
      final DefaultFunctionBlacklistQuery ref = _ref.get();
      if (ref == null) {
        getBlacklist().removeRuleListener(this);
      }
      return ref;
    }

    @Override
    protected void replaceRules(final Collection<FunctionBlacklistRule> rules) {
      final DefaultFunctionBlacklistQuery ref = getReference();
      if (ref != null) {
        ref.replaceRules(rules);
      }
    }

    @Override
    protected void addRule(final FunctionBlacklistRule rule) {
      final DefaultFunctionBlacklistQuery ref = getReference();
      if (ref != null) {
        ref.addRule(rule);
      }
    }

    @Override
    protected void addRules(final Collection<FunctionBlacklistRule> rules) {
      final DefaultFunctionBlacklistQuery ref = getReference();
      if (ref != null) {
        ref.addRules(rules);
      }
    }

    @Override
    protected void removeRule(final FunctionBlacklistRule rule) {
      final DefaultFunctionBlacklistQuery ref = getReference();
      if (ref != null) {
        ref.removeRule(rule);
      }
    }

    @Override
    protected void removeRules(final Collection<FunctionBlacklistRule> rules) {
      final DefaultFunctionBlacklistQuery ref = getReference();
      if (ref != null) {
        ref.removeRules(rules);
      }
    }

  }

  private final Set<FunctionBlacklistRule> _rules = new HashSet<FunctionBlacklistRule>();
  private volatile TreeEntry _root;

  public DefaultFunctionBlacklistQuery(final FunctionBlacklist blacklist) {
    (new Listener(blacklist, this)).init();
  }

  @Override
  public boolean isEmpty() {
    return _root == null;
  }

  @Override
  public boolean isBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters) {
    return TreeEntry.isBlacklisted(_root, functionIdentifier, functionParameters);
  }

  @Override
  public boolean isBlacklisted(final ComputationTargetSpecification target) {
    return TreeEntry.isBlacklisted(_root, target);
  }

  @Override
  public boolean isBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target) {
    return TreeEntry.isBlacklisted(_root, functionIdentifier, functionParameters, target);
  }

  @Override
  public boolean isBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target, final ValueSpecification[] inputs,
      final ValueSpecification[] outputs) {
    return TreeEntry.isBlacklisted(_root, functionIdentifier, functionParameters, target, inputs, outputs);
  }

  @Override
  public boolean isBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target, final Collection<ValueSpecification> inputs,
      final Collection<ValueSpecification> outputs) {
    return TreeEntry.isBlacklisted(_root, functionIdentifier, functionParameters, target, inputs, outputs);
  }

  protected synchronized void addRule(final FunctionBlacklistRule rule) {
    if (_rules.add(rule)) {
      addRuleImpl(rule);
    }
  }

  protected synchronized void addRules(final Collection<FunctionBlacklistRule> rules) {
    for (FunctionBlacklistRule rule : rules) {
      if (_rules.add(rule)) {
        addRuleImpl(rule);
      }
    }
  }

  protected synchronized void removeRule(final FunctionBlacklistRule rule) {
    if (_rules.remove(rule)) {
      removeRuleImpl(rule);
    }
  }

  protected synchronized void removeRules(final Collection<FunctionBlacklistRule> rules) {
    for (FunctionBlacklistRule rule : rules) {
      if (_rules.remove(rule)) {
        removeRuleImpl(rule);
      }
    }
  }

  protected synchronized void replaceRules(final Collection<FunctionBlacklistRule> rules) {
    _rules.clear();
    _root = null;
    for (FunctionBlacklistRule rule : rules) {
      if (_rules.add(rule)) {
        addRuleImpl(rule);
      }
    }
  }

  /**
   * Adds a rule to the collection used. The content of the rule is copied - no reference to the rule is retained. The caller must hold the monitor.
   * 
   * @param rule the rule to add, not null
   */
  private void addRuleImpl(final FunctionBlacklistRule rule) {
    if (((rule.getInputs() != null) && !rule.isInputsExactMatch()) || ((rule.getOutputs() != null) && !rule.isOutputsExactMatch())) {
      s_logger.warn("Rule {} specifies partial input/output match", rule);
    }
    if (_root == null) {
      _root = new MidTreeEntry(PivotField.FUNCTION_IDENTIFIER);
    }
    _root.add(rule, ALL_PIVOT_FIELDS);
  }

  // TODO: Sometimes might want to reorder the pivots. Would we want "cheapest" pivot first, the one with the most choices or the one with the fewest choices? The aim is
  // to get a "FALSE" as quickly and cheaply as possible as that is the typical case.

  /**
   * Removes a rule from the collection used. The rule must exist in the collection. The caller must hold the monitor.
   * 
   * @param rule the rule to remove, not null
   */
  private void removeRuleImpl(final FunctionBlacklistRule rule) {
    if (!_root.remove(rule)) {
      _root = null;
    }
  }

}
