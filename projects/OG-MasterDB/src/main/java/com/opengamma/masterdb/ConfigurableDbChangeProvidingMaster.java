/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb;

import com.opengamma.core.change.ChangeManager;


/**
 * Provides access to configuration methods on {@link AbstractDbMaster}, 
 * {@link AbstractDocumentDbMaster} (and potentially other implementations). 
 * This makes it possible to initialize in a standard way. See 
 * AbstractDocumentDbMasterComponentFactory (og-component) for further 
 * details.
 */
public interface ConfigurableDbChangeProvidingMaster extends ConfigurableDbMaster {

  /**
   * Sets the change manager.
   *
   * @param changeManager  the change manager, not null
   */
  void setChangeManager(final ChangeManager changeManager);

  /**
   * Gets the change manager.
   *
   * @return the change manager, not null
   */
  ChangeManager getChangeManager();

}
