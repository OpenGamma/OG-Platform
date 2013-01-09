/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.Set;

import org.joda.beans.MetaBean;

import com.opengamma.id.UniqueId;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class ExistingFungibleTradeBuilder extends FungibleTradeBuilder {

  private final UniqueId _tradeId;

  /* package */ ExistingFungibleTradeBuilder(PositionMaster positionMaster,
                                             SecurityMaster securityMaster,
                                             Set<MetaBean> metaBeans,
                                             UniqueId tradeId) {
    super(positionMaster, securityMaster, metaBeans);
    ArgumentChecker.notNull(tradeId, "tradeId");
    _tradeId = tradeId;
  }

  @Override
  ManageablePosition savePosition(ManageablePosition position) {
    // TODO implement savePosition()
    throw new UnsupportedOperationException("savePosition not implemented");
  }

  // TODO need to adjust the position's quantity by removing the quantity of the previous version of the trade
  @Override
  ManageablePosition getPosition(ManageableTrade trade) {
    // TODO implement getPosition()
    throw new UnsupportedOperationException("getPosition not implemented");
  }
}
