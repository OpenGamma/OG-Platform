<#escape x as x?html>
<@page title="LegalEntity - ${legalEntityDoc.name}">

<@section css="info" if=deleted>
  <p>This legal entity has been deleted</p>
</@section>


<#-- SECTION LegalEntity output -->
<@section title="LegalEntity">
  <p>
    <@rowout label="Name">${legalEntityDoc.name}</@rowout>
    <@rowout label="Reference">${legalEntityDoc.uniqueId.value}, version ${legalEntityDoc.uniqueId.version}, <a href="${uris.legalEntityVersions()}">view history</a>
</@rowout>
  </p>

<#-- SUBSECTION Main data -->
<@subsection title="Detail" if=deleted>
    <@rowout label="Data"><textarea readonly style="width:650px;height:300px;">${legalEntityXML}</textarea></@rowout>
</@subsection>
</@section>


<#-- SECTION Update legal entity -->
<@section title="Update legal entity" if=!deleted>
  <@form method="PUT" action="${uris.legalEntity()}">
  <p>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="${legalEntityDoc.name}" /></@rowin>
    <@rowin label="LegalEntity (XML)">
      <div style="border:1px solid black;padding:2px;"><textarea rows="30" cols="80" name="legalEntityXML" id="xmltextarea">${legalEntityXML}</textarea></div>
    </@rowin>
    <@rowin><input type="submit" value="Update" /></@rowin>
<script type="text/javascript" src="/js/lib/codemirror/codemirror.js"></script>
<script type="text/javascript">
var editor = CodeMirror.fromTextArea("xmltextarea", {
  parserfile: ["parsexml.js"],
  path: "/js/lib/codemirror/",
  stylesheet: "/css/lib/codemirror/xmlcolors.css",
  width: "650px",
  height: "dynamic",
  minHeight: "300",
  reindentOnLoad: true,
  iframeClass: "xmleditor"
});
</script>
  </p>
  </@form>
</@section>


<#-- SECTION Delete legal entity -->
<@section title="Delete legal entity" if=!deleted>
  <@form method="DELETE" action="${uris.legalEntity()}">
  <p>
    <@rowin><input type="submit" value="Delete" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.legalEntityVersions()}">History of this legal entity</a><br />
    <a href="${uris.legalEntities()}">LegalEntity home</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
