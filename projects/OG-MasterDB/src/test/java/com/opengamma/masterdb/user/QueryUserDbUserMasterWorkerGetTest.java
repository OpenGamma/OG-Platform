/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueId;
import com.opengamma.master.user.UserDocument;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests query by get.
 */
@Test(groups = TestGroup.UNIT_DB)
public class QueryUserDbUserMasterWorkerGetTest extends AbstractDbUserMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryUserDbUserMasterWorkerGetTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryUserDbUserMasterWorkerGetTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, true);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_getUser_nullUID() {
    _usrMaster.get((UniqueId)null);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getUser_versioned_notFoundId() {
    UniqueId uniqueId = UniqueId.of("DbUsr", "0", "0");
    _usrMaster.get(uniqueId);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getUser_versioned_notFoundVersion() {
    UniqueId uniqueId = UniqueId.of("DbUsr", "101", "1");
    _usrMaster.get(uniqueId);
  }

  @Test
  public void test_getUser_versioned_oneUserDate() {
    UniqueId uniqueId = UniqueId.of("DbUsr", "101", "0");
    UserDocument test = _usrMaster.get(uniqueId);
    assert101(test);
  }

  @Test
  public void test_getUser_versioned_twoUserDates() {
    UniqueId uniqueId = UniqueId.of("DbUsr", "102", "0");
    UserDocument test = _usrMaster.get(uniqueId);
    assert102(test);
  }

  @Test
  public void test_getUser_versioned_notLatest() {
    UniqueId uniqueId = UniqueId.of("DbUsr", "201", "0");
    UserDocument test = _usrMaster.get(uniqueId);
    assert201(test);
  }

  @Test
  public void test_getUser_versioned_latest() {
    UniqueId uniqueId = UniqueId.of("DbUsr", "201", "1");
    UserDocument test = _usrMaster.get(uniqueId);
    assert202(test);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getUser_unversioned_notFound() {
    UniqueId uniqueId = UniqueId.of("DbUsr", "0");
    _usrMaster.get(uniqueId);
  }

  @Test
  public void test_getUser_unversioned() {
    UniqueId oid = UniqueId.of("DbUsr", "201");
    UserDocument test = _usrMaster.get(oid);
    assert202(test);
  }

}
