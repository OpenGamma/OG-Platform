/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position;

import java.math.BigDecimal;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.id.UniqueIdentifier;

/**
 * A summary of a position.
 */
public final class PositionSummary {

  /**
   * The unique identifier.
   */
  private UniqueIdentifier _uid;
  /**
   * The amount of the position.
   */
  private BigDecimal _quantity;

  /**
   * Creates an instance.
   */
  public PositionSummary() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the unique identifier of the position.
   * @return the unique identifier
   */
  public UniqueIdentifier getUniqueIdentifier() {
    return _uid;
  }

  /**
   * Sets the unique identifier of the position.
   * @param uid  the unique identifier
   */
  public void setUniqueIdentifier(UniqueIdentifier uid) {
    _uid = uid;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the amount of the position held in terms of the security.
   * @return the amount of the position
   */
  public BigDecimal getQuantity() {
    return _quantity;
  }

  /**
   * Sets the amount of the position held in terms of the security.
   * @param quantity  the amount of the position
   */
  public void setQuantity(BigDecimal quantity) {
    _quantity = quantity;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  //-------------------------------------------------------------------------
  /** Field name. */
  private static final String UID_FIELD_NAME = "uid";
  /** Field name. */
  private static final String QUANTITY_FIELD_NAME = "quantity";

  /**
   * Serializes to a Fudge message.
   * @param context  the Fudge context, not null
   * @return the Fudge message, not null
   */
  public FudgeFieldContainer toFudgeMsg(final FudgeSerializationContext context) {
    MutableFudgeFieldContainer msg = context.newMessage();
    if (_uid != null) {
      msg.add(UID_FIELD_NAME, _uid.toFudgeMsg(context));
    }
    if (_quantity != null) {
      msg.add(QUANTITY_FIELD_NAME, _quantity);
    }
    return msg;
  }

  /**
   * Deserializes from a Fudge message.
   * @param context  the Fudge context, not null
   * @param msg  the Fudge message, not null
   * @return the pair, not null
   */
  public static PositionSummary fromFudgeMsg(final FudgeDeserializationContext context, final FudgeFieldContainer msg) {
    PositionSummary summary = new PositionSummary();
    if (msg.hasField(UID_FIELD_NAME)) {
      summary.setUniqueIdentifier(UniqueIdentifier.fromFudgeMsg(msg.getMessage(UID_FIELD_NAME)));
    }
    if (msg.hasField(QUANTITY_FIELD_NAME)) {
      summary.setQuantity(msg.getValue(BigDecimal.class, QUANTITY_FIELD_NAME));
    }
    return summary;
  }

}
