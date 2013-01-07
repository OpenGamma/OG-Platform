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
/* package */ class ExistingFungibleTradeBuilder extends FungibleTradeBuilder {

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
