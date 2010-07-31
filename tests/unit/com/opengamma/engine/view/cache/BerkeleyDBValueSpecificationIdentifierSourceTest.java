/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.fudgemsg.FudgeContext;
import org.junit.AfterClass;
import org.junit.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueIdentifier;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

/**
 * A simple unit test of {@link BerkeleyDBValueSpecificationIdentifierSource}.
 */
public class BerkeleyDBValueSpecificationIdentifierSourceTest {
  private static Set<File> s_dbDirsToDelete = new HashSet<File>();
  
  protected File createDbDir(String methodName) {
    File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    File dbDir = new File(tmpDir, "BerkeleyDBValueSpecificationIdentifierSourceTest." + methodName);
    dbDir.mkdirs();
    s_dbDirsToDelete.add(dbDir);
    return dbDir;
  }
  
  protected Environment createDbEnvironment(File dbDir) {
    EnvironmentConfig envConfig = new EnvironmentConfig();
    envConfig.setAllowCreate(true);
    envConfig.setTransactional(true);
    Environment dbEnvironment = new Environment(dbDir, envConfig);
    return dbEnvironment;
  }
  
  @AfterClass
  public static void deleteDbDirs() {
    for (File f : s_dbDirsToDelete) {
      try {
        FileUtils.deleteDirectory(f);
      } catch (IOException ioe) {
        // Just swallow it.
      }
    }
    s_dbDirsToDelete.clear();
  }

  @Test
  public void simpleOperation() throws IOException {
    File dbDir = createDbDir("simpleOperation");
    Environment dbEnvironment = createDbEnvironment(dbDir);
    FudgeContext fudgeContext = new FudgeContext();
    
    BerkeleyDBValueSpecificationIdentifierSource idSource = new BerkeleyDBValueSpecificationIdentifierSource(dbEnvironment, BerkeleyDBValueSpecificationIdentifierSource.DEFAULT_DATABASE_NAME, fudgeContext);
    idSource.start();
    
    Map<String, Long> identifiers = new HashMap<String, Long>();
    Set<Long> seenIdentifiers = new HashSet<Long>();
    for (int i = 0; i < 10; i++) {
      String valueName = "value-" + i;
      ValueSpecification valueSpec = new ValueSpecification(new ValueRequirement("value", new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("scheme", valueName))));
      long identifier = idSource.getIdentifier(valueSpec);
      assertFalse(seenIdentifiers.contains(identifier));
      identifiers.put(valueName, identifier);
    }
    
    for (int j = 0; j < 5; j++) {
      for (int i = 0; i < 10; i++) {
        String valueName = "value-" + i;
        ValueSpecification valueSpec = new ValueSpecification(new ValueRequirement("value", new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("scheme", valueName))));
        long identifier = idSource.getIdentifier(valueSpec);
        long existingIdentifier = identifiers.get(valueName);
        assertEquals(identifier, existingIdentifier);
      }
    }
    
    idSource.stop();
    
    dbEnvironment.close();
  }
  
  @Test
  public void reloadPreservesMaxValue() throws IOException {
    File dbDir = createDbDir("reloadPreservesMaxValue");
    Environment dbEnvironment = createDbEnvironment(dbDir);
    FudgeContext fudgeContext = new FudgeContext();
    
    BerkeleyDBValueSpecificationIdentifierSource idSource = new BerkeleyDBValueSpecificationIdentifierSource(dbEnvironment, BerkeleyDBValueSpecificationIdentifierSource.DEFAULT_DATABASE_NAME, fudgeContext);
    idSource.start();
    String valueName = "value-5";
    ValueSpecification valueSpec = new ValueSpecification(new ValueRequirement("value", new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("scheme", valueName))));
    long initialIdentifier = idSource.getIdentifier(valueSpec);
    
    // Cycle everything to simulate a clean shutdown and restart.
    idSource.stop();
    dbEnvironment.close();
    dbEnvironment = createDbEnvironment(dbDir);
    idSource = new BerkeleyDBValueSpecificationIdentifierSource(dbEnvironment, BerkeleyDBValueSpecificationIdentifierSource.DEFAULT_DATABASE_NAME, fudgeContext);
    idSource.start();
    
    // Check we get the same thing back.
    valueName = "value-5";
    valueSpec = new ValueSpecification(new ValueRequirement("value", new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("scheme", valueName))));
    long identifier = idSource.getIdentifier(valueSpec);
    assertEquals(initialIdentifier, identifier);
    
    // Check that the next one is the previous max + 1
    valueName = "value-99999";
    valueSpec = new ValueSpecification(new ValueRequirement("value", new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("scheme", valueName))));
    identifier = idSource.getIdentifier(valueSpec);
    assertEquals(initialIdentifier + 1, identifier);
  }
}
