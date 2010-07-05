/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

/**
 * An immutable pair consisting of two {@code Object} elements.
 * <p>
 * The pair is only immutable if the objects that are added are immutable, or treated as such.
 *
 * @param <A> the first element type
 * @param <B> the second element type
 */
public final class ObjectsPair<A, B> extends Pair<A, B> {

  /** The first element. */
  public final A first;  // CSIGNORE
  /** The second element. */
  public final B second;  // CSIGNORE

  /**
   * Creates a pair inferring the types.
   * @param <A> the first element type
   * @param <B> the second element type
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static <A, B> ObjectsPair<A, B> of(A first, B second) {
    return new ObjectsPair<A, B>(first, second);
  }

  /**
   * Constructs a pair.
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   */
  public ObjectsPair(A first, B second) {
    this.first = first;
    this.second = second;
  }

  //-------------------------------------------------------------------------
  @Override
  public A getFirst() {
    return first;
  }

  @Override
  public B getSecond() {
    return second;
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
    context.objectToFudgeMsg(msg, "first", null, first);
    context.objectToFudgeMsg(msg, "second", null, second);
    return msg;
  }

  /**
   * Deserializes this pair from a Fudge message.
   * @param context  the Fudge context, not null
   * @param msg  the Fudge message, not null
   * @return the pair, not null
   */
  public static ObjectsPair<?, ?> fromFudgeMsg(final FudgeDeserializationContext context, final FudgeFieldContainer msg) {
    Object first = context.fieldValueToObject(msg.getByName("first"));
    Object second = context.fieldValueToObject(msg.getByName("second"));
    return ObjectsPair.of(first, second);
  }

}
