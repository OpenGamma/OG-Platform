/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Basic rule for blacklisting a function.
 */
public final class FunctionBlacklistRule {

  private String _functionIdentifier;
  private FunctionParameters _functionParameters;
  private ComputationTargetSpecification _target;
  private Set<ValueSpecification> _inputs;
  private boolean _inputsExactMatch = true;
  private Set<ValueSpecification> _outputs;
  private boolean _outputsExactMatch = true;

  public FunctionBlacklistRule() {
  }

  public FunctionBlacklistRule(final ParameterizedFunction function) {
    setFunction(function);
  }

  public FunctionBlacklistRule(final ComputationTargetSpecification target) {
    setTarget(target);
  }

  public FunctionBlacklistRule(final ParameterizedFunction function, final ComputationTargetSpecification target) {
    setFunction(function);
    setTarget(target);
  }

  public FunctionBlacklistRule(final ParameterizedFunction function, final ComputationTargetSpecification target, final Set<ValueSpecification> inputs, final Set<ValueSpecification> outputs) {
    setFunction(function);
    setTarget(target);
    setInputs(inputs);
    setOutputs(outputs);
  }

  public String getFunctionIdentifier() {
    return _functionIdentifier;
  }

  public void setFunctionIdentifier(final String functionIdentifier) {
    _functionIdentifier = functionIdentifier;
  }

  public FunctionParameters getFunctionParameters() {
    return _functionParameters;
  }

  public void setFunctionParameters(final FunctionParameters functionParameters) {
    _functionParameters = functionParameters;
  }

  public void setFunction(final ParameterizedFunction function) {
    if (function != null) {
      setFunctionIdentifier(function.getFunction().getFunctionDefinition().getUniqueId());
      setFunctionParameters(function.getParameters());
    } else {
      setFunctionIdentifier(null);
      setFunctionParameters(null);
    }
  }

  public ComputationTargetSpecification getTarget() {
    return _target;
  }

  public void setTarget(final ComputationTargetSpecification target) {
    _target = target;
  }

  public void setInputs(final Collection<ValueSpecification> inputs) {
    if (inputs != null) {
      _inputs = Collections.unmodifiableSet(new HashSet<ValueSpecification>(inputs));
    } else {
      _inputs = null;
    }
  }

  public Set<ValueSpecification> getInputs() {
    return _inputs;
  }

  public void setInputsExactMatch(final boolean exactMatch) {
    _inputsExactMatch = exactMatch;
  }

  public boolean isInputsExactMatch() {
    return _inputsExactMatch;
  }

  public void setOutputs(final Collection<ValueSpecification> outputs) {
    if (outputs != null) {
      _outputs = Collections.unmodifiableSet(new HashSet<ValueSpecification>(outputs));
    } else {
      _outputs = null;
    }
  }

  public Set<ValueSpecification> getOutputs() {
    return _outputs;
  }

  public void setOutputsExactMatch(final boolean exactMatch) {
    _outputsExactMatch = exactMatch;
  }

  public boolean isOutputsExactMatch() {
    return _outputsExactMatch;
  }

  @Override
  public int hashCode() {
    int hc = 1;
    hc += hc * 16 + ObjectUtils.hashCode(_functionIdentifier);
    hc += hc * 16 + ObjectUtils.hashCode(_functionParameters);
    hc += hc * 16 + ObjectUtils.hashCode(_target);
    hc += hc * 16 + ObjectUtils.hashCode(_inputs);
    hc += hc * 16 + (_inputsExactMatch ? 1 : 0);
    hc += hc * 16 + ObjectUtils.hashCode(_outputs);
    hc += hc * 16 + (_outputsExactMatch ? 1 : 0);
    return hc;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof FunctionBlacklistRule)) {
      return false;
    }
    final FunctionBlacklistRule other = (FunctionBlacklistRule) o;
    return ObjectUtils.equals(_functionIdentifier, other._functionIdentifier)
        && ObjectUtils.equals(_functionParameters, other._functionParameters)
        && ObjectUtils.equals(_target, other._target)
        && ObjectUtils.equals(_inputs, other._inputs)
        && (_inputsExactMatch == other._inputsExactMatch)
        && ObjectUtils.equals(_outputs, other._outputs)
        && (_outputsExactMatch == other._outputsExactMatch);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("FunctionBlacklistRule[");
    boolean comma = false;
    if (getFunctionIdentifier() != null) {
      sb.append("functionIdentifier=").append(getFunctionIdentifier());
      comma = true;
    }
    if (getFunctionParameters() != null) {
      if (comma) {
        sb.append(',');
      }
      sb.append("functionParameters=").append(getFunctionParameters());
      comma = true;
    }
    if (getTarget() != null) {
      if (comma) {
        sb.append(',');
      }
      sb.append("target=").append(getTarget());
      comma = true;
    }
    if (getInputs() != null) {
      if (comma) {
        sb.append(',');
      }
      sb.append("inputs=").append(getInputs());
      if (!isInputsExactMatch()) {
        sb.append(" (partial match)");
      }
      comma = true;
    }
    if (getOutputs() != null) {
      if (comma) {
        sb.append(',');
      }
      sb.append("outputs=").append(getOutputs());
      if (!isOutputsExactMatch()) {
        sb.append(" (partial match)");
      }
    }
    return sb.append(']').toString();
  }

}
