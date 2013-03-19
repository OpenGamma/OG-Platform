/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import static com.google.common.collect.Lists.newArrayList;
import static org.testng.AssertJUnit.assertEquals;
import static org.threeten.bp.temporal.ChronoUnit.HOURS;
import static org.threeten.bp.temporal.ChronoUnit.MINUTES;
import static org.threeten.bp.temporal.ChronoUnit.SECONDS;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneOffset;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests ModifyConfigDbConfigMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class ModifyConfigDbConfigMasterWorkerReplaceVersionsTest extends AbstractDbConfigMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyConfigDbConfigMasterWorkerReplaceVersionsTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifyConfigDbConfigMasterWorkerReplaceVersionsTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, false);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------  

  @Test
  public void test_ReplaceVersion_of_some_middle_version() {
    Clock origClock = _cfgMaster.getClock();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _cfgMaster.setClock(Clock.fixed(now.plus(2, HOURS), ZoneOffset.UTC));
      ConfigDocument latestDoc = _cfgMaster.get(baseOid, VersionCorrection.LATEST);
      Instant latestFrom = latestDoc.getVersionFromInstant();

      List<ConfigDocument> replacement = newArrayList();
      for (int i = 0; i <= 10; i++) {
        String val = "test" + i;
        ConfigDocument doc = new ConfigDocument(ConfigItem.of(val, "some_name_"+i));
        doc.setVersionFromInstant(latestFrom.plus(i, MINUTES));
        replacement.add(doc);
      }

      _cfgMaster.replaceVersion(latestDoc.getUniqueId(), replacement);

      ConfigSearchRequest<String> searchRequest = new ConfigSearchRequest<String>();
      searchRequest.addConfigId(baseOid);
      searchRequest.setVersionCorrection(VersionCorrection.LATEST);
      searchRequest.setType(String.class);
      ConfigSearchResult<String> result = _cfgMaster.search(searchRequest);      
      List<ConfigItem<String>> values = result.getValues();

      assertEquals(1, values.size());
      String val = values.get(0).getValue();
      assertEquals("test10", val);

    } finally {
      _cfgMaster.setClock(origClock);
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_ReplaceVersion_of_some_middle_version_timeBoundsNotExact() {
    Clock origClock = _cfgMaster.getClock();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _cfgMaster.setClock(Clock.fixed(now.plus(2, HOURS), ZoneOffset.UTC));
      ConfigDocument latestDoc = _cfgMaster.get(baseOid, VersionCorrection.LATEST);
      Instant latestFrom = latestDoc.getVersionFromInstant();

      List<ConfigDocument> replacement = newArrayList();
      for (int i = 1; i <= 10; i++) {
        String val = "test" + i;
        ConfigDocument doc = new ConfigDocument(ConfigItem.of(val));
        doc.setVersionFromInstant(latestFrom.plus(i, MINUTES));
        replacement.add(doc);
      }

      _cfgMaster.replaceVersion(latestDoc.getUniqueId(), replacement);

      ConfigSearchRequest<String> searchRequest = new ConfigSearchRequest<String>();
      searchRequest.addConfigId(baseOid);
      searchRequest.setVersionCorrection(VersionCorrection.LATEST);
      ConfigSearchResult<String> result = _cfgMaster.search(searchRequest);
      List<ConfigItem<String>> values = result.getValues();
      assertEquals(1, values.size());
      String val = values.get(0).getValue();
      assertEquals("test10", val);

    } finally {
      _cfgMaster.setClock(origClock);
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_ReplaceVersion_which_is_not_current() {
    Clock origClock = _cfgMaster.getClock();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _cfgMaster.setClock(Clock.fixed(now.plus(2, HOURS), ZoneOffset.UTC));


      ConfigHistoryRequest<String> historyRequest = new ConfigHistoryRequest<String>();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsToInstant(null);
      ConfigHistoryResult<String> result = _cfgMaster.history(historyRequest);
      List<ConfigDocument> values = result.getDocuments();

      ConfigDocument lastButOneDoc = values.get(values.size() - 1);
      Instant lastButOneDocVersionFrom = lastButOneDoc.getVersionFromInstant();

      List<ConfigDocument> replacement = newArrayList();
      for (int i = 1; i <= 10; i++) {
        String val = "test" + i;
        ConfigDocument doc = new ConfigDocument(ConfigItem.of(val));
        doc.setVersionFromInstant(lastButOneDocVersionFrom.plus(i, MINUTES));
        replacement.add(doc);
      }

      _cfgMaster.replaceVersion(lastButOneDoc.getUniqueId(), replacement);

    } finally {
      _cfgMaster.setClock(origClock);
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_ReplaceVersion_which_is_not_in_the_time_bounds_of_the_replaced_doc() {
    Clock origClock = _cfgMaster.getClock();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _cfgMaster.setClock(Clock.fixed(now.plus(2, HOURS), ZoneOffset.UTC));


      ConfigHistoryRequest<String> historyRequest = new ConfigHistoryRequest<String>();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsToInstant(null);
      ConfigHistoryResult<String> result = _cfgMaster.history(historyRequest);
      List<ConfigDocument> values = result.getDocuments();

      ConfigDocument lastButOneDoc = values.get(values.size() - 3);
      Instant lastButOneDocVersionFrom = lastButOneDoc.getVersionFromInstant();

      List<ConfigDocument> replacement = newArrayList();
      for (int i = 1; i <= 10; i++) {
        String val = "test" + i;
        ConfigDocument doc = new ConfigDocument(ConfigItem.of(val));
        doc.setVersionFromInstant(lastButOneDocVersionFrom.plus(i, MINUTES));
        replacement.add(doc);
      }

      _cfgMaster.replaceVersion(lastButOneDoc.getUniqueId(), replacement);

    } finally {
      _cfgMaster.setClock(origClock);
    }
  }

  @Test
  public void test_ReplaceVersions() {
    Clock origClock = _cfgMaster.getClock();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _cfgMaster.setClock(Clock.fixed(now.plus(2, HOURS), ZoneOffset.UTC));
      ConfigDocument latestDoc = _cfgMaster.get(baseOid, VersionCorrection.LATEST);
      Instant latestFrom = latestDoc.getVersionFromInstant();

      List<ConfigDocument> replacement = newArrayList();
      for (int i = 0; i <= 10; i++) {
        String val = "test" + i;
        ConfigDocument doc = new ConfigDocument(ConfigItem.of(val, "some_name_"+i));
        doc.setVersionFromInstant(latestFrom.plus(i, SECONDS));
        replacement.add(doc);
      }

      _cfgMaster.replaceVersions(latestDoc, replacement);

      ConfigHistoryRequest<String> historyRequest = new ConfigHistoryRequest<String>();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, HOURS));
      ConfigHistoryResult<String> result = _cfgMaster.history(historyRequest);
      List<ConfigDocument> values = result.getDocuments();

      assertEquals(15, values.size());

    } finally {
      _cfgMaster.setClock(origClock);
    }
  }

  @Test
  public void test_ReplaceVersions2() {
    Clock origClock = _cfgMaster.getClock();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _cfgMaster.setClock(Clock.fixed(now.plus(2, HOURS), ZoneOffset.UTC));
      ConfigDocument latestDoc = _cfgMaster.get(baseOid, VersionCorrection.LATEST);
      Instant latestFrom = latestDoc.getVersionFromInstant();

      List<ConfigDocument> replacement = newArrayList();
      for (int i = 0; i <= 10; i++) {
        String val = "test" + i;
        ConfigDocument doc = new ConfigDocument(ConfigItem.of(val, "some_name_"+i));
        doc.setVersionFromInstant(latestFrom.plus(i - 3, MINUTES));
        replacement.add(doc);
      }

      _cfgMaster.replaceVersions(latestDoc, replacement);

      ConfigHistoryRequest<String> historyRequest = new ConfigHistoryRequest<String>();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, HOURS));
      ConfigHistoryResult<String> result = _cfgMaster.history(historyRequest);
      List<ConfigDocument> values = result.getDocuments();

      assertEquals(12, values.size());

    } finally {
      _cfgMaster.setClock(origClock);
    }
  }

  @Test
  /**
   *
   *       |                        |             |
   *       |                        |             |
   *       |                        |             |                           
   *       |                        |             |                           
   *       |                        |             |                           
   *       |                        |             |                           
   *       |                        |             |                           
   *       |                        |             |                           
   *       |                        |             |                           
   *       |                        |             |                           
   *       |                        |             |                           
   *       |                        |             |                           
   *       |                        |             |                           
   *       |                        |             |                           
   *       |                        |             |                           
   *       |     setup_4            |             |                           
   *       |                        |             |                           
   *   +5m |------------------------|             |                           
   *       |                        |             |                           
   *       |     setup_3            |             |                           
   *       |                        |             |                           
   *   +4m |------------------------|             |                           
   *       |                        |             |      replace_4            
   *       |     setup_2            |  <-- +3m20s |----------------------------------->>>   
   *       |                        |             |      replace_3            
   *   +3m |------------------------|  <-- +3m00s |----------------------------------->>>   
   *       |                        |             |      replace_2            
   *       |                        |  <-- +2m40s |----------------------------------->>>   
   *       |     setup_1            |             |      replace_1            
   *       |                        |  <-- +2m20s |----------------------------------->>>   
   *       |                        |
   *       |                        |                       setup_1 (copy)
   *   +2m |------------------------ ... --------------------------------------------->>>                                                                              
   *       |                                                               
   *       |     setup_0                                   setup_0 (continuation)
   *       |                         
   *   +1m |------------------------ ... --------------------------------------------->>>                                       
   *
   *
   *   NOW =================================================================================
   *
   */
  public void test_ReplaceVersions3() {
    Clock origClock = _cfgMaster.getClock();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _cfgMaster.setClock(Clock.fixed(now.plus(2, HOURS), ZoneOffset.UTC));
      ConfigDocument latestDoc = _cfgMaster.get(baseOid, VersionCorrection.LATEST);
      Instant latestFrom = latestDoc.getVersionFromInstant();

      List<ConfigDocument> replacement = newArrayList();
      for (int i = 1; i <= 4; i++) {
        String val = "replace_" + i;
        ConfigDocument doc = new ConfigDocument(ConfigItem.of(val, "some_name_"+i));
        doc.setVersionFromInstant(now.plus(1, MINUTES).plus(i * 20, SECONDS));
        replacement.add(doc);
      }

      _cfgMaster.replaceVersions(latestDoc, replacement);

      ConfigHistoryRequest<String> historyRequest = new ConfigHistoryRequest<String>();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, HOURS));
      ConfigHistoryResult<String> result = _cfgMaster.history(historyRequest);
      List<ConfigDocument> values = result.getDocuments();

      assertEquals(6, values.size());

      latestDoc = _cfgMaster.get(baseOid, VersionCorrection.LATEST);
      latestFrom = latestDoc.getVersionFromInstant();
      assertEquals(now.plus(2, MINUTES).plus(20, SECONDS), latestFrom);

      assertEquals(now, values.get(5).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES), values.get(5).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES), values.get(4).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES).plus(20, SECONDS), values.get(4).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES).plus(20, SECONDS), values.get(3).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES).plus(40, SECONDS), values.get(3).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES).plus(40, SECONDS), values.get(2).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES).plus(60, SECONDS), values.get(2).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES).plus(60, SECONDS), values.get(1).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES).plus(80, SECONDS), values.get(1).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES).plus(80, SECONDS), values.get(0).getVersionFromInstant());
      assertEquals(null, values.get(0).getVersionToInstant());
    } finally {
      _cfgMaster.setClock(origClock);
    }
  }

  @Test
  /**
   *
   *       |                                      
   *       |                                      
   *       |                                                                 
   *       |                                                                 
   *       |                                                                 
   *       |                                                                 
   *       |                                                                 
   *       |                                                                 
   *       |                                                                 
   *       |                                                                 
   *       |                                                                 
   *       |                                                                 
   *       |                                                                 
   *       |                                                                 
   *       |                                                                 
   *       |     setup_4                                                     
   *       |                                                                 
   *   +5m |------------------------ ... --------------------------------------------->>>                                        
   *       |                                                                 
   *       |     setup_3                                                     
   *       |                                                                 
   *   +4m |------------------------ ... --------------------------------------------->>>        
   *       |                        |                      setup_2 (copy)
   *       |                        |
   *       |                        |  <-- +3m40s |----------------------------------->>>
   *       |                        |             |      replace_4            
   *       |     setup_2            |  <-- +3m20s |----------------------------------->>>   
   *       |                        |             |      replace_3            
   *   +3m |------------------------|  <-- +3m00s |----------------------------------->>>   
   *       |                        |             |      replace_2            
   *       |                        |  <-- +2m40s |----------------------------------->>>   
   *       |     setup_1            |             |      replace_1            
   *       |                        |  <-- +2m20s |----------------------------------->>>   
   *       |                        |
   *       |                        |                      setup_1 (copy)
   *   +2m |------------------------ ... --------------------------------------------->>>                                                                              
   *       |                                                               
   *       |     setup_0                                   setup_0 (continuation)
   *       |                         
   *   +1m |------------------------ ... --------------------------------------------->>>                                       
   *
   *
   *   NOW =================================================================================
   *
   */
  public void test_ReplaceVersions4() {
    Clock origClock = _cfgMaster.getClock();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _cfgMaster.setClock(Clock.fixed(now.plus(2, HOURS), ZoneOffset.UTC));
      ConfigDocument latestDoc = _cfgMaster.get(baseOid, VersionCorrection.LATEST);

      List<ConfigDocument> replacement = newArrayList();
      for (int i = 1; i <= 4; i++) {
        String val = "replace_" + i;
        ConfigDocument doc = new ConfigDocument(ConfigItem.of(val, "some_name_"+i));
        doc.setVersionFromInstant(now.plus(1, MINUTES).plus(i * 20, SECONDS));
        replacement.add(doc);
      }
      replacement.get(replacement.size() - 1).setVersionToInstant(now.plus(1, MINUTES).plus(100, SECONDS));

      _cfgMaster.replaceVersions(latestDoc, replacement);

      ConfigHistoryRequest<String> historyRequest = new ConfigHistoryRequest<String>();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, HOURS));
      ConfigHistoryResult<String> result = _cfgMaster.history(historyRequest);
      List<ConfigDocument> values = result.getDocuments();

      assertEquals(9, values.size());

      assertEquals(now, values.get(8).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES), values.get(8).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES), values.get(7).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES).plus(20, SECONDS), values.get(7).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES).plus(20, SECONDS), values.get(6).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES).plus(40, SECONDS), values.get(6).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES).plus(40, SECONDS), values.get(5).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES).plus(60, SECONDS), values.get(5).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES).plus(60, SECONDS), values.get(4).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES).plus(80, SECONDS), values.get(4).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES).plus(80, SECONDS), values.get(3).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES).plus(100, SECONDS), values.get(3).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES).plus(100, SECONDS), values.get(2).getVersionFromInstant());
      assertEquals(now.plus(3, MINUTES), values.get(2).getVersionToInstant());
      //
      assertEquals(now.plus(3, MINUTES), values.get(1).getVersionFromInstant());
      assertEquals(now.plus(4, MINUTES), values.get(1).getVersionToInstant());
      //
      assertEquals(now.plus(4, MINUTES), values.get(0).getVersionFromInstant());
      assertEquals(null, values.get(0).getVersionToInstant());

    } finally {
      _cfgMaster.setClock(origClock);
    }
  }


  @Test
  /**
   *
   *       |                                                                 
   *       |                                                                 
   *       |                                                                                                         
   *       |                          
   *       |     setup_4              
   *       |                          
   *   +4m |------------------------ ... ------------------------------------------------>>>        
   *       |                          
   *       |     setup_3                  
   *       |                            
   *   +3m |------------------------ ... ------------------------------------------------>>>      
   *       |                                             
   *       |     setup_2               
   *       |                          
   *       |                           
   *       |                           
   *   +2m |------------------------ ... ------------------------------------------------>>>                                                                   
   *       |                        |             setup_1 (copy)
   *       |     setup_1            |    
   *       |                        |     <-- +2m30s |----------------------------------->>> 
   *   +1m |------------------------|                |      replace_4                                                
   *       |                        |     <-- +2m00s |----------------------------------->>> 
   *       |                        |                |      replace_3                        
   *       |     setup_0            |     <-- +1m30s |----------------------------------->>>       
   *       |                        |                |      replace_2                        
   *   NOW |========================|     <-- +1m00s |----------------------------------->>> 
   *                                                 |      replace_1                        
   *                                      <-- +0m30s |----------------------------------->>> 
   *
   *
   *
   */
  public void test_ReplaceVersions5() {
    Clock origClock = _cfgMaster.getClock();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _cfgMaster.setClock(Clock.fixed(now.plus(2, HOURS), ZoneOffset.UTC));
      ConfigDocument latestDoc = _cfgMaster.get(baseOid, VersionCorrection.LATEST);

      List<ConfigDocument> replacement = newArrayList();
      for (int i = 1; i <= 4; i++) {
        String val = "replace_" + i;
        ConfigDocument doc = new ConfigDocument(ConfigItem.of(val, "some_name_"+i));
        doc.setVersionFromInstant(now.minus(60, SECONDS).plus(i * 30, SECONDS));
        replacement.add(doc);
      }
      replacement.get(replacement.size() - 1).setVersionToInstant(now.plus(90, SECONDS));

      _cfgMaster.replaceVersions(latestDoc, replacement);

      ConfigHistoryRequest<String> historyRequest = new ConfigHistoryRequest<String>();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, HOURS));
      ConfigHistoryResult<String> result = _cfgMaster.history(historyRequest);
      List<ConfigDocument> values = result.getDocuments();

      assertEquals(8, values.size());

      //
      assertEquals(now.plus(-30, SECONDS), values.get(7).getVersionFromInstant());
      assertEquals(now.plus(0, SECONDS), values.get(7).getVersionToInstant());
      //
      assertEquals(now.plus(0, SECONDS), values.get(6).getVersionFromInstant());
      assertEquals(now.plus(30, SECONDS), values.get(6).getVersionToInstant());
      //
      assertEquals(now.plus(30, SECONDS), values.get(5).getVersionFromInstant());
      assertEquals(now.plus(60, SECONDS), values.get(5).getVersionToInstant());
      //
      assertEquals(now.plus(60, SECONDS), values.get(4).getVersionFromInstant());
      assertEquals(now.plus(90, SECONDS), values.get(4).getVersionToInstant());
      //
      assertEquals(now.plus(90, SECONDS), values.get(3).getVersionFromInstant());
      assertEquals(now.plus(120, SECONDS), values.get(3).getVersionToInstant());
      //
      assertEquals(now.plus(120, SECONDS), values.get(2).getVersionFromInstant());
      assertEquals(now.plus(180, SECONDS), values.get(2).getVersionToInstant());
      //
      assertEquals(now.plus(180, SECONDS), values.get(1).getVersionFromInstant());
      assertEquals(now.plus(240, SECONDS), values.get(1).getVersionToInstant());
      //
      assertEquals(now.plus(240, SECONDS), values.get(0).getVersionFromInstant());
      assertEquals(null, values.get(0).getVersionToInstant());

    } finally {
      _cfgMaster.setClock(origClock);
    }
  }
}
