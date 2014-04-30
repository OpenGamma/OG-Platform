/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.db.management.jmx;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.google.common.collect.Sets;

/**
 * JMX management of a HSQL database.
 */
@ManagedResource(description = "Basic housekeeping operations on HSQL that can be managed via JMX")
public class HSQLDatabaseMBean extends DatabaseMBean {

  private static final Logger s_logger = LoggerFactory.getLogger(HSQLDatabaseMBean.class);

  /**
   * All active instances - used to generate meaningful names.
   */
  private static final Set<HSQLDatabaseMBean> s_instances = Sets.newSetFromMap(new WeakHashMap<HSQLDatabaseMBean, Boolean>());

  /* package */static void flush() {
    s_instances.clear();
  }

  /**
   * Name of the driver class used in configuration files.
   */
  protected static final String DRIVER_CLASS = "org.hsqldb.jdbcDriver";

  public HSQLDatabaseMBean() {
    super("HSQLDB");
    synchronized (s_instances) {
      s_instances.add(this);
    }
  }

  /**
   * Find the shortest unique name among the active instances.
   * <p>
   * Working from the right hand end of the local JDBC string the shortest form is found that uniquely identifies the database.
   * 
   * @return a name, not containing slashes, that can be used for the backup set.
   */
  protected String createBackupName() {
    final Integer one = 1;
    final Map<String, Integer> nameCount = new HashMap<String, Integer>();
    synchronized (s_instances) {
      for (HSQLDatabaseMBean instance : s_instances) {
        String jdbc = instance.getLocalJdbc();
        if (nameCount.put(jdbc, one) != null) {
          // Another MBean already has this JDBC string -- ignore
          continue;
        }
        String shortName = null;
        int slash = jdbc.lastIndexOf('/');
        while (slash >= 0) {
          if (shortName == null) {
            shortName = jdbc.substring(slash + 1);
          } else {
            shortName = jdbc.substring(slash + 1) + "-" + shortName;
          }
          jdbc = jdbc.substring(0, slash);
          slash = jdbc.lastIndexOf('/');
          final Integer count = nameCount.get(shortName);
          if (count == null) {
            s_logger.debug("Found {} for {}", shortName, instance);
            nameCount.put(shortName, one);
          } else {
            s_logger.debug("Name collision on {} for {}", shortName, instance);
            nameCount.put(shortName, count + 1);
          }
        }
      }
    }
    String jdbc = getLocalJdbc();
    String shortName = null;
    int slash = jdbc.lastIndexOf('/');
    while (slash >= 0) {
      if (shortName == null) {
        shortName = jdbc.substring(slash + 1);
      } else {
        shortName = jdbc.substring(slash + 1) + "-" + shortName;
      }
      if (one.equals(nameCount.get(shortName))) {
        s_logger.info("Using backup set {} for {}", shortName, this);
        return shortName;
      }
      jdbc = jdbc.substring(0, slash);
      slash = jdbc.lastIndexOf('/');
    }
    throw new UnsupportedOperationException("Can't generate short name for '" + getLocalJdbc() + "'");
  }

  protected String createBackupPath() {
    String backupPath = System.getProperty("backup.dir");
    if (backupPath == null) {
      throw new UnsupportedOperationException("backup.dir system property is not set");
    }
    backupPath = backupPath + File.separatorChar + "hsqldb" + File.separatorChar + createBackupName();
    final File backup = new File(backupPath);
    if (!backup.exists()) {
      backup.mkdirs();
      if (!backup.exists()) {
        throw new UnsupportedOperationException("Can't create folder " + backup);
      }
    }
    if (!backup.isDirectory()) {
      throw new UnsupportedOperationException("Can't create folder " + backup);
    }
    s_logger.info("Writing backup to {}", backupPath);
    return backupPath + File.separatorChar;
  }

  @ManagedOperation(description = "Performs an on-line, checkpointed, backup.")
  public String onlineBackup() {
    final String path = createBackupPath();
    try (Connection connection = getDataSource().getConnection()) {
      final PreparedStatement statement = connection.prepareStatement("BACKUP DATABASE TO '" + path + "' BLOCKING");
      statement.execute();
      s_logger.info("Checkpoint backup written to {}", path);
    } catch (SQLException e) {
      s_logger.error("Caught exception", e);
      throw new UnsupportedOperationException("SQL error attempting backup: " + e.getMessage());
    }
    return "Files backed up to:\n" + path;
  }

  @ManagedOperation(description = "Performs an on-line, hot, backup.")
  public String hotBackup() {
    final String path = createBackupPath();
    try (Connection connection = getDataSource().getConnection()) {
      final PreparedStatement statement = connection.prepareStatement("BACKUP DATABASE TO '" + path + "' NOT BLOCKING");
      statement.execute();
      s_logger.info("Hot backup written to {}", path);
    } catch (SQLException e) {
      s_logger.error("Caught exception", e);
      throw new UnsupportedOperationException("SQL error attempting backup: " + e.getMessage());
    }
    return "Files backed up to:\n" + path;
  }

}
