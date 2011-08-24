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

import com.opengamma.util.db.Paging;
import com.opengamma.util.db.PagingRequest;

/**
 * Fudge builder for {@code Paging}.
 */
@FudgeBuilderFor(Paging.class)
public final class PagingBuilder implements FudgeBuilder<Paging> {

  /** Field name. */
  public static final String FIRST_FIELD_NAME = "first";
  /** Field name. */
  public static final String SIZE_FIELD_NAME = "size";
  /** Field name. */
  public static final String TOTAL_FIELD_NAME = "total";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, Paging object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    msg.add(FIRST_FIELD_NAME, object.getRequest().getFirstItem());
    msg.add(SIZE_FIELD_NAME, object.getRequest().getPagingSize());
    msg.add(TOTAL_FIELD_NAME, object.getTotalItems());
    return msg;
  }

  @Override
  public Paging buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    final Integer first = msg.getInt(FIRST_FIELD_NAME);
    if (first == null) {
      throw new IllegalArgumentException("Fudge message is not a Paging - field 'first' is not present");
    }
    final Integer size = msg.getInt(SIZE_FIELD_NAME);
    if (size == null) {
      throw new IllegalArgumentException("Fudge message is not a Paging - field 'size' is not present");
    }
    final Integer total = msg.getInt(TOTAL_FIELD_NAME);
    if (total == null) {
      throw new IllegalArgumentException("Fudge message is not a Paging - field 'total' is not present");
    }
    return Paging.of(PagingRequest.ofIndex(first, size), total);
  }

}
