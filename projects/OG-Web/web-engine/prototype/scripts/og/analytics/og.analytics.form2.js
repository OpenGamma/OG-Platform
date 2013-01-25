/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.form2',
    dependencies: [],
    obj: function () {
        var module = this, constructor, menus = [],  query,
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
            dom = {
                form_container : $('.' + selectors.form_container),
                form_controls: {},
                menus: {}
            },
            form = new og.common.util.ui.Form({
                module: tashes.form_container,
                selector: '.' + selectors.form_container
            });

        var init = function (config) {
            form.on('form:load', load_handler);
            form.on('keydown', selectors.form_controls, keydown_handler);
            form.on('click', '.'+selectors.load_btn, load_form);
            form.children.push(
                (new og.analytics.form.Portfolios({form:form, val:'Alan'})).block,
                (new og.analytics.form.ViewDefinitions({form:form, val:'ARS Test'})).block,
                new og.analytics.form.DatasourcesMenu({form:form}),
                new og.analytics.form.TemporalMenu({form:form}),
                new og.analytics.form.AggregatorsMenu({form:form}),
                new og.analytics.form.FiltersMenu({form:form, index:'filters' })
            );
            form.dom();
        };

        var load_handler = function (event) {
            Object.keys(selectors.menus).map(function (entry, idx) {
                dom.menus[entry] = $('.'+selectors.menus[entry], dom.form_container);
                dom.form_controls[entry] = $(selectors.form_controls, dom.menus[entry]);
            });
            og.common.events.on('.og-portfolios.og-autocombo:autocombo:initialized', function () {
                return dom.menus['portfolios'].find('input').select();
            });
        };

        var load_form = function (event) {
            var compilation = form.compile();
            query = {
                aggregators: compilation.data.aggregators,
                providers: compilation.data.providers,
                viewdefinition: compilation.data.viewdefinition,
                portfolio: compilation.data.portfolio,
                temporal: compilation.data.temporal
             };
             console.log(query);
            $(event.srcElement || event.target).focus(0);
        };

        var keydown_handler = function (event) {
            if (event.keyCode !== 9) return;
            var $elem = $(event.srcElement || event.target), shift = event.shiftKey, menus = dom.menus,
                controls = dom.form_controls, menu = '.'+selectors.menu,
                load_btn = '.'+selectors.load_btn;
            if (!shift) {
                if ($elem.closest(dom.menus['views']).length) return toggle('datasources');
                if ($elem.is(controls['datasources'].eq(-1))) return toggle('datasources'), toggle('temporal');
                if ($elem.is(controls['temporal'].eq(-1))) return toggle('temporal'), toggle('aggregators');
                if ($elem.is(controls['aggregators'].eq(-1))) return toggle('aggregators'), toggle('filters');
                if ($elem.is(controls['filters'].eq(-1))) return toggle('filters');
            } else if (shift) {
                if ($elem.is(load_btn)) return toggle('filters');
                if ($elem.is(controls['filters'].eq(0))) return toggle('filters'), toggle('aggregators');
                if ($elem.is(controls['aggregators'].eq(0))) return toggle('aggregators'), toggle('temporal');
                if ($elem.is(controls['temporal'].eq(0))) return toggle('temporal'), toggle('datasources');
                if ($elem.is(controls['datasources'].eq(0))) return toggle('datasources');
            }
        };

        var toggle_menu = function (entry) {
            menus[entry].find(menu).toggleClass('og-active').toggle();
            controls[entry].eq(shift ? -1 : 0).focus(0);
        };

        constructor = function (config) {
            return og.views.common.layout.main.allowOverflow('north'), init(config);
        };

        constructor.prototype.fire = og.common.events.fire;
        constructor.prototype.on = og.common.events.on;
        constructor.prototype.off = og.common.events.off;

        return constructor;
    }
});
