/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.security.user;

/**
 * None of the update operations on this interface 'cascade' the update.
 * <p>
 * Example: You change the user's password and add the user to an existing group. In addition, you
 * change the name of the group. You then call {@link #updateUser}.
 * <p>
 * In this case, the user's password and their group membership would be changed. However, to update the
 * name of the group, you would need to call {@link #updateUserGroup} separately.
 *      
 */
public interface UserManager {

  User getUser(final String username);
  void addUser(User user);
  void deleteUser(User user);
  void updateUser(User user);

  UserGroup getUserGroup(final String name);
  void addUserGroup(UserGroup group);
  void deleteUserGroup(UserGroup group);
  void updateUserGroup(UserGroup group);

  Authority getAuthority(final String authority);
  void addAuthority(Authority authority);
  void deleteAuthority(Authority authority);
  void updateAuthority(Authority authority);

}
