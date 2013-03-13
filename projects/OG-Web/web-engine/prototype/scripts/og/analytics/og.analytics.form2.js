/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.Form2',
    dependencies: [],
    obj: function () {
        var constructor, callback, avail_blocks = 0,
            tashes = { form_container:  'og.analytics.form_tash' },
            selectors = {
                form_container: 'OG-analytics-form',
                load_btn: 'og-load',
                form_controls: 'input, select, a, button',
                menu_toggle: 'og-menu-toggle',
                menu: 'og-menu',
                menus: {
                    portfolios: 'og-portfolios',
                    views: 'og-view',
                    datasources: 'og-datasources',
                    temporal: 'og-temporal',
                    aggregators: 'og-aggregation',
                    filters: 'og-filters'
                }
            },
            dom = {};

        var init = function (data) {
            dom.form_container = $('.' + selectors.form_container);
            var form = new og.common.util.ui.Form({
                module: tashes.form_container,
                selector: '.' + selectors.form_container
            });
            og.common.events.on('analytics:form:blockinitialized', function (block) {
                avail_blocks += 1;
                if (avail_blocks === form.children.length) return avail_blocks = 0, form.dom();
            })
            form.children.push(
                //new og.analytics.form.Portfolios({form:form, val: data.portfolio || null}),
                new og.analytics.form.ViewDefinitions({form:form, val: data.viewdefinition || null}),
                new og.analytics.form.DatasourcesMenu({form:form, source:  data.providers || null}),
                //new og.analytics.form.TemporalMenu({form:form, temporal: data.temporals || null }),
                new og.analytics.form.AggregatorsMenu({form:form, aggregators: data.aggregators || null})
                //new og.analytics.form.FiltersMenu({form:form, index:'filters', filters:  data.filters || null})
            );
            form.on('form:load', load_handler);
            form.on('form:submit', function (result) {load_form(result.data);});
            form.on('keydown', selectors.form_controls, keydown_handler);
        };

        var load_handler = function () {
            dom.menus = {};
            dom.form_controls = {};
            Object.keys(selectors.menus).map(function (entry) {
                dom.menus[entry] = $('.'+selectors.menus[entry], dom.form_container);
                dom.form_controls[entry] = $(selectors.form_controls, dom.menus[entry]);
            });
            dom.menus.views.find('input').select();
        };

        var load_form = function (data) {
            var query = {
                aggregators: data.aggregators,
                providers: data.providers,
                viewdefinition: data.viewdefinition,
                portfolio: data.portfolio,
                temporal: data.temporal
            };
            callback(query);
            $('.'+selectors.load_btn).focus(0);
        };

        var keydown_handler = function (event) {
            if (event.keyCode !== 9) return;
            var $elem = $(event.srcElement || event.target), shift = event.shiftKey, menus = dom.menus,
                controls = dom.form_controls, menu_toggle = '.'+selectors.menu_toggle,
                load_btn = '.'+selectors.load_btn, idx,
                toggle = function (entry) {
                    menus[entry].find(menu_toggle).trigger('click', event);
                };
            if (!shift) {
                if ($elem.closest(dom.menus.views).length) return toggle('datasources');
                if ($elem.is(controls.datasources.eq(-1))) return toggle('datasources'), toggle('aggregators');
                if ($elem.is(controls.aggregators.eq(-1))) return toggle('aggregators');
            } else if (shift) {
                if ($elem.is(load_btn)) return toggle('aggregators'), controls.aggregators.eq(-1).focus(0);
                if ($elem.is(controls.aggregators.eq(0)))
                    return toggle('aggregators'), toggle('datasources'), controls.datasources.eq(-1).focus(0);
                if ($elem.is(controls.datasources.eq(0))) return toggle('datasources');
            }
        };

        constructor = function (config) {
            callback = config.callback;
            return og.views.common.layout.main.allowOverflow('north'), init(config.data || {});
        };

        return constructor;
    }
});
