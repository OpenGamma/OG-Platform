/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.blotter',
    dependencies: ['og.common.routes', 'og.common.masthead'],
    obj: function () {
        var module = this, masthead = og.common.masthead, page_name = module.name.split('.').pop(),
            routes = og.common.routes, main_selector = '.OG-layout-analytics-center', view, form;
        module.rules = {load: {route: '/', method: module.name + '.load'}};
        return view = {
            check_state: og.views.common.state.check.partial('/'),
            load: function (args) {
                og.analytics.blotter = true;
//                masthead.menu.set_tab(page_name);
                $('.new_trade').css({display: 'inline-block'}).click(function (){
                    new og.blotter.Dialog({portfolio:{name:"My Portfolio", id:"XYZ"}});
                });
                $('.fxforward').click(function (){
                    og.api.rest.blotter.trades.get({id:"DbPos~164134"}).pipe(function(data){
                        new og.blotter.Dialog({details: data, portfolio:{name:"My Portfolio", id:"XYZ"}});
                    });
                });
                $('.capfloorcmsspread').click(function (){
                    og.api.rest.blotter.trades.get({id:"DbPos~164152"}).pipe(function(data){
                        new og.blotter.Dialog({details: data, portfolio:{name:"My Portfolio", id:"XYZ"}});
                    });
                });
                $('.capfloor').click(function (){
                    og.api.rest.blotter.trades.get({id:"DbPos~164182"}).pipe(function(data){
                        new og.blotter.Dialog({details: data, portfolio:{name:"My Portfolio", id:"XYZ"}});
                    });
                });
                $('.equityvar').click(function (){
                    og.api.rest.blotter.trades.get({id:"DbPos~164208"}).pipe(function(data){
                        new og.blotter.Dialog({details: data, portfolio:{name:"My Portfolio", id:"XYZ"}});
                    });
                });
                $('.fra').click(function (){
                    og.api.rest.blotter.trades.get({id:"DbPos~164245"}).pipe(function(data){
                        new og.blotter.Dialog({details: data, portfolio:{name:"My Portfolio", id:"XYZ"}});
                    });
                });
                $('.fxbarrier').click(function (){
                    og.api.rest.blotter.trades.get({id:"DbPos~164255"}).pipe(function(data){
                        new og.blotter.Dialog({details: data, portfolio:{name:"My Portfolio", id:"XYZ"}});
                    });
                });
                $('.fxoption').click(function (){
                    og.api.rest.blotter.trades.get({id:"DbPos~164257"}).pipe(function(data){
                        new og.blotter.Dialog({details: data, portfolio:{name:"My Portfolio", id:"XYZ"}});
                    });
                });
                $('.nondelfxoption').click(function (){
                    og.api.rest.blotter.trades.get({id:"DbPos~164259"}).pipe(function(data){
                        new og.blotter.Dialog({details: data, portfolio:{name:"My Portfolio", id:"XYZ"}});
                    });
                });
                $('.swap').click(function (){
                    og.api.rest.blotter.trades.get({id:"DbPos~164306"}).pipe(function(data){
                        new og.blotter.Dialog({details: data, portfolio:{name:"My Portfolio", id:"XYZ"}});
                    });
                });
                $('.swaption').click(function (){
                    og.api.rest.blotter.trades.get({id:"DbPos~164328"}).pipe(function(data){
                        new og.blotter.Dialog({details: data, portfolio:{name:"My Portfolio", id:"XYZ"}});
                    });
                });
                $('.fungibleb').click(function (){
                    og.api.rest.blotter.trades.get({id:"DbPos~164540"}).pipe(function(data){
                        new og.blotter.Dialog({details: data, portfolio:{name:"My Portfolio", id:"XYZ"}});
                    });
                });
                $('.fungiblebf').click(function (){
                    og.api.rest.blotter.trades.get({id:"DbPos~164562"}).pipe(function(data){
                        new og.blotter.Dialog({details: data, portfolio:{name:"My Portfolio", id:"XYZ"}});
                    });
                });
                $('.fungibleet').click(function (){
                    og.api.rest.blotter.trades.get({id:"DbPos~164554"}).pipe(function(data){
                        new og.blotter.Dialog({details: data, portfolio:{name:"My Portfolio", id:"XYZ"}});
                    });
                });
            },
            load_item: function (args) {
                view.check_state({args: args, conditions: [{new_page: view.load}]});
                og.analytics.url.process(args);
            },
            init: function () {for (var rule in view.rules) routes.add(view.rules[rule]);},
            rules: {
                load: {route: '/', method: module.name + '.load'},
                load_item: {route: '/:data', method: module.name + '.load_item'}
            }
        };
    }
});