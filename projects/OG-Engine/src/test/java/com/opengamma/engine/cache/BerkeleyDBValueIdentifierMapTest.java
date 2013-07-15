/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.test.TestGroup;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

// TODO kirk 2010-08-02 -- We should have concurrency performance tests as well.
// The correctness is largely taken care of by the database transactions, which go
// far beyond normal synchronization in handling concurrency.

/**
 * A simple unit test of {@link BerkeleyDBIdentifierMap}.
 */
@Test(groups = TestGroup.INTEGRATION)
public class BerkeleyDBValueIdentifierMapTest {
  private static final Logger s_logger = LoggerFactory.getLogger(BerkeleyDBValueIdentifierMapTest.class);
  private static Set<File> s_dbDirsToDelete = new HashSet<File>();

  protected File createDbDir(String methodName) {
    File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    File dbDir = new File(tmpDir, "BerkeleyDBValueSpecification-" + System.currentTimeMillis() + "-" + methodName);
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
        s_logger.info("Deleting temp directory {}", f);
        FileUtils.deleteDirectory(f);
      } catch (IOException ioe) {
        s_logger.warn("Unable to recursively delete directory {}", f);
        // Just swallow it.
      }
    }
    s_dbDirsToDelete.clear();
  }

  private ValueSpecification getValueSpec(String valueName) {
    ValueSpecification valueSpec = new ValueSpecification("Value", ComputationTargetSpecification.of(UniqueId.of("scheme", valueName)),
        ValueProperties.with(ValuePropertyNames.FUNCTION, "mockFunctionId").get());
    return valueSpec;
  }

  @Test
  public void simpleOperation() throws IOException {
    File dbDir = createDbDir("simpleOperation");
    Environment dbEnvironment = createDbEnvironment(dbDir);
    FudgeContext fudgeContext = OpenGammaFudgeContext.getInstance();

    BerkeleyDBIdentifierMap idSource = new BerkeleyDBIdentifierMap(dbEnvironment, fudgeContext);
    idSource.start();

    Map<String, Long> identifiers = new HashMap<String, Long>();
    LongSet seenIdentifiers = new LongOpenHashSet();
    for (int i = 0; i < 10; i++) {
      String valueName = "value-" + i;
      ValueSpecification valueSpec = getValueSpec(valueName);
      long identifier = idSource.getIdentifier(valueSpec);
      assertFalse(seenIdentifiers.contains(identifier));
      seenIdentifiers.add(identifier);
      identifiers.put(valueName, identifier);
    }

    for (int j = 0; j < 5; j++) {
      Long2ObjectMap<ValueSpecification> valueSpecs = idSource.getValueSpecifications(seenIdentifiers);
      assertEquals(seenIdentifiers.size(), valueSpecs.size());
      for (int i = 0; i < 10; i++) {
        String valueName = "value-" + i;
        ValueSpecification valueSpec = getValueSpec(valueName);
        long identifier = idSource.getIdentifier(valueSpec);
        long existingIdentifier = identifiers.get(valueName);
        assertEquals(identifier, existingIdentifier);
        assertEquals(valueSpec, idSource.getValueSpecification(identifier));
        assertEquals(valueSpec, valueSpecs.get(identifier));
      }
    }

    idSource.stop();

    dbEnvironment.close();
  }

  @Test
  public void reloadPreservesMaxValue() throws IOException {
    File dbDir = createDbDir("reloadPreservesMaxValue");
    Environment dbEnvironment = createDbEnvironment(dbDir);
    FudgeContext fudgeContext = OpenGammaFudgeContext.getInstance();

    BerkeleyDBIdentifierMap idSource = new BerkeleyDBIdentifierMap(dbEnvironment, fudgeContext);
    idSource.start();
    String valueName = "value-5";
    ValueSpecification valueSpec = getValueSpec(valueName);
    long initialIdentifier = idSource.getIdentifier(valueSpec);

    // Cycle everything to simulate a clean shutdown and restart.
    idSource.stop();
    dbEnvironment.close();
    dbEnvironment = createDbEnvironment(dbDir);
    idSource = new BerkeleyDBIdentifierMap(dbEnvironment, fudgeContext);
    idSource.start();

    // Check we get the same thing back.
    valueName = "value-5";
    valueSpec = getValueSpec(valueName);
    long identifier = idSource.getIdentifier(valueSpec);
    assertEquals(initialIdentifier, identifier);

    // Check that the next one is the previous max + 1
    valueName = "value-99999";
    valueSpec = getValueSpec(valueName);
    identifier = idSource.getIdentifier(valueSpec);
    assertEquals(initialIdentifier + 1, identifier);
  }

  private void putPerformanceTestImpl(final boolean bulkOperation) {
    final int numRequirementNames = 100;
    final int numIdentifiers = 100;
    final long numSpecifications = ((long) numRequirementNames) * ((long) numIdentifiers);
    File dbDir = createDbDir("putPerformanceTest" + bulkOperation);
    Environment dbEnvironment = createDbEnvironment(dbDir);
    FudgeContext fudgeContext = OpenGammaFudgeContext.getInstance();
    BerkeleyDBIdentifierMap idSource = new BerkeleyDBIdentifierMap(dbEnvironment, fudgeContext);
    idSource.start();

    OperationTimer timer = new OperationTimer(s_logger, "Put performance test with {} elements", numSpecifications);

    if (bulkOperation) {
      bulkOperationGetIdentifier(numRequirementNames, numIdentifiers, idSource);
    } else {
      singleOperationGetIdentifier(numRequirementNames, numIdentifiers, idSource);
    }

    idSource.stop();
    long numMillis = timer.finished();

    double msPerPut = ((double) numMillis) / ((double) numSpecifications);
    double putsPerSecond = 1000.0 / msPerPut;

    s_logger.info("Split time was {}ms/put, {}puts/sec", msPerPut, putsPerSecond);

    dbEnvironment.close();
  }

  private void getPerformanceTestImpl(final boolean bulkOperation) {
    final int numRequirementNames = 100;
    final int numIdentifiers = 100;
    final long numSpecifications = ((long) numRequirementNames) * ((long) numIdentifiers);
    File dbDir = createDbDir("getPerformanceTest" + bulkOperation);
    Environment dbEnvironment = createDbEnvironment(dbDir);
    FudgeContext fudgeContext = OpenGammaFudgeContext.getInstance();
    BerkeleyDBIdentifierMap idSource = new BerkeleyDBIdentifierMap(dbEnvironment, fudgeContext);
    idSource.start();

    if (bulkOperation) {
      bulkOperationGetIdentifier(numRequirementNames, numIdentifiers, idSource);
    } else {
      singleOperationGetIdentifier(numRequirementNames, numIdentifiers, idSource);
    }

    OperationTimer timer = new OperationTimer(s_logger, "Get performance test with {} elements", numSpecifications);

    if (bulkOperation) {
      bulkOperationGetIdentifier(numRequirementNames, numIdentifiers, idSource);
    } else {
      singleOperationGetIdentifier(numRequirementNames, numIdentifiers, idSource);
    }

    long numMillis = timer.finished();
    idSource.stop();

    double msPerPut = ((double) numMillis) / ((double) numSpecifications);
    double putsPerSecond = 1000.0 / msPerPut;

    s_logger.info("Split time was {}ms/get, {}gets/sec", msPerPut, putsPerSecond);

    dbEnvironment.close();
  }

  @Test
  public void putPerformanceTest() {
    putPerformanceTestImpl(false);
  }

  @Test
  public void bulkPutPerformanceTest() {
    putPerformanceTestImpl(true);
  }

  @Test
  public void getPerformanceTest() {
    getPerformanceTestImpl(false);
  }

  @Test
  public void bulkGetPerformanceTest() {
    getPerformanceTestImpl(true);
  }

  @Test(timeOut = 30000)
  public void interruptThread() throws Throwable {
    final ExecutorService threads = Executors.newSingleThreadExecutor();
    try {
      final Thread main = Thread.currentThread();
      final Runnable interrupter = new Runnable() {
        @Override
        public void run() {
          try {
            Thread.sleep(1000);
            main.interrupt();
          } catch (InterruptedException e) {
            throw new OpenGammaRuntimeException("Interrupted", e);
          }
        }
      };
      threads.submit(interrupter);
      int count = 0;
      do {
        try {
          getPerformanceTest();
        } catch (OpenGammaRuntimeException e) {
          assertEquals("Interrupted", e.getMessage());
          count++;
          if (count <= 5) {
            threads.submit(interrupter);
          } else {
            break;
          }
        }
      } while (true);
    } finally {
      threads.shutdown();
      Thread.interrupted();
      threads.awaitTermination(5, TimeUnit.SECONDS);
    }
  }

  /**
   * @param numRequirementNames
   * @param numIdentifiers
   * @param idSource
   */
  private void singleOperationGetIdentifier(final int numRequirementNames, final int numIdentifiers, BerkeleyDBIdentifierMap idSource) {
    for (int iRequirementName = 0; iRequirementName < numRequirementNames; iRequirementName++) {
      String requirementName = "req-" + iRequirementName;

      for (int iIdentifier = 0; iIdentifier < numIdentifiers; iIdentifier++) {
        String identifierName = "identifier-" + iIdentifier;
        ValueSpecification valueSpec = new ValueSpecification(requirementName, ComputationTargetSpecification.of(UniqueId.of("scheme", identifierName)),
            ValueProperties.with(ValuePropertyNames.FUNCTION, "mockFunctionId").get());
        // Just throw away the actual identifier. We don't care.
        idSource.getIdentifier(valueSpec);
      }
    }
  }

  /**
   * @param numRequirementNames
   * @param numIdentifiers
   * @param idSource
   */
  private void bulkOperationGetIdentifier(final int numRequirementNames, final int numIdentifiers, BerkeleyDBIdentifierMap idSource) {
    for (int iRequirementName = 0; iRequirementName < numRequirementNames; iRequirementName++) {
      final Collection<ValueSpecification> valueSpecs = new ArrayList<ValueSpecification>(numIdentifiers);
      final String requirementName = "req-" + iRequirementName;
      for (int iIdentifier = 0; iIdentifier < numIdentifiers; iIdentifier++) {
        final String identifierName = "identifier-" + iIdentifier;
        valueSpecs.add(new ValueSpecification(requirementName, ComputationTargetSpecification.of(UniqueId.of("scheme", identifierName)),
            ValueProperties.with(ValuePropertyNames.FUNCTION, "mockFunctionId").get()));
      }
      idSource.getIdentifiers(valueSpecs);
    }
  }
}
