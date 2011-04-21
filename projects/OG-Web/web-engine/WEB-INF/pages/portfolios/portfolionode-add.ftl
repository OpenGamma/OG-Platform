<#escape x as x?html>
<@page title="Add child node - ${node.name}">


<#-- SECTION Node output -->
<@section title="Portfolio node">
  <p>
    <@rowout label="Portfolio"><a href="${uris.portfolio()}">${portfolio.name}</a></@rowout>
<#if parentNode?has_content>
    <@rowout label="Parent node"><a href="${uris.node(parentNode)}">${parentNode.name}</a></@rowout>
</#if>
    <@rowout label="Name">${node.name}</@rowout>
    <@rowout label="Reference">${node.uniqueId.value}, version ${node.uniqueId.version}</@rowout>
  </p>
</@section>


<#-- SECTION Add node -->
<@section title="Add child node">
  <@form method="POST" action="${uris.node()}">
  <p>
    <#if err_nameMissing??><div class="err">The name must be entered</div></#if>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="" /></@rowin>
    <@rowin><input type="submit" value="Add" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.node()}">Portfolio node - ${node.name}</a><br />
    <a href="${uris.portfolio()}">Portfolio - ${portfolio.name}</a><br />
    <a href="${uris.portfolios()}">Portfolio search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
