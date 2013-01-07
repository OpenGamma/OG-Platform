/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.Collections;
import java.util.Set;

import org.joda.beans.MetaBean;

import com.opengamma.id.UniqueId;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Builds a trade and security and saves it as a new version of an existing trade and security.
 */
/* package */ class ExistingOtcTradeBuilder extends OtcTradeBuilder {

  private final UniqueId _tradeId;

  /* package */ ExistingOtcTradeBuilder(UniqueId tradeId,
                                        SecurityMaster securityMaster,
                                        PositionMaster positionMaster,
                                        Set<MetaBean> metaBeans) {
    super(securityMaster, positionMaster, metaBeans);
    ArgumentChecker.notNull(tradeId, "tradeId");
    _tradeId = tradeId;
  }

  /**
   * Saves a security using {@link SecurityMaster#update}. The security data must contain an ID.
   * @param security The security
   * @return The updated security
   */
  @Override
  public ManageableSecurity saveSecurity(ManageableSecurity security) {
    // TODO need to check
    //   security has an ID
    //   previous version of security referred to by ID exists and has the same type - need to load existing
    return getSecurityMaster().update(new SecurityDocument(security)).getSecurity();
  }

  /**
   * Saves a position using {@link PositionMaster#update}. The position data must contain an ID.
   * @param position The position
   * @return The updated position
   */
  @Override
  ManageablePosition savePosition(ManageablePosition position) {
    // TODO check position ID exists and this is a valid update to the previous version (same security etc)
    return getPositionMaster().update(new PositionDocument(position)).getPosition();
  }

  @Override
  ManageablePosition getPosition(ManageableTrade trade) {
    if (!_tradeId.isVersioned()) {
      throw new IllegalArgumentException("trade ID must be versioned. trade: " + trade);
    }
    ManageableTrade originalTrade = getPositionMaster().getTrade(_tradeId);
    UniqueId positionId = originalTrade.getParentPositionId();
    ManageablePosition position = getPositionMaster().get(positionId).getPosition();
    // for OTCs there's always 1 trade per position, remove the existing trade because it's being replaced
    position.setTrades(Collections.<ManageableTrade>emptyList());
    return position;
  }
}
