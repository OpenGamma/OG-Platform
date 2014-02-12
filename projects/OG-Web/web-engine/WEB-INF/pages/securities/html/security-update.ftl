<#escape x as x?html>
<@page title="Update - ${security.name}">


<#-- SECTION Update security -->
<@section title="Update security">
  <@form method="PUT" action="${uris.security()}" id="updateSecurityForm">
  <p>
    <#if err_securityXml??>
      <div class="err">${err_securityXmlMsg}</div>
      <@rowin><input type="checkbox" name="useXml" value="true">Use XML</input></@rowin>
      <@rowin><div id="security-xml-editor">${securityXml}</div></@rowin>
      <@rowin><input type="hidden" name="securityXml" id="security-xml"/></@rowin>
      <script type="text/javascript">
        var editor = ace.edit("security-xml-editor")
        editor.getSession().setMode('ace/mode/xml')
        $("#security-xml-editor").show()

        $("#updateSecurityForm").submit( function(eventObj) {
          $("#security-xml").val(editor.getSession().getValue())
          return true
        })
      </script>
    </#if>
    
    <@rowin><input type="submit" value="Update" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.security()}">Security - ${security.name}</a><br />
    <a href="${uris.securities()}">Security search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
