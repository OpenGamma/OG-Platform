<section class="OG-masthead">
  <ul  class="og-nav">
    <li><a href="analytics.ftl" class="og-home"><span class="OG-logo-light-small"></span><span>OpenGamma</span></a></li>
    <!--<li><a href="analytics_legacy.ftl" class="og-analytics">Analytics Legacy</a></li>-->
    <li><a href="analytics.ftl" class="og-analytics-beta">Analytics</a></li>
    <li><a href="blotter.ftl" class="og-blotter">Booking</a></li>
    <li>
      <a href="admin.ftl#/configs/" class="og-menu-button og-configs">Configure</a>
      <div class="og-config og-menu og-active OG-shadow">
      </div>
    </li>
    <li>
        <a href="admin.ftl#/configs/" class="og-menu-button og-datas">Data</a>
        <div class="og-data og-menu og-active OG-shadow">
            <table>
                <tr>
                    <td>
                        <header>Market Data</header>
                        <ul>
                            <li><a href="admin.ftl#/timeseries/">Timeseries</a></li>
                        </ul>
                        <header>Legal Entities</header>
                        <ul>
                            <li><a href="admin.ftl#/legalentities/">Legal Entities</a></li>
                            <li><a href="admin.ftl#/exchanges/">Exchanges</a></li>
                            <li><a href="admin.ftl#/portfolios/">Portfolios</a></li>
                        </ul>
                    </td>
                    <td>
                        <header>Static Data</header>
                        <ul>
                            <li><a href="admin.ftl#/holidays">Holidays</a></li>
                            <li><a href="admin.ftl#/regions/">Regions</a></li>
                        </ul>
                        <header>Other</header>
                        <ul>
                            <li><a href="admin.ftl#/positions/">Positions</a></li>
                            <li><a href="admin.ftl#/securities/">Securities</a></li>
                        </ul>
                    </td>
                </tr>
            </table>
        </div>
    </li>
    <li>
      <a href="https://www.surveymonkey.com/s/opengamma-community-survey"
          target="_blank" class="og-analytics-feedback">
        <span class="OG-icon og-icon-speech"></span>Feedback
      </a>
    </li>
  </ul>
  <#if userSecurity.enabled && userSecurity.userName?has_content>
  <ul class="og-username-logout">
    <li>
      <span class="og-username">${userSecurity.userName}</span> | <span class="og-logout">Logout</span>
    </li>
  </ul>
  </#if>
</section>
<div class="OG-analytics-form"></div>