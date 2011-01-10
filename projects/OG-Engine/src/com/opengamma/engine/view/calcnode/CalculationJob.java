/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.CacheSelectHint;
import com.opengamma.engine.view.cache.IdentifierMap;
import com.opengamma.util.ArgumentChecker;

/**
 * The definition of a particular job that must be performed by
 * a Calculation Node.
 */
public class CalculationJob implements Serializable {
  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(CalculationJob.class);

  private final CalculationJobSpecification _specification;
  private final Collection<Long> _required;
  private final List<CalculationJobItem> _jobItems;

  private final CacheSelectHint _cacheSelect;

  /**
   * The tail is a set of jobs that must execute at the same location. It is not however part of the job so is not
   * serialized.
   */
  private Collection<CalculationJob> _tail;

  /**
   * The cancellation flag is used to abort a calculation mid-way if possible. It is not serialized.
   */
  private boolean _cancelled;

  public CalculationJob(String viewName, String calcConfigName, long iterationTimestamp, long jobId, List<CalculationJobItem> jobItems, final CacheSelectHint cacheSelect) {
    this(new CalculationJobSpecification(viewName, calcConfigName, iterationTimestamp, jobId), null, jobItems, cacheSelect);
  }

  public CalculationJob(CalculationJobSpecification specification, Collection<Long> requiredJobIds, List<CalculationJobItem> jobItems, final CacheSelectHint cacheSelect) {
    ArgumentChecker.notNull(specification, "specification");
    ArgumentChecker.notNull(jobItems, "jobItems");
    ArgumentChecker.notNull(cacheSelect, "cacheSelect");
    _specification = specification;
    _required = (requiredJobIds != null) ? new ArrayList<Long>(requiredJobIds) : null;
    _jobItems = new ArrayList<CalculationJobItem>(jobItems);
    _cacheSelect = cacheSelect;
  }

  /**
   * @return the specification
   */
  public CalculationJobSpecification getSpecification() {
    return _specification;
  }

  public Collection<Long> getRequiredJobIds() {
    return (_required != null) ? Collections.unmodifiableCollection(_required) : null;
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

  /**
   * Resolves the numeric identifiers passed in a Fudge message to the full {@link ValueSpecification} objects.
   * 
   * @param identifierMap Identifier map to resolve the inputs with
   */
  public void resolveInputs(final IdentifierMap identifierMap) {
    _cacheSelect.resolveSpecifications(identifierMap);
    for (CalculationJobItem item : _jobItems) {
      item.resolveInputs(identifierMap);
    }
  }

  /**
   * Converts full {@link ValueSpecification} objects to numeric identifiers for Fudge message encoding.
   * 
   * @param identifierMap Identifier map to convert the inputs with
   */
  public void convertInputs(final IdentifierMap identifierMap) {
    getCacheSelectHint().convertSpecifications(identifierMap);
    for (CalculationJobItem item : _jobItems) {
      item.convertInputs(identifierMap);
    }
  }

  @Override
  public String toString() {
    return "CalculationJob, spec = " + _specification.toString() + ", job item count = " + _jobItems.size();
  }

}
