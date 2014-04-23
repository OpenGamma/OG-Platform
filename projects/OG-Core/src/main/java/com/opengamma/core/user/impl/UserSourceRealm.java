/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.user.impl;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.MapCache;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.util.SoftHashMap;
import org.threeten.bp.ZoneId;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeEvent;
import com.opengamma.core.change.ChangeListener;
import com.opengamma.core.change.ChangeType;
import com.opengamma.core.user.DateStyle;
import com.opengamma.core.user.TimeStyle;
import com.opengamma.core.user.UserAccount;
import com.opengamma.core.user.UserProfile;
import com.opengamma.core.user.UserSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.auth.AuthUtils;

/**
 * A security {@code Realm} that accesses the user source.
 * <p>
 * The {@code UserSource} insulates the main application from Apache Shiro.
 */
public class UserSourceRealm extends AuthorizingRealm {

  /**
   * The user profiles.
   */
  private final MapCache<String, UserProfile> _profiles = new MapCache<>("profiles", new SoftHashMap<String, UserProfile>());
  /**
   * The user master.
   */
  private final UserSource _userSource;

  /**
   * Creates an instance.
   * 
   * @param userSource  the user source, not null
   */
  public UserSourceRealm(UserSource userSource) {
    setName("UserSourceRealm");
    _userSource = ArgumentChecker.notNull(userSource, "userSource");
    // clear everything if any user changed
    _userSource.changeManager().addChangeListener(new ChangeListener() {
      @Override
      public void entityChanged(ChangeEvent event) {
        if (event.getType() == ChangeType.CHANGED || event.getType() == ChangeType.REMOVED) {
          Cache<Object, AuthenticationInfo> authnCache = getAuthenticationCache();
          if (authnCache != null) {
            authnCache.clear();
          }
          Cache<Object, AuthorizationInfo> authzCache = getAuthorizationCache();
          if (authzCache != null) {
            authzCache.clear();
          }
          _profiles.clear();
        }
      }
    });
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the user profile.
   * 
   * @param userName  the user name, not null
   * @return the user profile, null if not found
   */
  UserProfile getUserProfile(String userName) {
    UserProfile profile = _profiles.get(userName);
    if (profile == null) {
      try {
        profile = _userSource.getAccount(userName).getProfile();
        _profiles.put(userName, profile);
      } catch (DataNotFoundException ex) {
        // ignored
      }
    }
    return profile;
  }

  @Override
  public boolean supports(AuthenticationToken token) {
    return token instanceof UsernamePasswordToken;
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    try {
      UsernamePasswordToken upToken = (UsernamePasswordToken) token;
      String userName = upToken.getUsername();
      UserAccount account = loadUserByName(userName);
      account.getStatus().check();
      _profiles.put(userName, account.getProfile());
      AuthUtils.getSubject().getSession().setAttribute(UserProfile.class.getName(), new ProxyProfile(userName));
      SimplePrincipalCollection principals = new SimplePrincipalCollection();
      principals.add(account.getUserName(), getName());
      return new SimpleAuthenticationInfo(principals, account.getPasswordHash());
      
    } catch (AuthenticationException ex) {
      throw ex;
    } catch (RuntimeException ex) {
      throw new AuthenticationException("Unable to load authentication data: " + token, ex);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    if (principals == null) {
      throw new AuthorizationException("PrincipalCollection must not be null");
    }
    try {
      // try UniqueId
      Collection<String> userNames = principals.byType(String.class);
      if (userNames.size() == 0) {
        return null;
      }
      if (userNames.size() > 1) {
        throw new AuthorizationException("PrincipalCollection must not contain two UserAccount instances");
      }
      String userName = userNames.iterator().next();
      UserAccount account = loadUserByName(userName);
      SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
      info.addRoles(account.getRoles());
      for (String permStr : account.getPermissions()) {
        info.addObjectPermission(getPermissionResolver().resolvePermission(permStr));
      }
      return info;
      
    } catch (AuthorizationException ex) {
      throw ex;
    } catch (RuntimeException ex) {
      throw new AuthorizationException("Unable to load authorization data: " + principals, ex);
    }
  }

  private UserAccount loadUserByName(String userName) {
    try {
      return _userSource.getAccount(userName);
    } catch (DataNotFoundException ex) {
      throw new UnknownAccountException("User not found: " + userName, ex);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * The proxy profile.
   */
  class ProxyProfile implements UserProfile {
    private final String _userName;

    ProxyProfile(String userName) {
      _userName = userName;
    }

    @Override
    public String getDisplayName() {
      return getUserProfile(_userName).getDisplayName();
    }

    @Override
    public Locale getLocale() {
      return getUserProfile(_userName).getLocale();
    }

    @Override
    public ZoneId getZone() {
      return getUserProfile(_userName).getZone();
    }

    @Override
    public DateStyle getDateStyle() {
      return getUserProfile(_userName).getDateStyle();
    }

    @Override
    public TimeStyle getTimeStyle() {
      return getUserProfile(_userName).getTimeStyle();
    }

    @Override
    public Map<String, String> getExtensions() {
      return getUserProfile(_userName).getExtensions();
    }

    @Override
    public String toString() {
      return String.format("ProxyProfile[%s]", _userName);
    }
  }

}
