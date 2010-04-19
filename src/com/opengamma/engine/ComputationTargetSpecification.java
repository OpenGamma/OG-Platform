/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
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

import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.Security;
import com.opengamma.id.Identifier;
import com.opengamma.id.Identifiable;
import com.opengamma.util.ArgumentChecker;

/**
 * A specification of a particular computation target that will be resolved
 * later on in a computation process.
 *
 * @author kirk
 */
public class ComputationTargetSpecification implements Serializable {
  public static final String TYPE_FIELD_NAME = "computationTargetType";
  public static final String IDENTIFIER_FIELD_NAME = "computationTargetIdentifier";
  
  private final ComputationTargetType _type;
  private final Identifier _identifier;
  
  public ComputationTargetSpecification(ComputationTargetType targetType, Identifier identifier) {
    ArgumentChecker.checkNotNull(targetType, "Computation Target Type");
    switch(targetType) {
    case SECURITY:
    case POSITION:
    case MULTIPLE_POSITIONS:
      ArgumentChecker.checkNotNull(identifier, "Identifier (required for this target type)");
      break;
    default:
      // Not required for Primitive.
      break;
    }
    _type = targetType;
    _identifier = identifier;
  }

  /**
   * Construct a specification that refers to the specified object.
   * 
   * @param target
   */
  public ComputationTargetSpecification(Object target) {
    Identifier dsid = null;
    ComputationTargetType type = ComputationTargetType.determineFromTarget(target);
    // Special DSID logic.
    // REVIEW kirk 2010-03-31 -- Does this belong up in ComputationTargetType somewhere?
    if((type == null) && (target instanceof Identifier)) {
      dsid = (Identifier) target;
      if(ObjectUtils.equals(dsid.getDomain(), PortfolioNode.PORTFOLIO_NODE_IDENTITY_KEY_DOMAIN)) {
        type = ComputationTargetType.MULTIPLE_POSITIONS;
      } else if(ObjectUtils.equals(dsid.getDomain(), Position.POSITION_IDENTITY_KEY_DOMAIN)) {
        type = ComputationTargetType.POSITION;
      } else if(ObjectUtils.equals(dsid.getDomain(), Security.SECURITY_IDENTITY_KEY_DOMAIN)) {
        type = ComputationTargetType.SECURITY;
      } else {
        type = ComputationTargetType.PRIMITIVE;
      }
    }
    if(type == null) {
      throw new IllegalArgumentException("Cannot determine a target type for " + target);
    }
    if((dsid == null) && (target instanceof Identifiable)) {
      dsid = ((Identifiable)target).getIdentityKey();
    }
    
    _type = type;
    _identifier = dsid;
  }

  /**
   * @return the type
   */
  public ComputationTargetType getType() {
    return _type;
  }

  /**
   * @return the identifier
   */
  public Identifier getIdentifier() {
    return _identifier;
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj == null) {
      return false;
    }
    if(!(obj instanceof ComputationTargetSpecification)) {
      return false;
    }
    ComputationTargetSpecification other = (ComputationTargetSpecification) obj;
    if(_type != other._type) {
      return false;
    }
    if(!ObjectUtils.equals(_identifier, other._identifier)) {
      return false;
    }
    return true;
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
  
  public void toFudgeMsg (FudgeMessageFactory fudgeContext, MutableFudgeFieldContainer msg) {
    msg.add(TYPE_FIELD_NAME, _type.name());
    
    FudgeFieldContainer identifierMsg = _identifier.toFudgeMsg(fudgeContext);
    msg.add(IDENTIFIER_FIELD_NAME, identifierMsg);
  }
  
  public static ComputationTargetSpecification fromFudgeMsg(FudgeFieldContainer msg) {
    if(msg == null) {
      return null;
    }
    ComputationTargetType type = ComputationTargetType.valueOf(msg.getString(TYPE_FIELD_NAME));
    Identifier identifier = new Identifier(msg.getMessage(IDENTIFIER_FIELD_NAME));
    return new ComputationTargetSpecification(type, identifier);
  }
}
