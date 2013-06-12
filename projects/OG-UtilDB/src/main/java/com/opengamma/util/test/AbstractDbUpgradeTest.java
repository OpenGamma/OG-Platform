/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Tests the creation + upgrade sequence results in the same structure as a pure create.
 */
public abstract class AbstractDbUpgradeTest extends DbTest {

  private static final Map<String, Map<String, String>> s_targetSchema = Maps.newHashMap();

  private final List<DbComparison> _comparisons = Lists.newLinkedList();

  private final String _masterDB;

  /**
   * Creates an instance.
   */
  protected AbstractDbUpgradeTest(String databaseType, String masterDB, final String targetVersion, final String createVersion) {
    super(databaseType, targetVersion, createVersion);
    _masterDB = masterDB;
  }

  //-------------------------------------------------------------------------
  @BeforeMethod(groups = TestGroup.UNIT_DB)
  public void setUp() throws Exception {
    DbTool dbTool = getDbTool();
    dbTool.setTargetVersion(getTargetVersion());
    dbTool.setCreateVersion(getCreateVersion());
    dbTool.dropTestSchema();
    dbTool.createTestSchema();
    dbTool.createTables(_masterDB, dbTool.getTestCatalog(), dbTool.getTestSchema(), Integer.parseInt(getTargetVersion()), Integer.parseInt(getCreateVersion()), this);
    dbTool.clearTestTables();
  }
  
  protected Map<String, String> getVersionSchemas() {
    Map<String, String> versionSchema = s_targetSchema.get(getDatabaseType());
    if (versionSchema == null) {
      versionSchema = new HashMap<>();
      s_targetSchema.put(getDatabaseType(), versionSchema);
    }
    return versionSchema;
  }

  //-------------------------------------------------------------------------
  @Test(groups = TestGroup.UNIT_DB)
  public void testDatabaseUpgrade() {
    for (DbComparison comparison : _comparisons) {
      if (comparison.getIndicatedVersion() != null) {
        String message = getDatabaseType() + ": " + comparison.getPrefix() + " database indicates different version than script name on "
            + comparison.getExpectedVersion() + (comparison.getMigrateDescription() == null ? " create" : " migrate");
        assertEquals(message, comparison.getExpectedVersion(), (int) comparison.getIndicatedVersion());
      }
      if (comparison.getMigrateDescription() == null) {
        continue;
      }
      int diff = StringUtils.indexOfDifference(comparison.getCreateDescription(), comparison.getMigrateDescription());
      if (diff >= 0) {
        System.err.println("Difference at " + diff);
        System.err.println("Upgraded --->..." + StringUtils.substring(comparison.getCreateDescription(), diff - 200, diff) +
          "<-!!!->" + StringUtils.substring(comparison.getCreateDescription(), diff, diff + 200) + "...");
        System.err.println(" Created --->..." + StringUtils.substring(comparison.getMigrateDescription(), diff - 200, diff) +
          "<-!!!->" + StringUtils.substring(comparison.getMigrateDescription(), diff, diff + 200) + "...");
      }
      assertEquals(getDatabaseType() + ": " + comparison.getPrefix(), comparison.getCreateDescription(), comparison.getMigrateDescription());
    }
  }

  @Override
  public void tablesCreatedOrUpgraded(final String version, final String prefix) {
    Integer dbVersion = getDbTool().getDbManagement().getSchemaGroupVersion(getDbTool().getTestCatalog(), getDbTool().getSchema(), prefix.substring(0, 3));
    final Map<String, String> versionSchemas = getVersionSchemas();
    if (versionSchemas.containsKey(prefix + "_" + version)) {
      // if we've already done the full schema, then we want to test that this upgrade has given us the same (but defer the comparison)
      _comparisons.add(new DbComparison(prefix, Integer.parseInt(version), dbVersion, versionSchemas.get(prefix + "_" + version), getDbTool().describeDatabase(prefix)));
    } else {
      // tests are run with most recent full schema first, so we can store that as a reference
      versionSchemas.put(prefix + "_" + version, getDbTool().describeDatabase(prefix));
      _comparisons.add(new DbComparison(prefix, Integer.parseInt(version), dbVersion, null, null));
    }
  }

  @Override
  public String toString() {
    return getDatabaseType() + "/" + _masterDB + ":" + getCreateVersion() + " >>> " + getTargetVersion();
  }
  
  private class DbComparison {
    
    private final String _prefix;
    private final int _expectedVersion;
    private final Integer _indicatedVersion;
    private final String _createDescription;
    private final String _migrateDescription;
    
    public DbComparison(String prefix, int expectedVersion, Integer indicatedVersion, String createDescription, String migrateDescription) {
      _prefix = prefix;
      _expectedVersion = expectedVersion;
      _indicatedVersion = indicatedVersion;
      _createDescription = createDescription;
      _migrateDescription = migrateDescription;
    }
    
    public String getPrefix() {
      return _prefix;
    }
    
    public int getExpectedVersion() {
      return _expectedVersion;
    }
    
    public Integer getIndicatedVersion() {
      return _indicatedVersion;
    }
    
    public String getCreateDescription() {
      return _createDescription;
    }
    
    public String getMigrateDescription() {
      return _migrateDescription;
    }
    
  }

}

