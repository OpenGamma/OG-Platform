/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.entitlement;

import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.client.DistributedEntitlementChecker;
import com.opengamma.transport.ByteArrayFudgeRequestSender;
import com.opengamma.transport.FudgeRequestDispatcher;
import com.opengamma.transport.InMemoryByteArrayRequestConduit;
import com.opengamma.util.test.TestGroup;

/**
 * Integration test between {@link DistributedEntitlementChecker} and {@link EntitlementServer}.
 */
@Test(groups = TestGroup.UNIT)
public class DistributedEntitlementCheckerTest {

  // TODO reenable test once entitlement checking has been reimplemented correctly
  @Test(enabled = false)
  public void testRequestResponse() {
    PermissiveLiveDataEntitlementChecker delegate = new PermissiveLiveDataEntitlementChecker();
    EntitlementServer server = new EntitlementServer(delegate); 
    
    FudgeRequestDispatcher fudgeRequestDispatcher = new FudgeRequestDispatcher(server);
    InMemoryByteArrayRequestConduit inMemoryByteArrayRequestConduit = new InMemoryByteArrayRequestConduit(fudgeRequestDispatcher);
    ByteArrayFudgeRequestSender fudgeRequestSender = new ByteArrayFudgeRequestSender(inMemoryByteArrayRequestConduit);

    DistributedEntitlementChecker client = new DistributedEntitlementChecker(fudgeRequestSender);

    LiveDataSpecification testSpec = new LiveDataSpecification(
        "TestNormalization",
        ExternalId.of("test1", "test1"));
    UserPrincipal megan = new UserPrincipal("megan", "127.0.0.1");

    assertTrue(client.isEntitled(megan, testSpec));
  }

}
