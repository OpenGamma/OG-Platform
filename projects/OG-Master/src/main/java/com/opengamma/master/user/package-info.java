/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

/**
 * User master API providing the ability to query and update users and roles.
 * <p>
 * OpenGamma provides a user management system based on users and roles.
 * Roles are at their simplest just a group of users.
 * However, since roles can include other roles, complex hierarchies can be created.
 * <p>
 * Permissions can be attached directly to a user, but are normally associated with a role.
 * The complete set of permissions and roles for a user can be resolved when necessary.
 */
package com.opengamma.master.user;
