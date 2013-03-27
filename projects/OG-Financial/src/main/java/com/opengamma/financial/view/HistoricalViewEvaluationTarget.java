/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.LocalDate;

import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.execution.ArbitraryViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionSequence;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * Target for {@link HistoricalViewEvaluationFunction} which ensures that an execution sequence is constructed which matches the function's expectations.
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

  /**
   * Creates a new target.
   * 
   * @param user the user the view is created for, not null.
   * @param startDate the start date as a {@link DateConstraint} encoded string, not null
   * @param includeStart whether to include the start date in the evaluation.
   * @param endDate the end date as a {@link DateConstraint} encoded string, not null
   * @param includeEnd whether to include the end date in the evaluation.
   */
  public HistoricalViewEvaluationTarget(UserPrincipal user, String startDate, boolean includeStart, String endDate, boolean includeEnd) {
    super(user, new HistoricalSequence(startDate, includeStart, endDate, includeEnd));
  }

  protected HistoricalViewEvaluationTarget(ViewDefinition viewDefinition, final HistoricalSequence sequence) {
    super(viewDefinition, sequence);
  }

  private HistoricalViewEvaluationTarget(FudgeDeserializer deserializer, FudgeMsg message) {
    this(deserializer, message, message.getString(START_DATE_FIELD), message.getBoolean(INCLUDE_START_FIELD), message.getString(END_DATE_FIELD), message.getBoolean(INCLUDE_END_FIELD));
  }

  private HistoricalViewEvaluationTarget(FudgeDeserializer deserializer, FudgeMsg message, String startDate, boolean includeStart, String endDate, boolean includeEnd) {
    super(deserializer, message, new HistoricalSequence(startDate, includeStart, endDate, includeEnd));
  }

  protected HistoricalViewEvaluationTarget(final HistoricalViewEvaluationTarget copyFrom, final UniqueId uid) {
    super(copyFrom, uid);
  }

  @Override
  protected ViewEvaluationTarget createUnion(final ViewDefinition newViewDefinition) {
    return new HistoricalViewEvaluationTarget(newViewDefinition, (HistoricalSequence) getExecutionSequence());
  }

  private static final class HistoricalSequence implements ViewCycleExecutionSequenceDescriptor {

    private final String _startDateDescriptor;
    private final boolean _includeStart;
    private final String _endDateDescriptor;
    private final boolean _includeEnd;

    public HistoricalSequence(final String startDateDescriptor, final boolean includeStart, final String endDateDescriptor, final boolean includeEnd) {
      ArgumentChecker.notNull(startDateDescriptor, "startDate");
      ArgumentChecker.notNull(endDateDescriptor, "endDate");
      _startDateDescriptor = startDateDescriptor;
      _includeStart = includeStart;
      _endDateDescriptor = endDateDescriptor;
      _includeEnd = includeEnd;
    }

    // ViewCycleExecutionSequenceDescriptor

    @Override
    public ViewCycleExecutionSequence createSequence(FunctionExecutionContext executionContext) {
      LocalDate startDate = DateConstraint.evaluate(executionContext, _startDateDescriptor);
      LocalDate endDate = DateConstraint.evaluate(executionContext, _endDateDescriptor);
      if (!_includeStart) {
        startDate = startDate.plusDays(1);
      }
      if (!_includeEnd) {
        endDate = endDate.minusDays(1);
      }
      List<ViewCycleExecutionOptions> executionSequence = new LinkedList<ViewCycleExecutionOptions>();
      LocalDate currentDate = startDate;
      while (!currentDate.isAfter(endDate)) {
        // TODO: Is it correct to leave the valuation time blank? We do know it - it's in the execution context
        ViewCycleExecutionOptions executionOptions = ViewCycleExecutionOptions.builder()
            .setMarketDataSpecification(new FixedHistoricalMarketDataSpecification(currentDate))
            .create();
        executionSequence.add(executionOptions);
        currentDate = currentDate.plusDays(1);
      }
      return new ArbitraryViewCycleExecutionSequence(executionSequence);
    }

    // Object

    @Override
    public int hashCode() {
      int hc = 1;
      hc += (hc << 4) + ObjectUtils.hashCode(_startDateDescriptor);
      hc += (hc << 4) + (_includeStart ? 1 : 0);
      hc += (hc << 4) + ObjectUtils.hashCode(_endDateDescriptor);
      hc += (hc << 4) + (_includeEnd ? 1 : 0);
      return hc;
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof HistoricalSequence)) {
        return false;
      }
      final HistoricalSequence other = (HistoricalSequence) o;
      return ObjectUtils.equals(_startDateDescriptor, other._startDateDescriptor)
          && (_includeStart == other._includeStart)
          && ObjectUtils.equals(_endDateDescriptor, other._endDateDescriptor)
          && (_includeEnd == other._includeEnd);
    }

  }

  public String getStartDate() {
    return ((HistoricalSequence) getExecutionSequence())._startDateDescriptor;
  }

  public boolean isIncludeStart() {
    return ((HistoricalSequence) getExecutionSequence())._includeStart;
  }

  public String getEndDate() {
    return ((HistoricalSequence) getExecutionSequence())._endDateDescriptor;
  }

  public boolean isIncludeEnd() {
    return ((HistoricalSequence) getExecutionSequence())._includeEnd;
  }

  @Override
  public HistoricalViewEvaluationTarget withUniqueId(final UniqueId uid) {
    return new HistoricalViewEvaluationTarget(this, uid);
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
