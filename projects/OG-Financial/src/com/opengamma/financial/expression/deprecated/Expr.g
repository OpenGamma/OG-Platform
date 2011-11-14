/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
 
grammar Expr;

options {
  output = AST;
}

tokens {
  LT          = '<';
  LTE         = '<=';
  GT          = '>';
  GTE         = '>=';
  EQ          = '=';
  NEQ         = '<>';
}

@header {
package com.opengamma.financial.expression.deprecated;
//CSOFF
}
@lexer::header {
package com.opengamma.financial.expression.deprecated;
//CSOFF
}

// Case insensitive keywords
AND : ('a'|'A')('n'|'N')('d'|'D') ;
NOT : ('n'|'N')('o'|'O')('t'|'T') ;
OR : ('o'|'O')('r'|'R') ;
TRUE : ('t'|'T')('r'|'R')('u'|'U')('e'|'E') ;
FALSE : ('f'|'F')('a'|'A')('l'|'L')('s'|'S')('e'|'E') ;
IDENTIFIER : ('a'..'z'|'A'..'Z'|'_')('a'..'z'|'A'..'Z'|'0'..'9'|'_'|'.')* ;
STRING : '"' ( options { greedy = false; } : ('\\'.|.) )* '"' ;
STRING_IDENTIFIER : '$"' ( options { greedy = false; } : ('\\'.|.) )* '"' ;
WHITESPACE : (' '|'\t'|'\r'|'\n')+ { skip (); } ;
INTEGER : ('+'|'-')? '0'..'9'+ ;
FLOAT : ('+'|'-')? ('0'..'9')* '.' ('0'..'9')+ ( ('e'|'E') ('+'|'-')? ('0'..'9')+ )? 'f'? ;

variable
  : IDENTIFIER
  | STRING_IDENTIFIER
  ;

literal
  : FALSE
  | FLOAT
  | INTEGER
  | STRING
  | TRUE
  ;

value_expr
  : variable
  | literal
  | '('! expr ')'!
  ;

cmp_expr
  : value_expr ((EQ^ | GT^ | GTE^ | LT^ | LTE^ | NEQ^) cmp_expr)?
  | NOT^ cmp_expr
  ;

and_expr : cmp_expr (AND^ and_expr)? ;

or_expr : and_expr (OR ^or_expr)? ;

expr : or_expr ;

root : expr EOF ;