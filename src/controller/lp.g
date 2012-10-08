lexer grammar lp;

// Put the generated lexer class in this package.
@header { package controller; }

// CASE INSENSITIVITY
fragment A: 'a' | 'A';
fragment B: 'b' | 'B';
fragment C: 'c' | 'C';
fragment D: 'd' | 'D';
fragment E: 'e' | 'E';
fragment F: 'f' | 'F';
fragment G: 'g' | 'G';
fragment H: 'h' | 'H';
fragment I: 'i' | 'I';
fragment J: 'j' | 'J';
fragment K: 'k' | 'K';
fragment L: 'l' | 'L';
fragment M: 'm' | 'M';
fragment N: 'n' | 'N';
fragment O: 'o' | 'O';
fragment P: 'p' | 'P';
fragment Q: 'q' | 'Q';
fragment R: 'r' | 'R';
fragment S: 's' | 'S';
fragment T: 't' | 'T';
fragment U: 'u' | 'U';
fragment V: 'v' | 'V';
fragment W: 'w' | 'W';
fragment X: 'x' | 'X';
fragment Y: 'y' | 'Y';
fragment Z: 'z' | 'Z';

// Send runs of space and tab characters to the hidden channel.
SPACE	:	(' ' | '\t');
WHITESPACE:	SPACE+ { $channel = HIDDEN; };
	
// Treat runs of newline characters as a single NEWLINE token.
// On some platforms, newlines are represented by a \n character.
// On others they are represented by a \r and a \n character.
NEWLINE:	('\r'? '\n')+;

// Single-line comments begin with \, are followed by any characters
// other than those in a newline, and are terminated by newline characters.
SINGLE_COMMENT:		'\\' ~('\r' | '\n')* NEWLINE { skip(); };

fragment NONZERODIGIT:	'1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9';
fragment DIGIT:		'0' | NONZERODIGIT;
fragment SIGN:		'+' | '-';
fragment INTEGER:	'0' | SIGN? '1'..'9' '0'..'9'*;
fragment FLOAT:		INTEGER '.' '0'..'9'+;
fragment NUMBER:	INTEGER | FLOAT;
fragment BNUM:		NUMBER | '+infinity' | '-infinity' | '+inf' | '-inf';
fragment UPPERLETTER:	'A'..'Z';
fragment LOWERLETTER:	'a'..'z';
fragment LETTER:	UPPERLETTER | LOWERLETTER;
fragment ALPHANUM:	LETTER | DIGIT;
fragment VARSYMBOLBEGIN:'!' | '"' | '#' | '$' | '%' | '&' | '(' | ')' | '\/' | ',' | ';' | '?' | '@' | '_' | '`' | '\'' | '{' | '}' | '|' | '~';
fragment VARSYMBOL:	VARSYMBOLBEGIN | '.';
fragment VARALPHANUM:	VARSYMBOL | ALPHANUM;
fragment SENSE:		'<' | '<=' | '=<' | '>' | '>=' | '=>' | '=';

// Section titles
MAXIMIZE:		M A X I M I Z E | M A X I M U M | M A X;
MINIMIZE:		M I N I M I Z E | M I N I M U M | M I N;
SUBJECTTO:		S U B J E C T SPACE T O | S U C H SPACE T H A T | S T | S '.' T;
BOUNDS:			B O U N D S?;
GENERAL:		G E N E R A L S? | G E N;
BINARY:			B I N A R Y | B I N A R I E S | B I N;
END:			E N D;

// LP STUFF
VAR:			(VARSYMBOLBEGIN | LETTER) VARALPHANUM*;
VARS:			SIGN? (VAR SIGN)* VAR;
OBJECTIVE:		(VAR':')? VARS;
CONSTRAINT:		(VAR':')? VARS SENSE NUMBER;
BOUND:			(BNUM '<=' VAR '<=' BNUM) | (BNUM '<' '='? VAR) | (BNUM '>' '='? VAR) | (BNUM '=' VAR) | (VAR '<' '='? BNUM) | (VAR '>' '='? BNUM) | (VAR '=' BNUM);
