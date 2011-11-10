// $ANTLR 3.2 Fedora release 15 (Rawhide) Tue Feb  8 02:02:23 UTC 2011 com/opengamma/financial/expression/deprecated/Expr.g 2011-11-09 15:09:05

package com.opengamma.financial.expression.deprecated;
//CSOFF


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class ExprLexer extends Lexer {
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

    public ExprLexer() {;} 
    public ExprLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public ExprLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "com/opengamma/financial/expression/deprecated/Expr.g"; }

    // $ANTLR start "LT"
    public final void mLT() throws RecognitionException {
        try {
            int _type = LT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com/opengamma/financial/expression/deprecated/Expr.g:8:4: ( '<' )
            // com/opengamma/financial/expression/deprecated/Expr.g:8:6: '<'
            {
            match('<'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LT"

    // $ANTLR start "LTE"
    public final void mLTE() throws RecognitionException {
        try {
            int _type = LTE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com/opengamma/financial/expression/deprecated/Expr.g:9:5: ( '<=' )
            // com/opengamma/financial/expression/deprecated/Expr.g:9:7: '<='
            {
            match("<="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LTE"

    // $ANTLR start "GT"
    public final void mGT() throws RecognitionException {
        try {
            int _type = GT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com/opengamma/financial/expression/deprecated/Expr.g:10:4: ( '>' )
            // com/opengamma/financial/expression/deprecated/Expr.g:10:6: '>'
            {
            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GT"

    // $ANTLR start "GTE"
    public final void mGTE() throws RecognitionException {
        try {
            int _type = GTE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com/opengamma/financial/expression/deprecated/Expr.g:11:5: ( '>=' )
            // com/opengamma/financial/expression/deprecated/Expr.g:11:7: '>='
            {
            match(">="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GTE"

    // $ANTLR start "EQ"
    public final void mEQ() throws RecognitionException {
        try {
            int _type = EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com/opengamma/financial/expression/deprecated/Expr.g:12:4: ( '=' )
            // com/opengamma/financial/expression/deprecated/Expr.g:12:6: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EQ"

    // $ANTLR start "NEQ"
    public final void mNEQ() throws RecognitionException {
        try {
            int _type = NEQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com/opengamma/financial/expression/deprecated/Expr.g:13:5: ( '<>' )
            // com/opengamma/financial/expression/deprecated/Expr.g:13:7: '<>'
            {
            match("<>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NEQ"

    // $ANTLR start "T__21"
    public final void mT__21() throws RecognitionException {
        try {
            int _type = T__21;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com/opengamma/financial/expression/deprecated/Expr.g:14:7: ( '(' )
            // com/opengamma/financial/expression/deprecated/Expr.g:14:9: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__21"

    // $ANTLR start "T__22"
    public final void mT__22() throws RecognitionException {
        try {
            int _type = T__22;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com/opengamma/financial/expression/deprecated/Expr.g:15:7: ( ')' )
            // com/opengamma/financial/expression/deprecated/Expr.g:15:9: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__22"

    // $ANTLR start "AND"
    public final void mAND() throws RecognitionException {
        try {
            int _type = AND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com/opengamma/financial/expression/deprecated/Expr.g:32:5: ( ( 'a' | 'A' ) ( 'n' | 'N' ) ( 'd' | 'D' ) )
            // com/opengamma/financial/expression/deprecated/Expr.g:32:7: ( 'a' | 'A' ) ( 'n' | 'N' ) ( 'd' | 'D' )
            {
            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AND"

    // $ANTLR start "NOT"
    public final void mNOT() throws RecognitionException {
        try {
            int _type = NOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com/opengamma/financial/expression/deprecated/Expr.g:33:5: ( ( 'n' | 'N' ) ( 'o' | 'O' ) ( 't' | 'T' ) )
            // com/opengamma/financial/expression/deprecated/Expr.g:33:7: ( 'n' | 'N' ) ( 'o' | 'O' ) ( 't' | 'T' )
            {
            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NOT"

    // $ANTLR start "OR"
    public final void mOR() throws RecognitionException {
        try {
            int _type = OR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com/opengamma/financial/expression/deprecated/Expr.g:34:4: ( ( 'o' | 'O' ) ( 'r' | 'R' ) )
            // com/opengamma/financial/expression/deprecated/Expr.g:34:6: ( 'o' | 'O' ) ( 'r' | 'R' )
            {
            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "OR"

    // $ANTLR start "TRUE"
    public final void mTRUE() throws RecognitionException {
        try {
            int _type = TRUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com/opengamma/financial/expression/deprecated/Expr.g:35:6: ( ( 't' | 'T' ) ( 'r' | 'R' ) ( 'u' | 'U' ) ( 'e' | 'E' ) )
            // com/opengamma/financial/expression/deprecated/Expr.g:35:8: ( 't' | 'T' ) ( 'r' | 'R' ) ( 'u' | 'U' ) ( 'e' | 'E' )
            {
            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TRUE"

    // $ANTLR start "FALSE"
    public final void mFALSE() throws RecognitionException {
        try {
            int _type = FALSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com/opengamma/financial/expression/deprecated/Expr.g:36:7: ( ( 'f' | 'F' ) ( 'a' | 'A' ) ( 'l' | 'L' ) ( 's' | 'S' ) ( 'e' | 'E' ) )
            // com/opengamma/financial/expression/deprecated/Expr.g:36:9: ( 'f' | 'F' ) ( 'a' | 'A' ) ( 'l' | 'L' ) ( 's' | 'S' ) ( 'e' | 'E' )
            {
            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FALSE"

    // $ANTLR start "IDENTIFIER"
    public final void mIDENTIFIER() throws RecognitionException {
        try {
            int _type = IDENTIFIER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com/opengamma/financial/expression/deprecated/Expr.g:37:12: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' | '.' )* )
            // com/opengamma/financial/expression/deprecated/Expr.g:37:14: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' | '.' )*
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // com/opengamma/financial/expression/deprecated/Expr.g:37:37: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' | '.' )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0=='.'||(LA1_0>='0' && LA1_0<='9')||(LA1_0>='A' && LA1_0<='Z')||LA1_0=='_'||(LA1_0>='a' && LA1_0<='z')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // com/opengamma/financial/expression/deprecated/Expr.g:
            	    {
            	    if ( input.LA(1)=='.'||(input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "IDENTIFIER"

    // $ANTLR start "STRING"
    public final void mSTRING() throws RecognitionException {
        try {
            int _type = STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com/opengamma/financial/expression/deprecated/Expr.g:38:8: ( '\"' ( options {greedy=false; } : ( '\\\\' . | . ) )* '\"' )
            // com/opengamma/financial/expression/deprecated/Expr.g:38:10: '\"' ( options {greedy=false; } : ( '\\\\' . | . ) )* '\"'
            {
            match('\"'); 
            // com/opengamma/financial/expression/deprecated/Expr.g:38:14: ( options {greedy=false; } : ( '\\\\' . | . ) )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0=='\"') ) {
                    alt3=2;
                }
                else if ( ((LA3_0>='\u0000' && LA3_0<='!')||(LA3_0>='#' && LA3_0<='\uFFFF')) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // com/opengamma/financial/expression/deprecated/Expr.g:38:46: ( '\\\\' . | . )
            	    {
            	    // com/opengamma/financial/expression/deprecated/Expr.g:38:46: ( '\\\\' . | . )
            	    int alt2=2;
            	    int LA2_0 = input.LA(1);

            	    if ( (LA2_0=='\\') ) {
            	        int LA2_1 = input.LA(2);

            	        if ( (LA2_1=='\"') ) {
            	            alt2=1;
            	        }
            	        else if ( (LA2_1=='\\') ) {
            	            alt2=1;
            	        }
            	        else if ( ((LA2_1>='\u0000' && LA2_1<='!')||(LA2_1>='#' && LA2_1<='[')||(LA2_1>=']' && LA2_1<='\uFFFF')) ) {
            	            alt2=1;
            	        }
            	        else {
            	            NoViableAltException nvae =
            	                new NoViableAltException("", 2, 1, input);

            	            throw nvae;
            	        }
            	    }
            	    else if ( ((LA2_0>='\u0000' && LA2_0<='[')||(LA2_0>=']' && LA2_0<='\uFFFF')) ) {
            	        alt2=2;
            	    }
            	    else {
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 2, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt2) {
            	        case 1 :
            	            // com/opengamma/financial/expression/deprecated/Expr.g:38:47: '\\\\' .
            	            {
            	            match('\\'); 
            	            matchAny(); 

            	            }
            	            break;
            	        case 2 :
            	            // com/opengamma/financial/expression/deprecated/Expr.g:38:53: .
            	            {
            	            matchAny(); 

            	            }
            	            break;

            	    }


            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);

            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STRING"

    // $ANTLR start "STRING_IDENTIFIER"
    public final void mSTRING_IDENTIFIER() throws RecognitionException {
        try {
            int _type = STRING_IDENTIFIER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com/opengamma/financial/expression/deprecated/Expr.g:39:19: ( '$\"' ( options {greedy=false; } : ( '\\\\' . | . ) )* '\"' )
            // com/opengamma/financial/expression/deprecated/Expr.g:39:21: '$\"' ( options {greedy=false; } : ( '\\\\' . | . ) )* '\"'
            {
            match("$\""); 

            // com/opengamma/financial/expression/deprecated/Expr.g:39:26: ( options {greedy=false; } : ( '\\\\' . | . ) )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0=='\"') ) {
                    alt5=2;
                }
                else if ( ((LA5_0>='\u0000' && LA5_0<='!')||(LA5_0>='#' && LA5_0<='\uFFFF')) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // com/opengamma/financial/expression/deprecated/Expr.g:39:58: ( '\\\\' . | . )
            	    {
            	    // com/opengamma/financial/expression/deprecated/Expr.g:39:58: ( '\\\\' . | . )
            	    int alt4=2;
            	    int LA4_0 = input.LA(1);

            	    if ( (LA4_0=='\\') ) {
            	        int LA4_1 = input.LA(2);

            	        if ( (LA4_1=='\"') ) {
            	            alt4=1;
            	        }
            	        else if ( (LA4_1=='\\') ) {
            	            alt4=1;
            	        }
            	        else if ( ((LA4_1>='\u0000' && LA4_1<='!')||(LA4_1>='#' && LA4_1<='[')||(LA4_1>=']' && LA4_1<='\uFFFF')) ) {
            	            alt4=1;
            	        }
            	        else {
            	            NoViableAltException nvae =
            	                new NoViableAltException("", 4, 1, input);

            	            throw nvae;
            	        }
            	    }
            	    else if ( ((LA4_0>='\u0000' && LA4_0<='[')||(LA4_0>=']' && LA4_0<='\uFFFF')) ) {
            	        alt4=2;
            	    }
            	    else {
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 4, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt4) {
            	        case 1 :
            	            // com/opengamma/financial/expression/deprecated/Expr.g:39:59: '\\\\' .
            	            {
            	            match('\\'); 
            	            matchAny(); 

            	            }
            	            break;
            	        case 2 :
            	            // com/opengamma/financial/expression/deprecated/Expr.g:39:65: .
            	            {
            	            matchAny(); 

            	            }
            	            break;

            	    }


            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);

            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STRING_IDENTIFIER"

    // $ANTLR start "WHITESPACE"
    public final void mWHITESPACE() throws RecognitionException {
        try {
            int _type = WHITESPACE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com/opengamma/financial/expression/deprecated/Expr.g:40:12: ( ( ' ' | '\\t' | '\\r' | '\\n' )+ )
            // com/opengamma/financial/expression/deprecated/Expr.g:40:14: ( ' ' | '\\t' | '\\r' | '\\n' )+
            {
            // com/opengamma/financial/expression/deprecated/Expr.g:40:14: ( ' ' | '\\t' | '\\r' | '\\n' )+
            int cnt6=0;
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( ((LA6_0>='\t' && LA6_0<='\n')||LA6_0=='\r'||LA6_0==' ') ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // com/opengamma/financial/expression/deprecated/Expr.g:
            	    {
            	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt6 >= 1 ) break loop6;
                        EarlyExitException eee =
                            new EarlyExitException(6, input);
                        throw eee;
                }
                cnt6++;
            } while (true);

             skip (); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WHITESPACE"

    // $ANTLR start "INTEGER"
    public final void mINTEGER() throws RecognitionException {
        try {
            int _type = INTEGER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com/opengamma/financial/expression/deprecated/Expr.g:41:9: ( ( '+' | '-' )? ( '0' .. '9' )+ )
            // com/opengamma/financial/expression/deprecated/Expr.g:41:11: ( '+' | '-' )? ( '0' .. '9' )+
            {
            // com/opengamma/financial/expression/deprecated/Expr.g:41:11: ( '+' | '-' )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0=='+'||LA7_0=='-') ) {
                alt7=1;
            }
            switch (alt7) {
                case 1 :
                    // com/opengamma/financial/expression/deprecated/Expr.g:
                    {
                    if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;

            }

            // com/opengamma/financial/expression/deprecated/Expr.g:41:22: ( '0' .. '9' )+
            int cnt8=0;
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( ((LA8_0>='0' && LA8_0<='9')) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // com/opengamma/financial/expression/deprecated/Expr.g:41:22: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt8 >= 1 ) break loop8;
                        EarlyExitException eee =
                            new EarlyExitException(8, input);
                        throw eee;
                }
                cnt8++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "INTEGER"

    // $ANTLR start "FLOAT"
    public final void mFLOAT() throws RecognitionException {
        try {
            int _type = FLOAT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // com/opengamma/financial/expression/deprecated/Expr.g:42:7: ( ( '+' | '-' )? ( '0' .. '9' )* '.' ( '0' .. '9' )+ ( ( 'e' | 'E' ) ( '+' | '-' )? ( '0' .. '9' )+ )? ( 'f' )? )
            // com/opengamma/financial/expression/deprecated/Expr.g:42:9: ( '+' | '-' )? ( '0' .. '9' )* '.' ( '0' .. '9' )+ ( ( 'e' | 'E' ) ( '+' | '-' )? ( '0' .. '9' )+ )? ( 'f' )?
            {
            // com/opengamma/financial/expression/deprecated/Expr.g:42:9: ( '+' | '-' )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0=='+'||LA9_0=='-') ) {
                alt9=1;
            }
            switch (alt9) {
                case 1 :
                    // com/opengamma/financial/expression/deprecated/Expr.g:
                    {
                    if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;

            }

            // com/opengamma/financial/expression/deprecated/Expr.g:42:20: ( '0' .. '9' )*
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( ((LA10_0>='0' && LA10_0<='9')) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // com/opengamma/financial/expression/deprecated/Expr.g:42:21: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    break loop10;
                }
            } while (true);

            match('.'); 
            // com/opengamma/financial/expression/deprecated/Expr.g:42:36: ( '0' .. '9' )+
            int cnt11=0;
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( ((LA11_0>='0' && LA11_0<='9')) ) {
                    alt11=1;
                }


                switch (alt11) {
            	case 1 :
            	    // com/opengamma/financial/expression/deprecated/Expr.g:42:37: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt11 >= 1 ) break loop11;
                        EarlyExitException eee =
                            new EarlyExitException(11, input);
                        throw eee;
                }
                cnt11++;
            } while (true);

            // com/opengamma/financial/expression/deprecated/Expr.g:42:48: ( ( 'e' | 'E' ) ( '+' | '-' )? ( '0' .. '9' )+ )?
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( (LA14_0=='E'||LA14_0=='e') ) {
                alt14=1;
            }
            switch (alt14) {
                case 1 :
                    // com/opengamma/financial/expression/deprecated/Expr.g:42:50: ( 'e' | 'E' ) ( '+' | '-' )? ( '0' .. '9' )+
                    {
                    if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // com/opengamma/financial/expression/deprecated/Expr.g:42:60: ( '+' | '-' )?
                    int alt12=2;
                    int LA12_0 = input.LA(1);

                    if ( (LA12_0=='+'||LA12_0=='-') ) {
                        alt12=1;
                    }
                    switch (alt12) {
                        case 1 :
                            // com/opengamma/financial/expression/deprecated/Expr.g:
                            {
                            if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                                input.consume();

                            }
                            else {
                                MismatchedSetException mse = new MismatchedSetException(null,input);
                                recover(mse);
                                throw mse;}


                            }
                            break;

                    }

                    // com/opengamma/financial/expression/deprecated/Expr.g:42:71: ( '0' .. '9' )+
                    int cnt13=0;
                    loop13:
                    do {
                        int alt13=2;
                        int LA13_0 = input.LA(1);

                        if ( ((LA13_0>='0' && LA13_0<='9')) ) {
                            alt13=1;
                        }


                        switch (alt13) {
                    	case 1 :
                    	    // com/opengamma/financial/expression/deprecated/Expr.g:42:72: '0' .. '9'
                    	    {
                    	    matchRange('0','9'); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt13 >= 1 ) break loop13;
                                EarlyExitException eee =
                                    new EarlyExitException(13, input);
                                throw eee;
                        }
                        cnt13++;
                    } while (true);


                    }
                    break;

            }

            // com/opengamma/financial/expression/deprecated/Expr.g:42:86: ( 'f' )?
            int alt15=2;
            int LA15_0 = input.LA(1);

            if ( (LA15_0=='f') ) {
                alt15=1;
            }
            switch (alt15) {
                case 1 :
                    // com/opengamma/financial/expression/deprecated/Expr.g:42:86: 'f'
                    {
                    match('f'); 

                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FLOAT"

    public void mTokens() throws RecognitionException {
        // com/opengamma/financial/expression/deprecated/Expr.g:1:8: ( LT | LTE | GT | GTE | EQ | NEQ | T__21 | T__22 | AND | NOT | OR | TRUE | FALSE | IDENTIFIER | STRING | STRING_IDENTIFIER | WHITESPACE | INTEGER | FLOAT )
        int alt16=19;
        alt16 = dfa16.predict(input);
        switch (alt16) {
            case 1 :
                // com/opengamma/financial/expression/deprecated/Expr.g:1:10: LT
                {
                mLT(); 

                }
                break;
            case 2 :
                // com/opengamma/financial/expression/deprecated/Expr.g:1:13: LTE
                {
                mLTE(); 

                }
                break;
            case 3 :
                // com/opengamma/financial/expression/deprecated/Expr.g:1:17: GT
                {
                mGT(); 

                }
                break;
            case 4 :
                // com/opengamma/financial/expression/deprecated/Expr.g:1:20: GTE
                {
                mGTE(); 

                }
                break;
            case 5 :
                // com/opengamma/financial/expression/deprecated/Expr.g:1:24: EQ
                {
                mEQ(); 

                }
                break;
            case 6 :
                // com/opengamma/financial/expression/deprecated/Expr.g:1:27: NEQ
                {
                mNEQ(); 

                }
                break;
            case 7 :
                // com/opengamma/financial/expression/deprecated/Expr.g:1:31: T__21
                {
                mT__21(); 

                }
                break;
            case 8 :
                // com/opengamma/financial/expression/deprecated/Expr.g:1:37: T__22
                {
                mT__22(); 

                }
                break;
            case 9 :
                // com/opengamma/financial/expression/deprecated/Expr.g:1:43: AND
                {
                mAND(); 

                }
                break;
            case 10 :
                // com/opengamma/financial/expression/deprecated/Expr.g:1:47: NOT
                {
                mNOT(); 

                }
                break;
            case 11 :
                // com/opengamma/financial/expression/deprecated/Expr.g:1:51: OR
                {
                mOR(); 

                }
                break;
            case 12 :
                // com/opengamma/financial/expression/deprecated/Expr.g:1:54: TRUE
                {
                mTRUE(); 

                }
                break;
            case 13 :
                // com/opengamma/financial/expression/deprecated/Expr.g:1:59: FALSE
                {
                mFALSE(); 

                }
                break;
            case 14 :
                // com/opengamma/financial/expression/deprecated/Expr.g:1:65: IDENTIFIER
                {
                mIDENTIFIER(); 

                }
                break;
            case 15 :
                // com/opengamma/financial/expression/deprecated/Expr.g:1:76: STRING
                {
                mSTRING(); 

                }
                break;
            case 16 :
                // com/opengamma/financial/expression/deprecated/Expr.g:1:83: STRING_IDENTIFIER
                {
                mSTRING_IDENTIFIER(); 

                }
                break;
            case 17 :
                // com/opengamma/financial/expression/deprecated/Expr.g:1:101: WHITESPACE
                {
                mWHITESPACE(); 

                }
                break;
            case 18 :
                // com/opengamma/financial/expression/deprecated/Expr.g:1:112: INTEGER
                {
                mINTEGER(); 

                }
                break;
            case 19 :
                // com/opengamma/financial/expression/deprecated/Expr.g:1:120: FLOAT
                {
                mFLOAT(); 

                }
                break;

        }

    }


    protected DFA16 dfa16 = new DFA16(this);
    static final String DFA16_eotS =
        "\1\uffff\1\24\1\26\3\uffff\5\13\5\uffff\1\34\6\uffff\2\13\1\37\2"+
        "\13\1\uffff\1\42\1\43\1\uffff\2\13\2\uffff\1\46\1\13\1\uffff\1\50"+
        "\1\uffff";
    static final String DFA16_eofS =
        "\51\uffff";
    static final String DFA16_minS =
        "\1\11\2\75\3\uffff\1\116\1\117\2\122\1\101\4\uffff\2\56\6\uffff"+
        "\1\104\1\124\1\56\1\125\1\114\1\uffff\2\56\1\uffff\1\105\1\123\2"+
        "\uffff\1\56\1\105\1\uffff\1\56\1\uffff";
    static final String DFA16_maxS =
        "\1\172\1\76\1\75\3\uffff\1\156\1\157\2\162\1\141\4\uffff\2\71\6"+
        "\uffff\1\144\1\164\1\172\1\165\1\154\1\uffff\2\172\1\uffff\1\145"+
        "\1\163\2\uffff\1\172\1\145\1\uffff\1\172\1\uffff";
    static final String DFA16_acceptS =
        "\3\uffff\1\5\1\7\1\10\5\uffff\1\16\1\17\1\20\1\21\2\uffff\1\23\1"+
        "\2\1\6\1\1\1\4\1\3\5\uffff\1\22\2\uffff\1\13\2\uffff\1\11\1\12\2"+
        "\uffff\1\14\1\uffff\1\15";
    static final String DFA16_specialS =
        "\51\uffff}>";
    static final String[] DFA16_transitionS = {
            "\2\16\2\uffff\1\16\22\uffff\1\16\1\uffff\1\14\1\uffff\1\15\3"+
            "\uffff\1\4\1\5\1\uffff\1\17\1\uffff\1\17\1\21\1\uffff\12\20"+
            "\2\uffff\1\1\1\3\1\2\2\uffff\1\6\4\13\1\12\7\13\1\7\1\10\4\13"+
            "\1\11\6\13\4\uffff\1\13\1\uffff\1\6\4\13\1\12\7\13\1\7\1\10"+
            "\4\13\1\11\6\13",
            "\1\22\1\23",
            "\1\25",
            "",
            "",
            "",
            "\1\27\37\uffff\1\27",
            "\1\30\37\uffff\1\30",
            "\1\31\37\uffff\1\31",
            "\1\32\37\uffff\1\32",
            "\1\33\37\uffff\1\33",
            "",
            "",
            "",
            "",
            "\1\21\1\uffff\12\20",
            "\1\21\1\uffff\12\20",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\35\37\uffff\1\35",
            "\1\36\37\uffff\1\36",
            "\1\13\1\uffff\12\13\7\uffff\32\13\4\uffff\1\13\1\uffff\32\13",
            "\1\40\37\uffff\1\40",
            "\1\41\37\uffff\1\41",
            "",
            "\1\13\1\uffff\12\13\7\uffff\32\13\4\uffff\1\13\1\uffff\32\13",
            "\1\13\1\uffff\12\13\7\uffff\32\13\4\uffff\1\13\1\uffff\32\13",
            "",
            "\1\44\37\uffff\1\44",
            "\1\45\37\uffff\1\45",
            "",
            "",
            "\1\13\1\uffff\12\13\7\uffff\32\13\4\uffff\1\13\1\uffff\32\13",
            "\1\47\37\uffff\1\47",
            "",
            "\1\13\1\uffff\12\13\7\uffff\32\13\4\uffff\1\13\1\uffff\32\13",
            ""
    };

    static final short[] DFA16_eot = DFA.unpackEncodedString(DFA16_eotS);
    static final short[] DFA16_eof = DFA.unpackEncodedString(DFA16_eofS);
    static final char[] DFA16_min = DFA.unpackEncodedStringToUnsignedChars(DFA16_minS);
    static final char[] DFA16_max = DFA.unpackEncodedStringToUnsignedChars(DFA16_maxS);
    static final short[] DFA16_accept = DFA.unpackEncodedString(DFA16_acceptS);
    static final short[] DFA16_special = DFA.unpackEncodedString(DFA16_specialS);
    static final short[][] DFA16_transition;

    static {
        int numStates = DFA16_transitionS.length;
        DFA16_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA16_transition[i] = DFA.unpackEncodedString(DFA16_transitionS[i]);
        }
    }

    class DFA16 extends DFA {

        public DFA16(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 16;
            this.eot = DFA16_eot;
            this.eof = DFA16_eof;
            this.min = DFA16_min;
            this.max = DFA16_max;
            this.accept = DFA16_accept;
            this.special = DFA16_special;
            this.transition = DFA16_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( LT | LTE | GT | GTE | EQ | NEQ | T__21 | T__22 | AND | NOT | OR | TRUE | FALSE | IDENTIFIER | STRING | STRING_IDENTIFIER | WHITESPACE | INTEGER | FLOAT );";
        }
    }
 

}