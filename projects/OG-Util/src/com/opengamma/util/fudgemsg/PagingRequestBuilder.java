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
  public static final String PAGE_FIELD_NAME = "page";
  /** Field name. */
  public static final String PAGING_SIZE_FIELD_NAME = "pagingSize";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, PagingRequest object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    msg.add(PAGE_FIELD_NAME, object.getPage());
    msg.add(PAGING_SIZE_FIELD_NAME, object.getPagingSize());
    return msg;
  }

  @Override
  public PagingRequest buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    final Integer page = msg.getInt(PAGE_FIELD_NAME);
    final Integer pagingSize = msg.getInt(PAGING_SIZE_FIELD_NAME);
    return new PagingRequest(page != null ? page : 1, pagingSize != null ? pagingSize : PagingRequest.DEFAULT_PAGING_SIZE);
  }

}
