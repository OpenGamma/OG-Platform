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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.credential.PasswordService;
import org.joda.beans.impl.flexi.FlexiBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.master.user.UserForm;
import com.opengamma.master.user.UserFormError;
import com.opengamma.master.user.UserFormException;
import com.opengamma.master.user.UserMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.OpenGammaClock;
import com.opengamma.web.AbstractSingletonWebResource;

/**
 * RESTful resource for the registration page.
 */
@Path("/register")
public class WebRegisterResource extends AbstractSingletonWebResource {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(WebRegisterResource.class);
  /**
   * The ftl file.
   */
  private static final String REGISTER_GREEN = "users/html/register.ftl";

  /**
   * The user master.
   */
  private final UserMaster _userMaster;
  /**
   * The password service.
   */
  private final PasswordService _pwService;

  /**
   * Creates the resource.
   * 
   * @param userMaster  the user master, not null
   * @param pwService  the password service, not null
   */
  public WebRegisterResource(UserMaster userMaster, PasswordService pwService) {
    _userMaster = ArgumentChecker.notNull(userMaster, "userMaster");
    _pwService = ArgumentChecker.notNull(pwService, "pwService");
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getGreen(
      @Context HttpServletRequest request,
      @Context ServletContext servletContext,
      @Context UriInfo uriInfo) {
    
    FlexiBean out = createRootData(uriInfo);
    out.put("username", "");
    out.put("password", "");
    out.put("timezone", OpenGammaClock.getZone().toString());
    out.put("email", "");
    out.put("displayname", "");
    return getFreemarker(servletContext).build(REGISTER_GREEN, out);
  }

  //-------------------------------------------------------------------------
  @POST
  @Produces(MediaType.TEXT_HTML)
  public Response loginGreen(
      @Context ServletContext servletContext,
      @Context UriInfo uriInfo,
      @FormParam("username") String userName,
      @FormParam("password") String password,
      @FormParam("email") String email,
      @FormParam("displayname") String displayName,
      @FormParam("locale") String locale,
      @FormParam("timezone") String zone,
      @FormParam("datestyle") String dateStyle,
      @FormParam("timestyle") String timeStyle) {
    try {
      UserForm form = new UserForm(userName, password, email, displayName, locale, zone, dateStyle, timeStyle);
      form.add(_userMaster, _pwService);
      SecurityUtils.getSubject().getSession().setAttribute(WebLoginResource.LOGIN_USERNAME, userName);
      return Response.seeOther(WebLoginResource.uri(uriInfo)).build();
      
    } catch (UserFormException ex) {
      ex.logUnexpected(s_logger);
      FlexiBean out = createRootData(uriInfo);
      out.put("username", userName);
      out.put("email", email);
      out.put("displayname", displayName);
      out.put("locale", locale);
      out.put("timezone", zone);
      out.put("datestyle", dateStyle);
      out.put("timestyle", timeStyle);
      out.put("err", ex.getErrors().size() > 0);
      for (UserFormError error : ex.getErrors()) {
        out.put("err_" + error.toLowerCamel(), true);
      }
      return Response.ok(getFreemarker(servletContext).build(REGISTER_GREEN, out)).build();
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this page.
   * 
   * @param uriInfo  the uriInfo, not null
   * @return the URI, not null
   */
  public static URI uri(UriInfo uriInfo) {
    return uriInfo.getBaseUriBuilder().path(WebRegisterResource.class).build();
  }

}
