/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  
  public CalculationJob(
      String viewName,
      String calcConfigName,
      long iterationTimestamp,
      long jobId,
      List<CalculationJobItem> jobItems) {
    this(new CalculationJobSpecification(viewName, calcConfigName, iterationTimestamp, jobId),
        jobItems);
  }
  
  public CalculationJob(
      CalculationJobSpecification specification,
      List<CalculationJobItem> jobItems) {
    ArgumentChecker.notNull(specification, "Job spec");
    ArgumentChecker.notNull(jobItems, "Job items");
    _specification = specification;
    _jobItems = new ArrayList<CalculationJobItem>(jobItems);
  }

  /**
   * @return the specification
   */
  public CalculationJobSpecification getSpecification() {
    return _specification;
  }
  
  public List<CalculationJobItem> getJobItems() {
    return _jobItems;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
  
  public FudgeFieldContainer toFudgeMsg(FudgeSerializationContext fudgeContext) {
    MutableFudgeFieldContainer msg = fudgeContext.newMessage();
    
    getSpecification().writeFields(msg);

    for (CalculationJobItem item : getJobItems()) {
      msg.add(ITEM_FIELD_NAME, item.toFudgeMsg(fudgeContext));
    }
    
    return msg;
  }

  public static CalculationJob fromFudgeMsg(FudgeDeserializationContext fudgeContext, FudgeFieldContainer msg) {
    CalculationJobSpecification jobSpec = CalculationJobSpecification.fromFudgeMsg(msg);
    
    List<CalculationJobItem> jobItems = new ArrayList<CalculationJobItem>();
    for (FudgeField field : msg.getAllByName(ITEM_FIELD_NAME)) {
      CalculationJobItem jobItem = CalculationJobItem.fromFudgeMsg(fudgeContext, (FudgeFieldContainer) field.getValue());
      jobItems.add(jobItem);
    }
    
    return new CalculationJob(jobSpec, jobItems);
  }
}
