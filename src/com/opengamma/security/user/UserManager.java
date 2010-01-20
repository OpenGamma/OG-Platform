/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
 * @author pietari
 */
public interface UserManager {
  
  public User getUser(final String username);
  public void addUser(User user);
  public void deleteUser(User user);
  public void updateUser(User user);

  public UserGroup getUserGroup(final String name);
  public void addUserGroup(UserGroup group);
  public void deleteUserGroup(UserGroup group);
  public void updateUserGroup(UserGroup group);

  public Authority getAuthority(final String authority);
  public void addAuthority(Authority authority);
  public void deleteAuthority(Authority authority);
  public void updateAuthority(Authority authority);

}
