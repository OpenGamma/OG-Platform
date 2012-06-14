/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.form',
    dependencies: [],
    obj: function () {
        return function (selector) {
            $.when(
                og.api.text({module: 'og.analytics.form_tash'}),
                og.api.rest.configs.get({handler: function (r) {return r}})
            ).then(function (template, search) {
                $(selector).html(template);
                og.common.util.ui.combobox({
                    selector: '.OG-analytics-form .og-view',
                    data: search.data.data,
                    placeholder: 'search...'
                }).select();
            });
        }
    }
});