/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.exchange;

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

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeHistoryRequest;
import com.opengamma.master.exchange.ExchangeHistoryResult;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests ModifyExchangeDbExchangeMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class ModifyExchangeDbExchangeMasterWorkerReplaceAllVersionsTest extends AbstractDbExchangeMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyExchangeDbExchangeMasterWorkerReplaceAllVersionsTest.class);  

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifyExchangeDbExchangeMasterWorkerReplaceAllVersionsTest(String databaseType, String databaseVersion) {
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
    Clock origClock = _exgMaster.getClock();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _exgMaster.setClock(Clock.fixed(now.plus(2, HOURS), ZoneOffset.UTC));
      ExchangeDocument latestDoc = _exgMaster.get(baseOid, VersionCorrection.LATEST);

      final ExternalIdBundle bundle = ExternalIdBundle.of("B", "B0");
      final ExternalIdBundle region = ExternalIdBundle.of("R", "R0");
      List<ExchangeDocument> replacement = newArrayList();
      for (int i = 1; i <= 4; i++) {
        ManageableExchange ex = new ManageableExchange(bundle, "replace_" + i, region, null);
        ExchangeDocument doc = new ExchangeDocument(ex);
        doc.setVersionFromInstant(now.plus(2, MINUTES).plus(i * 20, SECONDS));
        replacement.add(doc);
      }

      _exgMaster.replaceAllVersions(latestDoc, replacement);

      ExchangeHistoryRequest historyRequest = new ExchangeHistoryRequest();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, HOURS));
      ExchangeHistoryResult result = _exgMaster.history(historyRequest);
      List<ExchangeDocument> exchanges = result.getDocuments();

      assertEquals(4, exchanges.size());

      assertEquals(now.plus(2, MINUTES).plus(20, SECONDS), exchanges.get(3).getVersionFromInstant());
      assertEquals(now.plus(2, MINUTES).plus(40, SECONDS), exchanges.get(3).getVersionToInstant());
      //
      assertEquals(now.plus(2, MINUTES).plus(40, SECONDS), exchanges.get(2).getVersionFromInstant());
      assertEquals(now.plus(3, MINUTES).plus(0, SECONDS), exchanges.get(2).getVersionToInstant());
      //
      assertEquals(now.plus(3, MINUTES).plus(0, SECONDS), exchanges.get(1).getVersionFromInstant());
      assertEquals(now.plus(3, MINUTES).plus(20, SECONDS), exchanges.get(1).getVersionToInstant());
      //
      assertEquals(now.plus(3, MINUTES).plus(20, SECONDS), exchanges.get(0).getVersionFromInstant());
      assertEquals(null, exchanges.get(0).getVersionToInstant());
      //
    } finally {
      _exgMaster.setClock(origClock);
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
    Clock origClock = _exgMaster.getClock();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _exgMaster.setClock(Clock.fixed(now.plus(2, HOURS), ZoneOffset.UTC));
      ExchangeDocument latestDoc = _exgMaster.get(baseOid, VersionCorrection.LATEST);
      
      final ExternalIdBundle bundle = ExternalIdBundle.of("B", "B0");
      final ExternalIdBundle region = ExternalIdBundle.of("R", "R0");
      List<ExchangeDocument> replacement = newArrayList();
      for (int i = 1; i <= 4; i++) {
        ManageableExchange ex = new ManageableExchange(bundle, "replace_" + i, region, null);
        ExchangeDocument doc = new ExchangeDocument(ex);
        doc.setVersionFromInstant(now.plus(2, MINUTES).plus(i * 20, SECONDS));
        replacement.add(doc);
      }
      replacement.get(replacement.size() - 1).setVersionToInstant(now.plus(2, MINUTES).plus(100, SECONDS));

      _exgMaster.replaceAllVersions(latestDoc, replacement);

      ExchangeHistoryRequest historyRequest = new ExchangeHistoryRequest();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, HOURS));
      ExchangeHistoryResult result = _exgMaster.history(historyRequest);
      List<ExchangeDocument> exchanges = result.getDocuments();

      assertEquals(4, exchanges.size());

      assertEquals(now.plus(2, MINUTES).plus(20, SECONDS), exchanges.get(3).getVersionFromInstant());
      assertEquals(now.plus(2, MINUTES).plus(40, SECONDS), exchanges.get(3).getVersionToInstant());
      //
      assertEquals(now.plus(2, MINUTES).plus(40, SECONDS), exchanges.get(2).getVersionFromInstant());
      assertEquals(now.plus(3, MINUTES), exchanges.get(2).getVersionToInstant());
      //
      assertEquals(now.plus(3, MINUTES), exchanges.get(1).getVersionFromInstant());
      assertEquals(now.plus(3, MINUTES).plus(20, SECONDS), exchanges.get(1).getVersionToInstant());
      //
      assertEquals(now.plus(3, MINUTES).plus(20, SECONDS), exchanges.get(0).getVersionFromInstant());
      assertEquals(now.plus(3, MINUTES).plus(40, SECONDS), exchanges.get(0).getVersionToInstant());

    } finally {
      _exgMaster.setClock(origClock);
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
    Clock origClock = _exgMaster.getClock();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _exgMaster.setClock(Clock.fixed(now.plus(2, HOURS), ZoneOffset.UTC));
      ExchangeDocument latestDoc = _exgMaster.get(baseOid, VersionCorrection.LATEST);
      
      final ExternalIdBundle bundle = ExternalIdBundle.of("B", "B0");
      final ExternalIdBundle region = ExternalIdBundle.of("R", "R0");
      List<ExchangeDocument> replacement = newArrayList();
      for (int i = 1; i <= 4; i++) {
        ManageableExchange ex = new ManageableExchange(bundle, "replace_" + i, region, null);
        ExchangeDocument doc = new ExchangeDocument(ex);
        doc.setVersionFromInstant(now.minus(60, SECONDS).plus(i * 30, SECONDS));
        replacement.add(doc);
      }
      replacement.get(replacement.size() - 1).setVersionToInstant(now.plus(90, SECONDS));

      _exgMaster.replaceAllVersions(latestDoc, replacement);

      ExchangeHistoryRequest historyRequest = new ExchangeHistoryRequest();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, HOURS));
      ExchangeHistoryResult result = _exgMaster.history(historyRequest);
      List<ExchangeDocument> exchanges = result.getDocuments();

      assertEquals(4, exchanges.size());

      //
      assertEquals(now.plus(-30, SECONDS), exchanges.get(3).getVersionFromInstant());
      assertEquals(now.plus(0, SECONDS), exchanges.get(3).getVersionToInstant());
      //
      assertEquals(now.plus(0, SECONDS), exchanges.get(2).getVersionFromInstant());
      assertEquals(now.plus(30, SECONDS), exchanges.get(2).getVersionToInstant());
      //
      assertEquals(now.plus(30, SECONDS), exchanges.get(1).getVersionFromInstant());
      assertEquals(now.plus(60, SECONDS), exchanges.get(1).getVersionToInstant());
      //
      assertEquals(now.plus(60, SECONDS), exchanges.get(0).getVersionFromInstant());
      assertEquals(now.plus(90, SECONDS), exchanges.get(0).getVersionToInstant());
      //      
    } finally {
      _exgMaster.setClock(origClock);
    }
  }
}
