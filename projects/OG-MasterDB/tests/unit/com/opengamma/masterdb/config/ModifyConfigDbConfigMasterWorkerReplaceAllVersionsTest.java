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

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.util.test.DbTest;

/**
 * Tests ModifyConfigDbConfigMasterWorker.
 */
public class ModifyConfigDbConfigMasterWorkerReplaceAllVersionsTest extends AbstractDbConfigMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyConfigDbConfigMasterWorkerReplaceAllVersionsTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifyConfigDbConfigMasterWorkerReplaceAllVersionsTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, false);
    s_logger.info("running testcases for {}", databaseType);
  }


  @Test
  /**
   *
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
   *       |                        |
   *   +2m |------------------------|                                                                              
   *       |                        |                                       
   *       |     setup_0            |
   *       |                        | 
   *   +1m |------------------------|                                       
   *
   *
   *   NOW =================================================================================
   *
   */
  public void test_ReplaceAllVersions1() {
    TimeSource origTimeSource = _cfgMaster.getTimeSource();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _cfgMaster.setTimeSource(TimeSource.fixed(now.plus(2, TimeUnit.HOURS)));
      ConfigDocument latestDoc = _cfgMaster.get(baseOid, VersionCorrection.LATEST);


      List<ConfigDocument> replacement = newArrayList();
      for (int i = 1; i <= 4; i++) {
        String val = "replace_" + i;
        ConfigDocument doc = new ConfigDocument(ConfigItem.of(val, "some_name_"+i));

        doc.setVersionFromInstant(now.plus(2, TimeUnit.MINUTES).plus(i * 20, TimeUnit.SECONDS));
        replacement.add(doc);
      }

      _cfgMaster.replaceAllVersions(latestDoc, replacement);

      ConfigHistoryRequest<String> historyRequest = new ConfigHistoryRequest<String>();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, TimeUnit.HOURS));
      ConfigHistoryResult<String> result = _cfgMaster.history(historyRequest);
      List<ConfigDocument> values = result.getDocuments();

      assertEquals(4, values.size());

      assertEquals(now.plus(2, TimeUnit.MINUTES).plus(20, TimeUnit.SECONDS), values.get(3).getVersionFromInstant());
      assertEquals(now.plus(2, TimeUnit.MINUTES).plus(40, TimeUnit.SECONDS), values.get(3).getVersionToInstant());
      //
      assertEquals(now.plus(2, TimeUnit.MINUTES).plus(40, TimeUnit.SECONDS), values.get(2).getVersionFromInstant());
      assertEquals(now.plus(3, TimeUnit.MINUTES).plus(0, TimeUnit.SECONDS), values.get(2).getVersionToInstant());
      //
      assertEquals(now.plus(3, TimeUnit.MINUTES).plus(0, TimeUnit.SECONDS), values.get(1).getVersionFromInstant());
      assertEquals(now.plus(3, TimeUnit.MINUTES).plus(20, TimeUnit.SECONDS), values.get(1).getVersionToInstant());
      //
      assertEquals(now.plus(3, TimeUnit.MINUTES).plus(20, TimeUnit.SECONDS), values.get(0).getVersionFromInstant());
      assertEquals(null, values.get(0).getVersionToInstant());
      //
    } finally {
      _cfgMaster.setTimeSource(origTimeSource);
    }
  }

  @Test
  /**
   *
   *       |                        |                                                       
   *       |                        |                                         
   *       |                        |                                         
   *       |                        |                                         
   *       |                        |                                         
   *       |                        |                                         
   *       |     setup_4            |                                         
   *       |                        |                                         
   *   +5m |------------------------|                                      
   *       |                        |                                         
   *       |     setup_3            |                                         
   *       |                        |                                         
   *   +4m |------------------------|  
   *       |                        |                     
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
   *       |                        |                   
   *   +2m |------------------------|                                                                               
   *       |                        |                                       
   *       |     setup_0            |                       
   *       |                        | 
   *   +1m |------------------------|                                     
   *
   *
   *   NOW =================================================================================
   *
   */
  public void test_ReplaceAllVersions2() {
    TimeSource origTimeSource = _cfgMaster.getTimeSource();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _cfgMaster.setTimeSource(TimeSource.fixed(now.plus(2, TimeUnit.HOURS)));
      ConfigDocument latestDoc = _cfgMaster.get(baseOid, VersionCorrection.LATEST);

      List<ConfigDocument> replacement = newArrayList();
      for (int i = 1; i <= 4; i++) {
        String val = "replace_" + i;
        ConfigDocument doc = new ConfigDocument(ConfigItem.of(val,  "some_name_"+i));

        doc.setVersionFromInstant(now.plus(2, TimeUnit.MINUTES).plus(i * 20, TimeUnit.SECONDS));
        replacement.add(doc);
      }
      replacement.get(replacement.size() - 1).setVersionToInstant(now.plus(2, TimeUnit.MINUTES).plus(100, TimeUnit.SECONDS));

      _cfgMaster.replaceAllVersions(latestDoc, replacement);

      ConfigHistoryRequest<String> historyRequest = new ConfigHistoryRequest<String>();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, TimeUnit.HOURS));
      ConfigHistoryResult<String> result = _cfgMaster.history(historyRequest);
      List<ConfigDocument> values = result.getDocuments();

      assertEquals(4, values.size());

      assertEquals(now.plus(2, TimeUnit.MINUTES).plus(20, TimeUnit.SECONDS), values.get(3).getVersionFromInstant());
      assertEquals(now.plus(2, TimeUnit.MINUTES).plus(40, TimeUnit.SECONDS), values.get(3).getVersionToInstant());
      //
      assertEquals(now.plus(2, TimeUnit.MINUTES).plus(40, TimeUnit.SECONDS), values.get(2).getVersionFromInstant());
      assertEquals(now.plus(3, TimeUnit.MINUTES), values.get(2).getVersionToInstant());
      //
      assertEquals(now.plus(3, TimeUnit.MINUTES), values.get(1).getVersionFromInstant());
      assertEquals(now.plus(3, TimeUnit.MINUTES).plus(20, TimeUnit.SECONDS), values.get(1).getVersionToInstant());
      //
      assertEquals(now.plus(3, TimeUnit.MINUTES).plus(20, TimeUnit.SECONDS), values.get(0).getVersionFromInstant());
      assertEquals(now.plus(3, TimeUnit.MINUTES).plus(40, TimeUnit.SECONDS), values.get(0).getVersionToInstant());

    } finally {
      _cfgMaster.setTimeSource(origTimeSource);
    }
  }


  @Test
  /**
   *
   *       |                        |                                         
   *       |                        |                                         
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
   *       |                        | 
   *       |                        | 
   *   +2m |------------------------|                                                                 
   *       |                        |
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
  public void test_ReplaceAllVersions3() {
    TimeSource origTimeSource = _cfgMaster.getTimeSource();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _cfgMaster.setTimeSource(TimeSource.fixed(now.plus(2, TimeUnit.HOURS)));
      ConfigDocument latestDoc = _cfgMaster.get(baseOid, VersionCorrection.LATEST);


      List<ConfigDocument> replacement = newArrayList();
      for (int i = 1; i <= 4; i++) {
        String val = "replace_" + i;
        ConfigDocument doc = new ConfigDocument(ConfigItem.of(val, "some_name_"+i));

        doc.setVersionFromInstant(now.minus(60, TimeUnit.SECONDS).plus(i * 30, TimeUnit.SECONDS));
        replacement.add(doc);
      }
      replacement.get(replacement.size() - 1).setVersionToInstant(now.plus(90, TimeUnit.SECONDS));

      _cfgMaster.replaceAllVersions(latestDoc, replacement);

      ConfigHistoryRequest<String> historyRequest = new ConfigHistoryRequest<String>();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, TimeUnit.HOURS));
      ConfigHistoryResult<String> result = _cfgMaster.history(historyRequest);
      List<ConfigDocument> values = result.getDocuments();

      assertEquals(4, values.size());

      //
      assertEquals(now.plus(-30, TimeUnit.SECONDS), values.get(3).getVersionFromInstant());
      assertEquals(now.plus(0, TimeUnit.SECONDS), values.get(3).getVersionToInstant());
      //
      assertEquals(now.plus(0, TimeUnit.SECONDS), values.get(2).getVersionFromInstant());
      assertEquals(now.plus(30, TimeUnit.SECONDS), values.get(2).getVersionToInstant());
      //
      assertEquals(now.plus(30, TimeUnit.SECONDS), values.get(1).getVersionFromInstant());
      assertEquals(now.plus(60, TimeUnit.SECONDS), values.get(1).getVersionToInstant());
      //
      assertEquals(now.plus(60, TimeUnit.SECONDS), values.get(0).getVersionFromInstant());
      assertEquals(now.plus(90, TimeUnit.SECONDS), values.get(0).getVersionToInstant());
      //      
    } finally {
      _cfgMaster.setTimeSource(origTimeSource);
    }
  }
}
