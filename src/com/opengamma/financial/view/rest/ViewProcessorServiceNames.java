/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */

package com.opengamma.financial.view.rest;

/**
 * Names used in the view processor RESTful service.
 */
public interface ViewProcessorServiceNames {

  // CSOFF: Just a set of constants
  
  /**
   * The default name of the view processor.
   */
  public static final String DEFAULT_VIEWPROCESSOR_NAME = "0";
  public static final String VIEWPROCESSOR_LIVECOMPUTATIONSUPPORTED = "liveComputationSupported";
  public static final String VIEWPROCESSOR_ONEOFFCOMPUTATIONSUPPORTED = "oneOffComputationSupported";
  public static final String VIEWPROCESSOR_AVAILABLEVIEWNAMES = "availableViewNames";
  public static final String VIEWPROCESSOR_LIVECOMPUTINGVIEWNAMES = "liveComputingViewNames";
  public static final String VIEWPROCESSOR_VIEW = "view";
  public static final String VIEWPROCESSOR_SUPPORTED = "supported";
  public static final String VIEWPROCESSOR_LIVECALCULATION = "liveCalculation";
  public static final String VIEWPROCESSOR_LIVECALCULATION_ACTION = "action";
  public static final String VIEWPROCESSOR_LIVECALCULATION_ACTION_START = "START";
  public static final String VIEWPROCESSOR_LIVECALCULATION_ACTION_STOP = "STOP";
  
  public static final String VIEW_ALLSECURITYTYPES = "allSecurityTypes";
  public static final String VIEW_ALLVALUENAMES = "allValueNames";
  public static final String VIEW_LATESTRESULT = "latestResult";
  public static final String VIEW_NAME = "name";
  public static final String VIEW_PORTFOLIO = "portfolio";
  public static final String VIEW_REQUIREMENTNAMES = "requirementNames";
  public static final String VIEW_REQUIRED_LIVE_DATA = "requiredLiveData";
  public static final String VIEW_STATUS = "status";
  public static final String VIEW_LIVECOMPUTATIONRUNNING = "liveComputationRunning";
  public static final String VIEW_RESULTAVAILABLE = "resultAvailable";
  public static final String VIEW_PERFORMCOMPUTATION = "performComputation";
  public static final String VIEW_COMPUTATIONRESULT = "computationResult";
  public static final String VIEW_DELTARESULT = "deltaResult";
  public static final String VIEW_LIVE_DATA_INJECTOR = "liveDataInjector";

  // CSON: Just a set of constants
}
