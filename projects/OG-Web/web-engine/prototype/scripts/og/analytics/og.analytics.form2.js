/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.form2',
    dependencies: [],
    obj: function () {
        var module = this, constructor, menus = [], portfolio_dropdown, viewdefinitions_dropdown,
            aggregation_menu, datasources_menu, temporal_menu, query, url_config,
            events = {
                focus: 'focus',
                focused:'focused',
                open: 'open',
                opened: 'opened',
                close: 'close',
                closed: 'closed',
                closeall: 'closeall',
                selected: 'selected',
                cancelled: 'cancelled',
                replay: 'replay',
                reset:'reset'
            },
            tashes = {
                form_container:  'og.analytics.form_tash'
            },
            selectors = {
                form_container: 'OG-analytics-form'
            },
            dom = { form_container : $('.' + selectors.form_container) },
            form = new og.common.util.ui.Form({
                module: tashes.form_container,
                selector: '.' + selectors.form_container
            });

        var init = function (callback) {
            form.on('click', '.og-load', function () {
                var compilation = form.compile();
                query = {
                    aggregators: compilation.aggregators,
                    providers: compilation.providers,
                    viewdefinition: 'DbCfg~2197901'
                };
                callback(query);
            });
            form.children.push(
                (portfolios_dropdown = new og.analytics.form.Portfolios({form:form})).block,
                (viewdefinitions_dropdown = new og.analytics.form.ViewDefinitions({form:form})).block,
                (datasources_menu = new og.analytics.DatasourcesMenu({form:form})).block,
                (temporal_menu = new og.analytics.TemporalMenu({form:form})).block,
                (aggregation_menu = new og.analytics.AggregatorsMenu({form:form})).block
            );
            form.dom();
            menus.push(portfolios_dropdown, viewdefinitions_dropdown, datasources_menu, temporal_menu, aggregation_menu);
        };

        constructor = function (callback) {
            return og.views.common.layout.main.allowOverflow('north'), init(console.log);
        };

        constructor.prototype.fire = og.common.events.fire;
        constructor.prototype.on = og.common.events.on;
        constructor.prototype.off = og.common.events.off;

        return constructor;
    }
});
