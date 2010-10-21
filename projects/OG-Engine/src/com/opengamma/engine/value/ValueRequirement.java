/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.text.StrBuilder;

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
 * The actual value which is computed is available as a matching {@link ValueSpecification}.
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
   * Creates a requirement.
   * @param valueName  the value to load, not null
   * @param targetType  the target type, not null
   * @param targetIdentifier  the target identifier, may be null
   */
  public ValueRequirement(String valueName, ComputationTargetType targetType, UniqueIdentifier targetIdentifier) {
    this(valueName, new ComputationTargetSpecification(targetType, targetIdentifier));
  }

  public ValueRequirement(String valueName, ComputationTargetType targetType, UniqueIdentifier targetIdentifier, ValueProperties constraints) {
    this(valueName, new ComputationTargetSpecification(targetType, targetIdentifier), constraints);
  }

  /**
   * Creates a requirement from an object.
   * Example objects are {@link Position} and {@link Security}.
   * @param valueName  the value to load, not null
   * @param target  the target object, may be null
   */
  public ValueRequirement(String valueName, Object target) {
    this(valueName, new ComputationTargetSpecification(target));
  }

  /**
   * Creates a requirement.
   * @param valueName  the value to load, not null
   * @param targetSpecification  the target specification, not null
   */
  public ValueRequirement(String valueName, ComputationTargetSpecification targetSpecification) {
    this(valueName, targetSpecification, ValueProperties.none());
  }

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

  public ValueProperties getConstraints() {
    return _constraints;
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
          ObjectUtils.equals(_targetSpecification, other._targetSpecification) && ObjectUtils.equals(_constraints, other._constraints);
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
    return new StrBuilder().append("ValueReq[").append(getValueName()).append(", ").append(getTargetSpecification()).append(']').toString();
  }

  public LiveDataSpecification getRequiredLiveData(SecuritySource securitySource) {
    return getTargetSpecification().getRequiredLiveData(securitySource);
  }

}
