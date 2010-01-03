/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class ViewDeltaResultModelImpl implements ViewDeltaResultModel,
    Serializable {
  private long _inputDataTimestamp;
  private long _resultTimestamp;
  private long _previousResultTimestamp;
  private final Set<ComputationTargetSpecification> _allTargets = new HashSet<ComputationTargetSpecification>();
  private final Map<ComputationTargetSpecification, Set<ComputedValue>> _values =
    new HashMap<ComputationTargetSpecification, Set<ComputedValue>>();

  /**
   * @return the inputDataTimestamp
   */
  public long getInputDataTimestamp() {
    return _inputDataTimestamp;
  }

  /**
   * @param inputDataTimestamp the inputDataTimestamp to set
   */
  public void setInputDataTimestamp(long inputDataTimestamp) {
    _inputDataTimestamp = inputDataTimestamp;
  }

  /**
   * @return the resultTimestamp
   */
  public long getResultTimestamp() {
    return _resultTimestamp;
  }

  /**
   * @param resultTimestamp the resultTimestamp to set
   */
  public void setResultTimestamp(long resultTimestamp) {
    _resultTimestamp = resultTimestamp;
  }

  /**
   * @return the previousResultTimestamp
   */
  public long getPreviousResultTimestamp() {
    return _previousResultTimestamp;
  }

  /**
   * @param previousResultTimestamp the previousResultTimestamp to set
   */
  public void setPreviousResultTimestamp(long previousResultTimestamp) {
    _previousResultTimestamp = previousResultTimestamp;
  }
  
  public void addTarget(ComputationTargetSpecification targetSpecification) {
    ArgumentChecker.checkNotNull(targetSpecification, "Target Specification");
    _allTargets.add(targetSpecification);
    _values.put(targetSpecification, new HashSet<ComputedValue>());
  }
  
  public void addValue(ComputedValue value) {
    ComputationTargetSpecification targetSpecification = value.getSpecification().getRequirementSpecification().getTargetSpecification();
    addTarget(targetSpecification);
    Set<ComputedValue> values = _values.get(targetSpecification);
    assert values != null;
    values.add(value);
  }
  
  @Override
  public Collection<ComputationTargetSpecification> getAllTargets() {
    return Collections.unmodifiableSet(_allTargets);
  }

  @Override
  public Collection<ComputedValue> getDeltaValues(
      ComputationTargetSpecification target) {
    Set<ComputedValue> result = _values.get(target);
    if(result == null) {
      return Collections.emptySet();
    }
    return Collections.unmodifiableSet(result);
  }
}
