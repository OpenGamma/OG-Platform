/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.user;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.user.UserAccountStatus;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.user.ManageableUser;
import com.opengamma.master.user.UserEventHistoryRequest;
import com.opengamma.master.user.UserEventHistoryResult;
import com.opengamma.master.user.UserForm;
import com.opengamma.master.user.UserFormError;
import com.opengamma.master.user.UserFormException;

/**
 * RESTful resource for a user.
 */
@Path("/users/name/{userName}")
public class WebUserResource extends AbstractWebUserResource {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(WebUserResource.class);
  /**
   * The ftl file.
   */
  private static final String USER_PAGE = HTML_DIR + "user.ftl";
  /**
   * The ftl file.
   */
  private static final String USER_UPDATE_PAGE = HTML_DIR + "user-update.ftl";
  /**
   * The ftl file.
   */
  private static final String USER_PW_RESET_PAGE = HTML_DIR + "user-pwreset.ftl";

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebUserResource(final AbstractWebUserResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML() {
    final FlexiBean out = createRootData();
    return getFreemarker().build(USER_PAGE, out);
  }

  //-------------------------------------------------------------------------
  @PUT
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response putHTML(
      @FormParam("username") String userName,
      @FormParam("email") String email,
      @FormParam("displayname") String displayName,
      @FormParam("idBloombergEmrs") String idBloomberg,
      @FormParam("idWindows") String idWindows) {
    idBloomberg = StringUtils.trimToNull(idBloomberg);
    idWindows = StringUtils.trimToNull(idWindows);
    Set<ExternalId> externalIds = new HashSet<>();
    if (idBloomberg != null) {
      externalIds.add(ExternalSchemes.bloombergEmrsUserId(idBloomberg));
    }
    if (idWindows != null) {
      externalIds.add(ExternalSchemes.windowsUserId(idWindows));
    }
    ExternalIdBundle bundle = ExternalIdBundle.of(externalIds);
    try {
      UserForm form = new UserForm(data().getUser());
      form.getBaseUser().setAlternateIds(bundle);
      form.setUserName(userName);
      form.setEmailAddress(email);
      form.setDisplayName(displayName);
      form.update(data().getUserMaster(), data().getPasswordService());
      return Response.seeOther(uri(data())).build();
      
    } catch (UserFormException ex) {
      ex.logUnexpected(s_logger);
      FlexiBean out = createRootData();
      out.put("username", userName);
      out.put("email", email);
      out.put("displayname", displayName);
      out.put("idBloombergEmrs", idBloomberg);
      out.put("idWindows", idWindows);
      for (UserFormError error : ex.getErrors()) {
        out.put("err_" + error.toLowerCamel(), true);
      }
      return Response.ok(getFreemarker().build(USER_UPDATE_PAGE, out)).build();
    }
  }

  @POST
  @Path("resetpassword")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response postResetPwHTML(
      @FormParam("password") String password) {
    password = StringUtils.trimToNull(password);
    try {
      UserForm form = new UserForm(data().getUser(), password);
      form.update(data().getUserMaster(), data().getPasswordService());
      return Response.seeOther(uri(data())).build();
      
    } catch (UserFormException ex) {
      ex.logUnexpected(s_logger);
      FlexiBean out = createRootData();
      for (UserFormError error : ex.getErrors()) {
        out.put("err_" + error.toLowerCamel(), true);
      }
      return Response.ok(getFreemarker().build(USER_PW_RESET_PAGE, out)).build();
    }
  }

  @POST
  @Path("status")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response postStatusHTML(
      @FormParam("status") String status) {
    status = StringUtils.trimToNull(status);
    ManageableUser user = data().getUser();
    user.setStatus("ENABLED".equals(status) ? UserAccountStatus.ENABLED : UserAccountStatus.DISABLED);
    data().getUserMaster().update(user);
    return Response.seeOther(uri(data())).build();
  }

  //-------------------------------------------------------------------------
  @DELETE
  @Produces(MediaType.TEXT_HTML)
  public Response deleteHTML() {
    ManageableUser user = data().getUser();
    data().getUserMaster().removeById(user.getUniqueId().getObjectId());
    URI uri = WebUserResource.uri(data());
    return Response.seeOther(uri).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    ManageableUser user = data().getUser();
    out.put("user", user);
    out.put("deleted", user.getObjectId() == null);
    ExternalId emrsId = user.getAlternateIds().getExternalId(ExternalSchemes.BLOOMBERG_EMRSID);
    out.put("idBloombergEmrs", emrsId != null ? emrsId.getValue() : null);
    ExternalId windowsId = user.getAlternateIds().getExternalId(ExternalSchemes.WINDOWS_USER_ID);
    out.put("idWindows", windowsId != null ? windowsId.getValue() : null);
    UserEventHistoryRequest historyRequest = new UserEventHistoryRequest(data().getUser().getUserName());
    UserEventHistoryResult history = data().getUserMaster().eventHistory(historyRequest);
    out.put("eventHistory", history);
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uriResetPassword(final WebUserData data) {
    String userName = data.getBestUserUriName(null);
    return data.getUriInfo().getBaseUriBuilder().path(WebUserResource.class).path("resetpassword").build(userName);
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uriStatus(final WebUserData data) {
    String userName = data.getBestUserUriName(null);
    return data.getUriInfo().getBaseUriBuilder().path(WebUserResource.class).path("status").build(userName);
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebUserData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @param overrideUserName  the override user name, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebUserData data, final String overrideUserName) {
    String userName = data.getBestUserUriName(overrideUserName);
    return data.getUriInfo().getBaseUriBuilder().path(WebUserResource.class).build(userName);
  }

}
