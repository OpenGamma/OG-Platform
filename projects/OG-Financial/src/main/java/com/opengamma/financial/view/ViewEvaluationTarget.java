/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import org.apache.commons.lang.ObjectUtils;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.temptarget.TempTarget;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;

/**
 * Target for functions which can create and manipulate a view client to perform one or more evaluations.
 */
public class ViewEvaluationTarget extends TempTarget {

  /**
   * Fudge field containing the view definition.
   */
  protected static final String VIEW_DEFINITION_FIELD = "viewDefinition";
  /**
   * Fudge field containing the execution sequence.
   */
  protected static final String EXECUTION_SEQUENCE_FIELD = "executionSequence";

  /**
   * The view definition to be executed.
   */
  private final ViewDefinition _viewDefinition;

  /**
   * The execution sequence.
   */
  private final ViewCycleExecutionSequenceDescriptor _executionSequence;

  /**
   * Creates a new target with an empty/default view definition.
   * 
   * @param user the market data user, for example taken from the parent/containing view definition, not null
   * @param executionSequence the execution sequence to evaluate, not null
   */
  public ViewEvaluationTarget(UserPrincipal user, ViewCycleExecutionSequenceDescriptor executionSequence) {
    _viewDefinition = new ViewDefinition("Temp", user);
    _executionSequence = executionSequence;
  }

  protected ViewEvaluationTarget(FudgeDeserializer deserializer, FudgeMsg message) {
    super(deserializer, message);
    _viewDefinition = deserializer.fieldValueToObject(ViewDefinition.class, message.getByName(VIEW_DEFINITION_FIELD));
    _executionSequence = deserializer.fieldValueToObject(ViewCycleExecutionSequenceDescriptor.class, message.getByName(EXECUTION_SEQUENCE_FIELD));
  }

  protected ViewEvaluationTarget(FudgeDeserializer deserializer, FudgeMsg message, ViewCycleExecutionSequenceDescriptor executionSequence) {
    super(deserializer, message);
    _viewDefinition = deserializer.fieldValueToObject(ViewDefinition.class, message.getByName(VIEW_DEFINITION_FIELD));
    _executionSequence = executionSequence;
  }

  protected ViewEvaluationTarget(ViewDefinition viewDefinition, ViewCycleExecutionSequenceDescriptor executionSequence) {
    _viewDefinition = viewDefinition;
    _executionSequence = executionSequence;
  }

  protected ViewEvaluationTarget(final ViewEvaluationTarget copyFrom, final UniqueId uid) {
    super(uid);
    _viewDefinition = copyFrom._viewDefinition;
    _executionSequence = copyFrom._executionSequence;
  }

  public ViewDefinition getViewDefinition() {
    return _viewDefinition;
  }

  public ViewCycleExecutionSequenceDescriptor getExecutionSequence() {
    return _executionSequence;
  }

  /**
   * Creates a target which is the union of this and another. The other target must have compatible valuation parameters.
   * 
   * @param other the other target, not null
   * @return null if the other target is not compatible, otherwise a new instance containing a view definition that is the union of the two participant view definitions
   */
  public ViewEvaluationTarget union(ViewEvaluationTarget other) {
    // Check the valuation parameters are compatible
    if (!getExecutionSequence().equals(other.getExecutionSequence())) {
      return null;
    }
    // Check the basic view definitions are compatible
    final ViewDefinition myView = getViewDefinition();
    final ViewDefinition otherView = other.getViewDefinition();
    if (!ObjectUtils.equals(myView.getDefaultCurrency(), otherView.getDefaultCurrency())
        || !ObjectUtils.equals(myView.getMarketDataUser(), otherView.getMarketDataUser())
        || !ObjectUtils.equals(myView.getMaxDeltaCalculationPeriod(), otherView.getMaxDeltaCalculationPeriod())
        || !ObjectUtils.equals(myView.getMaxFullCalculationPeriod(), otherView.getMaxFullCalculationPeriod())
        || !ObjectUtils.equals(myView.getMinDeltaCalculationPeriod(), otherView.getMinDeltaCalculationPeriod())
        || !ObjectUtils.equals(myView.getMinFullCalculationPeriod(), otherView.getMinFullCalculationPeriod())
        || !ObjectUtils.equals(myView.getName(), otherView.getName())
        || !ObjectUtils.equals(myView.getPortfolioId(), otherView.getPortfolioId())
        || !ObjectUtils.equals(myView.getResultModelDefinition(), otherView.getResultModelDefinition())) {
      return null;
    }
    // Check the calc configs are compatible
    for (final ViewCalculationConfiguration myConfig : myView.getAllCalculationConfigurations()) {
      final ViewCalculationConfiguration otherConfig = otherView.getCalculationConfiguration(myConfig.getName());
      if (!ObjectUtils.equals(myConfig.getDefaultProperties(), otherConfig.getDefaultProperties())
          || !ObjectUtils.equals(myConfig.getDeltaDefinition(), otherConfig.getDeltaDefinition())
          || !ObjectUtils.equals(myConfig.getResolutionRuleTransform(), otherConfig.getResolutionRuleTransform())) {
        // Configs aren't compatible
        return null;
      }
    }
    // Create a new view definition that is the union of all calc configs
    final ViewDefinition newView = myView.copyWith(myView.getName(), myView.getPortfolioId(), myView.getMarketDataUser());
    for (final ViewCalculationConfiguration otherConfig : otherView.getAllCalculationConfigurations()) {
      final ViewCalculationConfiguration newConfig = newView.getCalculationConfiguration(otherConfig.getName());
      if (newConfig == null) {
        myView.addViewCalculationConfiguration(newConfig);
      } else {
        newConfig.addSpecificRequirements(otherConfig.getSpecificRequirements());
      }
    }
    return createUnion(newView);
  }

  protected ViewEvaluationTarget createUnion(final ViewDefinition newViewDefinition) {
    return new ViewEvaluationTarget(newViewDefinition, getExecutionSequence());
  }

  // TempTarget

  @Override
  public ViewEvaluationTarget withUniqueId(final UniqueId uid) {
    return new ViewEvaluationTarget(this, uid);
  }

  @Override
  protected boolean equalsImpl(Object o) {
    final ViewEvaluationTarget other = (ViewEvaluationTarget) o;
    return getExecutionSequence().equals(other.getExecutionSequence())
        && getViewDefinition().equals(other.getViewDefinition());
  }

  @Override
  protected int hashCodeImpl() {
    int hc = 1;
    hc += (hc << 4) + getViewDefinition().hashCode();
    hc += (hc << 4) + getExecutionSequence().hashCode();
    return hc;
  }

  @Override
  protected void toFudgeMsgImpl(FudgeSerializer serializer, MutableFudgeMsg message) {
    super.toFudgeMsgImpl(serializer, message);
    serializer.addToMessageWithClassHeaders(message, VIEW_DEFINITION_FIELD, null, getViewDefinition(), ViewDefinition.class);
    serializeExecutionSequence(serializer, message);
  }

  protected void serializeExecutionSequence(FudgeSerializer serializer, MutableFudgeMsg message) {
    serializer.addToMessageWithClassHeaders(message, EXECUTION_SEQUENCE_FIELD, null, getExecutionSequence());
  }

  public static ViewEvaluationTarget fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg message) {
    return new ViewEvaluationTarget(deserializer, message);
  }

}
