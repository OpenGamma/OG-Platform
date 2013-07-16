/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db.pool;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.mockito.Mockito;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.testng.annotations.Test;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.opengamma.util.async.BlockingOperation;
import com.opengamma.util.db.pool.BoneCPHack;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.Timeout;

/**
 * Tests the {@link BoneCPHack} class.
 */
@Test(groups = TestGroup.INTEGRATION)
public class BoneCPHackTest {

  private BoneCPConfig createConfig() {
    final BoneCPConfig config = new BoneCPConfig();
    config.setPartitionCount(1);
    config.setLazyInit(false);
    config.setMinConnectionsPerPartition(3);
    config.setMaxConnectionsPerPartition(3);
    config.setDatasourceBean(new AbstractDataSource() {

      @Override
      public Connection getConnection() throws SQLException {
        return Mockito.mock(Connection.class);
      }

      @Override
      public Connection getConnection(String username, String password) throws SQLException {
        return getConnection();
      }
      
      public Logger getParentLogger() {
        return null;
      }
    });
    config.setConnectionTimeoutInMs(Timeout.standardTimeoutMillis());
    config.setConnectionHook(new BoneCPHack(config.getConnectionHook()));
    return config;
  }

  public void testNonBlocking() throws SQLException {
    final BoneCP bcp = new BoneCP(createConfig());
    BlockingOperation.off();
    try {
      final Connection h1 = bcp.getConnection();
      assertNotNull(h1);
      final Connection h2 = bcp.getConnection();
      assertNotNull(h2);
      final Connection h3 = bcp.getConnection();
      assertNotNull(h3);
      h1.close();
      h2.close();
      h3.close();
    } finally {
      BlockingOperation.on();
    }
  }

  @Test(expectedExceptions = {BlockingOperation.class })
  public void testBlocking() throws SQLException {
    final BoneCP bcp = new BoneCP(createConfig());
    BlockingOperation.off();
    try {
      final Connection h1 = bcp.getConnection();
      assertNotNull(h1);
      final Connection h2 = bcp.getConnection();
      assertNotNull(h2);
      final Connection h3 = bcp.getConnection();
      assertNotNull(h3);
      bcp.getConnection();
      fail();
    } finally {
      BlockingOperation.on();
    }
  }

}
