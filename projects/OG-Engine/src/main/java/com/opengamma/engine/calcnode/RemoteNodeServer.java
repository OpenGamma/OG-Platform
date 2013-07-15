/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.cache.IdentifierMap;
import com.opengamma.engine.calcnode.msg.Init;
import com.opengamma.engine.calcnode.msg.Ready;
import com.opengamma.engine.calcnode.msg.RemoteCalcNodeMessage;
import com.opengamma.engine.calcnode.msg.RemoteCalcNodeMessageVisitor;
import com.opengamma.engine.calcnode.stats.FunctionCosts;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.blacklist.DefaultFunctionBlacklistMaintainer;
import com.opengamma.engine.function.blacklist.DefaultFunctionBlacklistQuery;
import com.opengamma.engine.function.blacklist.DummyFunctionBlacklistMaintainer;
import com.opengamma.engine.function.blacklist.DummyFunctionBlacklistQuery;
import com.opengamma.engine.function.blacklist.FunctionBlacklistMaintainer;
import com.opengamma.engine.function.blacklist.FunctionBlacklistPolicy;
import com.opengamma.engine.function.blacklist.FunctionBlacklistProvider;
import com.opengamma.engine.function.blacklist.FunctionBlacklistQuery;
import com.opengamma.engine.function.blacklist.ManageableFunctionBlacklist;
import com.opengamma.engine.function.blacklist.ManageableFunctionBlacklistProvider;
import com.opengamma.engine.function.blacklist.MultipleFunctionBlacklistMaintainer;
import com.opengamma.engine.function.blacklist.MultipleFunctionBlacklistQuery;
import com.opengamma.transport.FudgeConnection;
import com.opengamma.transport.FudgeConnectionReceiver;

/**
 * Server end to RemoteNodeClient to receive requests from remote calculation nodes and marshal
 * them into RemoteNodeJobInvokers that a JobDispatcher can then use.
 */
public class RemoteNodeServer implements FudgeConnectionReceiver {

  private static final Logger s_logger = LoggerFactory.getLogger(RemoteNodeServer.class);
  
  /**
   * Callback interface for supplying a blacklist maintainer to each host invoker.
   */
  public interface FunctionBlacklistMaintainerProvider {

    /**
     * Returns the maintenance interface.
     * 
     * @param hostId the host handshake identifier
     * @return the interface, or null for none
     */
    FunctionBlacklistMaintainer getUpdate(String hostId);
    
  }
  
  /**
   * Implementation of {@link FunctionBlacklistMaintainerProvider} that always returns a fixed blacklist maintainer.
   */
  public static class StaticFunctionBlacklistMaintainerProvider implements FunctionBlacklistMaintainerProvider {

    private final FunctionBlacklistMaintainer _maintainer;

    public StaticFunctionBlacklistMaintainerProvider(final FunctionBlacklistMaintainer maintainer) {
      _maintainer = maintainer;
    }

    @Override
    public FunctionBlacklistMaintainer getUpdate(final String hostId) {
      return _maintainer;
    }

  }

  /**
   * Implementation of {@link FunctionBlacklistMaintainerProvider} that updates a blacklist which is the host identifier prefixed with a fixed string. For example host "calc42" might use a blacklist
   * called "REMOTE_NODE_calc42" if the bean is set with the default prefix of "REMOTE_NODE_".
   */
  public static class FunctionBlacklistMaintainerProviderBean implements FunctionBlacklistMaintainerProvider {

    private ManageableFunctionBlacklistProvider _blacklistProvider;

    private String _blacklistPrefix = "REMOTE_NODE_";

    private FunctionBlacklistPolicy _policy;

    public void setBlacklistProvider(final ManageableFunctionBlacklistProvider blacklistProvider) {
      _blacklistProvider = blacklistProvider;
    }

    public ManageableFunctionBlacklistProvider getBlacklistProvider() {
      return _blacklistProvider;
    }

    public void setBlacklistPrefix(final String prefix) {
      _blacklistPrefix = prefix;
    }

    public String getBlacklistPrefix() {
      return _blacklistPrefix;
    }

    public void setBlacklistPolicy(final FunctionBlacklistPolicy policy) {
      _policy = policy;
    }

    public FunctionBlacklistPolicy getBlacklistPolicy() {
      return _policy;
    }

    @Override
    public FunctionBlacklistMaintainer getUpdate(final String hostId) {
      if (getBlacklistPolicy().isEmpty()) {
        return null;
      }
      final ManageableFunctionBlacklist blacklist = getBlacklistProvider().getBlacklist(getBlacklistPrefix() + hostId);
      if (blacklist == null) {
        return null;
      }
      return new DefaultFunctionBlacklistMaintainer(getBlacklistPolicy(), blacklist);
    }

  }

