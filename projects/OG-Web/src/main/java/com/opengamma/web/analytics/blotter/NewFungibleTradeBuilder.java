/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;

/**
 *
 */
/* package */ class NewFungibleTradeBuilder extends FungibleTradeBuilder {

  @Override
  ManageablePosition savePosition(ManageablePosition position) {
    // TODO implement savePosition()
    throw new UnsupportedOperationException("savePosition not implemented");
  }

  // TODO need the node ID? find a position on the node in the same security and add to that, otherwise create new position
  @Override
  ManageablePosition getPosition(ManageableTrade trade) {
    // TODO implement getPosition()
    throw new UnsupportedOperationException("getPosition not implemented");
  }
}
