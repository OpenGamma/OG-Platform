/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import com.opengamma.engine.ComputationTargetSpecification;

/**
 * 
 */
public class BatchError {
  
  private final String _calculationConfiguration;
  
  private final ComputationTargetSpecification _computationTarget;
  
  private final String _valueName;
  
  private final String _functionUniqueId;
  
  private final String _exceptionClass;
  
  private final String _exceptionMsg;
  
  private final String _stackTrace;
  
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
