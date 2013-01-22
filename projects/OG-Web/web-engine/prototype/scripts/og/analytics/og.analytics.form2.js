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
                opendropmenu:'dropmenu:open'
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
                menus: []
            },
            form = new og.common.util.ui.Form({
                module: tashes.form_container,
                selector: '.' + selectors.form_container
            });

        var keydown_handler = function (event) {
            if (event.keyCode !== 9) return;
            var $elem = $(event.srcElement), menu, shift_key = event.shiftKey, cntrls;
            if (!shift_key) {
                if ($elem.is(':focus') && $elem.parents('.'+selectors.menus.views).length) {
                    console.log(datasources);
                    datasources.fire(events.opendropmenu);
                }
            } else if (shift_key) {

            }
            /* if (!shift_key) {
                if (ac_menu.state === 'focused') ag_menu.emitEvent(events.open);
                if ($elem.is($dom.ag_fcntrls.eq(-1))) ds_menu.emitEvent(events.open);
                if ($elem.is($dom.ds_fcntrls.eq(-1))) ds_menu.emitEvent(events.close);
            } else if (shift_key) {
                if ($elem.is($dom.load_btn)) ds_menu.emitEvent(events.open);
                if ($elem.is($dom.ds_fcntrls.eq(0))) ag_menu.emitEvent(events.open);
                if ($elem.is($dom.ag_fcntrls.eq(0))) ag_menu.emitEvent(events.close);
            }*/
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
            Object.keys(selectors.menus).map(function (entry) {
                dom.menus.push($('.'+selectors.menus[entry], '.'+selectors.form_container));
            });
        };

        var init = function (config) {
            form.on('form:load', load_handler);
            og.common.events.on('.og-portfolios.og-autocombo:autocombo:initialized', function () {
                dom.menus.filter(function (menu) {
                    if (menu.hasClass('og-portfolios')) return menu.find('input').select();
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
