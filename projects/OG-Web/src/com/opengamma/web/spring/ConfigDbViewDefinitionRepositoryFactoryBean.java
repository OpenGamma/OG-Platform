/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.spring;

import com.opengamma.engine.view.ViewDefinitionRepository;
import com.opengamma.financial.view.NotifyingConfigDbViewDefinitionRepository;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Creates a ViewDefinitionRepository (using the MapViewDefinitionRepository implementation) for a set of views.
 * <p>
 * The views are loaded from the database using configmaster
 */
public class ConfigDbViewDefinitionRepositoryFactoryBean extends SingletonFactoryBean<ViewDefinitionRepository> {

  private ConfigMaster _configMaster;
  
  public ConfigMaster getConfigMaster() {
    return _configMaster;
  }

  public void setConfigMaster(ConfigMaster configMaster) {
    _configMaster = configMaster;
  }

  @Override
  protected ViewDefinitionRepository createObject() {
    ArgumentChecker.notNullInjected(getConfigMaster(), "configMaster");
    return new NotifyingConfigDbViewDefinitionRepository(getConfigMaster());
  }

}
