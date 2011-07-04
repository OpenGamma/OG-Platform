/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.combining;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.server.AbstractLiveDataServer;
import com.opengamma.livedata.server.CombiningLiveDataServer;
import com.opengamma.livedata.server.DistributionSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Combines live data servers by chosing the first server which can resolve the ID
 * If non can then the first server is returned ( which will fail )
 */
public class PriorityResolvingCombiningLiveDataServer extends CombiningLiveDataServer {
  private final List<? extends AbstractLiveDataServer> _servers;

  /**
   * @param servers Servers in preference order (Best first)
   */
  public PriorityResolvingCombiningLiveDataServer(List<? extends AbstractLiveDataServer> servers) {
    ArgumentChecker.notEmpty(servers, "servers");
    _servers = servers;
  }

  @Override
  protected Map<AbstractLiveDataServer, Collection<LiveDataSpecification>> groupByServer(
      Collection<LiveDataSpecification> specs) {
    Map<AbstractLiveDataServer, Collection<LiveDataSpecification>> ret = new HashMap<AbstractLiveDataServer, Collection<LiveDataSpecification>>();

    Collection<LiveDataSpecification> unresolvedSpecs = specs;

    for (AbstractLiveDataServer server : _servers) {
      Map<LiveDataSpecification, DistributionSpecification> resolved = server.getDistributionSpecificationResolver()
          .resolve(unresolvedSpecs);

      unresolvedSpecs = new HashSet<LiveDataSpecification>();
      Set<LiveDataSpecification> resolvedSpecs = new HashSet<LiveDataSpecification>();

      for (Entry<LiveDataSpecification, DistributionSpecification> entry : resolved.entrySet()) {
        if (entry.getValue() != null) {
          resolvedSpecs.add(entry.getKey());
        } else {
          unresolvedSpecs.add(entry.getKey());
        }
      }
      ret.put(server, resolvedSpecs);
      if (unresolvedSpecs.size() == 0) {
        return ret;
      }
    }

    AbstractLiveDataServer defaultServer = _servers.get(0);
    Collection<LiveDataSpecification> defaultSet = ret.get(defaultServer);
    if (defaultSet == null) {
      ret.put(defaultServer, unresolvedSpecs);
    } else {
      defaultSet.addAll(unresolvedSpecs);
    }
    return ret;

  }
}
