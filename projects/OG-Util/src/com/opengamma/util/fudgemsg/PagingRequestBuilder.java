/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.util.db.PagingRequest;

/**
 * Fudge builder for {@code PagingRequest}.
 */
@FudgeBuilderFor(PagingRequest.class)
public final class PagingRequestBuilder implements FudgeBuilder<PagingRequest> {

  /** Field name. */
  public static final String FIRST_FIELD_NAME = "first";
  /** Field name. */
  public static final String SIZE_FIELD_NAME = "size";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, PagingRequest object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    msg.add(FIRST_FIELD_NAME, object.getFirstItem());
    msg.add(SIZE_FIELD_NAME, object.getPagingSize());
    return msg;
  }

  @Override
  public PagingRequest buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    final Integer first = msg.getInt(FIRST_FIELD_NAME);
    final Integer size = msg.getInt(SIZE_FIELD_NAME);
    return PagingRequest.ofIndex(first != null ? first : 1, size != null ? size : PagingRequest.DEFAULT_PAGING_SIZE);
  }

}
