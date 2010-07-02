/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.id.IdentifierBundle;

/**
 * A bond that can be delivered to satisfy a bond future contract.  Typically held in a set representing
 * the 'deliverable basket'.
 */
public class BondFutureDeliverable {
  
  protected static final String IDENTIFIERS_KEY = "identifiers";
  protected static final String CONVERSIONFACTOR_KEY = "conversionFactor";

  private IdentifierBundle _identifiers;
  private double _conversionFactor;
 
  public BondFutureDeliverable(IdentifierBundle identifierBundle,
      double conversionFactor) {
    super();
    _identifiers = identifierBundle;
    _conversionFactor = conversionFactor;
  }

  /**
   * @return the identifiers
   */
  public IdentifierBundle getIdentifiers() {
    return _identifiers;
  }

  /**
   * @param identifiers the identifiers to set
   */
  public void setIdentifiers(IdentifierBundle identifiers) {
    _identifiers = identifiers;
  }

  /**
   * @return the conversionFactor
   */
  public double getConversionFactor() {
    return _conversionFactor;
  }

  /**
   * @param conversionFactor the conversionFactor to set
   */
  public void setConversionFactor(double conversionFactor) {
    _conversionFactor = conversionFactor;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_conversionFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result
        + ((_identifiers == null) ? 0 : _identifiers.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    BondFutureDeliverable other = (BondFutureDeliverable) obj;
    if (Double.doubleToLongBits(_conversionFactor) != Double.doubleToLongBits(other._conversionFactor)) {
      return false;
    }
    if (_identifiers == null) {
      if (other._identifiers != null) {
        return false;
      }
    } else if (!_identifiers.equals(other._identifiers)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  protected void toFudgeMsg(final FudgeSerializationContext context, final MutableFudgeFieldContainer message) {
    context.objectToFudgeMsg(message, IDENTIFIERS_KEY, null, getIdentifiers());
    message.add(CONVERSIONFACTOR_KEY, getConversionFactor());
  }
    
  public FudgeFieldContainer toFudgeMsg(final FudgeSerializationContext context) {
    final MutableFudgeFieldContainer message = context.newMessage();
    FudgeSerializationContext.addClassHeader(message, getClass());
    toFudgeMsg(context, message);
    return message;
  }

  public static BondFutureDeliverable fromFudgeMsg(final FudgeDeserializationContext context,
      final FudgeFieldContainer message) {
    return new BondFutureDeliverable(context.fieldValueToObject(IdentifierBundle.class, message
        .getByName(IDENTIFIERS_KEY)), message.getDouble(CONVERSIONFACTOR_KEY));
  }
}
