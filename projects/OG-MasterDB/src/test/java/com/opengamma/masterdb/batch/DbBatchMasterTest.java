/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.batch.RunCreationMode;
import com.opengamma.batch.SnapshotMode;
import com.opengamma.batch.domain.RiskRun;
import com.opengamma.batch.rest.BatchRunSearchRequest;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cycle.ViewCycleMetadata;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT_DB)
public class DbBatchMasterTest extends AbstractDbBatchTest {

  private DbBatchMaster _batchMaster;
  private ViewCycleMetadata _cycleMetadataStub;
  private ComputationTargetSpecification _compTargetSpec;
  private ValueRequirement _requirement;
  private ValueSpecification _specification;

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbBatchMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doSetUp() {
    _batchMaster = new DbBatchMaster(getDbConnector(), new DefaultComputationTargetResolver());

    final String calculationConfigName = "config_1";

    _compTargetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Sec", "APPL"));

    _requirement = new ValueRequirement("FAIR_VALUE", _compTargetSpec);
    _specification = new ValueSpecification("FAIR_VALUE", _compTargetSpec, ValueProperties.with(ValuePropertyNames.FUNCTION, "IDENTITY_FUNCTION").get());

    _cycleMetadataStub = new ViewCycleMetadata() {

      @Override
      public UniqueId getViewCycleId() {
        return UniqueId.of("viewcycle", "viewcycle", "viewcycle");
      }
      
      @Override
      public Collection<String> getAllCalculationConfigurationNames() {
        return newArrayList(calculationConfigName);
      }

      @Override
      public Collection<com.opengamma.engine.ComputationTargetSpecification> getComputationTargets(String configurationName) {
        if (configurationName.equals(calculationConfigName)) {
          return Arrays.asList(new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueId.of("Primitive", "Value")), _compTargetSpec);
        } else {
          return emptyList();
        }
      }

      @Override
      public Map<ValueSpecification, Set<ValueRequirement>> getTerminalOutputs(String configurationName) {
        Map<ValueSpecification, Set<ValueRequirement>> map = Maps.newHashMap();
        map.put(_specification, Sets.newHashSet(_requirement));
        return map;
      }

      @Override
      public UniqueId getMarketDataSnapshotId() {
        return UniqueId.of("snapshot", "snapshot", "snapshot");
      }

      @Override
      public Instant getValuationTime() {
        return Instant.now();
      }

      @Override
      public VersionCorrection getVersionCorrection() {
        return VersionCorrection.LATEST;
      }

      @Override
      public UniqueId getViewDefinitionId() {
        return UniqueId.of("viewdef", "viewdef", "viewdef");
      }

      @Override
      public String getName() {
        return "cycle_name";
      }

    };
  }

  //-------------------------------------------------------------------------
  @Test
  public void searchAllBatches() {
    final UniqueId marketDataUid = _cycleMetadataStub.getMarketDataSnapshotId();                
    _batchMaster.createMarketData(marketDataUid);            
    RiskRun run = _batchMaster.startRiskRun(_cycleMetadataStub, Maps.<String, String>newHashMap(), RunCreationMode.AUTO, SnapshotMode.PREPARED);

    BatchRunSearchRequest request = new BatchRunSearchRequest();

    Pair<List<RiskRun>, Paging> result = _batchMaster.searchRiskRun(request);
    assertNotNull(result);

    assertEquals(1, result.getFirst().size());
    RiskRun item = result.getFirst().get(0);
    assertNotNull(item.getObjectId());
    assertEquals(item.getValuationTime(), run.getValuationTime());
    assertEquals(false, item.isComplete());

    _batchMaster.endRiskRun(item.getObjectId());
    
    result = _batchMaster.searchRiskRun(request);
    assertEquals(1, result.getFirst().size());
    item = result.getFirst().get(0);
    assertNotNull(item.getObjectId());
    assertEquals(item.getValuationTime(), run.getValuationTime());
    assertEquals(true, item.isComplete());
    assertEquals("cycle_name", item.getName());
  }


  @Test
  public void searchOneBatch() {
    final UniqueId marketDataUid = _cycleMetadataStub.getMarketDataSnapshotId();                
    _batchMaster.createMarketData(marketDataUid);            
    RiskRun run = _batchMaster.startRiskRun(_cycleMetadataStub, Maps.<String, String>newHashMap(), RunCreationMode.AUTO, SnapshotMode.PREPARED);

    BatchRunSearchRequest request = new BatchRunSearchRequest();
    request.setValuationTime(run.getValuationTime());

    Pair<List<RiskRun>, Paging> result = _batchMaster.searchRiskRun(request);
    assertNotNull(result);

    assertEquals(1, result.getFirst().size());
    RiskRun item = result.getFirst().get(0);
    assertNotNull(item.getObjectId());
    assertEquals(item.getValuationTime(), run.getValuationTime());
    assertEquals(false, item.isComplete());
    assertEquals("cycle_name", item.getName());
  }

}
