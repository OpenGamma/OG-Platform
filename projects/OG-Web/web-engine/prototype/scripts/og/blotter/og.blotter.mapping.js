/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.mapping',
    dependencies: [],
    obj: function () {
        var mapping, module = this, tmpl_prefix = 'og.blotter.forms.', tmpl_suffix = '_tash', 
            template_name = function (name) {return tmpl_prefix + name + tmpl_suffix;};
        return mapping = {
            forms : {
                VAR_SWAP : {title: 'Variance Swap', tmpl: template_name('variance_swap')},
                VAN_SWAP : {title: 'Vannilla Swap', tmpl: template_name('vanilla_swap')},
                SWAPTION : {title: 'Swaption', tmpl: template_name('swaption')}
            }
        };
    }
});