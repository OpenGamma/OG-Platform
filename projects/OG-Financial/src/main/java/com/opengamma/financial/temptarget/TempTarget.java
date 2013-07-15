/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.temptarget;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;

/**
 * Base class for temporary target objects.
 */
public abstract class TempTarget implements UniqueIdentifiable {

  /**
   * The computation target type corresponding to a temporary target.
   */
  public static final ComputationTargetType TYPE = ComputationTargetType.of(TempTarget.class);

  private final UniqueId _uid;

  public TempTarget() {
    _uid = null;
  }

  protected TempTarget(UniqueId uid) {
    _uid = uid;
  }

  protected TempTarget(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final FudgeField field = message.getByName("uid");
    if (field != null) {
      _uid = deserializer.fieldValueToObject(UniqueId.class, field);
    } else {
      _uid = null;
    }
  }

  /**
   * Returns the unique identifier of the target, if one is set.
   * 
   * @return the unique identifier, null if none is set
   */
  @Override
  public UniqueId getUniqueId() {
    return _uid;
  }

  public abstract TempTarget withUniqueId(final UniqueId uid);

  /**
   * Tests the target for equality against another, ignoring the unique identifier.
   * 
   * @param o the other object, not null, not this instance, and of the same class as this instance
   * @return true if the objects are equal (ignoring the unique identifier), false otherwise
   */
  protected abstract boolean equalsImpl(Object o);

  /**
   * Creates a hash code for the object, ignoring the unique identifier.
   * 
   * @return the hash code
   */
  protected abstract int hashCodeImpl();

  /**
   * Tests the target for equality against another, ignoring the unique identifier.
   * 
   * @param o the object to test against, possibly null
   * @return true if the objects are equal (ignoring the unique identifier), false otherwise
   */
  @Override
  public final boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if ((o == null) || (o.getClass() != getClass())) {
      return false;
    }
    return equalsImpl(o);
  }

  /**
   * Creates a hash code for the object, ignoring the unique identifier.
   * 
   * @return the hash code
   */
  @Override
  public final int hashCode() {
    return getClass().hashCode() + hashCodeImpl();
  }

  protected void toFudgeMsgImpl(final FudgeSerializer serializer, final MutableFudgeMsg message) {
    // No-op
  }

  public final void toFudgeMsg(final FudgeSerializer serializer, final MutableFudgeMsg message) {
    serializer.addToMessage(message, "uid", null, getUniqueId());
    toFudgeMsgImpl(serializer, message);
  }

}
