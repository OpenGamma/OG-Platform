/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.util.Comparator;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Position;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.CompareUtils;

/**
 * Super-simple comparator for positions or trades that compares the external id bundles, and if the same, compares the quantities of the positions.
 */
public class SimplePositionComparator implements Comparator<Position> {

  @Override
  public int compare(Position positionOrTrade1, Position positionOrTrade2) {
    ExternalIdBundle externalBundle1 = positionOrTrade1.getSecurityLink().getExternalId();
    ExternalIdBundle externalBundle2 = positionOrTrade2.getSecurityLink().getExternalId();
    ExternalId bestExId1 = getBestIdentifier(externalBundle1);
    ExternalId bestExId2 = getBestIdentifier(externalBundle2);
    int result = CompareUtils.compareWithNullLow(bestExId1, bestExId2);
    if (result == 0) {
      return positionOrTrade2.getQuantity().compareTo(positionOrTrade1.getQuantity());
    } else {
      return result;
    }
  }
  
  @SuppressWarnings("deprecation")
  public ExternalId getBestIdentifier(ExternalIdBundle idBundle) {
    ExternalScheme[] schemes = {ExternalSchemes.BLOOMBERG_TICKER, ExternalSchemes.BLOOMBERG_TICKER_WEAK, ExternalSchemes.BLOOMBERG_TCM,
                                ExternalSchemes.ACTIVFEED_TICKER, ExternalSchemes.RIC, ExternalSchemes.ISIN, ExternalSchemes.CUSIP};
    for (ExternalScheme scheme : schemes) {
      ExternalId externalId = idBundle.getExternalId(scheme);
      if (externalId != null) {
        return externalId;
      }
    }
    return null;
  }

}
