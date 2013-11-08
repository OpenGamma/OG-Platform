/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.helpers.MessageFormatter;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalId;
import com.opengamma.integration.tool.portfolio.PortfolioLoader;

/**
 * Checks that a given xmlId to externalSystemId combination is valid. This is important
 * since the {@link PortfolioLoader} expects externalIds to uniquely identify a trade so
 * that it can configure it properly in the database.
 * 
 * Cases that cause problems if not validated include:
 * <ul>
 * <li> Adding a position when external ids are not unique. This will cause the first trade to be overwritten.
 * <li> Referencing a trade/position from multiple different portfolios. This will cause dupes if external ids
 * are empty.
 * <ul>
 */
public final class XmlExternalIdValidator {

  /**
   * The existing mappings.
   */
  private Map<ExternalId, String> _existingMappings = new HashMap<>();

  /**
   * Checks that the given id combination is not already in use.
   * 
   * @param externalSystemId  the externalSystemId to check
   * @param tradeId  the identifier for information purposes
   * @throws OpenGammaRuntimeException if the identifier is invalid
   */
  public synchronized void validateExternalId(ExternalId externalSystemId, String tradeId) {
    if (_existingMappings.containsKey(externalSystemId)) {
      String previousTradeId = _existingMappings.get(externalSystemId);
      if (!previousTradeId.equals(tradeId)) {
        String message = MessageFormatter.arrayFormat(
            "External id '{}' already allocated to a different trade when processing this file. Previous trade id: '{}', this trade id: '{}'",
            new Object[] {externalSystemId, previousTradeId, tradeId }).getMessage();

        throw new OpenGammaRuntimeException(message);
      }
    }
    _existingMappings.put(externalSystemId, tradeId);
  }

}
