<#escape x as x?html>
<@page title="Users">


<#-- SECTION User search -->
<@section title="User search" if=searchRequest??>
  <@form method="GET" action="${uris.users()}">
  <p>
    <@rowin label="User name"><input type="text" size="30" name="username" value="${searchRequest.userName}" /></@rowin>
    <@rowin label="Email address"><input type="text" size="30" name="name" value="${searchRequest.emailAddress}" /></@rowin>
    <@rowin label="Display name"><input type="text" size="30" name="name" value="${searchRequest.displayName}" /></@rowin>
    <@rowin><input type="submit" value="Search" /></@rowin>
  </p>
  </@form>

<#-- SUBSECTION User results -->
<#if searchResult??>
<@subsection title="Results">
  <@table items=searchResult.users paging=paging empty="No user found" headers=["User Name","Name","Email","Reference","Actions"]; item>
      <td><a href="${uris.user(item)}">${item.userName}</a></td>
      <td>${item.profile.displayName}</td>
      <td>${item.emailAddress}</td>
      <td>${item.uniqueId.value}</td>
      <td><a href="${uris.user(item)}">View</a></td>
  </@table>
</@subsection>
</#if>
</@section>

<#-- SECTION Add user -->
<@section title="Add user">
  <@form method="POST" action="${uris.users()}">
  <p>
    <@rowin label="User name"><input type="text" size="40" maxlength="20" name="username" id="username" /></@rowin>
    <@rowin label="Password"><input type="password" size="40" maxlength="100" name="password" id="password" /></@rowin>
    <@rowin label="Email address"><input type="email" size="40" maxlength="200" name="email" id="email" /></@rowin>
    <@rowin label="Display name"><input type="text" size="40" maxlength="200" name="displayname" id="displayname" /></@rowin>
    <@rowin><input type="submit" value="Add" /></@rowin>
  </p>
  </@form>
</@section>

<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
