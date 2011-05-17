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
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.util.db.Paging;

/**
 * Fudge builder for {@code Paging}.
 */
@FudgeBuilderFor(Paging.class)
public final class PagingBuilder implements FudgeBuilder<Paging> {

  /** Field name. */
  public static final String PAGE_FIELD_NAME = "page";
  /** Field name. */
  public static final String PAGING_SIZE_FIELD_NAME = "pagingSize";
  /** Field name. */
  public static final String TOTAL_FIELD_NAME = "totalItems";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, Paging object) {
    final MutableFudgeMsg msg = context.newMessage();
    msg.add(PAGE_FIELD_NAME, object.getPage());
    msg.add(PAGING_SIZE_FIELD_NAME, object.getPagingSize());
    msg.add(TOTAL_FIELD_NAME, object.getTotalItems());
    return msg;
  }

  @Override
  public Paging buildObject(FudgeDeserializationContext context, FudgeMsg msg) {
    final Integer page = msg.getInt(PAGE_FIELD_NAME);
    if (page == null) {
      throw new IllegalArgumentException("Fudge message is not a Paging - field 'page' is not present");
    }
    final Integer pagingSize = msg.getInt(PAGING_SIZE_FIELD_NAME);
    if (pagingSize == null) {
      throw new IllegalArgumentException("Fudge message is not a Paging - field 'pagingSize' is not present");
    }
    final Integer totalItems = msg.getInt(TOTAL_FIELD_NAME);
    if (totalItems == null) {
      throw new IllegalArgumentException("Fudge message is not a Paging - field 'totalItems' is not present");
    }
    return Paging.of(page, pagingSize, totalItems);
  }

}
