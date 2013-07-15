/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.position.impl;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.DelegatingPositionSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;

/**
 * A {@code PositionSource} implemented using an underlying {@code PositionMaster} and {@code DelegatingPositionSource}.
 * <p>
 * The {@link PositionSource} interface provides portfolio and position to the engine via a narrow API. This class provides the source on top of a standard {@link PortfolioMaster} and
 * {@link DelegatingPositionSource}.
 */
@PublicSPI
public class MasterDelegatingPositionSource extends AbstractMasterPositionSource implements PositionSource {
  
  private static final Logger s_logger = LoggerFactory.getLogger(MasterDelegatingPositionSource.class);
 
  private final DelegatingPositionSource _delegatingPositionSource;
  
  public MasterDelegatingPositionSource(PortfolioMaster portfolioMaster, DelegatingPositionSource positionSource) {
    super(portfolioMaster);
    ArgumentChecker.notNull(positionSource, "positionSource");
    
    _delegatingPositionSource = positionSource;
  }
  
  protected Collection<Position> positions(PositionSearchRequest positionSearch) {
    s_logger.debug("findPositions.positionSearchRequest {}", positionSearch);
    List<Position> positions = Lists.newArrayList();
    for (ObjectId positionObjectId : positionSearch.getPositionObjectIds()) {
      Position position = getDelegatingPositionSource().getPosition(positionObjectId, VersionCorrection.LATEST);
      if (position != null) {
        positions.add(position);
      } else {
        s_logger.warn("Position {} cannot be found in DelegatingPositionSource {}", positionObjectId, getDelegatingPositionSource());
      }
    }
    return positions;
  }

  @Override
  public Position getPosition(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    return getDelegatingPositionSource().getPosition(uniqueId);
  }

  @Override
  public Position getPosition(ObjectId objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    return getDelegatingPositionSource().getPosition(objectId, versionCorrection);
  }

  @Override
  public Trade getTrade(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    return getDelegatingPositionSource().getTrade(uniqueId);
  }

  /**
   * Gets the delegatingPositionSource.
   * @return the delegatingPositionSource
   */
  public DelegatingPositionSource getDelegatingPositionSource() {
    return _delegatingPositionSource;
  }

  @Override
  protected ChangeProvider[] changeProviders() {
    return new ChangeProvider[] {getPortfolioMaster()};
  }
  
  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getPortfolioMaster() + "," + getDelegatingPositionSource() + "]";
  }
  
}
