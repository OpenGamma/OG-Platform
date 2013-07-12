/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p/>
 * Please see distribution for license.
 */
package com.opengamma.auth;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Collections;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.ZoneId;

import com.opengamma.auth.master.portfolio.PortfolioCapability;
import com.opengamma.auth.master.portfolio.SecurePortfolioMaster;
import com.opengamma.auth.master.portfolio.SecurePortfolioMasterWrapper;
import com.opengamma.core.user.OGEntitlement;
import com.opengamma.core.user.ResourceAccess;
import com.opengamma.id.UniqueId;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.user.ManageableOGRole;
import com.opengamma.master.user.ManageableOGUser;
import com.opengamma.master.user.RoleMaster;
import com.opengamma.master.user.RoleSearchRequest;
import com.opengamma.master.user.RoleSearchResult;
import com.opengamma.master.user.UserDocument;
import com.opengamma.master.user.UserMaster;
import com.opengamma.master.user.UserSearchRequest;
import com.opengamma.master.user.UserSearchResult;
import com.opengamma.util.test.TestGroup;


@Test(groups = TestGroup.UNIT)
public class SecurePortfolioMasterTest {

  private UserMaster _userMaster;
  private RoleMaster _roleMaster;

  private UserDocument _robDoc;
  private String robPassword = "robs_p455";
  private UserManager _userManager;
  private PortfolioMaster _portfolioMaster;
  public SecurePortfolioMaster _securePortfolioMaster;
  private static final UniqueId PORTFOLIO_UNIQUE_ID = UniqueId.of("test", "uid", "2");
  private static final PortfolioDocument TEST_PORTFOLIO_DOCUMENT = mock(PortfolioDocument.class);

  @BeforeMethod
  public void setUp() {

    _userMaster = mock(UserMaster.class);
    _roleMaster = mock(RoleMaster.class);

    _portfolioMaster = mock(PortfolioMaster.class);


    when(_portfolioMaster.get(PORTFOLIO_UNIQUE_ID)).thenReturn(TEST_PORTFOLIO_DOCUMENT);


    _userManager = new UserManager(_userMaster, _roleMaster);

    _securePortfolioMaster = new SecurePortfolioMasterWrapper(_portfolioMaster);

    ManageableOGUser rob = new ManageableOGUser("rob");
    rob.setName("Rob Robertson");
    rob.setEmailAddress("rob@acme.com");
    rob.setPasswordHash(Signer.hashPassword(robPassword));
    rob.setUniqueId(UniqueId.of("test", "rob"));
    rob.setTimeZone(ZoneId.of("UTC"));
    _robDoc = new UserDocument(rob);

    RoleSearchResult roleSearchResult = mock(RoleSearchResult.class);
    ManageableOGRole role = new ManageableOGRole("the role");
    role.setEntitlements(new OGEntitlement(PORTFOLIO_UNIQUE_ID.getObjectId().toString(),
                                           "portfolio",
                                           ResourceAccess.READ));
    when(roleSearchResult.getRoles()).thenReturn(newArrayList(role));

    when(_roleMaster.search(RoleSearchRequest.byUserUid(rob.getUniqueId()))).thenReturn(roleSearchResult);
    when(_userMaster.search(UserSearchRequest.byUserId("rob"))).thenReturn(new UserSearchResult(Collections.singletonList(
        _robDoc)));
    when(_userMaster.search(UserSearchRequest.byUserId("dan"))).thenReturn(new UserSearchResult());
  }

  public void testRob() {
    assertEquals(new UserSearchResult(Collections.singletonList(_robDoc)),
                 _userMaster.search(UserSearchRequest.byUserId("rob")));

    ManageableOGUser rob = _userMaster.search(UserSearchRequest.byUserId("rob")).getFirstUser();

    assertTrue(_userManager.authenticateUser("rob", robPassword));

    PortfolioCapability capability = _userManager.getEntitledPortfolios(rob);

    PortfolioDocument portfolioDocument = _securePortfolioMaster.get(capability, PORTFOLIO_UNIQUE_ID);

    assertEquals(portfolioDocument, TEST_PORTFOLIO_DOCUMENT);
  }
}
