/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.id.DelegateByScheme;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A {@link PositionMaster} implementation which allows the scheme of the incoming {@link UniqueIdentifier} to control
 * which underlying {@link PositionMaster} will handle the request. If no scheme-specific handler has been registered,
 * a default is used.
 */
public class DelegatingPositionMaster extends DelegateByScheme<PositionMaster> implements PositionMaster {

  /**
   * Constructs a new {@link DelegatingPositionMaster}.
   * 
   * @param defaultMaster  the {@link PositionMaster} on which to fall back when no registered schemes match that of
   *                       an incoming UniqueIdentifier.
   */
  public DelegatingPositionMaster(PositionMaster defaultMaster) {
    super(defaultMaster);
  }

  @Override
  public Portfolio getPortfolio(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    return chooseDelegate(uid).getPortfolio(uid);
  }

  @Override
  public PortfolioNode getPortfolioNode(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    return chooseDelegate(uid).getPortfolioNode(uid);
  }

  @Override
  public Position getPosition(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    return chooseDelegate(uid).getPosition(uid);
  }

  @Override
  public Set<UniqueIdentifier> getPortfolioIds() {
    Set<UniqueIdentifier> result = new HashSet<UniqueIdentifier>(getDefaultDelegate().getPortfolioIds());
    for (PositionMaster delegateMaster : getDelegates().values()) {
      result.addAll(delegateMaster.getPortfolioIds());
    }
    return Collections.unmodifiableSet(result);
  }
}
