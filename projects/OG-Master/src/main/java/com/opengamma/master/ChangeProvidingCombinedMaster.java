/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master;

import java.util.List;

import com.opengamma.core.change.AggregatingChangeManager;
import com.opengamma.core.change.ChangeManager;

/**
 * Adds logic for handling changes across a list of combined masters.
 * @param <D> document type
 * @param <M> type of a change providing master
 */
public class ChangeProvidingCombinedMaster<D extends AbstractDocument, M extends AbstractChangeProvidingMaster<D>> extends CombinedMaster<D, M> implements AbstractChangeProvidingMaster<D> {

  private final AggregatingChangeManager _changeManager;

  protected ChangeProvidingCombinedMaster(List<M> masterList) {
    super(masterList);
    
    AggregatingChangeManager changeManager = new AggregatingChangeManager();
    
    for (M master : masterList) {
      changeManager.addChangeManager(master.changeManager());
    }
    
    _changeManager = changeManager;

    
  }

  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

}
