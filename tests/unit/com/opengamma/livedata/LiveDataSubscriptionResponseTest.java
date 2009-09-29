/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import org.junit.Test;

/**
 * This class is primarily here to allow us to eliminate the dummy classes.
 *
 * @author kirk
 */
public class LiveDataSubscriptionResponseTest {
  
  @Test
  public void simpleConstruction() {
    new LiveDataSubscriptionResponse("Kirk Wylie", new LiveDataSpecificationImpl(), new LiveDataSpecificationImpl(), LiveDataSubscriptionResult.SUCCESS);
  }

}
