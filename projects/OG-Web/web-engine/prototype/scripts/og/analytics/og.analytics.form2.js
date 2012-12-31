/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.form2',
    dependencies: [],
    obj: function () {
        var module = this, constructor, menus = [], viewdef_menu, aggregation_menu, datasources_menu, query,
            url_config,
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
                aggregation_cntr: 'og-aggregation',
                datasources_cntr: 'og-datasources'
            },
            dom = { form_container : $('.' + selectors.form_container) },
            form = new og.common.util.ui.Form({
                module: tashes.form_container,
                selector: '.' + selectors.form_container
            });

        var init = function () {
            form.children.push(
                (datasources_menu = new og.analytics.DatasourcesMenu({form:form})).block,
                (aggregation_menu = new og.analytics.AggregatorsMenu({form:form})).block
            );
            form.dom();
            menus.push(datasources_menu, aggregation_menu);
        };

        var replay = function (config) {
            if (!config) return;
            menus.forEach(function (menu) { if (menu) { menu.fire(events.replay, config); } });
        };

        var reset = function () {
            if (query) query = null;
            menus.forEach(function (menu) { if (menu) menu.fire(events.reset); });
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
