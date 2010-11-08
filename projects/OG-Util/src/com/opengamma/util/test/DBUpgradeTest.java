/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import com.opengamma.util.tuple.Triple;

/**
 * Tests the creation + upgrade sequence results in the same structure as a pure create.
 *
 * @author Andrew Griffin
 */
abstract public class DBUpgradeTest extends DBTest {
  
  private static final Map<String,Map<String,String>> s_targetSchema = new HashMap<String,Map<String,String>> ();
  
  private final List<Triple<String,String,String>> _comparisons = new LinkedList<Triple<String,String,String>> ();
  
  protected Map<String,String> getVersionSchemas () {
    Map<String,String> versionSchema = s_targetSchema.get (getDatabaseType ());
    if (versionSchema == null) {
      versionSchema = new HashMap<String,String> ();
      s_targetSchema.put (getDatabaseType (), versionSchema);
    }
    return versionSchema;
  }
  
  protected DBUpgradeTest(String databaseType, String databaseVersion) {
    super (databaseType, databaseVersion);
  }

  @Parameters
  public static Collection<Object[]> getParameters() {
    return getParameters(3);  // MAX_VALUE to test all upgrades
  }

  @Test
  public void testDatabaseUpgrade () {
    for (Triple<String,String,String> comparison : _comparisons) {
      /*
       * System.out.println(comparison.getFirst() + " expected:");
       * System.out.println(comparison.getSecond());
       * System.out.println(comparison.getFirst() + " found:");
       * System.out.println(comparison.getThird());
       */
      assertEquals(getDatabaseType() + ": " + comparison.getFirst (), comparison.getSecond (), comparison.getThird ());
    }
  }
  
  @Override
  public void tablesCreatedOrUpgraded (final String version) {
    final Map<String,String> versionSchemas = getVersionSchemas ();
    if (versionSchemas.containsKey (version)) {
      // if we've already done the full schema, then we want to test that this upgrade has given us the same (but defer the comparison)
      _comparisons.add (new Triple<String,String,String> (version, versionSchemas.get (version), getDbTool ().describeDatabase ()));
    } else {
      // tests are run with most recent full schema first, so we can store that as a reference
      versionSchemas.put (version, getDbTool ().describeDatabase ());
    }
  }

}
