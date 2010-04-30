/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMessageFactory;
import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.Identifiable;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A specification of a particular computation target that will be resolved
 * later on in a computation process.
 */
public class ComputationTargetSpecification implements Serializable {

  public static final String TYPE_FIELD_NAME = "computationTargetType";
  public static final String IDENTIFIER_FIELD_NAME = "computationTargetIdentifier";

  /**
   * The type of the target.
   */
  private final ComputationTargetType _type;
  /**
   * The identifier of the target.
   */
  private final Identifier _identifier;

  /**
   * Construct a specification that refers to the specified object.
   * 
   * @param target  the target to create a specification for, may be null
   */
  public ComputationTargetSpecification(Object target) {
    _type = ComputationTargetType.determineFromTarget(target);
    switch (_type) {
      case MULTIPLE_POSITIONS:
      case POSITION: {
        UniqueIdentifier identifier = ((UniqueIdentifiable) target).getUniqueIdentifier();
        _identifier = Identifier.of(identifier.getScheme(), identifier.getValue());
        break;
      }
      case SECURITY:  // TODO: remove once SECURITY only uses UniqueIdentifiable
      case PRIMITIVE: {
        if (target instanceof Identifiable) {
          _identifier = ((Identifiable) target).getIdentityKey();
        } else if (target instanceof Identifier) {
          _identifier = (Identifier) target;
        } else {
          _identifier = null;
        }
        break;
      }
      default:
        throw new OpenGammaRuntimeException("Unhandled computation target type: " + _type);
    }
  }

  /**
   * Creates a lightweight specification of a computation target.
   * @param type  the type of the target, not null
   * @param identifier  the target identifier, may be null
   */
  public ComputationTargetSpecification(ComputationTargetType targetType, UniqueIdentifier identifier) {
    ArgumentChecker.notNull(targetType, "target type");
    if (targetType != ComputationTargetType.PRIMITIVE) {
      ArgumentChecker.notNull(identifier, "identifier");
    }
    _type = targetType;
    _identifier = identifier == null ? null : Identifier.of(identifier.getScheme(), identifier.getValue());
  }

  /**
   * Creates a lightweight specification of a computation target.
   * @param type  the type of the target, not null
   * @param identifier  the target identifier, may be null
   */
  public ComputationTargetSpecification(ComputationTargetType targetType, Identifier identifier) {
    ArgumentChecker.notNull(targetType, "target type");
    if (targetType != ComputationTargetType.PRIMITIVE) {
      ArgumentChecker.notNull(identifier, "identifier");
    }
    _type = targetType;
    _identifier = identifier;
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
    return _identifier;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof ComputationTargetSpecification) {
      ComputationTargetSpecification other = (ComputationTargetSpecification) obj;
      return _type == other._type &&
          ObjectUtils.equals(_identifier, other._identifier);
    }
    return false;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _type.hashCode();
    if(_identifier != null) {
      result = prime * result + _identifier.hashCode();
    }
    return result;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  //-------------------------------------------------------------------------
  public void toFudgeMsg(FudgeMessageFactory fudgeContext, MutableFudgeFieldContainer msg) {
    msg.add(TYPE_FIELD_NAME, _type.name());
    FudgeFieldContainer identifierMsg = _identifier.toFudgeMsg(fudgeContext);
    msg.add(IDENTIFIER_FIELD_NAME, identifierMsg);
  }

  public static ComputationTargetSpecification fromFudgeMsg(FudgeFieldContainer msg) {
    if (msg == null) {
      return null;
    }
    ComputationTargetType type = ComputationTargetType.valueOf(msg.getString(TYPE_FIELD_NAME));
    Identifier identifier = Identifier.fromFudgeMsg(msg.getMessage(IDENTIFIER_FIELD_NAME));
    return new ComputationTargetSpecification(type, identifier);
  }

}
