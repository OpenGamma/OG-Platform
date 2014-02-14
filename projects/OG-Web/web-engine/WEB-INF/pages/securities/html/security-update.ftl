<#escape x as x?html>
<@page title="Update - ${security.name}" jquery=true aceXmlEditor=true>


<#-- SECTION Update security -->
<@section title="Update security">
  <@form method="PUT" action="${uris.security()}" id="updateSecurityForm">
  <p>
    <#if err_securityXml??>
      <div class="err">${err_securityXmlMsg}</div>
    </#if>
      <@rowin><div id="ace-xml-editor"><#if securityXml?has_content>${securityXml}</#if></div></@rowin>
      <@rowin><input type="hidden" name="securityXml" id="security-xml"/></@rowin>
      <script type="text/javascript">
        var editor = ace.edit("ace-xml-editor")
        editor.getSession().setMode('ace/mode/xml')
        $("#ace-xml-editor").show()

        $("#updateSecurityForm").submit( function(eventObj) {
          $("#security-xml").val(editor.getSession().getValue())
          return true
        })
      </script>
    <input type="hidden" name="type" value="xml"/>
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
