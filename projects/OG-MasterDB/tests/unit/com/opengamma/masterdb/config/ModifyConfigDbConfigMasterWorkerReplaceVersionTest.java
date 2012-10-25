/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import static com.google.common.collect.Lists.newArrayList;
import static org.testng.AssertJUnit.assertEquals;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.time.Instant;
import javax.time.TimeSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.test.DbTest;

/**
 * Tests ModifyConfigDbConfigMasterWorker.
 */
public class ModifyConfigDbConfigMasterWorkerReplaceVersionTest extends AbstractDbConfigMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyConfigDbConfigMasterWorkerReplaceVersionTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifyConfigDbConfigMasterWorkerReplaceVersionTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, false);
    s_logger.info("running testcases for {}", databaseType);
  }

  @Test
  public void test_ReplaceVersion_of_some_middle_version() {
    TimeSource origTimeSource = _cfgMaster.getTimeSource();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _cfgMaster.setTimeSource(TimeSource.fixed(now.plus(2, TimeUnit.HOURS)));
      ConfigDocument latestDoc = _cfgMaster.get(baseOid, VersionCorrection.LATEST);
      Instant latestFrom = latestDoc.getVersionFromInstant();

      List<ConfigDocument> replacement = newArrayList();
      for (int i = 0; i <= 10; i++) {
        String val = "test" + i;        
        ConfigDocument doc = new ConfigDocument(ConfigItem.of(val, "some_name_"+i));
        doc.setVersionFromInstant(latestFrom.plus(i, TimeUnit.MINUTES));
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
      _cfgMaster.setTimeSource(origTimeSource);
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_ReplaceVersion_of_some_middle_version_timeBoundsNotExact() {
    TimeSource origTimeSource = _cfgMaster.getTimeSource();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _cfgMaster.setTimeSource(TimeSource.fixed(now.plus(2, TimeUnit.HOURS)));
      ConfigDocument latestDoc = _cfgMaster.get(baseOid, VersionCorrection.LATEST);
      Instant latestFrom = latestDoc.getVersionFromInstant();

      List<ConfigDocument> replacement = newArrayList();
      for (int i = 1; i <= 10; i++) {
        String val = "test" + i;
        ConfigDocument doc = new ConfigDocument(ConfigItem.of(val));
        doc.setVersionFromInstant(latestFrom.plus(i, TimeUnit.MINUTES));
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
      _cfgMaster.setTimeSource(origTimeSource);
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_ReplaceVersion_which_is_not_current() {
    TimeSource origTimeSource = _cfgMaster.getTimeSource();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _cfgMaster.setTimeSource(TimeSource.fixed(now.plus(2, TimeUnit.HOURS)));

      ConfigHistoryRequest<String> historyRequest = new ConfigHistoryRequest<String>();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsToInstant(null);
      ConfigHistoryResult<String> result = _cfgMaster.history(historyRequest);
      List<ConfigDocument> confDocs = result.getDocuments();

      ConfigDocument lastButOneDoc = confDocs.get(confDocs.size() - 1);
      Instant lastButOneDocVersionFrom = lastButOneDoc.getVersionFromInstant();


      List<ConfigDocument> replacement = newArrayList();
      for (int i = 1; i <= 10; i++) {
        String val = "test" + i;
        ConfigDocument doc = new ConfigDocument(ConfigItem.of(val));
        doc.setVersionFromInstant(lastButOneDocVersionFrom.plus(i, TimeUnit.MINUTES));
        replacement.add(doc);
      }

      _cfgMaster.replaceVersion(lastButOneDoc.getUniqueId(), replacement);

    } finally {
      _cfgMaster.setTimeSource(origTimeSource);
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_ReplaceVersion_which_is_not_in_the_time_bounds_of_the_replaced_doc() {
    TimeSource origTimeSource = _cfgMaster.getTimeSource();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _cfgMaster.setTimeSource(TimeSource.fixed(now.plus(2, TimeUnit.HOURS)));


      ConfigHistoryRequest<String> historyRequest = new ConfigHistoryRequest<String>();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsToInstant(null);
      ConfigHistoryResult<String> result = _cfgMaster.history(historyRequest);
      List<ConfigDocument> confDocs = result.getDocuments();

      ConfigDocument lastButOneDoc = confDocs.get(confDocs.size() - 3);
      Instant lastButOneDocVersionFrom = lastButOneDoc.getVersionFromInstant();


      List<ConfigDocument> replacement = newArrayList();
      for (int i = 1; i <= 10; i++) {
        String val = "test" + i;
        ConfigDocument doc = new ConfigDocument(ConfigItem.of(val));
        doc.setVersionFromInstant(lastButOneDocVersionFrom.plus(i, TimeUnit.MINUTES));
        replacement.add(doc);
      }

      _cfgMaster.replaceVersion(lastButOneDoc.getUniqueId(), replacement);

    } finally {
      _cfgMaster.setTimeSource(origTimeSource);
    }
  }

  @Test
  public void test_ReplaceVersions() {
    TimeSource origTimeSource = _cfgMaster.getTimeSource();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _cfgMaster.setTimeSource(TimeSource.fixed(now.plus(2, TimeUnit.HOURS)));
      ConfigDocument latestDoc = _cfgMaster.get(baseOid, VersionCorrection.LATEST);
      Instant latestFrom = latestDoc.getVersionFromInstant();


      List<ConfigDocument> replacement = newArrayList();
      for (int i = 0; i <= 10; i++) {
        String val = "test" + i;
        ConfigDocument doc = new ConfigDocument(ConfigItem.of(val, "some_name_"+i));
        doc.setVersionFromInstant(latestFrom.plus(i, TimeUnit.SECONDS));
        replacement.add(doc);
      }

      _cfgMaster.replaceVersions(latestDoc, replacement);

      ConfigHistoryRequest<String> historyRequest = new ConfigHistoryRequest<String>();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, TimeUnit.HOURS));
      ConfigHistoryResult<String> result = _cfgMaster.history(historyRequest);
      List<ConfigDocument> confDocs = result.getDocuments();

      assertEquals(15, confDocs.size());

    } finally {
      _cfgMaster.setTimeSource(origTimeSource);
    }
  }

  @Test
  public void test_ReplaceVersions2() {
    TimeSource origTimeSource = _cfgMaster.getTimeSource();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _cfgMaster.setTimeSource(TimeSource.fixed(now.plus(2, TimeUnit.HOURS)));
      ConfigDocument latestDoc = _cfgMaster.get(baseOid, VersionCorrection.LATEST);
      Instant latestFrom = latestDoc.getVersionFromInstant();


      List<ConfigDocument> replacement = newArrayList();
      for (int i = 0; i <= 10; i++) {
        String val = "test" + i;
        ConfigDocument doc = new ConfigDocument(ConfigItem.of(val, "some_name_"+i));
        doc.setVersionFromInstant(latestFrom.plus(i - 3, TimeUnit.MINUTES));
        replacement.add(doc);
      }

      _cfgMaster.replaceVersions(latestDoc, replacement);

      ConfigHistoryRequest<String> historyRequest = new ConfigHistoryRequest<String>();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, TimeUnit.HOURS));
      ConfigHistoryResult<String> result = _cfgMaster.history(historyRequest);
      List<ConfigDocument> confDocs = result.getDocuments();

      assertEquals(12, confDocs.size());

    } finally {
      _cfgMaster.setTimeSource(origTimeSource);
    }
  }

  /**
   *
   *       |                        |             |                                      
   *       |                        |             |                                      
   *       |     setup_4            |             |                                      
   *       |                        |             |                                      
   *   +5m |------------------------|             |                                      
   *       |                        |             |                                      
   *       |     setup_3            |             |                                      
   *       |                        |             |                                      
   *   +4m |------------------------|             |                                      
   *       |                        |             |                                      
   *       |     setup_2            |             |                                         
   *       |                        |             |                        
   *   +3m |------------------------|   |         |
   *       |                        |             |      replace_3                       
   *       |                        |  <-- +2m30s |----------------------------------->>>   
   *       |     setup_1            |             |      replace_2                       
   *       |                        |  <-- +2m20s |----------------------------------->>>   
   *       |                        |             |      replace_1                       
   *       |                        |  <-- +2m10s |----------------------------------->>>
   *   +2m |------------------------                                                                               
   *       |                                                               
   *       |     setup_0             
   *       |                         
   *   +1m |------------------------ ... --------------------------------------------->>>                                       
   *
   *
   *   NOW =================================================================================
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_ReplaceVersion_notInTimeBounds() {
    TimeSource origTimeSource = _cfgMaster.getTimeSource();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _cfgMaster.setTimeSource(TimeSource.fixed(now.plus(2, TimeUnit.HOURS)));
      @SuppressWarnings("unused")
      ConfigDocument latestDoc = _cfgMaster.get(baseOid, VersionCorrection.LATEST);
      
      List<ConfigDocument> replacement = newArrayList();
      for (int i = 1; i <= 3; i++) {
        String val = "replace_" + i;
        ConfigDocument doc = new ConfigDocument(ConfigItem.of(val));
        doc.setVersionFromInstant(now.plus(1, TimeUnit.MINUTES).plus(i * 10, TimeUnit.SECONDS));
        replacement.add(doc);
      }

      ConfigHistoryRequest<String> historyRequest = new ConfigHistoryRequest<String>();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, TimeUnit.HOURS));
      ConfigHistoryResult<String> result = _cfgMaster.history(historyRequest);
      List<ConfigDocument> confDocs = result.getDocuments();

      ConfigDocument secondVersionDoc = confDocs.get(confDocs.size() - 2);

      _cfgMaster.replaceVersion(secondVersionDoc.getUniqueId(), replacement);

    } finally {
      _cfgMaster.setTimeSource(origTimeSource);
    }
  }

  /**
   *
   *       |                        |                                                   
   *       |                        |                                                   
   *       |     setup_4            |                                                   
   *       |                        |                                                   
   *   +4m |------------------------|                                                   
   *       |                        |                                                   
   *       |     setup_3            |                                                   
   *       |                        |                                                   
   *   +3m |------------------------|                                                   
   *       |                        |                                                   
   *       |     setup_2            |                                                      
   *       |                        |                                     
   *   +2m |------------------------|   |         
   *       |                        |  <-- +1m40s |----------------------------------->>>
   *       |                        |             |      replace_3                       
   *       |                        |  <-- +1m30s |----------------------------------->>>   
   *       |     setup_1            |             |      replace_2                       
   *       |                        |  <-- +1m20s |----------------------------------->>>   
   *       |                        |             |      replace_1                       
   *       |                        |  <-- +1m10s |----------------------------------->>>
   *   +1m |------------------------                                                                               
   *       |                                                               
   *       |     setup_0             
   *       |                         
   *   NOW ===========================================================================>>>                                       
   *
   *
   *
   *
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_ReplaceVersion_inTimeBounds() {
    TimeSource origTimeSource = _cfgMaster.getTimeSource();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _cfgMaster.setTimeSource(TimeSource.fixed(now.plus(2, TimeUnit.HOURS)));
      @SuppressWarnings("unused")
      ConfigDocument latestDoc = _cfgMaster.get(baseOid, VersionCorrection.LATEST);

      List<ConfigDocument> replacement = newArrayList();
      for (int i = 1; i <= 3; i++) {
        String val = "replace_" + i;
        ConfigDocument doc = new ConfigDocument(ConfigItem.of(val));
        doc.setVersionFromInstant(now.plus(1, TimeUnit.MINUTES).plus(i * 10, TimeUnit.SECONDS));
        replacement.add(doc);
      }
      replacement.get(replacement.size() - 1).setVersionToInstant(now.plus(1, TimeUnit.MINUTES).plus(40, TimeUnit.SECONDS));

      ConfigHistoryRequest<String> historyRequest = new ConfigHistoryRequest<String>();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, TimeUnit.HOURS));
      ConfigHistoryResult<String> result = _cfgMaster.history(historyRequest);
      List<ConfigDocument> confDocs = result.getDocuments();

      ConfigDocument secondVersionDoc = confDocs.get(confDocs.size() - 2);

      _cfgMaster.replaceVersion(secondVersionDoc.getUniqueId(), replacement);
    } finally {
      _cfgMaster.setTimeSource(origTimeSource);
    }
  }

  /**
   *
   *       |                        |                                                   
   *       |                        |                                                   
   *       |     setup_4            |                                                   
   *       |                        |                                                   
   *   +4m |------------------------|                                                   
   *       |                        |                                                   
   *       |     setup_3            |                                                   
   *       |                        |                                                   
   *   +3m |------------------------|                                                   
   *       |                        |                                                   
   *       |     setup_2            |                                                      
   *       |                        |                                     
   *   +2m |------------------------|  <-- +2m00s |----------------------------------->>>          
   *       |                        |             |      replace_3                          
   *       |     setup_1            |  <-- +1m40s |----------------------------------->>>
   *       |                        |             |      replace_2                          
   *       |                        |  <-- +1m20s |----------------------------------->>>
   *       |                        |             |      replace_1                       
   *   +1m |------------------------   <-- +1m00s |----------------------------------->>>                                                                            
   *       |                                                               
   *       |     setup_0             
   *       |                         
   *   NOW ===========================================================================>>>                                       
   *
   *
   *
   *
   */
  @Test
  public void test_ReplaceVersion_atTimeBounds() {
    TimeSource origTimeSource = _cfgMaster.getTimeSource();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _cfgMaster.setTimeSource(TimeSource.fixed(now.plus(2, TimeUnit.HOURS)));


      List<ConfigDocument> replacement = newArrayList();
      for (int i = 0; i <= 2; i++) {
        String val = "replace_" + i;
        ConfigDocument doc = new ConfigDocument(ConfigItem.of(val, "some_name_"+i));
        doc.setVersionFromInstant(now.plus(1, TimeUnit.MINUTES).plus(i * 20, TimeUnit.SECONDS));
        replacement.add(doc);
      }
      replacement.get(replacement.size() - 1).setVersionToInstant(now.plus(2, TimeUnit.MINUTES));

      ConfigHistoryRequest<String> historyRequest = new ConfigHistoryRequest<String>();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, TimeUnit.HOURS));
      ConfigHistoryResult<String> result = _cfgMaster.history(historyRequest);
      List<ConfigDocument> confDocs = result.getDocuments();

      ConfigDocument secondVersionDoc = confDocs.get(confDocs.size() - 2);

      _cfgMaster.replaceVersion(secondVersionDoc.getUniqueId(), replacement);

      historyRequest = new ConfigHistoryRequest<String>();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, TimeUnit.HOURS));
      result = _cfgMaster.history(historyRequest);
      confDocs = result.getDocuments();

      assertEquals(7, confDocs.size());

      assertEquals(now, confDocs.get(6).getVersionFromInstant());
      assertEquals(now.plus(1, TimeUnit.MINUTES), confDocs.get(6).getVersionToInstant());
      //
      assertEquals(now.plus(1, TimeUnit.MINUTES), confDocs.get(5).getVersionFromInstant());
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(20, TimeUnit.SECONDS), confDocs.get(5).getVersionToInstant());
      //
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(20, TimeUnit.SECONDS), confDocs.get(4).getVersionFromInstant());
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(40, TimeUnit.SECONDS), confDocs.get(4).getVersionToInstant());
      //
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(40, TimeUnit.SECONDS), confDocs.get(3).getVersionFromInstant());
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(60, TimeUnit.SECONDS), confDocs.get(3).getVersionToInstant());
      //
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(60, TimeUnit.SECONDS), confDocs.get(2).getVersionFromInstant());
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(120, TimeUnit.SECONDS), confDocs.get(2).getVersionToInstant());
      //
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(120, TimeUnit.SECONDS), confDocs.get(1).getVersionFromInstant());
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(180, TimeUnit.SECONDS), confDocs.get(1).getVersionToInstant());
      //
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(180, TimeUnit.SECONDS), confDocs.get(0).getVersionFromInstant());
      assertEquals(null, confDocs.get(0).getVersionToInstant());
    } finally {
      _cfgMaster.setTimeSource(origTimeSource);
    }
  }

  /**
   *
   *       |                                      |                            |                                  
   *       |                                      |                            |                                  
   *       |     setup_4                          |                            |      setup_4                                  
   *       |                                      |                            |                                  
   *   +4m |------------------------              |                            |-------------------                                  
   *       |                                      |                            |                                  
   *       |     setup_3                          |                            |      setup_3                            
   *       |                                      |                            |                                  
   *   +3m |------------------------              |                            |-------------------                                  
   *       |                                      |                            |                                  
   *       |     setup_2                          |                      =>    |      setup_2                                     
   *       |                                      |                            |                    
   *   +2m |------------------------   <-- +2m00s |                            |-------------------                   
   *       |                                      |      replace_2             |      replace_2                       
   *       |     setup_1               <-- +1m40s |-------------------         |-------------------
   *       |                                      |      replace_1             |      replace_1    
   *       |                           <-- +1m20s |-------------------         |-------------------
   *       |                                      |      replace_0             |      replace_0    
   *   +1m |------------------------   <-- +1m00s |-------------------         |-------------------                                                                         
   *       |                                                               
   *       |     setup_0                                                              setup_0
   *       |                         
   *   NOW ======================================================================================>>>                                       
   *
   *
   *
   *
   */
  @Test
  public void test_ReplaceVersion_atTimeBounds2() {
    TimeSource origTimeSource = _cfgMaster.getTimeSource();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _cfgMaster.setTimeSource(TimeSource.fixed(now.plus(2, TimeUnit.HOURS)));


      List<ConfigDocument> replacement = newArrayList();
      for (int i = 0; i <= 2; i++) {
        String val = "replace_" + i;
        ConfigDocument doc = new ConfigDocument(ConfigItem.of(val, "some_name_"+i));
        doc.setVersionFromInstant(now.plus(1, TimeUnit.MINUTES).plus(i * 20, TimeUnit.SECONDS));
        replacement.add(doc);
      }

      ConfigHistoryRequest<String> historyRequest = new ConfigHistoryRequest<String>();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, TimeUnit.HOURS));
      ConfigHistoryResult<String> result = _cfgMaster.history(historyRequest);
      List<ConfigDocument> confDocs = result.getDocuments();

      ConfigDocument secondVersionDoc = confDocs.get(confDocs.size() - 2);

      _cfgMaster.replaceVersion(secondVersionDoc.getUniqueId(), replacement);

      historyRequest = new ConfigHistoryRequest<String>();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, TimeUnit.HOURS));
      result = _cfgMaster.history(historyRequest);
      confDocs = result.getDocuments();

      assertEquals(7, confDocs.size());

      assertEquals(now, confDocs.get(6).getVersionFromInstant());
      assertEquals(now.plus(1, TimeUnit.MINUTES), confDocs.get(6).getVersionToInstant());
      //
      assertEquals(now.plus(1, TimeUnit.MINUTES), confDocs.get(5).getVersionFromInstant());
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(20, TimeUnit.SECONDS), confDocs.get(5).getVersionToInstant());
      //
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(20, TimeUnit.SECONDS), confDocs.get(4).getVersionFromInstant());
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(40, TimeUnit.SECONDS), confDocs.get(4).getVersionToInstant());
      //
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(40, TimeUnit.SECONDS), confDocs.get(3).getVersionFromInstant());
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(60, TimeUnit.SECONDS), confDocs.get(3).getVersionToInstant());
      //
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(60, TimeUnit.SECONDS), confDocs.get(2).getVersionFromInstant());
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(120, TimeUnit.SECONDS), confDocs.get(2).getVersionToInstant());
      //
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(120, TimeUnit.SECONDS), confDocs.get(1).getVersionFromInstant());
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(180, TimeUnit.SECONDS), confDocs.get(1).getVersionToInstant());
      //
      assertEquals(now.plus(1, TimeUnit.MINUTES).plus(180, TimeUnit.SECONDS), confDocs.get(0).getVersionFromInstant());
      assertEquals(null, confDocs.get(0).getVersionToInstant());
    } finally {
      _cfgMaster.setTimeSource(origTimeSource);
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_ReplaceVersion_noUid() {
    TimeSource origTimeSource = _cfgMaster.getTimeSource();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _cfgMaster.setTimeSource(TimeSource.fixed(now.plus(2, TimeUnit.HOURS)));


      List<ConfigDocument> replacement = newArrayList();
      for (int i = 0; i <= 2; i++) {
        String val = "replace_" + i;
        ConfigDocument doc = new ConfigDocument(ConfigItem.of(val));
        doc.setVersionFromInstant(now.plus(1, TimeUnit.MINUTES).plus(i * 20, TimeUnit.SECONDS));
        replacement.add(doc);
      }

      _cfgMaster.replaceVersion(baseOid.atVersion("no such uid"), replacement);
    } finally {
      _cfgMaster.setTimeSource(origTimeSource);
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_replaceVersion_nullDocument() {
    _cfgMaster.replaceVersion(null);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_replaceVersion_notFound() {
    UniqueId uniqueId = UniqueId.of("DbCfg", "0", "0");
    String val = "Test";
    ConfigDocument doc = new ConfigDocument(ConfigItem.of(val));
    doc.setUniqueId(uniqueId);
    _cfgMaster.replaceVersion(doc);
  }

}
