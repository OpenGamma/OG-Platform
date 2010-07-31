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

  @Test
  public void simpleOperation() throws IOException {
    EnvironmentConfig envConfig = new EnvironmentConfig();
    envConfig.setAllowCreate(true);
    envConfig.setTransactional(true);
    File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    File dbDir = new File(tmpDir, "BerkeleyDBValueSpecificationIdentifierSourceTest.simpleOperation");
    dbDir.mkdirs();
    Environment dbEnvironment = new Environment(dbDir, envConfig);
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
    
    FileUtils.deleteDirectory(dbDir);
  }
}
