/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.form2',
    dependencies: [],
    obj: function () {
        var module = this, constructor, menus = [], portfolio_dropdown, viewdef_menu, aggregation_menu,
            datasources_menu, query, url_config,
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
                form_container: 'OG-analytics-form',
                calculations_cntr: 'og-calculations',
                aggregation_cntr: 'og-aggregation',
                datasources_cntr: 'og-datasources'
            },
            dom = { form_container : $('.' + selectors.form_container) },
            form = new og.common.util.ui.Form({
                module: tashes.form_container,
                selector: '.' + selectors.form_container
            }).on('click', '.og-load', function () {
                og.analytics.url.main(query = {
                    aggregators: aggregation_menu.get_query() || [],
                    providers: datasources_menu.get_query(),
                    viewdefinition: 'DbCfg~2197901'
                });
            });

        var init = function () {
            form.children.push(
                //(portfolio_dropdown = new og.common.util.ui.AutoCombo({form:form})),
                (datasources_menu = new og.analytics.DatasourcesMenu({form:form})).block,
                (aggregation_menu = new og.analytics.AggregatorsMenu({form:form})).block
            );
            form.dom();
            menus.push(datasources_menu, aggregation_menu);
        };

        var replay = function (config) {
            if (!config) return;
            menus.forEach(function (menu) { if (menu) { menu.replay(config); } });
        };

        var reset = function () {
            if (query) query = null;
            menus.forEach(function (menu) { if (menu) menu.reset(); });
        };

        constructor = function () {
            this.on(events.reset, reset).on(events.replay, replay);
            return og.views.common.layout.main.allowOverflow('north'), init();
        };

        constructor.prototype.fire = og.common.events.fire;
        constructor.prototype.on = og.common.events.on;
        constructor.prototype.off = og.common.events.off;

        return constructor;
    }
});
