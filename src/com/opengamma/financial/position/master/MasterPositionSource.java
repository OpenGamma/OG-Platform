/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.master;

import java.util.HashSet;
import java.util.Set;

import javax.time.Instant;
import javax.time.InstantProvider;

import org.apache.commons.lang.Validate;

import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionSource;
import com.opengamma.id.UniqueIdentifier;

/**
 * A {@code PositionSource} implemented using an underlying {@code PositionMaster}.
 * <p>
 * The {@link PositionSource} interface provides securities to the engine via a narrow API.
 * This class provides the source on top of a standard {@link PositionMaster}.
 */
public class MasterPositionSource implements PositionSource {

  /**
   * The position master.
   */
  private final PositionMaster _positionMaster;
  /**
   * The instant to search for a version at.
   * Null is treated as the latest version.
   */
  private final Instant _versionAsOfInstant;
  /**
   * The instant to search for corrections for.
   * Null is treated as the latest correction.
   */
  private final Instant _correctedToInstant;

  /**
   * Creates an instance with an underlying position master.
   * @param positionMaster  the position master, not null
   */
  public MasterPositionSource(final PositionMaster positionMaster) {
    this(positionMaster, null, null);
  }

  /**
   * Creates an instance with an underlying position master viewing the version
   * that existed on the specified instant.
   * @param positionMaster  the position master, not null
   * @param versionAsOfInstantProvider  the version instant to retrieve, null for latest version
   */
  public MasterPositionSource(final PositionMaster positionMaster, InstantProvider versionAsOfInstantProvider) {
    this(positionMaster, versionAsOfInstantProvider, null);
  }

  /**
   * Creates an instance with an underlying position master viewing the version
   * that existed on the specified instant as corrected to the correction instant.
   * @param positionMaster  the position master, not null
   * @param versionAsOfInstantProvider  the version instant to retrieve, null for latest version
   * @param correctedToInstantProvider  the instant that the data should be corrected to, null for latest correction
   */
  public MasterPositionSource(final PositionMaster positionMaster, InstantProvider versionAsOfInstantProvider, InstantProvider correctedToInstantProvider) {
    Validate.notNull(positionMaster, "positionMaster");
    _positionMaster = positionMaster;
    if (versionAsOfInstantProvider != null) {
      _versionAsOfInstant = Instant.of(versionAsOfInstantProvider);
    } else {
      _versionAsOfInstant = null;
    }
    if (correctedToInstantProvider != null) {
      _correctedToInstant = Instant.of(correctedToInstantProvider);
    } else {
      _correctedToInstant = null;
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public Set<UniqueIdentifier> getPortfolioIds() {
    final PortfolioTreeSearchRequest request = new PortfolioTreeSearchRequest();
    request.setVersionAsOfInstant(_versionAsOfInstant);
    request.setCorrectedToInstant(_correctedToInstant);
    final PortfolioTreeSearchResult result = _positionMaster.searchPortfolioTrees(request);
    final Set<UniqueIdentifier> ids = new HashSet<UniqueIdentifier>();
    for (PortfolioTreeDocument doc : result.getDocuments()) {
      ids.add(doc.getPortfolioId());
    }
    return ids;
  }

  @Override
  public Portfolio getPortfolio(final UniqueIdentifier uid) {
    Validate.notNull(uid, "uid");
    final FullPortfolioGetRequest request = new FullPortfolioGetRequest(uid);
    request.setVersionAsOfInstant(_versionAsOfInstant);
    request.setCorrectedToInstant(_correctedToInstant);
    return _positionMaster.getFullPortfolio(request);
  }

  @Override
  public PortfolioNode getPortfolioNode(final UniqueIdentifier uid) {
    Validate.notNull(uid, "uid");
    final FullPortfolioNodeGetRequest request = new FullPortfolioNodeGetRequest(uid);
    request.setVersionAsOfInstant(_versionAsOfInstant);
    request.setCorrectedToInstant(_correctedToInstant);
    return _positionMaster.getFullPortfolioNode(request);
  }

  @Override
  public Position getPosition(final UniqueIdentifier uid) {
    Validate.notNull(uid, "uid");
    final FullPositionGetRequest request = new FullPositionGetRequest(uid);
    request.setVersionAsOfInstant(_versionAsOfInstant);
    request.setCorrectedToInstant(_correctedToInstant);
    return _positionMaster.getFullPosition(request);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    String str = "MasterPositionSource[" + _positionMaster;
    if (_versionAsOfInstant != null) {
      str += ",versionAsOf=" + _versionAsOfInstant;
    }
    if (_versionAsOfInstant != null) {
      str += ",correctedTo=" + _correctedToInstant;
    }
    return str + "]";
  }

}
