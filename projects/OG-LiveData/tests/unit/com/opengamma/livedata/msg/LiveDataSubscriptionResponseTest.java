/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.msg;

import org.junit.Test;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.LiveDataSubscriptionResult;

/**
 * This class is primarily here to allow us to eliminate the dummy classes.
 *
 * @author kirk
 */
public class LiveDataSubscriptionResponseTest {
  
  @Test
  public void simpleConstruction() {
    new LiveDataSubscriptionResponse(new LiveDataSpecification("Foo"),
        LiveDataSubscriptionResult.SUCCESS,
        null,
        new LiveDataSpecification("Foo"),
        null,
        null);
  }

}
