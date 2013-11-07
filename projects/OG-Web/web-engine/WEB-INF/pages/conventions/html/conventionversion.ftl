<#escape x as x?html>
<@page title="Convention - ${conventionDoc.name}">

<@section css="info" if=deleted>
  <p>This convention has been deleted</p>
</@section>


<#-- SECTION Convention output -->
<@section title="Convention">
  <p>
    <@rowout label="Name">${conventionDoc.name}</@rowout>
    <@rowout label="Type">${conventionDescription}</@rowout>
    <@rowout label="Reference">${conventionDoc.uniqueId.value}, version ${conventionDoc.uniqueId.version}
</@rowout>
  </p>

<#-- SUBSECTION Main data -->
<@subsection title="Detail">
    <div style="border:1px solid black;padding:2px;"><textarea rows="30" cols="80" name="conventionxml" id="xmltextarea" readonly>${conventionXml}</textarea></div>
</@subsection>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.conventionVersions()}">All versions</a><br />
    <a href="${uris.convention()}">Latest version - ${latestConventionDoc.uniqueId.version}</a><br />
    <a href="${uris.conventions()}">Convention home</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
