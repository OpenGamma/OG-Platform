/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.batch.domain;

import com.opengamma.engine.ComputationTargetSpecification;

/**
 * An error that occurs when running a batch.
 * <p>
 * This class is non-modifiable, however a subclass might not be.
 */
public class BatchError {

  /**
   * The configuration used.
   */
  private final String _calculationConfiguration;
  /**
   * The computation target.
   */
  private final ComputationTargetSpecification _computationTarget;
  /**
   * The name of the value.
   */
  private final String _valueName;
  /**
   * The function id.
   */
  private final String _functionUniqueId;
  /**
   * The exception class name.
   */
  private final String _exceptionClass;
  /**
   * The exception message.
   */
  private final String _exceptionMsg;
  /**
   * The stack trace.
   */
  private final String _stackTrace;

  /**
   * Creates an instance.
   * 
   * @param calculationConfiguration  the configuration
   * @param computationTarget  the computation target
   * @param valueName  the value name
   * @param functionUniqueId  the function id
   * @param exceptionClass  the exception class name
   * @param exceptionMsg  the exception message
   * @param stackTrace  the stack trace
   */
  public BatchError(String calculationConfiguration,
      ComputationTargetSpecification computationTarget,
      String valueName,
      String functionUniqueId,
      String exceptionClass,
      String exceptionMsg,
      String stackTrace) {
    _calculationConfiguration = calculationConfiguration;
    _computationTarget = computationTarget;
    _valueName = valueName;
    _functionUniqueId = functionUniqueId;
    _exceptionClass = exceptionClass;
    _exceptionMsg = exceptionMsg;
    _stackTrace = stackTrace;
  }

  //-------------------------------------------------------------------------
  public String getCalculationConfiguration() {
    return _calculationConfiguration;
  }

  public ComputationTargetSpecification getComputationTarget() {
    return _computationTarget;
  }

  public String getValueName() {
    return _valueName;
  }

  public String getFunctionUniqueId() {
    return _functionUniqueId;
  }

  public String getExceptionClass() {
    return _exceptionClass;
  }

  public String getExceptionMsg() {
    return _exceptionMsg;
  }

  public String getStackTrace() {
    return _stackTrace;
  }

}
