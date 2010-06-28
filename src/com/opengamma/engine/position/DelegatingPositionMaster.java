/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A {@link PositionMaster} implementation which allows the scheme of the incoming {@link UniqueIdentifier} to control
 * which underlying {@link PositionMaster} will handle the request. If no scheme-specific handler has been registered,
 * a default is used.
 */
public class DelegatingPositionMaster implements PositionMaster {

  private final PositionMaster _defaultMaster;
  private final Map<String, PositionMaster> _schemeToDelegateMap = new ConcurrentHashMap<String, PositionMaster>();
  
  /**
   * Constructs a new {@link DelegatingPositionMaster}.
   * 
   * @param defaultMaster  the {@link PositionMaster} on which to fall back when no registered schemes match that of
   *                       an incoming UniqueIdentifier.
   */
  public DelegatingPositionMaster(PositionMaster defaultMaster) {
    ArgumentChecker.notNull(defaultMaster, "defaultMaster");
    _defaultMaster = defaultMaster;
  }
  
  public void registerPositionMaster(String scheme, PositionMaster positionMaster) {
    ArgumentChecker.notNull(scheme, "scheme");
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    _schemeToDelegateMap.put(scheme, positionMaster);
  }
  
  private PositionMaster choosePositionMaster(UniqueIdentifier uid) {
    PositionMaster schemeMaster = _schemeToDelegateMap.get(uid.getScheme());
    return schemeMaster != null ? schemeMaster : _defaultMaster;
  }
  
  @Override
  public Portfolio getPortfolio(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    return choosePositionMaster(uid).getPortfolio(uid);
  }

  @Override
  public PortfolioNode getPortfolioNode(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    return choosePositionMaster(uid).getPortfolioNode(uid);
  }

  @Override
  public Position getPosition(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    return choosePositionMaster(uid).getPosition(uid);
  }

  @Override
  public Set<UniqueIdentifier> getPortfolioIds() {
    Set<UniqueIdentifier> result = new HashSet<UniqueIdentifier>(_defaultMaster.getPortfolioIds());
    for (PositionMaster delegateMaster : _schemeToDelegateMap.values()) {
      result.addAll(delegateMaster.getPortfolioIds());
    }
    return Collections.unmodifiableSet(result);
  }
}
