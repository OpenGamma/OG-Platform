/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2LongMap;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.opengamma.engine.cache.CacheSelectHint;
import com.opengamma.engine.cache.IdentifierEncodedValueSpecifications;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * The definition of a particular job that must be performed by a calculation node.
 */
public class CalculationJob implements IdentifierEncodedValueSpecifications {

  private final CalculationJobSpecification _specification;
  private final long _functionInitializationIdentifier;
  private final VersionCorrection _resolverVersionCorrection;
  private final long[] _required;
  private final List<CalculationJobItem> _jobItems;

  private final CacheSelectHint _cacheSelect;

  /**
   * The tail is a set of jobs that must execute at the same location. It is not however part of the job so is not serialized.
   */
  private Collection<CalculationJob> _tail;

  /**
   * The cancellation flag is used to abort a calculation mid-way if possible. It is not serialized.
   */
  private boolean _cancelled;

  public CalculationJob(CalculationJobSpecification specification, long functionInitializationIdentifier, final VersionCorrection resolverVersionCorrection, long[] requiredJobIds,
      List<CalculationJobItem> jobItems, final CacheSelectHint cacheSelect) {
    ArgumentChecker.notNull(specification, "specification");
    ArgumentChecker.notNull(resolverVersionCorrection, "resolverVersionCorrection");
    ArgumentChecker.notNull(jobItems, "jobItems");
    ArgumentChecker.notNull(cacheSelect, "cacheSelect");
    _specification = specification;
    _functionInitializationIdentifier = functionInitializationIdentifier;
    _resolverVersionCorrection = resolverVersionCorrection;
    _required = requiredJobIds;
    _jobItems = jobItems;
    _cacheSelect = cacheSelect;
  }

  /**
   * @return the specification
   */
  public CalculationJobSpecification getSpecification() {
    return _specification;
  }

  public long getFunctionInitializationIdentifier() {
    return _functionInitializationIdentifier;
  }

  public VersionCorrection getResolverVersionCorrection() {
    return _resolverVersionCorrection;
  }

  public long[] getRequiredJobIds() {
    return _required;
  }

  public CacheSelectHint getCacheSelectHint() {
    return _cacheSelect;
  }

  public List<CalculationJobItem> getJobItems() {
    return Collections.unmodifiableList(_jobItems);
  }

  public Collection<CalculationJob> getTail() {
    return _tail;
  }

  public void addTail(final CalculationJob tail) {
    if (_tail == null) {
      _tail = new LinkedList<CalculationJob>();
    }
    _tail.add(tail);
  }

  public boolean isCancelled() {
    return _cancelled;
  }

  public void cancel() {
    _cancelled = true;
  }

  @Override
  public void convertIdentifiers(final Long2ObjectMap<ValueSpecification> identifiers) {
    _cacheSelect.convertIdentifiers(identifiers);
    for (CalculationJobItem item : _jobItems) {
      item.convertIdentifiers(identifiers);
    }
  }

  @Override
  public void collectIdentifiers(final LongSet identifiers) {
    _cacheSelect.collectIdentifiers(identifiers);
    for (CalculationJobItem item : _jobItems) {
      item.collectIdentifiers(identifiers);
    }
  }

  @Override
  public void convertValueSpecifications(final Object2LongMap<ValueSpecification> valueSpecifications) {
    _cacheSelect.convertValueSpecifications(valueSpecifications);
    for (CalculationJobItem item : _jobItems) {
      item.convertValueSpecifications(valueSpecifications);
    }
  }

  @Override
  public void collectValueSpecifications(final Set<ValueSpecification> valueSpecifications) {
    _cacheSelect.collectValueSpecifications(valueSpecifications);
    for (CalculationJobItem item : _jobItems) {
      item.collectValueSpecifications(valueSpecifications);
    }
  }

  @Override
  public String toString() {
    return "CalculationJob, spec = " + _specification.toString() + ", job item count = " + _jobItems.size();
  }

}
