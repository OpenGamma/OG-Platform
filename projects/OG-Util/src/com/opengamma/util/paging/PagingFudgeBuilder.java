/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.paging;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * Fudge builder for {@code Paging}.
 */
@FudgeBuilderFor(Paging.class)
public final class PagingFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<Paging> {

  /** Field name. */
  public static final String FIRST_FIELD_NAME = "first";
  /** Field name. */
  public static final String SIZE_FIELD_NAME = "size";
  /** Field name. */
  public static final String TOTAL_FIELD_NAME = "total";

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, Paging object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer, final Paging object) {
    if (object == null) {
      return null;
    }
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(final FudgeSerializer serializer, final Paging object, final MutableFudgeMsg msg) {
    addToMessage(msg, FIRST_FIELD_NAME, object.getRequest().getFirstItem());
    addToMessage(msg, SIZE_FIELD_NAME, object.getRequest().getPagingSize());
    addToMessage(msg, TOTAL_FIELD_NAME, object.getTotalItems());
  }

  //-------------------------------------------------------------------------
  @Override
  public Paging buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return fromFudgeMsg(deserializer, msg);
  }

  public static Paging fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    if (msg == null) {
      return null;
    }
    final int first = msg.getInt(FIRST_FIELD_NAME);
    final int size = msg.getInt(SIZE_FIELD_NAME);
    final int total = msg.getInt(TOTAL_FIELD_NAME);
    return Paging.of(PagingRequest.ofIndex(first, size), total);
  }

}
