/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMessageFactory;
import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ResultModelDefinition {
  
  /**
   * Fudge message key for the name.
   */
  public static final String COMPUTE_PORTFOLIO_NODE_CALCULATIONS_KEY = "computePortfolioNodeCalculations";
  /**
   * Fudge message key for the portfolioId.
   */
  public static final String COMPUTE_POSITION_NODE_CALCULATIONS_KEY = "computePositionNodeCalculations";
  /**
   * Fudge message key for the liveDataUser.
   */
  public static final String COMPUTE_SECURITY_NODE_CALCULATIONS_KEY = "computeSecurityNodeCalculations";
  /**
   * Fudge message key for the liveDataUser.
   */
  public static final String COMPUTE_PRIMITIVE_NODE_CALCULATIONS_KEY = "computePrimitiveNodeCalculations";
  
  private boolean _computePortfolioNodeCalculations = true;
  private boolean _computePositionNodeCalculations = true;
  private boolean _computeSecurityNodeCalculations /*= false*/;
  private boolean _computePrimitiveNodeCalculations /*= false*/;

  /**
   * @return whether or not to compute all portfolio nodes, rather than just those required
   */
  public boolean isComputePortfolioNodeCalculations() {
    return _computePortfolioNodeCalculations;
  }

  /**
   * @param computePortfolioNodeCalculations whether or not to compute all portfolio nodes, rather than just those required
   */
  public void setComputePortfolioNodeCalculations(boolean computePortfolioNodeCalculations) {
    _computePortfolioNodeCalculations = computePortfolioNodeCalculations;
  }

  /**
   * @return whether or not to compute all position nodes, rather than just those required
   */
  public boolean isComputePositionNodeCalculations() {
    return _computePositionNodeCalculations;
  }

  /**
   * @param computePositionNodeCalculations whether or not to compute all position nodes, rather than just those required
   */
  public void setComputePositionNodeCalculations(boolean computePositionNodeCalculations) {
    _computePositionNodeCalculations = computePositionNodeCalculations;
  }

  /**
   * @return whether or not to compute all security nodes, rather than just those required
   */
  public boolean isComputeSecurityNodeCalculations() {
    return _computeSecurityNodeCalculations;
  }

  /**
   * @param computeSecurityNodeCalculations whether or not to compute all security nodes, rather than just those required
   */
  public void setComputeSecurityNodeCalculations(boolean computeSecurityNodeCalculations) {
    _computeSecurityNodeCalculations = computeSecurityNodeCalculations;
  }

  /**
   * @return whether or not to compute all primitive nodes, rather than just those required
   */
  public boolean isComputePrimitiveNodeCalculations() {
    return _computePrimitiveNodeCalculations;
  }

  /**
   * @param computePrimitiveNodeCalculations whether or not to compute all primitive nodes, rather than just those required
   */
  public void setComputePrimitiveNodeCalculations(boolean computePrimitiveNodeCalculations) {
    _computePrimitiveNodeCalculations = computePrimitiveNodeCalculations;
  }
  
  public boolean shouldWriteResults(ComputationTargetType computationTargetType) {
    switch (computationTargetType) {
      case PRIMITIVE:
        return isComputePrimitiveNodeCalculations();
      case SECURITY:
        return isComputeSecurityNodeCalculations();
      case POSITION:
        return isComputePositionNodeCalculations();
      case PORTFOLIO_NODE:
        return isComputePortfolioNodeCalculations();
      default:
        throw new RuntimeException("Unexpected type " + computationTargetType);
    }
  }
  
  /**
   * Serializes this ViewDefinition to a Fudge message.
   * @param factory  the Fudge context, not null
   * @return the Fudge message, not null
   */
  public FudgeFieldContainer toFudgeMsg(FudgeMessageFactory factory) {
    ArgumentChecker.notNull(factory, "Fudge Context");
    MutableFudgeFieldContainer msg = factory.newMessage();
    msg.add(COMPUTE_PORTFOLIO_NODE_CALCULATIONS_KEY, _computePortfolioNodeCalculations);
    msg.add(COMPUTE_POSITION_NODE_CALCULATIONS_KEY, _computePositionNodeCalculations);
    msg.add(COMPUTE_SECURITY_NODE_CALCULATIONS_KEY, _computeSecurityNodeCalculations);
    msg.add(COMPUTE_PRIMITIVE_NODE_CALCULATIONS_KEY, _computePrimitiveNodeCalculations);
    return msg;
  }
  
  /**
   * Deserializes this ResultModelDefinition from a Fudge message.
   * @param msg  the Fudge message, not null
   * @return the ResultModelDefinition, not null
   */
  public static ResultModelDefinition fromFudgeMsg(FudgeFieldContainer msg) {
    ResultModelDefinition result = new ResultModelDefinition();
    result._computePortfolioNodeCalculations = msg.getBoolean(COMPUTE_PORTFOLIO_NODE_CALCULATIONS_KEY);
    result._computePositionNodeCalculations = msg.getBoolean(COMPUTE_POSITION_NODE_CALCULATIONS_KEY);
    result._computePrimitiveNodeCalculations = msg.getBoolean(COMPUTE_PRIMITIVE_NODE_CALCULATIONS_KEY);
    result._computeSecurityNodeCalculations = msg.getBoolean(COMPUTE_SECURITY_NODE_CALCULATIONS_KEY);
    return result;
  }


}
