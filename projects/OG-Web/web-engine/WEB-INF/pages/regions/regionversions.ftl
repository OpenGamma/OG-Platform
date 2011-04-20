<#escape x as x?html>
<@page title="Region history  - ${region.name}">

<@section css="info" if=deleted>
  <p>This region has been deleted</p>
</@section>


<#-- SECTION Region output -->
<@section title="Region">
  <p>
    <@rowout label="Name">${region.name}</@rowout>
    <@rowout label="Reference">${region.uniqueId.value}</@rowout>
  </p>

<#-- SUBSECTION Main data -->
<@subsection title="History">
  <@table items=versionsResult.documents empty="No versions" headers=["Name","Reference","Valid from", "Valid to","Actions"]; item>
      <td><a href="${uris.regionVersion(item.region)}">${item.region.name}</a></td>
      <td>${item.uniqueId.value} v${item.uniqueId.version}</td>
      <td>${item.versionFromInstant}</td>
      <td>${item.versionToInstant}</td>
      <td><a href="${uris.regionVersion(item.region)}">View</a></td>
  </@table>
</@subsection>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.region()}">Latest version - ${region.uniqueId.version}</a><br />
    <a href="${uris.regions()}">Region search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
