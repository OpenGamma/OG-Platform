/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.user;

import java.net.URI;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.credential.PasswordService;
import org.joda.beans.impl.flexi.FlexiBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ObjectId;
import com.opengamma.master.user.ManageableUser;
import com.opengamma.master.user.UserEventHistoryRequest;
import com.opengamma.master.user.UserForm;
import com.opengamma.master.user.UserFormError;
import com.opengamma.master.user.UserFormException;
import com.opengamma.master.user.UserMaster;
import com.opengamma.master.user.UserSearchRequest;
import com.opengamma.master.user.UserSearchResult;
import com.opengamma.master.user.UserSearchSortOrder;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;

/**
 * RESTful resource for all users.
 * <p>
 * The users resource represents the whole of a user master.
 */
@Path("/users")
public class WebUsersResource extends AbstractWebUserResource {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(WebUsersResource.class);
  /**
   * The ftl file.
   */
  private static final String USERS_PAGE = HTML_DIR + "users.ftl";
  /**
   * The ftl file.
   */
  private static final String USER_ADD_PAGE = HTML_DIR + "user-add.ftl";

  /**
   * Creates the resource.
   * @param userMaster  the user master, not null
   * @param passwordService  the password service, not null
   */
  public WebUsersResource(final UserMaster userMaster, final PasswordService passwordService) {
    super(userMaster, passwordService);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML(
      @QueryParam("pgIdx") Integer pgIdx,
      @QueryParam("pgNum") Integer pgNum,
      @QueryParam("pgSze") Integer pgSze,
      @QueryParam("sort") String sort,
      @QueryParam("username") String username,
      @QueryParam("name") String name,
      @QueryParam("email") String email,
      @QueryParam("idscheme") String idScheme,
      @QueryParam("idvalue") String idValue,
      @QueryParam("userId") List<String> userIdStrs,
      @Context UriInfo uriInfo) {
    sort = StringUtils.trimToNull(sort);
    username = StringUtils.trimToNull(username);
    name = StringUtils.trimToNull(name);
    email = StringUtils.trimToNull(email);
    idScheme = StringUtils.trimToNull(idScheme);
    idValue = StringUtils.trimToNull(idValue);
    PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    UserSearchSortOrder so = buildSortOrder(sort, UserSearchSortOrder.NAME_ASC);
    FlexiBean out = createSearchResultData(pr, so, username, name, email, idScheme, idValue, userIdStrs, uriInfo);
    return getFreemarker().build(USERS_PAGE, out);
  }

  private FlexiBean createSearchResultData(
      PagingRequest pr, UserSearchSortOrder so,
      String username, String name, String email, String idScheme, String idValue,
      List<String> userIdStrs, UriInfo uriInfo) {
    FlexiBean out = createRootData();
    
    UserSearchRequest searchRequest = new UserSearchRequest();
    searchRequest.setPagingRequest(pr);
    searchRequest.setSortOrder(so);
    searchRequest.setUserName(username);
    searchRequest.setDisplayName(name);
    searchRequest.setEmailAddress(email);
    searchRequest.setAlternateIdScheme(StringUtils.trimToNull(idScheme));
    searchRequest.setAlternateIdValue(StringUtils.trimToNull(idValue));
    for (String userIdStr : userIdStrs) {
      searchRequest.addObjectId(ObjectId.parse(userIdStr));
    }
    out.put("searchRequest", searchRequest);
    
    if (data().getUriInfo().getQueryParameters().size() > 0) {
      UserSearchResult searchResult = data().getUserMaster().search(searchRequest);
      out.put("searchResult", searchResult);
      out.put("paging", new WebPaging(searchResult.getPaging(), uriInfo));
    }
    return out;
  }

  //-------------------------------------------------------------------------
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response postHTML(
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
      ManageableUser added = form.add(data().getUserMaster(), data().getPasswordService());
      URI uri = WebUserResource.uri(data(), added.getUserName());
      return Response.seeOther(uri).build();
      
    } catch (UserFormException ex) {
      ex.logUnexpected(s_logger);
      FlexiBean out = createRootData();
      out.put("username", userName);
      out.put("displayname", displayName);
      out.put("timezone", zone);
      out.put("email", email);
      for (UserFormError error : ex.getErrors()) {
        out.put("err_" + error.toLowerCamel(), true);
      }
      return Response.ok(getFreemarker().build(USER_ADD_PAGE, out)).build();
    }
  }

  //-------------------------------------------------------------------------
  @Path("name/{userName}")
  public WebUserResource findUser(@PathParam("userName") String userName) {
    data().setUriUserName(userName);
    try {
      ManageableUser user = data().getUserMaster().getByName(userName);
      data().setUser(user);
    } catch (DataNotFoundException ex) {
      UserEventHistoryRequest request = new UserEventHistoryRequest(userName);
      try {
        data().getUserMaster().eventHistory(request);
        ManageableUser user = new ManageableUser(userName);
        data().setUser(user);
      } catch (DataNotFoundException ex2) {
        throw ex;
      }
    }
    return new WebUserResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    UserSearchRequest searchRequest = new UserSearchRequest();
    out.put("searchRequest", searchRequest);
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for users.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(WebUserData data) {
    UriBuilder builder = data.getUriInfo().getBaseUriBuilder().path(WebUsersResource.class);
    return builder.build();
  }

}
