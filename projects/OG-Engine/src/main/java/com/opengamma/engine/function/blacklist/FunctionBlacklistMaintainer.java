/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.Collection;

import com.opengamma.engine.calcnode.CalculationJobItem;

/**
 * Builds/maintains a function blacklist as failure events are received.
 */
public interface FunctionBlacklistMaintainer {

  /**
   * Report a single failed job item.
   * 
   * @param item the failed job item, not null
   */
  void failedJobItem(CalculationJobItem item);

  /**
   * Reports one or more failed job items.
   * 
   * @param items the fail job items, not null
   */
  void failedJobItems(Collection<CalculationJobItem> items);

}
