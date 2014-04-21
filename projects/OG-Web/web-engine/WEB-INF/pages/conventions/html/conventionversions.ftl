<#escape x as x?html>
<@page title="Convention history  - ${conventionDoc.name}">

<@section css="info" if=deleted>
  <p>This convention has been deleted</p>
</@section>


<#-- SECTION Convention output -->
<@section title="Convention">
  <p>
    <@rowout label="Name">${conventionDoc.name}</@rowout>
    <@rowout label="Type">${conventionDescription}</@rowout>
    <@rowout label="Reference">${conventionDoc.uniqueId.value}</@rowout>
  </p>


<#-- SUBSECTION Main data -->
<@subsection title="History">
  <@table items=versionsResult.documents empty="No versions" headers=["Name","Reference","Valid from", "Valid to","Actions"]; item>
      <td><a href="${uris.conventionVersion(item.uniqueId)}">${item.name}</a></td>
      <td>${item.uniqueId.value} v${item.uniqueId.version}</td>
      <td>${item.versionFromInstant}</td>
      <td>${item.versionToInstant}</td>
      <td><a href="${uris.conventionVersion(item.uniqueId)}">View</a></td>
  </@table>
</@subsection>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.convention()}">Latest version - ${conventionDoc.uniqueId.version}</a><br />
    <a href="${uris.conventions()}">Convention home</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
