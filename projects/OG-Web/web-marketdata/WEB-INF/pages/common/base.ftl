<#-- Macro to produce a standard page with html/head/body -->
<#macro page title="OpenGamma">
<html>
  <head>
    <title>${title} - OpenGamma</title>
    <link type="text/css" rel="stylesheet" href="/css/og-base.css"/>
  </head>
  <body>
    <div id="header">
      <p id="logo">
        <a href="/"><img src="/images/opengamma.png" width="289" height="51" alt="OpenGamma - Financial Analytics and Risk Management" /></a>
      </p>
    </div>
    <div id="body">
<#nested>
    </div>
    <!-- Freemarker template: ${freemarkerTemplateName} -->
    <!-- Freemarker locale: ${freemarkerLocale} -->
    <!-- Freemarker version: ${freemarkerVersion} -->
    <!-- Processing time: ${now} -->
  </body>
</html>
</#macro>

<#-- Macro to produce a standard page section with title -->
<#macro section title="" css="" if=true>
<#if if>
<div class="section ${css}">
<#if title?has_content>
  <h2>${title}</h2>
</#if>
<#nested>
</div>
</#if>
</#macro>

<#-- Macro to produce a standard page subsection with title -->
<#macro subsection title="" if=true>
<#if if>
<div class="subsection">
<#if title?has_content>
  <h3>${title}</h3>
</#if>
<#nested>
</div>
</#if>
</#macro>

<#-- Macro to produce a standard table -->
<#macro table items empty="No data" headers=[] paging=[] loop=true if=true>
<#if if>
<#if items?size == 0>
  <p>${empty}</p>
<#else>
  <table>
<#if headers?size != 0>
    <tr><#rt>
<#list headers as header>
    <th>${header}</th><#t>
</#list>
    </tr><#lt>
</#if>
<#if loop>
<#list items as item>
    <tr>
<#nested item>
    </tr>
</#list>
<#else>
<#nested items>
</#if>
  </table>
<#if paging?has_content>
  <div class="paging">
    <div class="box">
<#if paging.previousPageExists>
      <div class="item page active"><a href="${paging.previousPage.uri}">prev</a></div>
<#else>
      <div class="item prev inactive">prev</div>
</#if>
<#list paging.pagesStandard as page>
<#if page.currentPage>
      <div class="item page current">${page.pageNumber}</div>
<#else>
      <div class="item page active"><a href="${page.uri}">${page.pageNumber}</a></div>
</#if>
</#list>
<#if paging.nextPageExists>
      <div class="item next active"><a href="${paging.nextPage.uri}">next</a></div>
<#else>
      <div class="item next inactive">next</div>
</#if>
    </div>
  </div>
</#if>
</#if>
</#if>
</#macro>

<#-- Macro to produce a standard form handling RESTful tunneling -->
<#macro form action method="GET">
  <form method="<#if method == "GET">GET<#else>POST</#if>" action="${action}"><#rt>
<#if method == "PUT" || method == "DELETE"><input type="hidden" name="method" value="${method}" /></#if>
<#nested>
  </form>
</#macro>

<#-- Macro to produce a standard input row -->
<#macro rowin id="" label="" if=true>
<#if if>
<div class="row in" <#if id?has_content>id="${id}"</#if>><#if label?has_content><div class="lbl">${label}</div></#if><div class="dat"><#nested></div></div><#rt>
</#if>
</#macro>

<#-- Macro to produce a standard output row -->
<#macro rowout label="" if=true>
<#if if>
<div class="row out"><div class="lbl">${label}</div><div class="dat"><#nested></div></div><#rt>
</#if>
</#macro>

<#-- Macro to produce a space between data -->
<#macro space>
<hr style="border: none; border-top: 1px solid #ccc;" />
</#macro>
