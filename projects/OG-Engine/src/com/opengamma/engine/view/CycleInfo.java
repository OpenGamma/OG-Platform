package com.opengamma.engine.view;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

import javax.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
public interface CycleInfo {
  Collection<String> getAllCalculationConfigurationNames();

  Collection<ComputationTarget> getComputationTargetsByConfigName(String calcConfName);

  Map<ValueSpecification,Set<ValueRequirement>> getTerminalOutputsByConfigName(String calcConfName);

  UniqueId getMarketDataSnapshotUniqueId();

  Instant getValuationTime();

  VersionCorrection getVersionCorrection();

  UniqueId getViewDefinitionUid();
}
