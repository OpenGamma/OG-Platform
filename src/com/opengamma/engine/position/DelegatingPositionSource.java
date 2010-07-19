/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.id.UniqueIdentifierSchemeDelegator;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * A {@link PositionSource} implementation which allows the scheme of the incoming {@link UniqueIdentifier} to control
 * which underlying {@link PositionSource} will handle the request. If no scheme-specific handler has been registered,
 * a default is used.
 */
public class DelegatingPositionSource extends UniqueIdentifierSchemeDelegator<PositionSource> implements PositionSource {

  /**
   * Constructs a new {@link DelegatingPositionSource}.
   * 
   * @param defaultMaster  the {@link PositionSource} on which to fall back when no registered schemes match that of
   *                       an incoming UniqueIdentifier.
   */
  public DelegatingPositionSource(PositionSource defaultMaster) {
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
    for (PositionSource delegateMaster : getDelegates().values()) {
      result.addAll(delegateMaster.getPortfolioIds());
    }
    return Collections.unmodifiableSet(result);
  }
}
