<#escape x as x?html>
<@page title="Exchanges">


<#-- SECTION Exchange search -->
<@section title="Exchange search" if=searchRequest??>
  <@form method="GET" action="${uris.exchanges()}">
  <p>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="${searchRequest.name}" /></@rowin>
    <@rowin><input type="submit" value="Search" /></@rowin>
  </p>
  </@form>

<#-- SUBSECTION Exchange results -->
<#if searchResult??>
<@subsection title="Results">
  <@table items=searchResult.documents paging=paging empty="No exchanges" headers=["Name","Reference","Version valid from","Actions"]; item>
      <td><a href="${uris.exchange(item.exchange)}">${item.exchange.name}</a></td>
      <td>${item.uniqueId.value}</td>
      <td>${item.versionFromInstant}</td>
      <td><a href="${uris.exchange(item.exchange)}">View</a></td>
  </@table>
</@subsection>
</#if>
</@section>


<#-- SECTION Add exchange -->
<@section title="Add exchange">
  <@form method="POST" action="${uris.exchanges()}">
  <p>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="" /></@rowin>
    <@rowin label="Scheme type"><input type="text" size="30" maxlength="80" name="idscheme" value="" /></@rowin>
    <@rowin label="Scheme id"><input type="text" size="30" maxlength="80" name="idvalue" value="" /></@rowin>
    <@rowin label="Region type"><input type="text" size="30" maxlength="80" name="regionscheme" value="" /></@rowin>
    <@rowin label="Region id"><input type="text" size="30" maxlength="80" name="regionvalue" value="" /></@rowin>
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
