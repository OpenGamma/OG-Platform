/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import java.io.Serializable;
import java.util.Set;

import org.apache.commons.lang.text.StrBuilder;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * An immutable requirement to obtain a value needed to perform a calculation.
 * This is a metadata-based requirement, and specifies only the minimal number of parameters
 * that are necessary to specify the user requirements.
 * 
 * The actual value which is computed is available as a {@link ValueSpecification} that is
 * capable of satisfying this requirement. A specification satisfies a requirement if its
 * properties satisfy the requirement constraints, plus the value name and target specifications
 * match.
 */
@PublicAPI
public final class ValueRequirement implements Serializable {
  /**
   * The value being requested.
   */
  private final String _valueName;
  /**
   * The specification of the object that the value refers to.
   */
  private final ComputationTargetSpecification _targetSpecification;
  /**
   * The constraints or additional parameters about the target. For example, a currency
   * constraint.
   */
  private final ValueProperties _constraints;

  /**
   * Creates a requirement with no value constraints.
   * @param valueName  the value to load, not null
   * @param targetType  the target type, not null
   * @param targetIdentifier  the target identifier, may be null
   */
  public ValueRequirement(String valueName, ComputationTargetType targetType, UniqueIdentifier targetIdentifier) {
    this(valueName, new ComputationTargetSpecification(targetType, targetIdentifier));
  }

  /**
   * Creates a requirement with value constraints.
   * @param valueName the name of the value to load, not {@code null}
   * @param targetType the target type, not {@code null}
   * @param targetIdentifier the unique identifier of the target, not {@code null}
   * @param constraints value constraints that must be satisfied
   */
  public ValueRequirement(String valueName, ComputationTargetType targetType, UniqueIdentifier targetIdentifier, ValueProperties constraints) {
    this(valueName, new ComputationTargetSpecification(targetType, targetIdentifier), constraints);
  }

  /**
   * Creates a requirement from an object with no value constraints.
   * Example objects are {@link Position} and {@link Security}.
   * @param valueName  the value to load, not null
   * @param target  the target object, may be null
   */
  public ValueRequirement(String valueName, Object target) {
    this(valueName, new ComputationTargetSpecification(target));
  }

  /**
   * Creates a requirement from an object with value constraints.
   * Example objects are {@link Position} and {@link Security}.
   * @param valueName the name of the value to load, not {@code null}
   * @param target the target object, may be {@code null}
   * @param constraints value constraints that must be satisfied
   */
  public ValueRequirement(String valueName, Object target, ValueProperties constraints) {
    this(valueName, new ComputationTargetSpecification(target), constraints);
  }

  /**
   * Creates a requirement from a target specification with no value constraints.
   * @param valueName  the value to load, not null
   * @param targetSpecification  the target specification, not null
   */
  public ValueRequirement(String valueName, ComputationTargetSpecification targetSpecification) {
    this(valueName, targetSpecification, ValueProperties.none());
  }

  /**
   * Creates a requirement from a target specification with value constraints.
   * @param valueName the name of the value to load, not {@code null}
   * @param targetSpecification the target specification, not {@code null}
   * @param constraints the value constraints that must be satisfied
   */
  public ValueRequirement(String valueName, ComputationTargetSpecification targetSpecification, ValueProperties constraints) {
    ArgumentChecker.notNull(valueName, "Value name");
    ArgumentChecker.notNull(targetSpecification, "Computation target specification");
    ArgumentChecker.notNull(constraints, "constraints");
    _valueName = valueName.intern();
    _targetSpecification = targetSpecification;
    _constraints = constraints;
  }

  // -------------------------------------------------------------------------
  /**
   * Gets the name of the value to load.
   * @return the valueName, not null
   */
  public String getValueName() {
    return _valueName;
  }

  /**
   * Gets the specification of the target that is to be loaded.
   * @return the target specification, not null
   */
  public ComputationTargetSpecification getTargetSpecification() {
    return _targetSpecification;
  }

  /**
   * Returns the constraints that must be satisfied.
   * 
   * @return the constraints
   */
  public ValueProperties getConstraints() {
    return _constraints;
  }

  /**
   * Returns a specific constraint that must be specified.
   * 
   * @param constraintName the constraint to query
   * @return the constraint value, or {@code null} if it is not defined. If the constraint
   * allows multiple specific values an arbitrary one is returned. 
   * @throws IllegalArgumentException if the constraint is a wild-card definition.
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

  // -------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ValueRequirement) {
      ValueRequirement other = (ValueRequirement) obj;
      return _valueName == other._valueName && // values are interned
          _targetSpecification.equals(other._targetSpecification) && _constraints.equals(other._constraints);
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

  /**
   * Creates and returns the live data specification for market data corresponding to the target.
   * 
   * @param securitySource a security source to resolve securities against
   * @return the live data specification
   */
  public LiveDataSpecification getRequiredLiveData(SecuritySource securitySource) {
    return getTargetSpecification().getRequiredLiveData(securitySource);
  }

  /**
   * Tests if this requirement can be satisfied by a given {@link ValueSpecification}. A value specification
   * can satisfy a requirement if:
   * <ul>
   * <li>it is for the same value on the same computation target; and
   * <li>the properties associated with the value satisfy the constraints on the requirement
   * </ul>
   * 
   * @param valueSpecification the value specification to test, not {@code null}
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

}
