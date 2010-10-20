/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import org.apache.commons.lang.Validate;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import it.unimi.dsi.fastutil.doubles.Double2DoubleMap;

/**
 * An immutable pair consisting of two {@code double} elements.
 * <p>
 * The class provides direct access to the primitive types and implements
 * the relevant fastutil interface.
 */
public final class DoublesPair extends Pair<Double, Double> implements Double2DoubleMap.Entry {

  public static DoublesPair of(Pair<Double, Double> pair) {
    Validate.notNull(pair, "pair");
    Validate.notNull(pair.getFirst(), "first");
    Validate.notNull(pair.getSecond(), "second");
    return new DoublesPair(pair.getFirst(), pair.getSecond());
  }
  
  /** The first element. */
  public final double first;  // CSIGNORE
  /** The second element. */
  public final double second;  // CSIGNORE

  /**
   * Constructor.
   * @param first  the first element
   * @param second  the second element
   */
  public DoublesPair(final double first, final double second) {
    this.first = first;
    this.second = second;
  }

  //-------------------------------------------------------------------------
  @Override
  public Double getFirst() {
    return first;
  }

  @Override
  public Double getSecond() {
    return second;
  }

  /**
   * Gets the first element as a primitive {@code double}.
   * @return the primitive
   */
  public double getFirstDouble() {
    return first;
  }

  /**
   * Gets the second element as a primitive {@code double}.
   * @return the primitive
   */
  public double getSecondDouble() {
    return second;
  }

  //-------------------------------------------------------------------------
  @Override
  public double getDoubleKey() {
    return first;
  }

  @Override
  public double getDoubleValue() {
    return second;
  }

  @Override
  public double setValue(double newValue) {
    throw new UnsupportedOperationException("Immutable");
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof DoublesPair) {
      final DoublesPair other = (DoublesPair) obj;
      return this.first == other.first && this.second == other.second;
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    // see Map.Entry API specification
    final long f = Double.doubleToLongBits(first);
    final long s = Double.doubleToLongBits(second);
    return ((int) (f ^ (f >>> 32))) ^ ((int) (s ^ (s >>> 32)));
  }

  //-------------------------------------------------------------------------
  /**
   * Serializes this pair to a Fudge message.
   * @param context  the Fudge context, not null
   * @return the Fudge message, not null
   */
  public FudgeFieldContainer toFudgeMsg(final FudgeSerializationContext context) {
    final MutableFudgeFieldContainer msg = context.newMessage();
    msg.add(0, getClass().getName());
    context.objectToFudgeMsg(msg, "first", null, getFirst());
    context.objectToFudgeMsg(msg, "second", null, getSecond());
    return msg;
  }

  /**
   * Deserializes this pair from a Fudge message.
   * @param context  the Fudge context, not null
   * @param msg  the Fudge message, not null
   * @return the pair, not null
   */
  public static DoublesPair fromFudgeMsg(final FudgeDeserializationContext context, final FudgeFieldContainer msg) {
    double first = (Double) context.fieldValueToObject(msg.getByName("first"));
    double second = (Double) context.fieldValueToObject(msg.getByName("second"));
    return DoublesPair.of(first, second);
  }

}
