<#escape x as x?html>
<@page title="Add position - ${node.name}">


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


<#-- SECTION Add position -->
<@section title="Add position">
  <@form method="POST" action="${uris.nodePositions(node)}">
  <p>
    <#if err_positionUrlMissing??><div class="err">The URL must be entered</div></#if>
    <#if err_positionUrlInvalid??><div class="err">The URL was invalid</div></#if>
    <@rowin label="Position URL"><input type="text" size="60" maxlength="255" name="positionurl" value="" /></@rowin>
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
