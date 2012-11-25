grammar LpFormat;

@header { package parser; }
@lexer::header { package parser; }

// Ignore all newlines and spaces and tabs.
WS			: (' ' | '\t' | '\n' | '\r' | '\f')+ {$channel = HIDDEN;};

// Single-line comments begin with \, are followed by any characters
// other than those in a newline, and are terminated by newline characters.
LINE_COMMENT		: '\\' (~ ('\r' | '\n'))*;

// CASE INSENSITIVITY
fragment A		: 'a' | 'A';
fragment B		: 'b' | 'B';
fragment C		: 'c' | 'C';
fragment D		: 'd' | 'D';
fragment E		: 'e' | 'E';
fragment F		: 'f' | 'F';
fragment G		: 'g' | 'G';
fragment H		: 'h' | 'H';
fragment I		: 'i' | 'I';
fragment J		: 'j' | 'J';
fragment K		: 'k' | 'K';
fragment L		: 'l' | 'L';
fragment M		: 'm' | 'M';
fragment N		: 'n' | 'N';
fragment O		: 'o' | 'O';
fragment P		: 'p' | 'P';
fragment Q		: 'q' | 'Q';
fragment R		: 'r' | 'R';
fragment S		: 's' | 'S';
fragment T		: 't' | 'T';
fragment U		: 'u' | 'U';
fragment V		: 'v' | 'V';
fragment W		: 'w' | 'W';
fragment X		: 'x' | 'X';
fragment Y		: 'y' | 'Y';
fragment Z		: 'z' | 'Z';

// Case insensitive keywords
fragment MAXIMIZE	: M A X I M I Z E | M A X I M U M | M A X;
fragment MINIMIZE	: M I N I M I Z E | M I N I M U M | M I N;
OBJECTIVESECTION	: MAXIMIZE | MINIMIZE;
CONSTRAINTSSECTION	: S U B J E C T ' ' T O | S U C H ' ' T H A T | S T | S '.' T;
BOUNDSSECTION		: B O U N D S?;
FREE			: F R E E;

// VARIABLE
fragment VARSYMBOLBEGIN	: '!' | '"' | '#' | '$' | '%' | '&' | '(' | ')' | '\/' | ',' | ';' | '?' | '@' | '_' | '`' | '\'' | '{' | '}' | '|' | '~';
fragment VARSYMBOL	: VARSYMBOLBEGIN | '.';
fragment VARALPHANUM	: VARSYMBOL | 'a'..'z' | 'A'..'Z' | '0'..'9';
VAR			: (VARSYMBOLBEGIN | 'a'..'z' | 'A'..'Z') VARALPHANUM*;
SIGN			: '+' | '-';
coeffvar		: (NUMBER)? VAR;
linear			: SIGN? coeffvar (SIGN coeffvar)*;

// Number stuff
fragment INTEGER	: '0' | '1'..'9' '0'..'9'*;
fragment FLOAT		: INTEGER '.' '0'..'9'+;
NUMBER			: INTEGER | FLOAT;

// Objective section
objective		: OBJECTIVESECTION (VAR ':')? linear;

// Constraints section
constraint		: (VAR ':')? linear ('<' | '<=' | '=<' | '>' | '>=' | '=>' | '=') NUMBER;
constraints		: CONSTRAINTSSECTION constraint+;

// Bound section
INF			: '+infinity' | '-infinity' | '+inf' | '-inf';
bound			: (VAR '<=' (NUMBER | INF)) | ((NUMBER | INF) '<=' VAR) | ((NUMBER | INF) '<=' VAR '<=' (NUMBER | INF)) | (VAR FREE);
bounds			: BOUNDSSECTION bound+;

lpfile			: objective constraints bounds? ('end' | 'End' | 'ENd' | 'END' | 'enD' | 'eND' | 'EnD' | 'eNd');
