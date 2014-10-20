/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.server;

/**
 * Enables the FunctionServerComponentFactory to provide which version correction instant is to be
 * used in the CycleRunner. Using cycle start mean that caching will not be as effective
 */
public enum CycleVersionCorrectionStrategy {
  /**
   * Version correction is set to the server start time
   */
  SERVER_START,
  /**
   * Version correction is set to cycle start type when a cycle runs
   */
  CYCLE_START
}
