/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.user.impl;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.user.ManageableOGUser;
import com.opengamma.master.user.UserDocument;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link InMemoryUserMaster}.
 */
@Test(groups = TestGroup.UNIT)
public class InMemoryUserMasterTest {

  private static final ExternalId EMAIL_ADDRESS = ExternalId.of("EmailAddress", "info@opengamma.com");
  private static final ExternalId BLOOMBERG_SID = ExternalId.of("BloombergSid", "837283");
  private static final ExternalId OTHER_USER_ID1 = ExternalId.of("OtherUserId", "sk03e47s");
  //private static final ExternalId OTHER_USER_ID2 = ExternalId.of("OtherUserId", "sdjaldif");
  private static final ExternalIdBundle BUNDLE_FULL = ExternalIdBundle.of(EMAIL_ADDRESS, BLOOMBERG_SID, OTHER_USER_ID1);
  //private static final ExternalIdBundle BUNDLE_TWO = ExternalIdBundle.of(EMAIL_ADDRESS, OTHER_USER_ID1);
  //private static final ExternalIdBundle BUNDLE_OTHER = ExternalIdBundle.of(EMAIL_ADDRESS, BLOOMBERG_SID, OTHER_USER_ID2);

  private InMemoryUserMaster master;
  private UserDocument addedDoc;

  @BeforeMethod
  public void setUp() {
    master = new InMemoryUserMaster();
    ManageableOGUser inputUser = new ManageableOGUser("testuser");
    inputUser.setExternalIdBundle(BUNDLE_FULL);
    UserDocument inputDoc = new UserDocument(inputUser);
    addedDoc = master.add(inputDoc);
  }

  //-------------------------------------------------------------------------
  public void test_get_match() {
    UserDocument result = master.get(addedDoc.getUniqueId());
    assertEquals(UniqueId.of("MemUsr", "1"), result.getUniqueId());
    assertEquals(addedDoc, result);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_get_noMatch() {
    master.get(UniqueId.of("A", "B"));
  }

}
