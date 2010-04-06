/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.fudgemsg.FudgeMessageFactory;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;

/**
 * A simple JavaBean-based implementation of {@link LiveDataValueUpdate}.
 *
 * @author kirk
 */
public class LiveDataValueUpdateBean implements LiveDataValueUpdate,
    Serializable {
  private static final String RELEVANT_TIMESTAMP_FIELD_NAME = "relevantTimestamp";
  private static final String SPECIFICATION_FIELD_NAME = "specification";
  private static final String FIELDS_FIELD_NAME = "fields";
  private final long _relevantTimestamp;
  private final LiveDataSpecification _specification;
  private final FudgeFieldContainer _fieldContainer;
  
  public LiveDataValueUpdateBean(long relevantTimestamp, LiveDataSpecification specification, FudgeFieldContainer fieldContainer) {
    // TODO kirk 2009-09-29 -- Check Inputs.
    _relevantTimestamp = relevantTimestamp;
    _specification = specification;
    _fieldContainer = fieldContainer;
  }

  @Override
  public FudgeFieldContainer getFields() {
    return _fieldContainer;
  }

  @Override
  public long getRelevantTimestamp() {
    return _relevantTimestamp;
  }

  @Override
  public LiveDataSpecification getSpecification() {
    return _specification;
  }
  
  public FudgeFieldContainer toFudgeMsg(FudgeMessageFactory fudgeMessageFactory) {
    MutableFudgeFieldContainer msg = fudgeMessageFactory.newMessage();
    msg.add(RELEVANT_TIMESTAMP_FIELD_NAME, getRelevantTimestamp());
    if(getSpecification() != null) {
      msg.add(SPECIFICATION_FIELD_NAME, getSpecification().toFudgeMsg(fudgeMessageFactory));
    }
    if(getFields() != null) {
      msg.add(FIELDS_FIELD_NAME, getFields());
    }
    return msg;
  }
  
  public static LiveDataValueUpdateBean fromFudgeMsg(FudgeFieldContainer msg) {
    Long relevantTimestamp = msg.getLong(RELEVANT_TIMESTAMP_FIELD_NAME);
    FudgeFieldContainer specificationFields = msg.getMessage(SPECIFICATION_FIELD_NAME);
    FudgeFieldContainer fields = msg.getMessage(FIELDS_FIELD_NAME);
    // REVIEW kirk 2009-10-28 -- Right thing to do here?
    if(relevantTimestamp == null) {
      return null;
    }
    if(specificationFields == null) {
      return null;
    }
    if(fields == null) {
      return null;
    }
    LiveDataSpecification spec = LiveDataSpecification.fromFudgeMsg(specificationFields);
    return new LiveDataValueUpdateBean(relevantTimestamp, spec, fields);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
