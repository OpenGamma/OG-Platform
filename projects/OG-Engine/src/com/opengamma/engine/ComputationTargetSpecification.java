/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.text.StrBuilder;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.id.Identifiable;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.normalization.StandardRules;
import com.opengamma.util.ArgumentChecker;

/**
 * An immutable specification of a particular computation target that will be resolved
 * later on in a computation process.
 */
public final class ComputationTargetSpecification implements Serializable {

  /**
   * The type of the target.
   */
  private final ComputationTargetType _type;
  /**
   * The identifier of the target.
   */
  private final UniqueIdentifier _uid;

  /**
   * Construct a specification that refers to the specified object.
   * 
   * @param target  the target to create a specification for, may be null
   */
  public ComputationTargetSpecification(final Object target) {
    _type = ComputationTargetType.determineFromTarget(target);
    switch (_type) {
      case PORTFOLIO_NODE:
      case POSITION:
      case SECURITY: {
        _uid = ((UniqueIdentifiable) target).getUniqueIdentifier();
        break;
      }
      case PRIMITIVE: {
        if (target instanceof UniqueIdentifiable) {
          _uid = ((UniqueIdentifiable) target).getUniqueIdentifier();
        } else if (target instanceof Identifiable) {
          final Identifier id = ((Identifiable) target).getIdentityKey();
          _uid = UniqueIdentifier.of(id.getScheme().getName(), id.getValue());
        } else if (target instanceof Identifier) {
          final Identifier id = (Identifier) target;
          _uid = UniqueIdentifier.of(id.getScheme().getName(), id.getValue());
        } else {
          _uid = null;
        }
        break;
      }
      default:
        throw new OpenGammaRuntimeException("Unhandled computation target type: " + _type);
    }
  }

  /**
   * Creates a lightweight specification of a computation target.
   * @param targetType the type of the target, not null
   * @param uid  the target identifier, may be null
   */
  public ComputationTargetSpecification(final ComputationTargetType targetType, final UniqueIdentifier uid) {
    ArgumentChecker.notNull(targetType, "target type");
    if (targetType != ComputationTargetType.PRIMITIVE) {
      ArgumentChecker.notNull(uid, "identifier");
    }
    _type = targetType;
    _uid = uid;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the type of the target.
   * @return the type, not null
   */
  public ComputationTargetType getType() {
    return _type;
  }

  /**
   * Gets the identifier to the actual target.
   * @return the identifier, may be null
   */
  public Identifier getIdentifier() {
    if (_uid == null) {
      return null;
    }
    return Identifier.of(_uid.getScheme(), _uid.getValue());
  }

  /**
   * Gets the unique identifier, if one exists.
   * @return the unique identifier, may be null
   */
  public UniqueIdentifier getUniqueIdentifier() {
    return _uid;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the required live data specification.
   * <p>
   * This method can only be called for primitives and securities.
   * For positions and nodes, there is no live data market line that would directly value
   * positions or porfolios.
   * 
   * @param securitySource  the source used to look up {@link Security} objects, not null
   * @return the specification of live data that directly produces a value for this computation target, not null
   * @throws OpenGammaRuntimeException If there is no live data directly corresponding to the target
   */
  public LiveDataSpecification getRequiredLiveData(final SecuritySource securitySource) {
    switch(getType()) {
      case PRIMITIVE:
        // Just use the identifier as given.
        return new LiveDataSpecification(StandardRules.getOpenGammaRuleSetId(), getIdentifier());
      case SECURITY:
        final Security security = securitySource.getSecurity(getUniqueIdentifier());
        if (security == null) {
          throw new OpenGammaRuntimeException("Unknown security in configured security source: " + getIdentifier());
        }
        // Package up the other identifiers
        return new LiveDataSpecification(StandardRules.getOpenGammaRuleSetId(), security.getIdentifiers());
      default:
        throw new OpenGammaRuntimeException("No LiveData is needed to for " + this);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ComputationTargetSpecification) {
      ComputationTargetSpecification other = (ComputationTargetSpecification) obj;
      return _type == other._type && ObjectUtils.equals(_uid, other._uid);
    }
    return false;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _type.hashCode();
    if (_uid != null) {
      result = prime * result + _uid.hashCode();
    }
    return result;
  }

  @Override
  public String toString() {
    return new StrBuilder()
      .append("CTSpec[")
      .append(getType())
      .append(", ")
      .append(getUniqueIdentifier())
      .append(']')
      .toString();
  }

}
