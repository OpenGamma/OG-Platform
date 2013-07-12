/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p/>
 * Please see distribution for license.
 */
package com.opengamma.auth;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Collections;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.ZoneId;

import com.opengamma.id.UniqueId;
import com.opengamma.master.user.ManageableOGUser;
import com.opengamma.master.user.RoleMaster;
import com.opengamma.master.user.UserDocument;
import com.opengamma.master.user.UserMaster;
import com.opengamma.master.user.UserSearchRequest;
import com.opengamma.master.user.UserSearchResult;
import com.opengamma.util.test.TestGroup;


@Test(groups = TestGroup.UNIT)
public class UserManagerTest {

  private UserMaster _userMaster;
  private RoleMaster _roleMaster;

  private UserDocument _robDoc;
  private String robPassword = "robs_p455";
  private UserManager _userManager;

  @BeforeMethod
  public void setUp() {

    _userMaster = mock(UserMaster.class);
    _roleMaster = mock(RoleMaster.class);

    _userManager = new UserManager(_userMaster, _roleMaster);

    ManageableOGUser rob = new ManageableOGUser("rob");
    rob.setName("Rob Robertson");
    rob.setEmailAddress("rob@acme.com");
    rob.setPasswordHash(Signer.hashPassword(robPassword));
    rob.setUniqueId(UniqueId.of("test", "rob"));
    rob.setTimeZone(ZoneId.of("UTC"));
    _robDoc = new UserDocument(rob);
    when(_userMaster.search(UserSearchRequest.byUserId("rob"))).thenReturn(new UserSearchResult(Collections.singletonList(
        _robDoc)));
    when(_userMaster.search(UserSearchRequest.byUserId("dan"))).thenReturn(new UserSearchResult());
  }

  public void testRob() {
    assertEquals(new UserSearchResult(Collections.singletonList(_robDoc)),
                 _userMaster.search(UserSearchRequest.byUserId("rob")));

    ManageableOGUser rob = _userMaster.search(UserSearchRequest.byUserId("rob")).getFirstUser();

    assertTrue(_userManager.authenticateUser("rob", robPassword));
  }
}
