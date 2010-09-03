/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
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

  private static final String ITEM_FIELD_NAME = "calculationJobItem";

  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(CalculationJob.class);

  private final CalculationJobSpecification _specification;
  private final List<CalculationJobItem> _jobItems;

  private final CacheSelectHint _cacheSelect;

  public CalculationJob(String viewName, String calcConfigName, long iterationTimestamp, long jobId, List<CalculationJobItem> jobItems, final CacheSelectHint cacheSelect) {
    this(new CalculationJobSpecification(viewName, calcConfigName, iterationTimestamp, jobId), jobItems, cacheSelect);
  }

  public CalculationJob(CalculationJobSpecification specification, List<CalculationJobItem> jobItems, final CacheSelectHint cacheSelect) {
    ArgumentChecker.notNull(specification, "specification");
    ArgumentChecker.notNull(jobItems, "jobItems");
    ArgumentChecker.notNull(cacheSelect, "cacheSelect");
    _specification = specification;
    _jobItems = new ArrayList<CalculationJobItem>(jobItems);
    _cacheSelect = cacheSelect;
  }

  /**
   * @return the specification
   */
  public CalculationJobSpecification getSpecification() {
    return _specification;
  }

  public CacheSelectHint getCacheSelectHint() {
    return _cacheSelect;
  }

  public List<CalculationJobItem> getJobItems() {
    return Collections.unmodifiableList(_jobItems);
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

  public FudgeFieldContainer toFudgeMsg(FudgeSerializationContext fudgeContext) {
    MutableFudgeFieldContainer msg = fudgeContext.newMessage();
    getSpecification().toFudgeMsg(msg);
    for (CalculationJobItem item : getJobItems()) {
      msg.add(ITEM_FIELD_NAME, item.toFudgeMsg(fudgeContext));
    }
    getCacheSelectHint().toFudgeMsg(msg);
    return msg;
  }

  public static CalculationJob fromFudgeMsg(FudgeDeserializationContext fudgeContext, FudgeFieldContainer msg) {
    CalculationJobSpecification jobSpec = CalculationJobSpecification.fromFudgeMsg(msg);
    final Collection<FudgeField> fields = msg.getAllByName(ITEM_FIELD_NAME);
    final List<CalculationJobItem> jobItems = new ArrayList<CalculationJobItem>(fields.size());
    for (FudgeField field : fields) {
      CalculationJobItem jobItem = CalculationJobItem.fromFudgeMsg(fudgeContext, (FudgeFieldContainer) field.getValue());
      jobItems.add(jobItem);
    }
    final CacheSelectHint cacheSelectFilter = CacheSelectHint.fromFudgeMsg(msg);
    return new CalculationJob(jobSpec, jobItems, cacheSelectFilter);
  }
}
