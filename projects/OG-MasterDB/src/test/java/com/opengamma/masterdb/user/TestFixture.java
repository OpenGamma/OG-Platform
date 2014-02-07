package com.opengamma.masterdb.user;

import static com.google.common.collect.Sets.newHashSet;

import java.util.Arrays;

import com.opengamma.core.user.OGEntitlement;
import com.opengamma.core.user.ResourceAccess;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.user.ManageableOGRole;
import com.opengamma.master.user.ManageableOGUser;
import com.opengamma.master.user.RoleDocument;
import com.opengamma.master.user.UserDocument;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.test.AbstractDbTest;

/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * <p/>
 * Please see distribution for license.
 */
public class TestFixture extends AbstractDbTest {

  public OGEntitlement ent_a_1;
  public OGEntitlement ent_a_11;
  public OGEntitlement ent_a_2;
  public OGEntitlement ent_b_1;
  public OGEntitlement ent_b_11;
  public OGEntitlement ent_b_2;
  public OGEntitlement ent_c_1;
  public OGEntitlement ent_c_11;
  public OGEntitlement ent_c_2;
  public ManageableOGRole role_a;
  public ManageableOGRole role_b;
  public ManageableOGRole role_c;
  public RoleDocument inserted_document_a;
  public RoleDocument inserted_document_b;
  public RoleDocument inserted_document_c;
  public ManageableOGUser user;
  public UserDocument insertedUser;
  public DbUserMaster _userMaster;
  public DbRoleMaster _roleMaster;


  public TestFixture(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
  }

  public void init() throws Exception {

    DbConnector dbConnector = getDbConnector();
    _roleMaster = new DbRoleMaster(dbConnector);
    _userMaster = new DbUserMaster(dbConnector);

    ent_a_1 = new OGEntitlement(ExternalId.of("Prt", "p1").toString(), "portfolio", ResourceAccess.READ);
    ent_a_11 = new OGEntitlement(ExternalId.of("Prt", "p1").toString(), "portfolio", ResourceAccess.WRITE);
    ent_a_2 = new OGEntitlement(ExternalId.of("Prt", "p2").toString(), "portfolio", ResourceAccess.READ);

    role_a = new ManageableOGRole("Role_A");
    role_a.setEntitlements(newHashSet(Arrays.<OGEntitlement>asList(ent_a_1, ent_a_11, ent_a_2)));

    ent_b_1 = new OGEntitlement(ExternalId.of("Prt", "p1").toString(), "portfolio", ResourceAccess.WRITE);
    ent_b_11 = new OGEntitlement(ExternalId.of("Prt", "p1").toString(), "portfolio", ResourceAccess.READ);
    ent_b_2 = new OGEntitlement(ExternalId.of("Prt", "p2").toString(), "portfolio", ResourceAccess.READ);

    role_b = new ManageableOGRole("Role_B");
    role_b.setEntitlements(newHashSet(Arrays.<OGEntitlement>asList(ent_b_1, ent_b_11, ent_b_2)));

    ent_c_1 = new OGEntitlement(ExternalId.of("Prt", "p5").toString(), "portfolio", ResourceAccess.READ);
    ent_c_11 = new OGEntitlement(ExternalId.of("Prt", "p5").toString(), "portfolio", ResourceAccess.WRITE);
    ent_c_2 = new OGEntitlement(ExternalId.of("Prt", "p6").toString(), "portfolio", ResourceAccess.READ);

    role_c = new ManageableOGRole("Role_C");
    role_c.setEntitlements(newHashSet(Arrays.<OGEntitlement>asList(ent_c_1, ent_c_11, ent_c_2)));

    inserted_document_a = _roleMaster.add(new RoleDocument(role_a));
    inserted_document_b = _roleMaster.add(new RoleDocument(role_b));
    inserted_document_c = _roleMaster.add(new RoleDocument(role_c));

    user = new ManageableOGUser("Test");
    user.setExternalIdBundle(ExternalIdBundle.of(ExternalId.of("A", "B"), ExternalId.of("C", "D")));
    user.setPasswordHash("PASSWORD_HASH");

    insertedUser = _userMaster.add(new UserDocument(user));
  }
}