  /**
   * Implementation of {@link FunctionBlacklistMaintainerProvider} that wraps multiple maintainers up to create a {@link MultipleFunctionBlacklistMaintainer} for the host.
   */
  public static class MultipleFunctionBlacklistMaintainerProvider implements FunctionBlacklistMaintainerProvider {

    private final Collection<FunctionBlacklistMaintainerProvider> _providers;

    public MultipleFunctionBlacklistMaintainerProvider(final Collection<FunctionBlacklistMaintainerProvider> providers) {
      _providers = providers;
    }

    @Override
    public FunctionBlacklistMaintainer getUpdate(final String hostId) {
      final Collection<FunctionBlacklistMaintainer> maintainers = new ArrayList<FunctionBlacklistMaintainer>(_providers.size());
      for (FunctionBlacklistMaintainerProvider provider : _providers) {
        final FunctionBlacklistMaintainer maintainer = provider.getUpdate(hostId);
        if (maintainer != null) {
          maintainers.add(maintainer);
        }
      }
      if (maintainers.isEmpty()) {
        return null;
      } else {
        return new MultipleFunctionBlacklistMaintainer(maintainers);
      }
    }

  }

  /**
   * Callback interface for supplying a blacklist query to each host invoker.
   */
  public interface FunctionBlacklistQueryProvider {

    /**
     * Returns the query interface.
     * 
     * @param hostId the host handshake identifier
     * @return the interface, or null for none
     */
    FunctionBlacklistQuery getQuery(String hostId);
    
  }

  /**
   * Implementation of {@link FunctionBlacklistQueryProvider} that always returns a fixed blacklist query.
   */
  public static class StaticFunctionBlacklistQueryProvider implements FunctionBlacklistQueryProvider {

    private final FunctionBlacklistQuery _query;

    public StaticFunctionBlacklistQueryProvider(final FunctionBlacklistQuery query) {
      _query = query;
    }

    @Override
    public FunctionBlacklistQuery getQuery(final String hostId) {
      return _query;
    }

  }

  /**
   * Implementation of {@link FunctionBlacklistQueryProvider} that queries a blacklist which is the host identifier prefixed with a fixed string. For example host "calc42" might use a blacklist called
   * "REMOTE_NODE_calc42" if the bean is set with the default prefix of "REMOTE_NODE_".
   */
  public static class FunctionBlacklistQueryProviderBean implements FunctionBlacklistQueryProvider {

    private FunctionBlacklistProvider _blacklistProvider;

    private String _blacklistPrefix = "REMOTE_NODE_";

    public void setBlacklistProvider(final FunctionBlacklistProvider blacklistProvider) {
      _blacklistProvider = blacklistProvider;
    }

    public FunctionBlacklistProvider getBlacklistProvider() {
      return _blacklistProvider;
    }

    public void setBlacklistPrefix(final String prefix) {
      _blacklistPrefix = prefix;
    }

    public String getBlacklistPrefix() {
      return _blacklistPrefix;
    }

    @Override
    public FunctionBlacklistQuery getQuery(final String hostId) {
      return new DefaultFunctionBlacklistQuery(getBlacklistProvider().getBlacklist(getBlacklistPrefix() + hostId));
    }

  }

  /**
   * Implementation of {@link FunctionBlacklistQueryProvider} that wraps multiple queries up to create a {@link MultipleFunctionBlacklistQuery} for the host.
   */
  public static class MultipleFunctionBlacklistQueryProvider implements FunctionBlacklistQueryProvider {

    private final Collection<FunctionBlacklistQueryProvider> _providers;

    public MultipleFunctionBlacklistQueryProvider(final Collection<FunctionBlacklistQueryProvider> providers) {
      _providers = providers;
    }

    @Override
    public FunctionBlacklistQuery getQuery(final String hostId) {
      final Collection<FunctionBlacklistQuery> queries = new ArrayList<FunctionBlacklistQuery>(_providers.size());
      for (FunctionBlacklistQueryProvider provider : _providers) {
        final FunctionBlacklistQuery query = provider.getQuery(hostId);
        if (query != null) {
          queries.add(query);
        }
      }
      if (queries.isEmpty()) {
        return null;
      } else {
        return new MultipleFunctionBlacklistQuery(queries);
      }
    }

  }

  private final JobInvokerRegister _jobInvokerRegister;
  private final IdentifierMap _identifierMap;
  private final ExecutorService _executorService = Executors.newCachedThreadPool();
  private final FunctionCosts _functionCosts;
  private final FunctionCompilationContext _functionCompilationContext;
  private Set<Capability> _capabilitiesToAdd;
  private FunctionBlacklistMaintainerProvider _blacklistUpdate;
  private FunctionBlacklistQueryProvider _blacklistQuery;

