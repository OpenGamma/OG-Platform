/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import com.opengamma.id.UniqueId;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Bean for constructing basic {@link FunctionBlacklistMaintainer} instances.
 */
public class FunctionBlacklistMaintainerFactoryBean extends SingletonFactoryBean<FunctionBlacklistMaintainer> {

  private ManageableFunctionBlacklist _blacklist;

  private ManageableFunctionBlacklistProvider _blacklistSource;
  private String _blacklistName = "DEFAULT";

  private FunctionBlacklistPolicy _policy;

  private FunctionBlacklistPolicySource _policySource;
  private String _policyName = "DEFAULT";
  private UniqueId _policyUniqueId;

  public ManageableFunctionBlacklist getFunctionBlacklist() {
    return _blacklist;
  }

  public void setFunctionBlacklist(final ManageableFunctionBlacklist blacklist) {
    if ((blacklist != null) && (_blacklistSource != null)) {
      throw new IllegalStateException("Can't set a specific blacklist - blacklist source already set");
    }
    _blacklist = blacklist;
    if (blacklist != null) {
      _blacklistName = blacklist.getName();
    }
  }

  public ManageableFunctionBlacklistProvider getFunctionBlacklistSource() {
    return _blacklistSource;
  }

  public void setFunctionBlacklistSource(final ManageableFunctionBlacklistProvider blacklistSource) {
    if ((blacklistSource != null) && (_blacklist != null)) {
      throw new IllegalStateException("Can't set a blacklist source - specific blacklist already set");
    }
    _blacklistSource = blacklistSource;
  }

  public String getFunctionBlacklistName() {
    return _blacklistName;
  }

  public void setFunctionBlacklistName(final String blacklistName) {
    if (_blacklist != null) {
      throw new IllegalStateException("Can't set a blacklist name - determined from specific blacklist");
    }
    _blacklistName = blacklistName;
  }

  public FunctionBlacklistPolicy getPolicy() {
    return _policy;
  }

  public void setPolicy(final FunctionBlacklistPolicy policy) {
    if ((policy != null) && (_policySource != null)) {
      throw new IllegalStateException("Can't set a specific policy - policy source already set");
    }
    _policy = policy;
    if (policy != null) {
      _policyName = policy.getName();
      _policyUniqueId = policy.getUniqueId();
    }
  }

  public FunctionBlacklistPolicySource getPolicySource() {
    return _policySource;
  }

  public void setPolicySource(final FunctionBlacklistPolicySource policySource) {
    if ((policySource != null) && (_policy != null)) {
      throw new IllegalStateException("Can't set a policy source - specific policy already set");
    }
    _policySource = policySource;
  }

  public String getPolicyName() {
    return _policyName;
  }

  public void setPolicyName(final String policyName) {
    if (_policy != null) {
      throw new IllegalStateException("Can't set a policy name - determined from specific policy");
    }
    _policyName = policyName;
  }

  public UniqueId getPolicyUniqueId() {
    return _policyUniqueId;
  }

  public void setPolicyUniqueId(final UniqueId policyUniqueId) {
    if (_policy != null) {
      throw new IllegalStateException("Can't set a policy unique identifier - determined from specific policy");
    }
    _policyUniqueId = policyUniqueId;
  }

  protected FunctionBlacklistPolicy getOrCreatePolicy() {
    if (getPolicy() != null) {
      return getPolicy();
    }
    if (getPolicySource() != null) {
      if (getPolicyUniqueId() != null) {
        return getPolicySource().getPolicy(getPolicyUniqueId());
      }
      if (getPolicyName() != null) {
        return getPolicySource().getPolicy(getPolicyName());
      }
    }
    return null;
  }

  protected ManageableFunctionBlacklist getOrCreateBlacklist() {
    if (getFunctionBlacklist() != null) {
      return getFunctionBlacklist();
    }
    if (getFunctionBlacklistSource() != null) {
      if (getFunctionBlacklistName() != null) {
        return getFunctionBlacklistSource().getBlacklist(getFunctionBlacklistName());
      }
    }
    return null;
  }

  @Override
  protected FunctionBlacklistMaintainer createObject() {
    final FunctionBlacklistPolicy policy = getOrCreatePolicy();
    if (policy == null) {
      throw new IllegalStateException("Function blacklisting policy or policy source not set");
    }
    final ManageableFunctionBlacklist blacklist = getOrCreateBlacklist();
    if (blacklist == null) {
      throw new IllegalStateException("Function blacklist or blacklist source not set");
    }
    return new DefaultFunctionBlacklistMaintainer(getOrCreatePolicy(), getOrCreateBlacklist());
  }

}
