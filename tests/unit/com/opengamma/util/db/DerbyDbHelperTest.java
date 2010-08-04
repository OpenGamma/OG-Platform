/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test DerbyDbHelper.
 */
public class DerbyDbHelperTest extends DbHelperTest {

  public DerbyDbHelperTest() {
    _helper = DerbyDbHelper.INSTANCE;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getName() {
    assertEquals("Derby", _helper.getName());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_sqlNextSequenceValueSelect() {
    assertEquals("SELECT NEXT VALUE FOR MySeq FROM sysibm.sysdummy1", _helper.sqlNextSequenceValueSelect("MySeq"));
  }

}
