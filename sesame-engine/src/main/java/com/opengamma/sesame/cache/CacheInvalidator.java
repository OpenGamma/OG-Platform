/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.cache;

import java.util.Collection;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.sesame.marketdata.MarketDataId;

/**
 * TODO review which of the register methods are still needed with MarketDataBundle
 */
public interface CacheInvalidator {

  void register(ExternalId id);

  void register(ExternalIdBundle bundle);

  void register(ObjectId id);

  // TODO can this work in the general case? where people add their own implementations?
  // could it be made to work if MarketDataKey had more methods?
  void register(MarketDataId marketDataId);

  void register(ValuationTimeCacheEntry entry);

  void invalidate(ZonedDateTime valuationTime,
                  VersionCorrection configVersionCorrection,
                  Collection<ExternalId> marketData,
                  Collection<ObjectId> dbData);
}
