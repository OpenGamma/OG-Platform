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
                form_container:  'og.analytics.form_tash'
            },
            selectors = {
                form_container: 'OG-analytics-form',
                aggregation_cntr: 'og-aggregation',
                datasources_cntr: 'og-datasources'
            },
            dom = { masthead : $('.' + selectors.masthead) },
            form = new og.common.util.ui.Form({
                module: tashes.form_container,
                selector: '.' + selectors.form_container
            });

        var init = function (aggregators) {
            form.children.push(
                (aggregation_menu = new og.analytics.AggregatorsMenu({form:form, data:aggregators.data})).block
            );
            form.dom();
        };

        constructor = function () {
            og.views.common.layout.main.allowOverflow('north');
            $.when(og.api.rest.aggregators.get()).pipe(init);
            return module;
        };

        return constructor;
    }
});
