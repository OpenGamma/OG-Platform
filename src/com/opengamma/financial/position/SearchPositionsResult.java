/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.opengamma.engine.position.Position;
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
  private final List<Position> _positions;

  /**
   * Creates an instance.
   * @param paging  the paging information, not null
   * @param positions  the positions, not null
   */
  public SearchPositionsResult(final Paging paging, final List<Position> positions) {
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
  public List<Position> getPositions() {
    return _positions;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[positions=" + _positions.size() + ", paging=" + _paging + "]";
  }

}
