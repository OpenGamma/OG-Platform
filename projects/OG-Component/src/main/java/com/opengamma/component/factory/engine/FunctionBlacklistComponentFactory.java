/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.engine;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.component.factory.ComponentInfoAttributes;
import com.opengamma.engine.function.blacklist.FunctionBlacklist;
import com.opengamma.engine.function.blacklist.FunctionBlacklistFactoryBean;
import com.opengamma.engine.function.blacklist.ManageableFunctionBlacklist;
import com.opengamma.engine.function.blacklist.RemoteFunctionBlacklist;
import com.opengamma.engine.function.blacklist.RemoteManageableFunctionBlacklist;

/**
 * Component factory form of {@link FunctionBlacklistFactoryBean}.
 */
public class FunctionBlacklistComponentFactory extends AbstractComponentFactory {

  /**
   * The underlying bean.
   */
  private final FunctionBlacklistFactoryBean _bean = new FunctionBlacklistFactoryBean();

  @Override
  public void init(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) {
    String classifier = "default";
    final Iterator<Map.Entry<String, String>> itr = configuration.entrySet().iterator();
    while (itr.hasNext()) {
      final Map.Entry<String, String> conf = itr.next();
      if ("classifier".equals(conf.getKey())) {
        classifier = conf.getValue();
      } else {
        try {
          if (conf.getValue().startsWith("::")) {
            final Class<?> property = PropertyUtils.getPropertyType(_bean, conf.getKey());
            final ComponentInfo info = repo.findInfo(property, conf.getValue().substring(2));
            if (info != null) {
              BeanUtils.setProperty(_bean, conf.getKey(), repo.getInstance(info));
            } else {
              BeanUtils.setProperty(_bean, conf.getKey(), conf.getValue());
            }
          } else {
            BeanUtils.setProperty(_bean, conf.getKey(), conf.getValue());
          }
        } catch (Exception e) {
          throw new OpenGammaRuntimeException("invalid property '" + conf.getKey() + "' on " + _bean, e);
        }
      }
      itr.remove();
    }
    final FunctionBlacklist blacklist = _bean.getObjectCreating();
    
    ComponentInfo infoRO = new ComponentInfo(FunctionBlacklist.class, classifier);
    infoRO.addAttribute(ComponentInfoAttributes.LEVEL, 1);
    infoRO.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteFunctionBlacklist.class);
    repo.registerComponent(infoRO, blacklist);
    
    if (blacklist instanceof ManageableFunctionBlacklist) {
      ComponentInfo infoMng = new ComponentInfo(ManageableFunctionBlacklist.class, classifier);
      infoMng.addAttribute(ComponentInfoAttributes.LEVEL, 1);
      infoMng.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteManageableFunctionBlacklist.class);
      repo.registerComponent(infoMng, blacklist);
    }
  }

}
