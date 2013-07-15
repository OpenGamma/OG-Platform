/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position.impl;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;

/**
 * A {@code PositionSource} implemented using an underlying {@code PositionMaster} and {@code PortfolioMaster}.
 * <p>
 * The {@link PositionSource} interface provides portfolio and position to the engine via a narrow API. This class provides the source on top of a standard {@link PortfolioMaster} and
 * {@link PositionMaster}.
 */
@PublicSPI
public class MasterPositionSource extends AbstractMasterPositionSource implements PositionSource {
  // TODO: This still needs work re versioning, as it crosses the boundary between two masters

  /**
   * The position master.
   */
  private final PositionMaster _positionMaster;

  /**
   * Creates an instance with underlying masters which does not override versions.
   *
   * @param portfolioMaster  the portfolio master, not null
   * @param positionMaster  the position master, not null
   */
  public MasterPositionSource(final PortfolioMaster portfolioMaster, final PositionMaster positionMaster) {
    super(portfolioMaster);
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    _positionMaster = positionMaster;
  }

  /**
   * Gets the underlying position master.
   *
   * @return the position master, not null
   */
  public PositionMaster getPositionMaster() {
    return _positionMaster;
  }

  @Override
  public Position getPosition(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final ManageablePosition manPos = getPositionMaster().get(uniqueId).getPosition();
    if (manPos == null) {
      throw new DataNotFoundException("Unable to find position: " + uniqueId);
    }
    return manPos.toPosition();
  }

  @Override
  public Position getPosition(final ObjectId objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final ManageablePosition position = getPositionMaster().get(objectId, versionCorrection).getPosition();
    if (position == null) {
      throw new DataNotFoundException("Unable to find position: " + objectId + " at " + versionCorrection);
    }
    return position.toPosition();
  }

  @Override
  public Trade getTrade(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final ManageableTrade manTrade = getPositionMaster().getTrade(uniqueId);
    if (manTrade == null) {
      throw new DataNotFoundException("Unable to find trade: " + uniqueId);
    }
    return manTrade;
  }
  
  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getPortfolioMaster() + "," + getPositionMaster() + "]";
  }

  @Override
  protected ChangeProvider[] changeProviders() {
    return new ChangeProvider[] {getPortfolioMaster(), getPositionMaster()};
  }

  @Override
  protected Collection<Position> positions(PositionSearchRequest positionSearch) {
    List<Position> result = Lists.newArrayList();
    final PositionSearchResult positions = getPositionMaster().search(positionSearch);
    for (final PositionDocument position : positions.getDocuments()) {
      result.add(position.getPosition().toPosition());
    }
    return result;
  }

}
