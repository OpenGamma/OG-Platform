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
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ExchangeSearchResult;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests ModifyExchangeDbExchangeMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class ModifyExchangeDbExchangeMasterWorkerReplaceVersionsTest extends AbstractDbExchangeMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyExchangeDbExchangeMasterWorkerReplaceVersionsTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifyExchangeDbExchangeMasterWorkerReplaceVersionsTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, false);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------  

  @Test
  public void test_ReplaceVersion_of_some_middle_version() {
    Clock origClock = _exgMaster.getClock();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _exgMaster.setClock(Clock.fixed(now.plus(2, HOURS), ZoneOffset.UTC));
      ExchangeDocument latestDoc = _exgMaster.get(baseOid, VersionCorrection.LATEST);
      Instant latestFrom = latestDoc.getVersionFromInstant();

      final ExternalIdBundle bundle = ExternalIdBundle.of("B", "B0");
      final ExternalIdBundle region = ExternalIdBundle.of("R", "R0");
      List<ExchangeDocument> replacement = newArrayList();
      for (int i = 0; i <= 10; i++) {
        ManageableExchange ex = new ManageableExchange(bundle, "test" + i, region, null);
        ExchangeDocument doc = new ExchangeDocument(ex);
        doc.setVersionFromInstant(latestFrom.plus(i, MINUTES));
        replacement.add(doc);
      }

      _exgMaster.replaceVersion(latestDoc.getUniqueId(), replacement);

      ExchangeSearchRequest searchRequest = new ExchangeSearchRequest();
      searchRequest.addExternalIds(bundle.getExternalIds());
      searchRequest.setVersionCorrection(VersionCorrection.LATEST);
      ExchangeSearchResult result = _exgMaster.search(searchRequest);
      List<ManageableExchange> exchanges = result.getExchanges();

      assertEquals(1, exchanges.size());
      ManageableExchange ex = exchanges.get(0);
      assertEquals("test10", ex.getName());

    } finally {
      _exgMaster.setClock(origClock);
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_ReplaceVersion_of_some_middle_version_timeBoundsNotExact() {
    Clock origClock = _exgMaster.getClock();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _exgMaster.setClock(Clock.fixed(now.plus(2, HOURS), ZoneOffset.UTC));
      ExchangeDocument latestDoc = _exgMaster.get(baseOid, VersionCorrection.LATEST);
      Instant latestFrom = latestDoc.getVersionFromInstant();

      final ExternalIdBundle bundle = ExternalIdBundle.of("B", "B0");
      final ExternalIdBundle region = ExternalIdBundle.of("R", "R0");
      List<ExchangeDocument> replacement = newArrayList();
      for (int i = 1; i <= 10; i++) {
        ManageableExchange ex = new ManageableExchange(bundle, "test" + i, region, null);
        ExchangeDocument doc = new ExchangeDocument(ex);
        doc.setVersionFromInstant(latestFrom.plus(i, MINUTES));
        replacement.add(doc);
      }

      _exgMaster.replaceVersion(latestDoc.getUniqueId(), replacement);

      ExchangeSearchRequest searchRequest = new ExchangeSearchRequest();
      searchRequest.addExternalIds(bundle.getExternalIds());
      searchRequest.setVersionCorrection(VersionCorrection.LATEST);
      ExchangeSearchResult result = _exgMaster.search(searchRequest);
      List<ManageableExchange> exchanges = result.getExchanges();

      assertEquals(1, exchanges.size());
      ManageableExchange ex = exchanges.get(0);
      assertEquals("test10", ex.getName());

    } finally {
      _exgMaster.setClock(origClock);
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_ReplaceVersion_which_is_not_current() {
    Clock origClock = _exgMaster.getClock();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _exgMaster.setClock(Clock.fixed(now.plus(2, HOURS), ZoneOffset.UTC));


      ExchangeHistoryRequest historyRequest = new ExchangeHistoryRequest();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsToInstant(null);
      ExchangeHistoryResult result = _exgMaster.history(historyRequest);
      List<ExchangeDocument> exchanges = result.getDocuments();

      ExchangeDocument lastButOneDoc = exchanges.get(exchanges.size() - 1);
      Instant lastButOneDocVersionFrom = lastButOneDoc.getVersionFromInstant();

      final ExternalIdBundle bundle = ExternalIdBundle.of("B", "B0");
      final ExternalIdBundle region = ExternalIdBundle.of("R", "R0");
      List<ExchangeDocument> replacement = newArrayList();
      for (int i = 1; i <= 10; i++) {
        ManageableExchange ex = new ManageableExchange(bundle, "test" + i, region, null);
        ExchangeDocument doc = new ExchangeDocument(ex);
        doc.setVersionFromInstant(lastButOneDocVersionFrom.plus(i, MINUTES));
        replacement.add(doc);
      }

      _exgMaster.replaceVersion(lastButOneDoc.getUniqueId(), replacement);

    } finally {
      _exgMaster.setClock(origClock);
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_ReplaceVersion_which_is_not_in_the_time_bounds_of_the_replaced_doc() {
    Clock origClock = _exgMaster.getClock();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _exgMaster.setClock(Clock.fixed(now.plus(2, HOURS), ZoneOffset.UTC));


      ExchangeHistoryRequest historyRequest = new ExchangeHistoryRequest();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsToInstant(null);
      ExchangeHistoryResult result = _exgMaster.history(historyRequest);
      List<ExchangeDocument> exchanges = result.getDocuments();

      ExchangeDocument lastButOneDoc = exchanges.get(exchanges.size() - 3);
      Instant lastButOneDocVersionFrom = lastButOneDoc.getVersionFromInstant();

      final ExternalIdBundle bundle = ExternalIdBundle.of("B", "B0");
      final ExternalIdBundle region = ExternalIdBundle.of("R", "R0");
      List<ExchangeDocument> replacement = newArrayList();
      for (int i = 1; i <= 10; i++) {
        ManageableExchange ex = new ManageableExchange(bundle, "test" + i, region, null);
        ExchangeDocument doc = new ExchangeDocument(ex);
        doc.setVersionFromInstant(lastButOneDocVersionFrom.plus(i, MINUTES));
        replacement.add(doc);
      }

      _exgMaster.replaceVersion(lastButOneDoc.getUniqueId(), replacement);

    } finally {
      _exgMaster.setClock(origClock);
    }
  }

  @Test
  public void test_ReplaceVersions() {
    Clock origClock = _exgMaster.getClock();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _exgMaster.setClock(Clock.fixed(now.plus(2, HOURS), ZoneOffset.UTC));
      ExchangeDocument latestDoc = _exgMaster.get(baseOid, VersionCorrection.LATEST);
      Instant latestFrom = latestDoc.getVersionFromInstant();

      final ExternalIdBundle bundle = ExternalIdBundle.of("B", "B0");
      final ExternalIdBundle region = ExternalIdBundle.of("R", "R0");
      List<ExchangeDocument> replacement = newArrayList();
      for (int i = 0; i <= 10; i++) {
        ManageableExchange ex = new ManageableExchange(bundle, "test" + i, region, null);
        ExchangeDocument doc = new ExchangeDocument(ex);
        doc.setVersionFromInstant(latestFrom.plus(i, SECONDS));
        replacement.add(doc);
      }

      _exgMaster.replaceVersions(latestDoc, replacement);

      ExchangeHistoryRequest historyRequest = new ExchangeHistoryRequest();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, HOURS));
      ExchangeHistoryResult result = _exgMaster.history(historyRequest);
      List<ExchangeDocument> exchanges = result.getDocuments();

      assertEquals(15, exchanges.size());

    } finally {
      _exgMaster.setClock(origClock);
    }
  }

  @Test
  public void test_ReplaceVersions2() {
    Clock origClock = _exgMaster.getClock();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _exgMaster.setClock(Clock.fixed(now.plus(2, HOURS), ZoneOffset.UTC));
      ExchangeDocument latestDoc = _exgMaster.get(baseOid, VersionCorrection.LATEST);
      Instant latestFrom = latestDoc.getVersionFromInstant();

      final ExternalIdBundle bundle = ExternalIdBundle.of("B", "B0");
      final ExternalIdBundle region = ExternalIdBundle.of("R", "R0");
      List<ExchangeDocument> replacement = newArrayList();
      for (int i = 0; i <= 10; i++) {
        ManageableExchange ex = new ManageableExchange(bundle, "test" + i, region, null);
        ExchangeDocument doc = new ExchangeDocument(ex);
        doc.setVersionFromInstant(latestFrom.plus(i - 3, MINUTES));
        replacement.add(doc);
      }

      _exgMaster.replaceVersions(latestDoc, replacement);

      ExchangeHistoryRequest historyRequest = new ExchangeHistoryRequest();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, HOURS));
      ExchangeHistoryResult result = _exgMaster.history(historyRequest);
      List<ExchangeDocument> exchanges = result.getDocuments();

      assertEquals(12, exchanges.size());

    } finally {
      _exgMaster.setClock(origClock);
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
    Clock origClock = _exgMaster.getClock();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _exgMaster.setClock(Clock.fixed(now.plus(2, HOURS), ZoneOffset.UTC));
      ExchangeDocument latestDoc = _exgMaster.get(baseOid, VersionCorrection.LATEST);
      Instant latestFrom = latestDoc.getVersionFromInstant();

      final ExternalIdBundle bundle = ExternalIdBundle.of("B", "B0");
      final ExternalIdBundle region = ExternalIdBundle.of("R", "R0");
      List<ExchangeDocument> replacement = newArrayList();
      for (int i = 1; i <= 4; i++) {
        ManageableExchange ex = new ManageableExchange(bundle, "replace_" + i, region, null);
        ExchangeDocument doc = new ExchangeDocument(ex);
        doc.setVersionFromInstant(now.plus(1, MINUTES).plus(i * 20, SECONDS));
        replacement.add(doc);
      }

      _exgMaster.replaceVersions(latestDoc, replacement);

      ExchangeHistoryRequest historyRequest = new ExchangeHistoryRequest();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, HOURS));
      ExchangeHistoryResult result = _exgMaster.history(historyRequest);
      List<ExchangeDocument> exchanges = result.getDocuments();

      assertEquals(6, exchanges.size());

      latestDoc = _exgMaster.get(baseOid, VersionCorrection.LATEST);
      latestFrom = latestDoc.getVersionFromInstant();
      assertEquals(now.plus(2, MINUTES).plus(20, SECONDS), latestFrom);

      assertEquals(now, exchanges.get(5).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES), exchanges.get(5).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES), exchanges.get(4).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES).plus(20, SECONDS), exchanges.get(4).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES).plus(20, SECONDS), exchanges.get(3).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES).plus(40, SECONDS), exchanges.get(3).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES).plus(40, SECONDS), exchanges.get(2).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES).plus(60, SECONDS), exchanges.get(2).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES).plus(60, SECONDS), exchanges.get(1).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES).plus(80, SECONDS), exchanges.get(1).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES).plus(80, SECONDS), exchanges.get(0).getVersionFromInstant());
      assertEquals(null, exchanges.get(0).getVersionToInstant());
    } finally {
      _exgMaster.setClock(origClock);
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
        doc.setVersionFromInstant(now.plus(1, MINUTES).plus(i * 20, SECONDS));
        replacement.add(doc);
      }
      replacement.get(replacement.size() - 1).setVersionToInstant(now.plus(1, MINUTES).plus(100, SECONDS));

      _exgMaster.replaceVersions(latestDoc, replacement);

      ExchangeHistoryRequest historyRequest = new ExchangeHistoryRequest();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, HOURS));
      ExchangeHistoryResult result = _exgMaster.history(historyRequest);
      List<ExchangeDocument> exchanges = result.getDocuments();

      assertEquals(9, exchanges.size());

      assertEquals(now, exchanges.get(8).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES), exchanges.get(8).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES), exchanges.get(7).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES).plus(20, SECONDS), exchanges.get(7).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES).plus(20, SECONDS), exchanges.get(6).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES).plus(40, SECONDS), exchanges.get(6).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES).plus(40, SECONDS), exchanges.get(5).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES).plus(60, SECONDS), exchanges.get(5).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES).plus(60, SECONDS), exchanges.get(4).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES).plus(80, SECONDS), exchanges.get(4).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES).plus(80, SECONDS), exchanges.get(3).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES).plus(100, SECONDS), exchanges.get(3).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES).plus(100, SECONDS), exchanges.get(2).getVersionFromInstant());
      assertEquals(now.plus(3, MINUTES), exchanges.get(2).getVersionToInstant());
      //
      assertEquals(now.plus(3, MINUTES), exchanges.get(1).getVersionFromInstant());
      assertEquals(now.plus(4, MINUTES), exchanges.get(1).getVersionToInstant());
      //
      assertEquals(now.plus(4, MINUTES), exchanges.get(0).getVersionFromInstant());
      assertEquals(null, exchanges.get(0).getVersionToInstant());

    } finally {
      _exgMaster.setClock(origClock);
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

      _exgMaster.replaceVersions(latestDoc, replacement);

      ExchangeHistoryRequest historyRequest = new ExchangeHistoryRequest();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, HOURS));
      ExchangeHistoryResult result = _exgMaster.history(historyRequest);
      List<ExchangeDocument> exchanges = result.getDocuments();

      assertEquals(8, exchanges.size());

      //
      assertEquals(now.plus(-30, SECONDS), exchanges.get(7).getVersionFromInstant());
      assertEquals(now.plus(0, SECONDS), exchanges.get(7).getVersionToInstant());
      //
      assertEquals(now.plus(0, SECONDS), exchanges.get(6).getVersionFromInstant());
      assertEquals(now.plus(30, SECONDS), exchanges.get(6).getVersionToInstant());
      //
      assertEquals(now.plus(30, SECONDS), exchanges.get(5).getVersionFromInstant());
      assertEquals(now.plus(60, SECONDS), exchanges.get(5).getVersionToInstant());
      //
      assertEquals(now.plus(60, SECONDS), exchanges.get(4).getVersionFromInstant());
      assertEquals(now.plus(90, SECONDS), exchanges.get(4).getVersionToInstant());
      //
      assertEquals(now.plus(90, SECONDS), exchanges.get(3).getVersionFromInstant());
      assertEquals(now.plus(120, SECONDS), exchanges.get(3).getVersionToInstant());
      //
      assertEquals(now.plus(120, SECONDS), exchanges.get(2).getVersionFromInstant());
      assertEquals(now.plus(180, SECONDS), exchanges.get(2).getVersionToInstant());
      //
      assertEquals(now.plus(180, SECONDS), exchanges.get(1).getVersionFromInstant());
      assertEquals(now.plus(240, SECONDS), exchanges.get(1).getVersionToInstant());
      //
      assertEquals(now.plus(240, SECONDS), exchanges.get(0).getVersionFromInstant());
      assertEquals(null, exchanges.get(0).getVersionToInstant());

    } finally {
      _exgMaster.setClock(origClock);
    }
  }
}
