/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.exchange;

import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.util.component.Component;
import com.opengamma.util.component.ComponentConfig;
import com.opengamma.util.component.ComponentRepository;
import com.opengamma.util.db.DbConnector;

/**
 * An exchange master component.
 */
public class ExchangeMasterComponent extends Component {

  @Override
  public void start(ComponentRepository repo, ComponentConfig config) {
    DbConnector connector = repo.get(DbConnector.class, "exchange");
    DbExchangeMaster master = new DbExchangeMaster(connector);
    
    String scheme = config.getString("scheme");
    if (scheme != null) {
      master.setUniqueIdScheme(scheme);
    }
    
    Integer maxRetries = config.getInteger("max.retries");
    if (maxRetries != null) {
      master.setMaxRetries(maxRetries);
    }
    
//    Boolean cache = config.getBoolean("cache");
//    if (BooleanUtils.isNotFalse(cache)) {
//    }
    
    repo.register(ExchangeMaster.class, master, "persistent");
  }

}
