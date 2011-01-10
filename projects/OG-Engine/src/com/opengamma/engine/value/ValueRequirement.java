/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import java.io.Serializable;
import java.util.Set;

import org.apache.commons.lang.text.StrBuilder;

import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * An immutable requirement to obtain a value needed to perform a calculation.
 * <p>
 * This is a metadata-based requirement, and specifies only the minimal number of
 * parameters that are necessary to specify the user requirements.
 * <p>
 * The actual value which is computed is available as a {@link ValueSpecification}
 * that is capable of satisfying this requirement. A specification satisfies a requirement
 * if its properties satisfy the requirement constraints, plus the value name and
 * target specifications match.
 * <p>
 * This class is immutable and thread-safe.
 */
@PublicAPI
public final class ValueRequirement implements Serializable {

  /**
   * The name of the value being requested.
   */
  private final String _valueName;
  /**
   * The specification of the object that the value refers to.
   */
  private final ComputationTargetSpecification _targetSpecification;
  /**
   * The constraints or additional parameters about the target.
   * For example, a currency constraint.
   */
  private final ValueProperties _constraints;

  /**
   * Creates a requirement with no value constraints.
   * <p>
   * This builds a {@link ComputationTargetSpecification} from the target type and id.
   * 
   * @param valueName  the value to load, not null
   * @param targetType  the target type, not null
   * @param targetId  the target identifier, may be null
   */
  public ValueRequirement(String valueName, ComputationTargetType targetType, UniqueIdentifier targetId) {
    this(valueName, new ComputationTargetSpecification(targetType, targetId));
  }

  /**
   * Creates a requirement with value constraints.
   * <p>
   * This builds a {@link ComputationTargetSpecification} from the target type and id.
   * 
   * @param valueName  the name of the value to load, not null
   * @param targetType  the target type, not null
   * @param targetId  the unique identifier of the target, not null
   * @param constraints  the value constraints that must be satisfied
   */
  public ValueRequirement(String valueName, ComputationTargetType targetType, UniqueIdentifier targetId, ValueProperties constraints) {
    this(valueName, new ComputationTargetSpecification(targetType, targetId), constraints);
  }

  /**
   * Creates a requirement from a target object with no value constraints.
   * <p>
   * This builds a {@link ComputationTargetSpecification} from the target.
   * Example objects are {@link Position} and {@link Security}.
   * 
   * @param valueName  the value to load, not null
   * @param target  the target object, may be null
   */
  public ValueRequirement(String valueName, Object target) {
    this(valueName, new ComputationTargetSpecification(target));
  }

  /**
   * Creates a requirement from a target object with value constraints.
   * <p>
   * This builds a {@link ComputationTargetSpecification} from the target.
   * Example objects are {@link Position} and {@link Security}.
   * 
   * @param valueName  the name of the value to load, not null
   * @param target  the target object, may be null
   * @param constraints  the value constraints that must be satisfied
   */
  public ValueRequirement(String valueName, Object target, ValueProperties constraints) {
    this(valueName, new ComputationTargetSpecification(target), constraints);
  }

  /**
   * Creates a requirement from a target specification with no value constraints.
   * 
   * @param valueName  the value to load, not null
   * @param targetSpecification  the target specification, not null
   */
  public ValueRequirement(String valueName, ComputationTargetSpecification targetSpecification) {
    this(valueName, targetSpecification, ValueProperties.none());
  }

  /**
   * Creates a requirement from a target specification with value constraints.
   * 
   * @param valueName  the name of the value to load, not null
   * @param targetSpecification  the target specification, not null
   * @param constraints  the value constraints that must be satisfied
   */
  public ValueRequirement(String valueName, ComputationTargetSpecification targetSpecification, ValueProperties constraints) {
    ArgumentChecker.notNull(valueName, "Value name");
    ArgumentChecker.notNull(targetSpecification, "Computation target specification");
    ArgumentChecker.notNull(constraints, "constraints");
    _valueName = valueName.intern();
    _targetSpecification = targetSpecification;
    _constraints = constraints;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the name of the value to load.
   * 
   * @return the valueName, not null
   */
  public String getValueName() {
    return _valueName;
  }

  /**
   * Gets the specification of the target that is to be loaded.
   * 
   * @return the target specification, not null
   */
  public ComputationTargetSpecification getTargetSpecification() {
    return _targetSpecification;
  }

  /**
   * Gets the constraints that must be satisfied.
   * 
   * @return the constraints, not null
   */
  public ValueProperties getConstraints() {
    return _constraints;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets a specific constraint that must be specified.
   * <p>
   * If the constraint allows multiple specific values an arbitrary one is returned. 
   * 
   * @param constraintName  the constraint to query
   * @return the constraint value, or null if it is not defined 
   * @throws IllegalArgumentException if the constraint is a wild-card definition
   */
  public String getConstraint(final String constraintName) {
    final Set<String> values = _constraints.getValues(constraintName);
    if (values == null) {
      return null;
    } else if (values.isEmpty()) {
      throw new IllegalArgumentException("constraint " + constraintName + " contains only wild-card values");
    } else {
      return values.iterator().next();
    }
  }

  /**
   * Creates and returns the live data specification for market data corresponding to the target.
   * 
   * @param securitySource  the security source to resolve securities against, not null
   * @return the live data specification, not null
   */
  public LiveDataSpecification getRequiredLiveData(SecuritySource securitySource) {
    return getTargetSpecification().getRequiredLiveData(securitySource);
  }

  /**
   * Tests if this requirement can be satisfied by a given value specification.
   * <p>
   * A {@link ValueSpecification} can satisfy a requirement if both of the following are true:
   * <ul>
   * <li>it is for the same value on the same computation target
   * <li>the properties associated with the value satisfy the constraints on the requirement
   * </ul>
   * 
   * @param valueSpecification  the value specification to test, not null
   * @return {@code true} if this requirement is satisfied by the specification, {@code false} otherwise.
   */
  public boolean isSatisfiedBy(final ValueSpecification valueSpecification) {
    // value names are interned by this and specifications
    if (getValueName() != valueSpecification.getValueName()) {
      return false;
    }
    if (!getTargetSpecification().equals(getTargetSpecification())) {
      return false;
    }
    if (!getConstraints().isSatisfiedBy(valueSpecification.getProperties())) {
      return false;
    }
    return true;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ValueRequirement) {
      ValueRequirement other = (ValueRequirement) obj;
      return _valueName == other._valueName && // values are interned
          _targetSpecification.equals(other._targetSpecification) &&
          _constraints.equals(other._constraints);
    }
    return false;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _valueName.hashCode();
    result = prime * result + _targetSpecification.hashCode();
    result = prime * result + _constraints.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return new StrBuilder().append("ValueReq[").append(getValueName()).append(", ").append(getTargetSpecification()).append(", ").append(getConstraints()).append(']').toString();
  }

}
