<#escape x as x?html>
<@page title="LegalEntity - ${legalEntityDoc.name}">

<@section css="info" if=deleted>
  <p>This legal entity has been deleted</p>
</@section>


<#-- SECTION LegalEntity output -->
<@section title="LegalEntity">
  <p>
    <@rowout label="Name">${legalEntityDoc.name}</@rowout>
    <@rowout label="Reference">${legalEntityDoc.uniqueId.value}, version ${legalEntityDoc.uniqueId.version}
</@rowout>
  </p>

<#-- SUBSECTION Main data -->
<@subsection title="Detail">
    <div style="border:1px solid black;padding:2px;"><textarea rows="30" cols="80" name="legalEntityXML" id="xmltextarea" readonly>${legalEntityXML}</textarea></div>
</@subsection>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.legalEntityVersions()}">All versions</a><br />
    <a href="${uris.legalEntity()}">Latest version - ${latestLegalEntity.uniqueId.version}</a><br />
    <a href="${uris.legalEntities()}">LegalEntity home</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
