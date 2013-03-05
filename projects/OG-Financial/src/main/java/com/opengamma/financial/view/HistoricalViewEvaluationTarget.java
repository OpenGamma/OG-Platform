/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import java.util.LinkedList;
import java.util.List;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.LocalDate;

import com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification;
import com.opengamma.engine.view.execution.ArbitraryViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionSequence;
import com.opengamma.livedata.UserPrincipal;

/**
 * Target for {@link HistoricalViewEvaluationFunction} which ensures that an execution sequence is constructed which
 * matches the function's expectations.
 */
public class HistoricalViewEvaluationTarget extends ViewEvaluationTarget {

  /**
   * Fudge field containing the start date
   */
  protected static final String START_DATE_FIELD = "startDate";
  /**
   * Fudge field containing whether the start date is inclusive.
   */
  protected static final String INCLUDE_START_FIELD = "includeStart";
  /**
   * Fudge field containing the end date.
   */
  protected static final String END_DATE_FIELD = "endDate";
  /**
   * Fudge field containing whether the end date is inclusive.
   */
  protected static final String INCLUDE_END_FIELD = "includeEnd";
  
  private final LocalDate _startDate;
  private final boolean _includeStart;
  private final LocalDate _endDate;
  private final boolean _includeEnd;
  
  public HistoricalViewEvaluationTarget(UserPrincipal user, LocalDate startDate, boolean includeStart,
      LocalDate endDate, boolean includeEnd) {
    super(user, buildExecutionSequence(startDate, includeStart, endDate, includeEnd));
    _startDate = startDate;
    _includeStart = includeStart;
    _endDate = endDate;
    _includeEnd = includeEnd;
  }
  
  public HistoricalViewEvaluationTarget(FudgeDeserializer deserializer, FudgeMsg message) {
    this(deserializer, message, message.getValue(LocalDate.class, START_DATE_FIELD),
        message.getBoolean(INCLUDE_START_FIELD), message.getValue(LocalDate.class, END_DATE_FIELD),
        message.getBoolean(INCLUDE_END_FIELD));
  }
  
  private HistoricalViewEvaluationTarget(FudgeDeserializer deserializer, FudgeMsg message, LocalDate startDate, boolean includeStart, LocalDate endDate, boolean includeEnd) {
    super(deserializer, message, buildExecutionSequence(startDate, includeStart, endDate, includeEnd));
    _startDate = startDate;
    _includeStart = includeStart;
    _endDate = endDate;
    _includeEnd = includeEnd;
  }
  
  private static ViewCycleExecutionSequence buildExecutionSequence(LocalDate startDate, boolean includeStart,
      LocalDate endDate, boolean includeEnd) {
    if (!includeStart) {
      startDate = startDate.plusDays(1);
    }
    if (!includeEnd) {
      endDate = endDate.minusDays(1);
    }
    List<ViewCycleExecutionOptions> executionSequence = new LinkedList<ViewCycleExecutionOptions>();
    LocalDate currentDate = startDate;
    while (!currentDate.isAfter(endDate)) {
      // Intentionally leave valuation time empty; this will be fixed in the default cycle execution options at runtime
      // so that it does not become part of the target and therefore the dependency graph.
      ViewCycleExecutionOptions executionOptions = ViewCycleExecutionOptions.builder()
          .setMarketDataSpecification(new FixedHistoricalMarketDataSpecification(currentDate))
          .create();
      executionSequence.add(executionOptions);
      currentDate = currentDate.plusDays(1);
    }
    return new ArbitraryViewCycleExecutionSequence(executionSequence);
  }
  
  public LocalDate getStartDate() {
    return _startDate;
  }

  public boolean isIncludeStart() {
    return _includeStart;
  }

  public LocalDate getEndDate() {
    return _endDate;
  }

  public boolean isIncludeEnd() {
    return _includeEnd;
  }

  @Override
  protected void toFudgeMsgImpl(FudgeSerializer serializer, MutableFudgeMsg message) {
    super.toFudgeMsgImpl(serializer, message);
  }
  
  @Override
  protected void serializeExecutionSequence(FudgeSerializer serializer, MutableFudgeMsg message) {
    // More efficient to recreate the execution sequence rather than serializing it
    message.add(START_DATE_FIELD, getStartDate());
    message.add(INCLUDE_START_FIELD, isIncludeStart());
    message.add(END_DATE_FIELD, getEndDate());
    message.add(INCLUDE_END_FIELD, isIncludeEnd());
  }

  public static HistoricalViewEvaluationTarget fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg message) {
    return new HistoricalViewEvaluationTarget(deserializer, message);
  }
  
}
