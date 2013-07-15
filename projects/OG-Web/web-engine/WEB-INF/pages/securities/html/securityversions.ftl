<#escape x as x?html>
<@page title="Security history  - ${security.name}">

<@section css="info" if=deleted>
  <p>This security has been deleted</p>
</@section>


<#-- SECTION Security output -->
<@section title="Security">
  <p>
    <@rowout label="Name">${security.name}</@rowout>
    <@rowout label="Reference">${security.uniqueId.value}</@rowout>
  </p>

<#-- SUBSECTION Main data -->
<@subsection title="History">
  <@table items=versionsResult.documents empty="No versions" headers=["Name","Reference","Valid from", "Valid to","Actions"]; item>
      <td><a href="${uris.securityVersion(item.security)}">${item.security.name}</a></td>
      <td>${item.uniqueId.value} v${item.uniqueId.version}</td>
      <td>${item.versionFromInstant}</td>
      <td>${item.versionToInstant}</td>
      <td><a href="${uris.securityVersion(item.security)}">View</a></td>
  </@table>
</@subsection>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.security()}">Latest version - ${security.uniqueId.version}</a><br />
    <a href="${uris.securities()}">Security search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
