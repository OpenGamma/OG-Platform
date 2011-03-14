/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.debug;

import java.util.Random;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtil;
import com.opengamma.language.livedata.MetaLiveData;
import com.opengamma.language.livedata.PublishedLiveData;

/**
 * Trivial live data for debugging. Returns random numbers.
 */
public class DebugLiveDataRandom implements PublishedLiveData {

  private final Random _random = new Random();

  private Data execute() {
    return DataUtil.of(_random.nextInt());
  }

  @Override
  public MetaLiveData getMetaLiveData() {
    final MetaLiveData metaLiveData = new MetaLiveData("DebugLiveDataRandom");
    // TODO: invocation
    return metaLiveData;
  }

}
