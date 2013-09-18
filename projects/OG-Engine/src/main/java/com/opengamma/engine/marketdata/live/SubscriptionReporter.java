/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import java.util.Map;

import javax.management.MXBean;

@MXBean
public interface SubscriptionReporter {

  Map<String, SubscriptionInfo> queryByTicker(String ticker);
}
