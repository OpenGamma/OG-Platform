<#escape x as x?html>
<@page title="Convention - ${conventionDoc.name}" jquery=true aceXmlEditor=true>

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
    <@rowin>
      <div id="ace-xml-editor"></div>
    </@rowin>
</@subsection>

<#noescape><@xmlEditorScript  xmlValue="${conventionXml}" readOnly=true></@xmlEditorScript></#noescape>
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
