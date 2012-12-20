/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.form2',
    dependencies: [],
    obj: function () {
        var module = this, constructor, viewdef_menu, aggregation_menu, datasources_menu, query,
            events = {
                focus: 'dropmenu:focus',
                focused:'dropmenu:focused',
                open: 'dropmenu:open',
                opened: 'dropmenu:opened',
                close: 'dropmenu:close',
                closed: 'dropmenu:closed',
                closeall: 'dropmenu:closeall',
                selected: 'dropmenu:selected',
                cancelled: 'dropmenu:cancelled',
                reset:'dropmenu:reset'
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
                (aggregation_menu = new og.analytics.AggregatorsMenu({form:form})).block
            );
            form.dom();
        };

        var replay = function () {
            console.log(arguments);
        };

        var reset = function () {
            console.log(arguments);
            if (query) query = null;
            form.children.forEach(function (menu) { if (menu) menu.fire(events.reset, arguments); });
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
