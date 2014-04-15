/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.user;

import java.net.URI;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.util.auth.AuthUtils;
import com.opengamma.web.AbstractSingletonWebResource;
import com.opengamma.web.WebHomeResource;

/**
 * RESTful resource for the login page.
 */
@Path("/login")
public class WebLoginResource extends AbstractSingletonWebResource {
  // take control of logout from Shiro to enable ftl files

  // one resource class handles two ftl files
  private static final String LOGIN_GREEN = "users/html/login.ftl";
  private static final String LOGIN_STYLISH = "users/html/login-og.ftl";
  // Key for the login user name
  static final Object LOGIN_USERNAME = WebLoginResource.class.getName() + ".LoginUserName";

  /**
   * Creates the resource.
   */
  public WebLoginResource() {
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getGreen(
      @Context HttpServletRequest request,
      @Context ServletContext servletContext,
      @Context UriInfo uriInfo) {
    
    SavedRequest savedRequest = WebUtils.getSavedRequest(request);
    if (savedRequest != null && savedRequest.getMethod().equalsIgnoreCase(AccessControlFilter.GET_METHOD)) {
      if (savedRequest.getRequestUrl() != null && savedRequest.getRequestUrl().contains("/bundles/fm/prototype/")) {
        return getStylish(servletContext, uriInfo);
      }
    }
    return get(servletContext, uriInfo, LOGIN_GREEN);
  }

  @GET
  @Path("og")
  @Produces(MediaType.TEXT_HTML)
  public String getStylish(
      @Context ServletContext servletContext,
      @Context UriInfo uriInfo) {
    return get(servletContext, uriInfo, LOGIN_STYLISH);
  }

  private String get(ServletContext servletContext, UriInfo uriInfo, String ftlFile) {
    FlexiBean out = createRootData(uriInfo);
    Subject subject = AuthUtils.getSubject();
    Session session = subject.getSession(false);
    if (session != null && session.getAttribute(LOGIN_USERNAME) != null) {
      out.put("username", session.getAttribute(LOGIN_USERNAME));
    } else {
      out.put("username", "");
    }
    return getFreemarker(servletContext).build(ftlFile, out);
  }

  //-------------------------------------------------------------------------
  @POST
  @Produces(MediaType.TEXT_HTML)
  public Response loginGreen(
      @Context HttpServletRequest request,
      @Context ServletContext servletContext,
      @Context UriInfo uriInfo,
      @FormParam("username") String username,
      @FormParam("password") String password) {
    return login(request, servletContext, uriInfo, username, password, LOGIN_GREEN);
  }

  @POST
  @Path("og")
  @Produces(MediaType.TEXT_HTML)
  public Response loginStylish(
      @Context HttpServletRequest request,
      @Context ServletContext servletContext,
      @Context UriInfo uriInfo,
      @FormParam("username") String username,
      @FormParam("password") String password) {
    return login(request, servletContext, uriInfo, username, password, LOGIN_STYLISH);
  }

  private Response login(
      HttpServletRequest request,
      ServletContext servletContext,
      UriInfo uriInfo,
      String username,
      String password,
      String ftlFile) {
    username = StringUtils.trimToNull(username);
    password = StringUtils.trimToNull(password);
    if (username == null) {
      return displayError(servletContext, uriInfo, username, ftlFile, "UserNameMissing");
    }
    if (password == null) {
      return displayError(servletContext, uriInfo, username, ftlFile, "PasswordMissing");
    }
    
    UsernamePasswordToken token = new UsernamePasswordToken(username, password, false, request.getRemoteHost());
    try {
      Subject subject = AuthUtils.getSubject();
      subject.login(token);
      token.clear();
      
      URI successUrl = null;
      SavedRequest savedRequest = WebUtils.getAndClearSavedRequest(request);
      if (savedRequest != null && savedRequest.getMethod().equalsIgnoreCase(AccessControlFilter.GET_METHOD)) {
        successUrl = uriInfo.getBaseUri().resolve(savedRequest.getRequestUrl());
      } else {
        if (ftlFile.equals(LOGIN_GREEN)) {
          successUrl = WebHomeResource.uri(uriInfo);
        } else {
          successUrl = uriInfo.getBaseUri().resolve("/");
        }
      }
      return Response.seeOther(successUrl).build();
      
    } catch (AuthenticationException ex) {
      String errorCode = StringUtils.substringBeforeLast(ex.getClass().getSimpleName(), "Exception");
      return displayError(servletContext, uriInfo, username, ftlFile, errorCode);
    }
  }

  private Response displayError(ServletContext servletContext, UriInfo uriInfo, String username, String ftlFile, String errorCode) {
    FlexiBean out = createRootData(uriInfo);
    out.put("username", username);
    out.put("err_invalidLogin", errorCode);
    return Response.ok(getFreemarker(servletContext).build(ftlFile, out)).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this page.
   * 
   * @param uriInfo  the uriInfo, not null
   * @return the URI, not null
   */
  public static URI uri(UriInfo uriInfo) {
    return uriInfo.getBaseUriBuilder().path(WebLoginResource.class).build();
  }

}
