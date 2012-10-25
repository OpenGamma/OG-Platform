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
 * Fudge builder for {@code PagingRequest}.
 */
@FudgeBuilderFor(PagingRequest.class)
public final class PagingRequestFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<PagingRequest> {

  /** Field name. */
  public static final String FIRST_FIELD_NAME = "first";
  /** Field name. */
  public static final String SIZE_FIELD_NAME = "size";

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, PagingRequest object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer, final PagingRequest object) {
    if (object == null) {
      return null;
    }
    final MutableFudgeMsg msg = serializer.newMessage();
    toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(final FudgeSerializer serializer, final PagingRequest object, final MutableFudgeMsg msg) {
    addToMessage(msg, FIRST_FIELD_NAME, object.getFirstItem());
    addToMessage(msg, SIZE_FIELD_NAME, object.getPagingSize());
  }

  //-------------------------------------------------------------------------
  @Override
  public PagingRequest buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return fromFudgeMsg(deserializer, msg);
  }

  public static PagingRequest fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    if (msg == null) {
      return null;
    }
    final int first = msg.getInt(FIRST_FIELD_NAME);
    final int size = msg.getInt(SIZE_FIELD_NAME);
    return PagingRequest.ofIndex(first, size);
  }

}
