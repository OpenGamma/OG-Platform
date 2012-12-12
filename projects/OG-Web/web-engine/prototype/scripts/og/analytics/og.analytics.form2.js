/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.form2',
    dependencies: [],
    obj: function () {
        var module = this, constructor, viewdef_form, aggregation_menu, datasources_menu,
            tashes = {
                form_container:  'og.analytics.form_tash',
                aggregation_menu: 'og.analytics.form_aggregation_tash',
                datasources_menu: 'og.analytics.form_datasources_tash'
            },
            selectors = {
                masthead: 'OG-masthead',
                aggregation_cntr: 'og-aggregation',
                datasources_cntr: 'og-datasources'
            },
            dom = { masthead : $('.' + selectors.masthead) };

        var init = function () {
            $.when(
                og.api.text({module: tashes.form_container}),
                og.api.text({module: tashes.datasources_menu}),
                og.api.text({module: tashes.aggregation_menu})
            ).pipe(function (form_container, datasources_menu, aggregation_menu) {
                dom.masthead.append(form_container);
                /*og.api.rest.aggregators.get().pipe(function (resp) {
                    datasources_menu = new og.analytics.AggregatorsMenu({
                        cntr: dom.aggregation_cntr = $('.' + selectors.aggregation_cntr, dom.masthead),
                        tmpl: aggregation_menu,
                        opts:
                    });
                });
                dom.datasources_cntr = $('.' + selectors.datasources_cntr, dom.masthead);
                */
                og.api.rest.aggregators.get().pipe(function (resp) {
                    aggregation_menu = new og.analytics.AggregatorsMenu({
                        cntr: dom.aggregation_cntr = $('.' + selectors.aggregation_cntr, dom.masthead),
                        tmpl: aggregation_menu,
                        data: resp.data,
                        opts: {}
                    });
                });
            });
        };

        constructor = function () {
            og.views.common.layout.main.allowOverflow('north');
            init();
            return this;
        };

        return constructor;
    }
});
