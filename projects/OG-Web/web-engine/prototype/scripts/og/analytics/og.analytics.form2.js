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

        constructor = function () {
            return og.views.common.layout.main.allowOverflow('north'), init(), module;
        };

        return constructor;
    }
});
