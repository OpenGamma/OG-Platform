/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.blotter',
    dependencies: [],
    obj: function () {
        var view, module = this, dialog, masthead = og.common.masthead, page_name = module.name.split('.').pop();
        $(window).load(function () { 
            $('.van_swap').click(function (){
                new og.blotter.forms.Vanilla_swap();
            });
            $('.var_swap').click(function (){
                new og.blotter.forms.Variance_swap();
            });
            $('.swaption').click(function (){
                new og.blotter.forms.Swaption();
            });
            $('.fx_for').click(function (){
                new og.blotter.forms.Fx_forward();
            });
            $('.fx_bar_op').click(function (){
                new og.blotter.forms.Fx_barrier_option();
            });
            $('.fx_op').click(function (){
                new og.blotter.forms.Fx_option();
            });
            $('.nd_fx_op').click(function (){
                new og.blotter.forms.Non_del_fx_option();
            });
        });
        return view = {
            init: function () {
                masthead.menu.set_tab(page_name);
                new og.blotter.Dialog();
            }
        };
    }
});