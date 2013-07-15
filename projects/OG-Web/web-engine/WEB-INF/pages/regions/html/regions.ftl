<#escape x as x?html>
<@page title="Regions">


<#-- SECTION Region search -->
<@section title="Region search" if=searchRequest??>
  <@form method="GET" action="${uris.regions()}">
  <p>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="${searchRequest.name}" /></@rowin>
    <@rowin><input type="submit" value="Search" /></@rowin>
  </p>
  </@form>

<#-- SUBSECTION Region results -->
<#if searchResult??>
<@subsection title="Results">
  <@table items=searchResult.documents paging=paging empty="No regions" headers=["Name","Reference","Version valid from","Actions"]; item>
      <td><a href="${uris.region(item.region)}">${item.region.name}</a></td>
      <td>${item.uniqueId.value}</td>
      <td>${item.versionFromInstant}</td>
      <td><a href="${uris.region(item.region)}">View</a></td>
  </@table>
</@subsection>
</#if>
</@section>


<#-- SECTION Add region >
<@section title="Add region">
  <@form method="POST" action="${uris.regions()}">
  <p>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="" /></@rowin>
    <@rowin label="Scheme type"><input type="text" size="30" maxlength="80" name="idscheme" value="" /></@rowin>
    <@rowin label="Scheme id"><input type="text" size="30" maxlength="80" name="idvalue" value="" /></@rowin>
    <@rowin label="Region type"><input type="text" size="30" maxlength="80" name="regionscheme" value="" /></@rowin>
    <@rowin label="Region id"><input type="text" size="30" maxlength="80" name="regionvalue" value="" /></@rowin>
    <@rowin><input type="submit" value="Add" /></@rowin>
  </p>
  </@form>
</@section-->


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
