/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.user;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.core.user.impl.SimpleUserAccount;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class AuthenticationUtilsTest {

  @Test
  public void test_roundTripPasswordHash() {
    String simple = "P455w0rD";
    String hash = AuthenticationUtils.generatePasswordHash(simple);
    assertNotNull(hash);
    assertFalse(hash.isEmpty());
    SimpleUserAccount oguser = new SimpleUserAccount("testuser");
    oguser.setPasswordHash(hash);
    
    assertTrue(AuthenticationUtils.passwordsMatch(oguser, simple));
    assertFalse(AuthenticationUtils.passwordsMatch(oguser, "Pa55w0rD"));
  }

}
