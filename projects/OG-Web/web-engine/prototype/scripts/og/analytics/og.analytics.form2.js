/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.form2',
    dependencies: [
        'og.common.util.ui.Form',
        'og.common.util.ui.AutoCombo',
        'og.views.common.layout'
    ],
    obj: function () { 
        return function (config) {
            var Form = og.common.util.ui.Form, blocks, form, fields, form_id, selector = config.selector,
                vds = config.viewdefs, aggs = config.aggregators, handle_error, load_handler,
                submit_handler, agg_block, data_source;
            handle_error = function () {};
            submit_handler = function () {};
            load_handler = function () {/*setTimeout(load_handler.partial(form));*/};
            agg_block =  function () {
                var tmpl = $((Handlebars.compile(aggs.template))(aggs.data)),
                    f = new form.Field({
                    url:null,
                    module: null,
                    generator: function () {}
                });
                f.html(function(){});
                return f;
            };
            (function () {
                form = new Form({
                    module: 'og.analytics.form_tash',
                    selector: selector
                });
                form.attach([
                    {type: 'form:load', handler: load_handler},
                    {type: 'form:submit', handler: submit_handler}
                ]);
                form.children.push(agg_block());
                form.dom();
            })();
        };
    }
});