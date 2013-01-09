/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.math.BigDecimal;
import java.util.Set;

import org.joda.beans.MetaBean;

import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityMaster;

/**
 *
 */
/* package */ class NewFungibleTradeBuilder extends FungibleTradeBuilder {

  /* package */ NewFungibleTradeBuilder(PositionMaster positionMaster,
                                        SecurityMaster securityMaster,
                                        Set<MetaBean> metaBeans) {
    super(positionMaster, securityMaster, metaBeans);
  }

  @Override
  /* package */ ManageablePosition savePosition(ManageablePosition position) {
    return getPositionMaster().add(new PositionDocument(position)).getPosition();
  }

  @Override
  /* package */ ManageablePosition getPosition(ManageableTrade trade) {
    // TODO need the node ID - find the node and reuse existing position in the security if possible
    ManageablePosition position = new ManageablePosition();
    position.setQuantity(BigDecimal.ZERO);
    return position;
  }
}
