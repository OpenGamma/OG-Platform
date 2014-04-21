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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.script.DbSchemaGroupMetadata;
import com.opengamma.util.db.script.DbScriptUtils;
import com.opengamma.util.db.tool.DbTool;
import com.opengamma.util.db.tool.DbTool.TableCreationCallback;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Triple;

/**
 * Tests the creation + upgrade sequence results in the same structure as a pure create.
 */
public abstract class AbstractDbUpgradeTest implements TableCreationCallback {

  private static final Map<String, Map<String, String>> s_targetSchema = Maps.newHashMap();

  private final List<Triple<String, String, String>> _comparisons = Lists.newLinkedList();

  private final String _schemaGroupName;
  private final String _databaseType;
  private final int _targetVersion;
  private final int _createVersion;
  private volatile DbTool _dbTool;

  static {
    // initialize the clock
    DateUtils.initTimeZone();
  }

  /**
   * Creates an instance.
   * 
   * @param databaseType  the database type, not null
   * @param schemaGroupName  the schema group name, not null
   * @param targetVersion  the target version
   * @param createVersion  the create version
   */
  protected AbstractDbUpgradeTest(String databaseType, String schemaGroupName, final int targetVersion, final int createVersion) {
    ArgumentChecker.notNull(schemaGroupName, "schameGroupName");
    ArgumentChecker.notNull(databaseType, "databaseType");
    _schemaGroupName = schemaGroupName;
    _databaseType = databaseType;
    _targetVersion = targetVersion;
    _createVersion = createVersion;
  }

  //-------------------------------------------------------------------------
  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    DbTool dbTool = getDbTool();
    dbTool.setTargetVersion(_targetVersion);
    dbTool.setCreateVersion(_createVersion);
    dbTool.dropTestSchema();
    dbTool.createTestSchema();
    dbTool.createTables(DbScriptUtils.getDbSchemaGroupMetadata(_schemaGroupName), dbTool.getTestCatalog(), dbTool.getTestSchema(), _targetVersion, _createVersion, this);
    dbTool.clearTestTables();
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() {
    // need to clear version cache from here
    // this is messy but necessary
    AbstractDbTest.s_databaseTypeVersion.clear();
  }

  //-------------------------------------------------------------------------
  protected DbTool getDbTool() {
    return initDbTool();
  }

  /**
   * Initializes the DBTool outside the constructor.
   * This works better with TestNG and Maven, where the constructor is called
   * even if the test is never run.
   */
  private DbTool initDbTool() {
    DbTool dbTool = _dbTool;
    if (dbTool == null) {
      synchronized (this) {
        dbTool = _dbTool;
        if (dbTool == null) {
          _dbTool = dbTool = DbTest.createDbTool(_databaseType, null);  // CSIGNORE
        }
      }
    }
    return dbTool;
  }

  //-------------------------------------------------------------------------
  protected Map<String, String> getVersionSchemas() {
    Map<String, String> versionSchema = s_targetSchema.get(_databaseType);
    if (versionSchema == null) {
      versionSchema = new HashMap<>();
      s_targetSchema.put(_databaseType, versionSchema);
    }
    return versionSchema;
  }

  //-------------------------------------------------------------------------
  @Test(groups = TestGroup.UNIT_DB)
  public void testDatabaseUpgrade() {
    for (Triple<String, String, String> comparison : _comparisons) {
      /*
       * System.out.println(comparison.getFirst() + " expected:");
       * System.out.println(comparison.getSecond());
       * System.out.println(comparison.getFirst() + " found:");
       * System.out.println(comparison.getThird());
       */
      int diff = StringUtils.indexOfDifference(comparison.getSecond(), comparison.getThird());
      if (diff >= 0) {
        System.err.println("Difference at " + diff + "in " + _databaseType + "/" + comparison.getFirst());
        System.err.println("Upgraded --->..." + StringUtils.substring(comparison.getSecond(), diff - 200, diff) +
          "<-!!!->" + StringUtils.substring(comparison.getSecond(), diff, diff + 200) + "...");
        System.err.println(" Created --->..." + StringUtils.substring(comparison.getThird(), diff - 200, diff) +
          "<-!!!->" + StringUtils.substring(comparison.getThird(), diff, diff + 200) + "...");
      }
      assertEquals(_databaseType + ": " + comparison.getFirst(), comparison.getSecond(), comparison.getThird());
    }
  }

  @Override
  public void tablesCreatedOrUpgraded(final int version, final DbSchemaGroupMetadata schemaGroupMetadata) {
    final Map<String, String> versionSchemas = getVersionSchemas();
    String key = schemaGroupMetadata.getSchemaGroupName() + "_" + version;
    if (versionSchemas.containsKey(key)) {
      // if we've already done the full schema, then we want to test that this upgrade has given us the same (but defer the comparison)
      _comparisons.add(Triple.of(key, versionSchemas.get(key), _dbTool.describeDatabase(schemaGroupMetadata.getSchemaGroupName())));
    } else {
      // tests are run with most recent full schema first, so we can store that as a reference
      versionSchemas.put(key, _dbTool.describeDatabase(schemaGroupMetadata.getSchemaGroupName()));
    }
  }

  @Override
  public String toString() {
    return _databaseType + "/" + _schemaGroupName + ":" + _createVersion + " >>> " + _targetVersion;
  }

}
