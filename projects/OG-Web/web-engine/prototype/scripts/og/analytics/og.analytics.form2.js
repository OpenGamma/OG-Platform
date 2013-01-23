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
            events = {
                datasources:{
                    'open': 'datasources:dropmenu:open',
                    'close': 'datasources:dropmenu:close',
                    'focus': 'datasources:dropmenu:focus'
                },
                temporal:{
                    'open': 'temporal:dropmenu:open',
                    'close': 'temporal:dropmenu:close',
                    'focus': 'temporal:dropmenu:focus'
                },
                aggregators:{
                    'open': 'aggregators:dropmenu:open',
                    'close': 'aggregators:dropmenu:close',
                    'focus': 'aggregators:dropmenu:focus'
                }
            },
            selectors = {
                form_container: 'OG-analytics-form',
                load_btn: 'og-load',
                form_controls: 'input, select, a, button',
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
                menus: {}
            },
            form = new og.common.util.ui.Form({
                module: tashes.form_container,
                selector: '.' + selectors.form_container
            });

        var keydown_handler = function (event) {
            if (event.keyCode !== 9) return;
            var $elem = $(event.srcElement), shift = event.shiftKey, controls = {};
            Object.keys(dom.menus).map(function (entry) {
                var cntrls = $(selectors.form_controls, dom.menus[entry]);
                if (cntrls.length) controls[entry] = cntrls;
            });
            if (!shift) {
                if ($elem.is(':focus') && $elem.closest(dom.menus['views']).length)
                    return og.common.events.fire(events.datasources.open);
                if ($elem.is(controls['datasources'].eq(-1)))
                    return og.common.events.fire(events.datasources.close), og.common.events.fire(events.temporal.open);
                if ($elem.is(controls['temporal'].eq(-1)))
                    return og.common.events.fire(events.temporal.close), og.common.events.fire(events.aggregators.open);
                if ($elem.is(controls['aggregators'].eq(-1))) return og.common.events.fire(events.aggregators.close);
            } else if (shift) {
                if ($elem.is('.'+selectors.load_btn)) return og.common.events.fire(events.aggregators.open);
                if ($elem.is(controls['aggregators'].eq(0)))
                    return og.common.events.fire(events.aggregators.close), og.common.events.fire(events.temporal.open);
                if ($elem.is(controls['temporal'].eq(0)))
                    return og.common.events.fire(events.temporal.close), og.common.events.fire(events.datasources.open);
                if ($elem.is(controls['datasources'].eq(0))) return og.common.events.fire(events.datasources.close);
            }
        };

        var load_form = function () {
            var compilation = form.compile();
            query = {
                aggregators: compilation.data.aggregators,
                providers: compilation.data.providers,
                viewdefinition: 'DbCfg~2197901'
            };
        };

        var load_handler = function (event) {
            Object.keys(selectors.menus).map(function (entry, idx) {
                dom.menus[entry] = $('.'+selectors.menus[entry], dom.form_container);
            });
        };

        var init = function (config) {
            form.on('form:load', load_handler);
            og.common.events.on('.og-portfolios.og-autocombo:autocombo:initialized', function () {
                Object.keys(dom.menus).filter(function (menu) {
                    if (dom.menus[menu].hasClass('og-portfolios')) return dom.menus[menu].find('input').select();
                });
            });
            form.on('keydown', selectors.form_controls, keydown_handler);
            form.on('click', '.'+selectors.load_btn, load_form);

            var fixtures = {
                aggregators:['Beta', 'Currency', 'Underlying', 'Region'],
                providers: [{
                    marketDataType:'live',
                    source: 'Bloomberg'
                },
                {
                    marketDataType:'snapshot',
                    snapshotId:'DbSnp~35365~1'
                },
                {
                    marketDataType:'latestHistorical',
                    resolverKey:'DbCfg~1047577'
                },
                {
                    date:'2013-01-04',
                    marketDataType:'fixedHistorical',
                    resolverKey:'DbCfg~991626'
                }]
            };

            form.children.push(
                (portfolios = new og.analytics.form.Portfolios({form:form})).block,
                (viewdefinitions = new og.analytics.form.ViewDefinitions({form:form})).block,
                (datasources = new og.analytics.form.DatasourcesMenu({form:form})),
                (temporal = new og.analytics.form.TemporalMenu({form:form})),
                (aggregators = new og.analytics.form.AggregatorsMenu({form:form})),
                (filters = new og.analytics.form.FiltersMenu({form:form, index:'filters' }))
            );
            menus.push(portfolios, viewdefinitions, datasources, temporal, aggregators, filters);
            form.dom();
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
