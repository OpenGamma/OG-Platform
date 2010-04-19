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
 *
 * @author kirk
 * @param <A> the first element type
 * @param <B> the second element type
 */
public final class ObjectsPair<A, B> extends Pair<A, B> {

  /** The first element. */
  private final A _first;
  /** The second element. */
  private final B _second;

  /**
   * Creates a pair inferring the types.
   * @param first  the first element, may be null
   * @param second  the second element, may be null
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
    _first = first;
    _second = second;
  }

  //-------------------------------------------------------------------------
  @Override
  public A getFirst() {
    return _first;
  }

  @Override
  public B getSecond() {
    return _second;
  }

  //-------------------------------------------------------------------------
  public FudgeFieldContainer toFudgeMsg(final FudgeSerializationContext context) {
    final MutableFudgeFieldContainer message = context.newMessage();
    message.add(0, getClass().getName());
    context.objectToFudgeMsg(message, "first", null, getFirst());
    context.objectToFudgeMsg(message, "second", null, getSecond());
    return message;
  }

  public static ObjectsPair<?,?> fromFudgeMsg(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    Object first = context.fieldValueToObject(message.getByName("first"));
    Object second = context.fieldValueToObject(message.getByName("second"));
    return ObjectsPair.of(first, second);
  }

}
