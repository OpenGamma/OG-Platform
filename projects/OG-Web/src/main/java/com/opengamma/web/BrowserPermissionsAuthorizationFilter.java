/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;

/**
 * Apache Shiro filter that checks permissions for browser/HTML access.
 */
public final class BrowserPermissionsAuthorizationFilter extends AuthorizationFilter {

  public boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws IOException {
    String accept = ((HttpServletRequest) request).getHeader("Accept");
    if (accept.contains("text/") || accept.contains("application/xhtml")) {
      Subject subject = getSubject(request, response);
      String[] perms = (String[]) mappedValue;
      if (perms != null && perms.length > 0) {
        if (perms.length == 1) {
          if (!subject.isPermitted(perms[0])) {
            return false;
          }
        } else {
          if (!subject.isPermittedAll(perms)) {
            return false;
          }
        }
      }
    }
    return true;
  }

}
