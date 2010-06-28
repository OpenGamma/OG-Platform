/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position;

import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.google.common.collect.ImmutableList;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.Paging;

/**
 * An immutable list of positions with paging.
 */
public final class SearchPositionsResult {

  /**
   * The paging information.
   */
  private final Paging _paging;
  /**
   * The paged list of positions.
   */
  private final List<PositionSummary> _positions;

  /**
   * Creates an instance.
   * @param paging  the paging information, not null
   * @param positions  the positions, not null
   */
  public SearchPositionsResult(final Paging paging, final List<PositionSummary> positions) {
    ArgumentChecker.notNull(paging, "paging");
    ArgumentChecker.noNulls(positions, "positions");
    _paging = paging;
    _positions = ImmutableList.copyOf(positions);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the paging information.
   * @return the paging information, not null
   */
  public Paging getPaging() {
    return _paging;
  }

  /**
   * Gets the list of positions.
   * @return the list of positions, unmodifiable, not null
   */
  public List<PositionSummary> getPositions() {
    return _positions;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[positions=" + _positions.size() + ", paging=" + _paging + "]";
  }

  //-------------------------------------------------------------------------
  /** Field name. */
  private static final String PAGING_FIELD_NAME = "paging";
  /** Field name. */
  private static final String POSITION_FIELD_NAME = "position";

  /**
   * Serializes to a Fudge message.
   * @param context  the Fudge context, not null
   * @return the Fudge message, not null
   */
  public FudgeFieldContainer toFudgeMsg(final FudgeSerializationContext context) {
    MutableFudgeFieldContainer msg = context.newMessage();
    context.objectToFudgeMsg(msg, PAGING_FIELD_NAME, null, _paging);
    for (PositionSummary summary : _positions) {
      context.objectToFudgeMsg(msg, POSITION_FIELD_NAME, null, summary);
    }
    return msg;
  }

  /**
   * Deserializes from a Fudge message.
   * @param context  the Fudge context, not null
   * @param msg  the Fudge message, not null
   * @return the pair, not null
   */
  public static SearchPositionsResult fromFudgeMsg(final FudgeDeserializationContext context, final FudgeFieldContainer msg) {
    Paging paging = context.fieldValueToObject(Paging.class, msg.getByName(PAGING_FIELD_NAME));
    List<PositionSummary> summaries = new ArrayList<PositionSummary>();
    for (FudgeField field : msg.getAllByName(POSITION_FIELD_NAME)) {
      PositionSummary summary = context.fieldValueToObject(PositionSummary.class, field);
      summaries.add(summary);
    }
    return new SearchPositionsResult(paging, summaries);
  }

}
