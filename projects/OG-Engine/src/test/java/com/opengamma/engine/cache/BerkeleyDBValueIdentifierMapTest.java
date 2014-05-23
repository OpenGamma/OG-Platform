/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
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
public class BerkeleyDBValueIdentifierMapTest extends AbstractIdentifierMapTest {
  private static final Logger s_logger = LoggerFactory.getLogger(BerkeleyDBValueIdentifierMapTest.class);
  private static Set<File> s_dbDirsToDelete = new HashSet<File>();
  private Environment _currDBEnvironment;

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

  @AfterClass(alwaysRun = true)
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

  @Override
  protected IdentifierMap createIdentifierMap(String testName) {
    File dbDir = createDbDir("simpleOperation");
    _currDBEnvironment = createDbEnvironment(dbDir);
    FudgeContext fudgeContext = OpenGammaFudgeContext.getInstance();

    BerkeleyDBIdentifierMap idMap = new BerkeleyDBIdentifierMap(_currDBEnvironment, fudgeContext);
    idMap.start();
    return idMap;
  }

  @AfterMethod
  public void tearDownEnvironment() {
    if (_currDBEnvironment != null) {
      _currDBEnvironment.close();
      _currDBEnvironment = null;
    }
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
        } finally {
          tearDownEnvironment();
        }
      } while (true);
    } finally {
      threads.shutdown();
      Thread.interrupted();
      threads.awaitTermination(5, TimeUnit.SECONDS);
    }
  }

}
