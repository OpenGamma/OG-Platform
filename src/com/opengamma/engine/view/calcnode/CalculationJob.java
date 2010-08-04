/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * The definition of a particular job that must be performed by
 * a Calculation Node.
 */
public class CalculationJob implements Serializable {
  
  private static final String ITEM_FIELD_NAME = "calculationJobItem";
  private static final String RESULT_WRITER_FIELD_NAME = "resultWriter";
  
  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(CalculationJob.class);
  
  private final CalculationJobSpecification _specification;
  private final List<CalculationJobItem> _jobItems;
  private final ResultWriter _resultWriter;
  
  public CalculationJob(
      String viewName,
      String calcConfigName,
      long iterationTimestamp,
      long jobId,
      List<CalculationJobItem> jobItems,
      ResultWriter resultWriter) {
    this(new CalculationJobSpecification(viewName, calcConfigName, iterationTimestamp, jobId),
        jobItems, resultWriter);
  }
  
  public CalculationJob(
      CalculationJobSpecification specification,
      List<CalculationJobItem> jobItems,
      ResultWriter resultWriter) {
    ArgumentChecker.notNull(specification, "Job spec");
    ArgumentChecker.notNull(jobItems, "Job items");
    _specification = specification;
    _jobItems = new ArrayList<CalculationJobItem>(jobItems);
    _resultWriter = resultWriter;
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
  
  public ResultWriter getResultWriter() {
    return _resultWriter;
  }
  
  @Override
  public String toString() {
    return "CalculationJob, spec = " + _specification.toString() + ", job item count = " + _jobItems.size();
  }
  
  public FudgeFieldContainer toFudgeMsg(FudgeSerializationContext fudgeContext) {
    MutableFudgeFieldContainer msg = fudgeContext.newMessage();
    
    getSpecification().writeFields(msg);
    
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeObject(getResultWriter());
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Failed to serialize", e);
    }
    msg.add(RESULT_WRITER_FIELD_NAME, baos.toByteArray());

    //FudgeFieldContainer resultWriter = fudgeContext.objectToFudgeMsg(getResultWriter());
    // msg.add(RESULT_WRITER_FIELD_NAME, resultWriter);

    for (CalculationJobItem item : getJobItems()) {
      msg.add(ITEM_FIELD_NAME, item.toFudgeMsg(fudgeContext));
    }
    
    return msg;
  }

  public static CalculationJob fromFudgeMsg(FudgeDeserializationContext fudgeContext, FudgeFieldContainer msg) {
    CalculationJobSpecification jobSpec = CalculationJobSpecification.fromFudgeMsg(msg);
    
    FudgeField resultWriterField = msg.getByName(RESULT_WRITER_FIELD_NAME);
    
    ResultWriter resultWriter;
    try {
      ByteArrayInputStream bais = new ByteArrayInputStream((byte[]) resultWriterField.getValue());
      ObjectInputStream inStream = new ObjectInputStream(bais);
      resultWriter = (ResultWriter) inStream.readObject();
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Failed to deserialize", e);
    }  

    //ResultWriter resultWriter = (ResultWriter) fudgeContext.fieldValueToObject(resultWriterField);
    
    List<CalculationJobItem> jobItems = new ArrayList<CalculationJobItem>();
    for (FudgeField field : msg.getAllByName(ITEM_FIELD_NAME)) {
      CalculationJobItem jobItem = CalculationJobItem.fromFudgeMsg(fudgeContext, (FudgeFieldContainer) field.getValue());
      jobItems.add(jobItem);
    }
    
    return new CalculationJob(jobSpec, jobItems, resultWriter);
  }
}
