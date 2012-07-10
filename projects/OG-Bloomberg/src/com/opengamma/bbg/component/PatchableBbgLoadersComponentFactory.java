/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.component;

import org.joda.beans.BeanDefinition;

import com.opengamma.bbg.PatchableReferenceDataProvider;
import com.opengamma.bbg.ReferenceDataProvider;
import com.opengamma.bbg.RemoteReferenceDataProviderFactoryBean;
import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;

/**
 * Component factory for a standard set of Bloomberg-based loader components with a patchable reference data provider
 */
@BeanDefinition
public class PatchableBbgLoadersComponentFactory extends BbgLoadersComponentFactory {

  protected ReferenceDataProvider initReferenceDataProvider(ComponentRepository repo) {
    RemoteReferenceDataProviderFactoryBean factory = new RemoteReferenceDataProviderFactoryBean();
    factory.setJmsConnector(getJmsConnector());
    factory.setRequestTopic(getReferenceDataJmsTopic());
    factory.setFudgeContext(getFudgeContext());
    
    ReferenceDataProvider refData = factory.getObjectCreating();
    ReferenceDataProvider wrappedRefData = new PatchableReferenceDataProvider(refData);
    ComponentInfo info = new ComponentInfo(ReferenceDataProvider.class, "bbg");
    repo.registerComponent(info, wrappedRefData);
    return wrappedRefData;
  }

}