  public RemoteNodeServer(final JobInvokerRegister jobInvokerRegister, final IdentifierMap identifierMap,
      final FunctionCosts functionCosts, final FunctionCompilationContext functionCompilationContext) {
    _jobInvokerRegister = jobInvokerRegister;
    _identifierMap = identifierMap;
    _functionCosts = functionCosts;
    _functionCompilationContext = functionCompilationContext;
  }

  /**
   * Specify capabilities to add to those explicitly declared by the remote nodes. If the nodes declare these
   * in the initial connection they will be overridden. After the initial connection any changes the node
   * sends will take effect again.
   * 
   * @param parameters Capabilities to add
   */
  public void setCapabilitiesToAdd(final Map<String, Double> parameters) {
    _capabilitiesToAdd = new HashSet<Capability>();
    for (Map.Entry<String, Double> parameter : parameters.entrySet()) {
      _capabilitiesToAdd.add(Capability.parameterInstanceOf(parameter.getKey(), parameter.getValue()));
    }
  }

  /**
   * Returns the mechanism for updating a node-specific blacklist with job failures.
   * 
   * @return the update mechanism, null for none
   */
  public FunctionBlacklistMaintainerProvider getBlacklistUpdate() {
    return _blacklistUpdate;
  }

  /**
   * Sets a mechanism for updating a node-specific blacklist with job failures.
   * 
   * @param provider the update mechanism, null for none
   */
  public void setBlacklistUpdate(final FunctionBlacklistMaintainerProvider provider) {
    _blacklistUpdate = provider;
  }

  protected FunctionBlacklistMaintainer getBlacklistUpdate(final String nodeId) {
    final FunctionBlacklistMaintainerProvider provider = getBlacklistUpdate();
    if (provider != null) {
      final FunctionBlacklistMaintainer maintainer = provider.getUpdate(nodeId);
      if (maintainer != null) {
        return maintainer;
      }
    }
    return new DummyFunctionBlacklistMaintainer();
  }

  /**
   * Returns the mechanism for querying a node-specific blacklist for job item suppression.
   * 
   * @return the query mechanism, null for none
   */
  public FunctionBlacklistQueryProvider getBlacklistQuery() {
    return _blacklistQuery;
  }

  /**
   * Sets the mechanism for querying a node-specific blacklist for job item suppression.
   * 
   * @param provider the query mechanism, null for none
   */
  public void setBlacklistQuery(final FunctionBlacklistQueryProvider provider) {
    _blacklistQuery = provider;
  }

  protected FunctionBlacklistQuery getBlacklistQuery(final String nodeId) {
    final FunctionBlacklistQueryProvider provider = getBlacklistQuery();
    if (provider != null) {
      final FunctionBlacklistQuery query = provider.getQuery(nodeId);
      if (query != null) {
        return query;
      }
    }
    return new DummyFunctionBlacklistQuery();
  }

  protected JobInvokerRegister getJobInvokerRegister() {
    return _jobInvokerRegister;
  }

  protected ExecutorService getExecutorService() {
    return _executorService;
  }

  protected IdentifierMap getIdentifierMap() {
    return _identifierMap;
  }

  protected FunctionCosts getFunctionCosts() {
    return _functionCosts;
  }

  protected FunctionCompilationContext getFunctionCompilationContext() {
    return _functionCompilationContext;
  }

  @Override
  public void connectionReceived(final FudgeContext fudgeContext, final FudgeMsgEnvelope message, final FudgeConnection connection) {
    final FudgeDeserializer deserializer = new FudgeDeserializer(fudgeContext);
    final RemoteCalcNodeMessage remoteCalcNodeMessage = deserializer.fudgeMsgToObject(RemoteCalcNodeMessage.class, message.getMessage());
    remoteCalcNodeMessage.accept(new RemoteCalcNodeMessageVisitor() {

      @Override
      protected void visitUnexpectedMessage(final RemoteCalcNodeMessage message) {
        s_logger.warn("Unexpected message {}", message);
      }

      @Override
      protected void visitReadyMessage(final Ready message) {
        s_logger.info("Remote node {} connected - {}", message.getHostId(), connection);
        final RemoteNodeJobInvoker invoker = new RemoteNodeJobInvoker(getExecutorService(), message, connection, getIdentifierMap(), getFunctionCosts(), getBlacklistQuery(message.getHostId()),
            getBlacklistUpdate(message.getHostId()));
        if (_capabilitiesToAdd != null) {
          invoker.addCapabilities(_capabilitiesToAdd);
        }
        final Init init = new Init(getFunctionCompilationContext().getFunctionInitId());
        invoker.sendMessage(init);
        getJobInvokerRegister().registerJobInvoker(invoker);
      }

    });
  }

}
