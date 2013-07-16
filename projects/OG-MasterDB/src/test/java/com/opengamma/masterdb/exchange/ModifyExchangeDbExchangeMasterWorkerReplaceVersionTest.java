/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.exchange;

import static com.google.common.collect.Lists.newArrayList;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
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

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
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
public class ModifyExchangeDbExchangeMasterWorkerReplaceVersionTest extends AbstractDbExchangeMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyExchangeDbExchangeMasterWorkerReplaceVersionTest.class);
  private static final ExternalIdBundle BUNDLE = ExternalIdBundle.of("A", "B");
  private static final ExternalIdBundle REGION = ExternalIdBundle.of("C", "D");

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public ModifyExchangeDbExchangeMasterWorkerReplaceVersionTest(String databaseType, String databaseVersion) {
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
    Clock origClock = _exgMaster.getClock();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _exgMaster.setClock(Clock.fixed(now.plus(2, HOURS), ZoneOffset.UTC));
      ExchangeDocument latestDoc = _exgMaster.get(baseOid, VersionCorrection.LATEST);
      assertNotNull(latestDoc);
      
      final ExternalIdBundle bundle = ExternalIdBundle.of("B", "B0");
      final ExternalIdBundle region = ExternalIdBundle.of("R", "R0");
      List<ExchangeDocument> replacement = newArrayList();
      for (int i = 1; i <= 3; i++) {
        ManageableExchange ex = new ManageableExchange(bundle, "replace_" + i, region, null);
        ExchangeDocument doc = new ExchangeDocument(ex);
        doc.setVersionFromInstant(now.plus(1, MINUTES).plus(i * 10, SECONDS));
        replacement.add(doc);
      }

      ExchangeHistoryRequest historyRequest = new ExchangeHistoryRequest();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, HOURS));
      ExchangeHistoryResult result = _exgMaster.history(historyRequest);
      List<ExchangeDocument> exchanges = result.getDocuments();

      ExchangeDocument secondVersionDoc = exchanges.get(exchanges.size() - 2);

      _exgMaster.replaceVersion(secondVersionDoc.getUniqueId(), replacement);

    } finally {
      _exgMaster.setClock(origClock);
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
    Clock origClock = _exgMaster.getClock();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _exgMaster.setClock(Clock.fixed(now.plus(2, HOURS), ZoneOffset.UTC));
      ExchangeDocument latestDoc = _exgMaster.get(baseOid, VersionCorrection.LATEST);
      assertNotNull(latestDoc);
      
      final ExternalIdBundle bundle = ExternalIdBundle.of("B", "B0");
      final ExternalIdBundle region = ExternalIdBundle.of("R", "R0");
      List<ExchangeDocument> replacement = newArrayList();
      for (int i = 1; i <= 3; i++) {
        ManageableExchange ex = new ManageableExchange(bundle, "replace_" + i, region, null);
        ExchangeDocument doc = new ExchangeDocument(ex);
        doc.setVersionFromInstant(now.plus(1, MINUTES).plus(i * 10, SECONDS));
        replacement.add(doc);
      }
      replacement.get(replacement.size() - 1).setVersionToInstant(now.plus(1, MINUTES).plus(40, SECONDS));

      ExchangeHistoryRequest historyRequest = new ExchangeHistoryRequest();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, HOURS));
      ExchangeHistoryResult result = _exgMaster.history(historyRequest);
      List<ExchangeDocument> exchanges = result.getDocuments();

      ExchangeDocument secondVersionDoc = exchanges.get(exchanges.size() - 2);

      _exgMaster.replaceVersion(secondVersionDoc.getUniqueId(), replacement);
    } finally {
      _exgMaster.setClock(origClock);
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
    Clock origClock = _exgMaster.getClock();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _exgMaster.setClock(Clock.fixed(now.plus(2, HOURS), ZoneOffset.UTC));

      final ExternalIdBundle bundle = ExternalIdBundle.of("B", "B0");
      final ExternalIdBundle region = ExternalIdBundle.of("R", "R0");
      List<ExchangeDocument> replacement = newArrayList();
      for (int i = 0; i <= 2; i++) {
        ManageableExchange ex = new ManageableExchange(bundle, "replace_" + i, region, null);
        ExchangeDocument doc = new ExchangeDocument(ex);
        doc.setVersionFromInstant(now.plus(1, MINUTES).plus(i * 20, SECONDS));
        replacement.add(doc);
      }
      replacement.get(replacement.size() - 1).setVersionToInstant(now.plus(2, MINUTES));

      ExchangeHistoryRequest historyRequest = new ExchangeHistoryRequest();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, HOURS));
      ExchangeHistoryResult result = _exgMaster.history(historyRequest);
      List<ExchangeDocument> exchanges = result.getDocuments();

      ExchangeDocument secondVersionDoc = exchanges.get(exchanges.size() - 2);

      _exgMaster.replaceVersion(secondVersionDoc.getUniqueId(), replacement);

      historyRequest = new ExchangeHistoryRequest();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, HOURS));
      result = _exgMaster.history(historyRequest);
      exchanges = result.getDocuments();

      assertEquals(7, exchanges.size());

      assertEquals(now, exchanges.get(6).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES), exchanges.get(6).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES), exchanges.get(5).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES).plus(20, SECONDS), exchanges.get(5).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES).plus(20, SECONDS), exchanges.get(4).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES).plus(40, SECONDS), exchanges.get(4).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES).plus(40, SECONDS), exchanges.get(3).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES).plus(60, SECONDS), exchanges.get(3).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES).plus(60, SECONDS), exchanges.get(2).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES).plus(120, SECONDS), exchanges.get(2).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES).plus(120, SECONDS), exchanges.get(1).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES).plus(180, SECONDS), exchanges.get(1).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES).plus(180, SECONDS), exchanges.get(0).getVersionFromInstant());
      assertEquals(null, exchanges.get(0).getVersionToInstant());
    } finally {
      _exgMaster.setClock(origClock);
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
    Clock origClock = _exgMaster.getClock();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _exgMaster.setClock(Clock.fixed(now.plus(2, HOURS), ZoneOffset.UTC));

      final ExternalIdBundle bundle = ExternalIdBundle.of("B", "B0");
      final ExternalIdBundle region = ExternalIdBundle.of("R", "R0");
      List<ExchangeDocument> replacement = newArrayList();
      for (int i = 0; i <= 2; i++) {
        ManageableExchange ex = new ManageableExchange(bundle, "replace_" + i, region, null);
        ExchangeDocument doc = new ExchangeDocument(ex);
        doc.setVersionFromInstant(now.plus(1, MINUTES).plus(i * 20, SECONDS));
        replacement.add(doc);
      }

      ExchangeHistoryRequest historyRequest = new ExchangeHistoryRequest();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, HOURS));
      ExchangeHistoryResult result = _exgMaster.history(historyRequest);
      List<ExchangeDocument> exchanges = result.getDocuments();

      ExchangeDocument secondVersionDoc = exchanges.get(exchanges.size() - 2);

      _exgMaster.replaceVersion(secondVersionDoc.getUniqueId(), replacement);

      historyRequest = new ExchangeHistoryRequest();
      historyRequest.setObjectId(baseOid);
      historyRequest.setCorrectionsFromInstant(now.plus(2, HOURS));
      result = _exgMaster.history(historyRequest);
      exchanges = result.getDocuments();

      assertEquals(7, exchanges.size());

      assertEquals(now, exchanges.get(6).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES), exchanges.get(6).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES), exchanges.get(5).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES).plus(20, SECONDS), exchanges.get(5).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES).plus(20, SECONDS), exchanges.get(4).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES).plus(40, SECONDS), exchanges.get(4).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES).plus(40, SECONDS), exchanges.get(3).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES).plus(60, SECONDS), exchanges.get(3).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES).plus(60, SECONDS), exchanges.get(2).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES).plus(120, SECONDS), exchanges.get(2).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES).plus(120, SECONDS), exchanges.get(1).getVersionFromInstant());
      assertEquals(now.plus(1, MINUTES).plus(180, SECONDS), exchanges.get(1).getVersionToInstant());
      //
      assertEquals(now.plus(1, MINUTES).plus(180, SECONDS), exchanges.get(0).getVersionFromInstant());
      assertEquals(null, exchanges.get(0).getVersionToInstant());
    } finally {
      _exgMaster.setClock(origClock);
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_ReplaceVersion_noUid() {
    Clock origClock = _exgMaster.getClock();
    try {
      Instant now = Instant.now();

      ObjectId baseOid = setupTestData(now);
      _exgMaster.setClock(Clock.fixed(now.plus(2, HOURS), ZoneOffset.UTC));

      final ExternalIdBundle bundle = ExternalIdBundle.of("B", "B0");
      final ExternalIdBundle region = ExternalIdBundle.of("R", "R0");
      List<ExchangeDocument> replacement = newArrayList();
      for (int i = 0; i <= 2; i++) {
        ManageableExchange ex = new ManageableExchange(bundle, "replace_" + i, region, null);
        ExchangeDocument doc = new ExchangeDocument(ex);
        doc.setVersionFromInstant(now.plus(1, MINUTES).plus(i * 20, SECONDS));
        replacement.add(doc);
      }

      _exgMaster.replaceVersion(baseOid.atVersion("no such uid"), replacement);
    } finally {
      _exgMaster.setClock(origClock);
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_replaceVersion_nullDocument() {
    _exgMaster.replaceVersion(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_replaceVersion_noExchangeId() {
    UniqueId uniqueId = UniqueId.of("DbExg", "101");
    ManageableExchange exchange = new ManageableExchange(BUNDLE, "Test", REGION, null);
    exchange.setUniqueId(uniqueId);
    ExchangeDocument doc = new ExchangeDocument(exchange);
    doc.setUniqueId(null);
    _exgMaster.replaceVersion(doc);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_replaceVersion_notFound() {
    UniqueId uniqueId = UniqueId.of("DbExg", "0", "0");
    ManageableExchange exchange = new ManageableExchange(BUNDLE, "Test", REGION, null);
    exchange.setUniqueId(uniqueId);
    ExchangeDocument doc = new ExchangeDocument(exchange);
    _exgMaster.replaceVersion(doc);
  }

}
