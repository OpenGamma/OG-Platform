/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.msg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class LiveDataSubscriptionResponseTest {

  public void simpleConstruction() {
    LiveDataSpecification lds = new LiveDataSpecification("Foo", ExternalId.of("A", "B"));
    LiveDataSubscriptionResponse ldsr = new LiveDataSubscriptionResponse(lds,
        LiveDataSubscriptionResult.SUCCESS,
        null,
        lds,
        null,
        null);
    assertEquals("Foo", lds.getNormalizationRuleSetId());
    assertEquals(ExternalIdBundle.of("A", "B"), lds.getIdentifiers());
    assertEquals(LiveDataSubscriptionResult.SUCCESS, ldsr.getSubscriptionResult());
    assertEquals(lds, ldsr.getRequestedSpecification());
  }

}
