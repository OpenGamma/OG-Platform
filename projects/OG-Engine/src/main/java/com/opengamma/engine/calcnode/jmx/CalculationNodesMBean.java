/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode.jmx;

/**
 * JMX exposure of the calculation node management.
 */
public interface CalculationNodesMBean {

  // TODO: Have the "local" ones exposed here. Potentially have proxies to the remote ones so that the JMX panel on the view processor can be used to tweak and monitor the remote nodes too

  int getTotalNodeCount();

  int getAvailableNodeCount();

  int getTotalJobCount();

  int getRunnableJobCount();

  int getPartialJobCount();

  String removeNode();

  String addNode();

}
