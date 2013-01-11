/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.Set;

import org.joda.beans.MetaBean;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.UniqueId;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
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
    // TODO should the ID come from the JSON? that way the URI can always be the object ID and the JSON ID can have a version
    ArgumentChecker.notNull(tradeId, "tradeId");
    _tradeId = tradeId;
  }

  @Override
  ManageablePosition savePosition(ManageablePosition position) {
    return getPositionMaster().update(new PositionDocument(position)).getPosition();
  }

  @Override
  ManageablePosition getPosition(ManageableTrade trade) {
    ArgumentChecker.notNull(trade, "trade");
    if (!trade.getUniqueId().isVersioned()) {
      throw new IllegalArgumentException("trade ID must be versioned. trade: " + trade);
    }
    if (!trade.getUniqueId().getObjectId().equals(_tradeId.getObjectId())) {
      throw new IllegalArgumentException("The trade ID in the path (" + _tradeId + ") doesn't match the trade ID " +
                                             "in the trade (" + trade.getUniqueId().getObjectId() + ")");
    }
    ManageableTrade originalTrade = getPositionMaster().getTrade(trade.getUniqueId());
    UniqueId positionId = originalTrade.getParentPositionId();
    ManageablePosition position = getPositionMaster().get(positionId).getPosition();
    boolean removed = position.getTrades().remove(originalTrade);
    if (!removed) {
      throw new OpenGammaRuntimeException("Failed to remove trade " + trade.getUniqueId() + " from position " +
                                              position.getUniqueId());
    }
    // TODO what if the updated trade's security doesn't match the position's?
    position.setQuantity(position.getQuantity().subtract(originalTrade.getQuantity()).add(trade.getQuantity()));
    position.addTrade(trade);
    return position;
  }
}
