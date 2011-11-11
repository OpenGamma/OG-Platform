// $ANTLR 3.2 Fedora release 15 (Rawhide) Tue Feb  8 02:02:23 UTC 2011 com/opengamma/financial/expression/deprecated/Expr.g 2011-11-09 15:09:05

package com.opengamma.financial.expression.deprecated;
//CSOFF


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;


import org.antlr.runtime.tree.*;

/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
public class ExprParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "LT", "LTE", "GT", "GTE", "EQ", "NEQ", "AND", "NOT", "OR", "TRUE", "FALSE", "IDENTIFIER", "STRING", "STRING_IDENTIFIER", "WHITESPACE", "INTEGER", "FLOAT", "'('", "')'"
    };
    public static final int INTEGER=19;
    public static final int LT=4;
    public static final int T__22=22;
    public static final int T__21=21;
    public static final int GTE=7;
    public static final int STRING_IDENTIFIER=17;
    public static final int WHITESPACE=18;
    public static final int FLOAT=20;
    public static final int NOT=11;
    public static final int AND=10;
    public static final int EOF=-1;
    public static final int LTE=5;
    public static final int TRUE=13;
    public static final int NEQ=9;
    public static final int IDENTIFIER=15;
    public static final int OR=12;
    public static final int GT=6;
    public static final int EQ=8;
    public static final int FALSE=14;
    public static final int STRING=16;

    // delegates
    // delegators


        public ExprParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public ExprParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return ExprParser.tokenNames; }
    public String getGrammarFileName() { return "com/opengamma/financial/expression/deprecated/Expr.g"; }


    public static class variable_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "variable"
    // com/opengamma/financial/expression/deprecated/Expr.g:44:1: variable : ( IDENTIFIER | STRING_IDENTIFIER );
    public final ExprParser.variable_return variable() throws RecognitionException {
        ExprParser.variable_return retval = new ExprParser.variable_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set1=null;

        Object set1_tree=null;

        try {
            // com/opengamma/financial/expression/deprecated/Expr.g:45:3: ( IDENTIFIER | STRING_IDENTIFIER )
            // com/opengamma/financial/expression/deprecated/Expr.g:
            {
            root_0 = (Object)adaptor.nil();

            set1=(Token)input.LT(1);
            if ( input.LA(1)==IDENTIFIER||input.LA(1)==STRING_IDENTIFIER ) {
                input.consume();
                adaptor.addChild(root_0, (Object)adaptor.create(set1));
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "variable"

    public static class literal_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "literal"
    // com/opengamma/financial/expression/deprecated/Expr.g:49:1: literal : ( FALSE | FLOAT | INTEGER | STRING | TRUE );
    public final ExprParser.literal_return literal() throws RecognitionException {
        ExprParser.literal_return retval = new ExprParser.literal_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set2=null;

        Object set2_tree=null;

        try {
            // com/opengamma/financial/expression/deprecated/Expr.g:50:3: ( FALSE | FLOAT | INTEGER | STRING | TRUE )
            // com/opengamma/financial/expression/deprecated/Expr.g:
            {
            root_0 = (Object)adaptor.nil();

            set2=(Token)input.LT(1);
            if ( (input.LA(1)>=TRUE && input.LA(1)<=FALSE)||input.LA(1)==STRING||(input.LA(1)>=INTEGER && input.LA(1)<=FLOAT) ) {
                input.consume();
                adaptor.addChild(root_0, (Object)adaptor.create(set2));
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "literal"

    public static class value_expr_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "value_expr"
    // com/opengamma/financial/expression/deprecated/Expr.g:57:1: value_expr : ( variable | literal | '(' expr ')' );
    public final ExprParser.value_expr_return value_expr() throws RecognitionException {
        ExprParser.value_expr_return retval = new ExprParser.value_expr_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal5=null;
        Token char_literal7=null;
        ExprParser.variable_return variable3 = null;

        ExprParser.literal_return literal4 = null;

        ExprParser.expr_return expr6 = null;


        Object char_literal5_tree=null;
        Object char_literal7_tree=null;

        try {
            // com/opengamma/financial/expression/deprecated/Expr.g:58:3: ( variable | literal | '(' expr ')' )
            int alt1=3;
            switch ( input.LA(1) ) {
            case IDENTIFIER:
            case STRING_IDENTIFIER:
                {
                alt1=1;
                }
                break;
            case TRUE:
            case FALSE:
            case STRING:
            case INTEGER:
            case FLOAT:
                {
                alt1=2;
                }
                break;
            case 21:
                {
                alt1=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 1, 0, input);

                throw nvae;
            }

            switch (alt1) {
                case 1 :
                    // com/opengamma/financial/expression/deprecated/Expr.g:58:5: variable
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_variable_in_value_expr532);
                    variable3=variable();

                    state._fsp--;

                    adaptor.addChild(root_0, variable3.getTree());

                    }
                    break;
                case 2 :
                    // com/opengamma/financial/expression/deprecated/Expr.g:59:5: literal
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_literal_in_value_expr538);
                    literal4=literal();

                    state._fsp--;

                    adaptor.addChild(root_0, literal4.getTree());

                    }
                    break;
                case 3 :
                    // com/opengamma/financial/expression/deprecated/Expr.g:60:5: '(' expr ')'
                    {
                    root_0 = (Object)adaptor.nil();

                    char_literal5=(Token)match(input,21,FOLLOW_21_in_value_expr544); 
                    pushFollow(FOLLOW_expr_in_value_expr547);
                    expr6=expr();

                    state._fsp--;

                    adaptor.addChild(root_0, expr6.getTree());
                    char_literal7=(Token)match(input,22,FOLLOW_22_in_value_expr549); 

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "value_expr"

    public static class cmp_expr_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "cmp_expr"
    // com/opengamma/financial/expression/deprecated/Expr.g:63:1: cmp_expr : ( value_expr ( ( EQ | GT | GTE | LT | LTE | NEQ ) cmp_expr )? | NOT cmp_expr );
    public final ExprParser.cmp_expr_return cmp_expr() throws RecognitionException {
        ExprParser.cmp_expr_return retval = new ExprParser.cmp_expr_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token EQ9=null;
        Token GT10=null;
        Token GTE11=null;
        Token LT12=null;
        Token LTE13=null;
        Token NEQ14=null;
        Token NOT16=null;
        ExprParser.value_expr_return value_expr8 = null;

        ExprParser.cmp_expr_return cmp_expr15 = null;

        ExprParser.cmp_expr_return cmp_expr17 = null;


        Object EQ9_tree=null;
        Object GT10_tree=null;
        Object GTE11_tree=null;
        Object LT12_tree=null;
        Object LTE13_tree=null;
        Object NEQ14_tree=null;
        Object NOT16_tree=null;

        try {
            // com/opengamma/financial/expression/deprecated/Expr.g:64:3: ( value_expr ( ( EQ | GT | GTE | LT | LTE | NEQ ) cmp_expr )? | NOT cmp_expr )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( ((LA4_0>=TRUE && LA4_0<=STRING_IDENTIFIER)||(LA4_0>=INTEGER && LA4_0<=21)) ) {
                alt4=1;
            }
            else if ( (LA4_0==NOT) ) {
                alt4=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // com/opengamma/financial/expression/deprecated/Expr.g:64:5: value_expr ( ( EQ | GT | GTE | LT | LTE | NEQ ) cmp_expr )?
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_value_expr_in_cmp_expr563);
                    value_expr8=value_expr();

                    state._fsp--;

                    adaptor.addChild(root_0, value_expr8.getTree());
                    // com/opengamma/financial/expression/deprecated/Expr.g:64:16: ( ( EQ | GT | GTE | LT | LTE | NEQ ) cmp_expr )?
                    int alt3=2;
                    int LA3_0 = input.LA(1);

                    if ( ((LA3_0>=LT && LA3_0<=NEQ)) ) {
                        alt3=1;
                    }
                    switch (alt3) {
                        case 1 :
                            // com/opengamma/financial/expression/deprecated/Expr.g:64:17: ( EQ | GT | GTE | LT | LTE | NEQ ) cmp_expr
                            {
                            // com/opengamma/financial/expression/deprecated/Expr.g:64:17: ( EQ | GT | GTE | LT | LTE | NEQ )
                            int alt2=6;
                            switch ( input.LA(1) ) {
                            case EQ:
                                {
                                alt2=1;
                                }
                                break;
                            case GT:
                                {
                                alt2=2;
                                }
                                break;
                            case GTE:
                                {
                                alt2=3;
                                }
                                break;
                            case LT:
                                {
                                alt2=4;
                                }
                                break;
                            case LTE:
                                {
                                alt2=5;
                                }
                                break;
                            case NEQ:
                                {
                                alt2=6;
                                }
                                break;
                            default:
                                NoViableAltException nvae =
                                    new NoViableAltException("", 2, 0, input);

                                throw nvae;
                            }

                            switch (alt2) {
                                case 1 :
                                    // com/opengamma/financial/expression/deprecated/Expr.g:64:18: EQ
                                    {
                                    EQ9=(Token)match(input,EQ,FOLLOW_EQ_in_cmp_expr567); 
                                    EQ9_tree = (Object)adaptor.create(EQ9);
                                    root_0 = (Object)adaptor.becomeRoot(EQ9_tree, root_0);


                                    }
                                    break;
                                case 2 :
                                    // com/opengamma/financial/expression/deprecated/Expr.g:64:24: GT
                                    {
                                    GT10=(Token)match(input,GT,FOLLOW_GT_in_cmp_expr572); 
                                    GT10_tree = (Object)adaptor.create(GT10);
                                    root_0 = (Object)adaptor.becomeRoot(GT10_tree, root_0);


                                    }
                                    break;
                                case 3 :
                                    // com/opengamma/financial/expression/deprecated/Expr.g:64:30: GTE
                                    {
                                    GTE11=(Token)match(input,GTE,FOLLOW_GTE_in_cmp_expr577); 
                                    GTE11_tree = (Object)adaptor.create(GTE11);
                                    root_0 = (Object)adaptor.becomeRoot(GTE11_tree, root_0);


                                    }
                                    break;
                                case 4 :
                                    // com/opengamma/financial/expression/deprecated/Expr.g:64:37: LT
                                    {
                                    LT12=(Token)match(input,LT,FOLLOW_LT_in_cmp_expr582); 
                                    LT12_tree = (Object)adaptor.create(LT12);
                                    root_0 = (Object)adaptor.becomeRoot(LT12_tree, root_0);


                                    }
                                    break;
                                case 5 :
                                    // com/opengamma/financial/expression/deprecated/Expr.g:64:43: LTE
                                    {
                                    LTE13=(Token)match(input,LTE,FOLLOW_LTE_in_cmp_expr587); 
                                    LTE13_tree = (Object)adaptor.create(LTE13);
                                    root_0 = (Object)adaptor.becomeRoot(LTE13_tree, root_0);


                                    }
                                    break;
                                case 6 :
                                    // com/opengamma/financial/expression/deprecated/Expr.g:64:50: NEQ
                                    {
                                    NEQ14=(Token)match(input,NEQ,FOLLOW_NEQ_in_cmp_expr592); 
                                    NEQ14_tree = (Object)adaptor.create(NEQ14);
                                    root_0 = (Object)adaptor.becomeRoot(NEQ14_tree, root_0);


                                    }
                                    break;

                            }

                            pushFollow(FOLLOW_cmp_expr_in_cmp_expr596);
                            cmp_expr15=cmp_expr();

                            state._fsp--;

                            adaptor.addChild(root_0, cmp_expr15.getTree());

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // com/opengamma/financial/expression/deprecated/Expr.g:65:5: NOT cmp_expr
                    {
                    root_0 = (Object)adaptor.nil();

                    NOT16=(Token)match(input,NOT,FOLLOW_NOT_in_cmp_expr604); 
                    NOT16_tree = (Object)adaptor.create(NOT16);
                    root_0 = (Object)adaptor.becomeRoot(NOT16_tree, root_0);

                    pushFollow(FOLLOW_cmp_expr_in_cmp_expr607);
                    cmp_expr17=cmp_expr();

                    state._fsp--;

                    adaptor.addChild(root_0, cmp_expr17.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "cmp_expr"

    public static class and_expr_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "and_expr"
    // com/opengamma/financial/expression/deprecated/Expr.g:68:1: and_expr : cmp_expr ( AND and_expr )? ;
    public final ExprParser.and_expr_return and_expr() throws RecognitionException {
        ExprParser.and_expr_return retval = new ExprParser.and_expr_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token AND19=null;
        ExprParser.cmp_expr_return cmp_expr18 = null;

        ExprParser.and_expr_return and_expr20 = null;


        Object AND19_tree=null;

        try {
            // com/opengamma/financial/expression/deprecated/Expr.g:68:10: ( cmp_expr ( AND and_expr )? )
            // com/opengamma/financial/expression/deprecated/Expr.g:68:12: cmp_expr ( AND and_expr )?
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_cmp_expr_in_and_expr618);
            cmp_expr18=cmp_expr();

            state._fsp--;

            adaptor.addChild(root_0, cmp_expr18.getTree());
            // com/opengamma/financial/expression/deprecated/Expr.g:68:21: ( AND and_expr )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==AND) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // com/opengamma/financial/expression/deprecated/Expr.g:68:22: AND and_expr
                    {
                    AND19=(Token)match(input,AND,FOLLOW_AND_in_and_expr621); 
                    AND19_tree = (Object)adaptor.create(AND19);
                    root_0 = (Object)adaptor.becomeRoot(AND19_tree, root_0);

                    pushFollow(FOLLOW_and_expr_in_and_expr624);
                    and_expr20=and_expr();

                    state._fsp--;

                    adaptor.addChild(root_0, and_expr20.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "and_expr"

    public static class or_expr_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "or_expr"
    // com/opengamma/financial/expression/deprecated/Expr.g:70:1: or_expr : and_expr ( OR or_expr )? ;
    public final ExprParser.or_expr_return or_expr() throws RecognitionException {
        ExprParser.or_expr_return retval = new ExprParser.or_expr_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token OR22=null;
        ExprParser.and_expr_return and_expr21 = null;

        ExprParser.or_expr_return or_expr23 = null;


        Object OR22_tree=null;

        try {
            // com/opengamma/financial/expression/deprecated/Expr.g:70:9: ( and_expr ( OR or_expr )? )
            // com/opengamma/financial/expression/deprecated/Expr.g:70:11: and_expr ( OR or_expr )?
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_and_expr_in_or_expr635);
            and_expr21=and_expr();

            state._fsp--;

            adaptor.addChild(root_0, and_expr21.getTree());
            // com/opengamma/financial/expression/deprecated/Expr.g:70:20: ( OR or_expr )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==OR) ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // com/opengamma/financial/expression/deprecated/Expr.g:70:21: OR or_expr
                    {
                    OR22=(Token)match(input,OR,FOLLOW_OR_in_or_expr638); 
                    OR22_tree = (Object)adaptor.create(OR22);
                    root_0 = (Object)adaptor.becomeRoot(OR22_tree, root_0);

                    pushFollow(FOLLOW_or_expr_in_or_expr641);
                    or_expr23=or_expr();

                    state._fsp--;

                    adaptor.addChild(root_0, or_expr23.getTree());

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "or_expr"

    public static class expr_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "expr"
    // com/opengamma/financial/expression/deprecated/Expr.g:72:1: expr : or_expr ;
    public final ExprParser.expr_return expr() throws RecognitionException {
        ExprParser.expr_return retval = new ExprParser.expr_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        ExprParser.or_expr_return or_expr24 = null;



        try {
            // com/opengamma/financial/expression/deprecated/Expr.g:72:6: ( or_expr )
            // com/opengamma/financial/expression/deprecated/Expr.g:72:8: or_expr
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_or_expr_in_expr652);
            or_expr24=or_expr();

            state._fsp--;

            adaptor.addChild(root_0, or_expr24.getTree());

            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "expr"

    public static class root_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "root"
    // com/opengamma/financial/expression/deprecated/Expr.g:74:1: root : expr EOF ;
    public final ExprParser.root_return root() throws RecognitionException {
        ExprParser.root_return retval = new ExprParser.root_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token EOF26=null;
        ExprParser.expr_return expr25 = null;


        Object EOF26_tree=null;

        try {
            // com/opengamma/financial/expression/deprecated/Expr.g:74:6: ( expr EOF )
            // com/opengamma/financial/expression/deprecated/Expr.g:74:8: expr EOF
            {
            root_0 = (Object)adaptor.nil();

            pushFollow(FOLLOW_expr_in_root661);
            expr25=expr();

            state._fsp--;

            adaptor.addChild(root_0, expr25.getTree());
            EOF26=(Token)match(input,EOF,FOLLOW_EOF_in_root663); 
            EOF26_tree = (Object)adaptor.create(EOF26);
            adaptor.addChild(root_0, EOF26_tree);


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "root"

    // Delegated rules


 

    public static final BitSet FOLLOW_set_in_variable0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_literal0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_variable_in_value_expr532 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_literal_in_value_expr538 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_21_in_value_expr544 = new BitSet(new long[]{0x00000000003BE800L});
    public static final BitSet FOLLOW_expr_in_value_expr547 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_22_in_value_expr549 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_value_expr_in_cmp_expr563 = new BitSet(new long[]{0x00000000000003F2L});
    public static final BitSet FOLLOW_EQ_in_cmp_expr567 = new BitSet(new long[]{0x00000000003BE800L});
    public static final BitSet FOLLOW_GT_in_cmp_expr572 = new BitSet(new long[]{0x00000000003BE800L});
    public static final BitSet FOLLOW_GTE_in_cmp_expr577 = new BitSet(new long[]{0x00000000003BE800L});
    public static final BitSet FOLLOW_LT_in_cmp_expr582 = new BitSet(new long[]{0x00000000003BE800L});
    public static final BitSet FOLLOW_LTE_in_cmp_expr587 = new BitSet(new long[]{0x00000000003BE800L});
    public static final BitSet FOLLOW_NEQ_in_cmp_expr592 = new BitSet(new long[]{0x00000000003BE800L});
    public static final BitSet FOLLOW_cmp_expr_in_cmp_expr596 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_cmp_expr604 = new BitSet(new long[]{0x00000000003BE800L});
    public static final BitSet FOLLOW_cmp_expr_in_cmp_expr607 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_cmp_expr_in_and_expr618 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_AND_in_and_expr621 = new BitSet(new long[]{0x00000000003BE800L});
    public static final BitSet FOLLOW_and_expr_in_and_expr624 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_and_expr_in_or_expr635 = new BitSet(new long[]{0x0000000000001002L});
    public static final BitSet FOLLOW_OR_in_or_expr638 = new BitSet(new long[]{0x00000000003BE800L});
    public static final BitSet FOLLOW_or_expr_in_or_expr641 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_or_expr_in_expr652 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expr_in_root661 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_EOF_in_root663 = new BitSet(new long[]{0x0000000000000002L});

}
