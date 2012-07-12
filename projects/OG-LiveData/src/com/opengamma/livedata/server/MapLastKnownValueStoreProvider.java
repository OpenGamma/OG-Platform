/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import com.opengamma.id.ExternalId;

/**
 * 
 */
public class MapLastKnownValueStoreProvider implements LastKnownValueStoreProvider {

  @Override
  public LastKnownValueStore newInstance(ExternalId security, String normalizationRuleSetId) {
    // Ignore the inputs.
    return new MapLastKnownValueStore();
  }

}
