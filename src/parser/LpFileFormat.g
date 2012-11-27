grammar LpFileFormat;

@header { 
    package parser;
    import java.util.HashMap;
    import org.apache.commons.math3.fraction.BigFraction;
    import org.apache.commons.math3.linear.Array2DRowFieldMatrix;
    import org.apache.commons.math3.linear.ArrayFieldVector;
    import java.util.Map.Entry;
    import model.LP;
}
@lexer::header { package parser; }

// Ignore all newlines, spaces and tabs.
WS
    :    (' ' | '\t' | '\n' | '\r' | '\f')+ {$channel = HIDDEN;}
    ;

// Single-line comments begin with \. Ignore all comments.
COMMENT
    :    '\\' (~ ('\r' | '\n'))* {$channel = HIDDEN;}
    ;

////////////////////////// CASE INSENSITIVE STUFF //////////////////////////
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

// Keywords
MAXIMIZE
    :   M A X I M I Z E 
    |   M A X I M U M | M A X 
    ;   

MINIMIZE
    :   M I N I M I Z E 
    |   M I N I M U M | M I N 
    ;   
      

CONSTRAINTSSECTION
    :   S U B J E C T ' ' T O 
    |   S U C H ' ' T H A T 
    |   S T | S '.' T
    ;   

BOUNDSSECTION
    :   B O U N D S?
    ;

END
    :   E N D
    ;

FREE
    :   F R E E
    ;
////////////////////////// END CASE INSENSITIVE STUFF //////////////////////////

////////////////////////// NUMBER STUFF //////////////////////////
// Match string literals of integers, doubles and floats.
// More info: http://docs.oracle.com/javase/specs/
NUMBER
    :   ('0'..'9')* '.' ('0'..'9')+ (('e' | 'E') ('+' | '-')? '0'..'9'+)? ('f' | 'd')?
    |   ('0'..'9')+ '.'                                                   ('f' | 'd')? // Allow "1."
    |   ('0'..'9')+                 (('e' | 'E') ('+' | '-')? '0'..'9'+)? ('f' | 'd')? // Allow "1e1"
    ;

// Basically evaluates a NUMBER with any number of + and - signs.
term returns [BigFraction val]
    :   { boolean positive = true; }
        ('+' | '-' { positive = !positive; })+ NUMBER {
            $val = new BigFraction(Double.valueOf($NUMBER.text));
            if (!positive) {
                $val = $val.negate();
            }
        }
    |   NUMBER {
            $val = new BigFraction(Double.valueOf($NUMBER.text));
        }
    ;
////////////////////////// END NUMBER STUFF //////////////////////////

////////////////////////// VARIABLE STUFF //////////////////////////
// Variable names can begin with these symbols (or letters a-z).
fragment VARSYMBOLBEGIN
    :   '!' | '"' | '#' | '$'
    |   '%' | '&' | '(' | ')'
    |   '\/' | ',' | ';' | '?'
    |   '@' | '_' | '`' | '\''
    |   '{' | '}' | '|' | '~'
    |   'a'..'z' | 'A'..'Z'
    ;

// Variable names can have dots and numbers in them, but they cannot start with them.
fragment VARSYMBOL
    :   VARSYMBOLBEGIN | '.' | '0'..'9'
    ;

VARNAME
    :   VARSYMBOLBEGIN VARSYMBOL*
    ;

// A variable exist of a coefficient and a name. Coefficient is 1 of not present.
var returns [BigFraction coeff, String name]
    :   { $coeff = BigFraction.ONE; }
        (term {$coeff = $term.val;})? VARNAME { $name = $VARNAME.text; }
    |   { $coeff = BigFraction.ONE; }
        ('+' | '-' {$coeff = $coeff.negate(); })+ VARNAME { $name = $VARNAME.text; }
    ;

// Several variables with coefficients. Return an ArrayList of coefficients and a HashMap of the variable names.
////////////////////////// END VARIABLE STUFF //////////////////////////

////////////////////////// LP SPECIFIC STUFF //////////////////////////
// Objective section
lpfile returns [HashMap<String, Integer> varnames, ArrayList<ArrayList<BigFraction>> coeffs, ArrayList<BigFraction> rhs]
    :   {
            $varnames = new HashMap<String, Integer>();
            $coeffs = new ArrayList<ArrayList<BigFraction>>();
            $rhs = new ArrayList<BigFraction>();
            boolean maximize = true;
            int row = 0;
            int col = 0;
            char sense = '<';
            boolean positive = true;
        }
        (MAXIMIZE | MINIMIZE { maximize = false; })
        (VARNAME ':')?
        var1=var {
            $coeffs.add(new ArrayList<BigFraction>());
            $varnames.put($var1.name, col);
            if (maximize) {
                $coeffs.get(row).add($var1.coeff);
            } else {
                $coeffs.get(row).add($var1.coeff.negate());
            }
        }
        (
        ('+' {positive = true;}| '-' {positive = false;})
        var2=var {
            while (col >= $coeffs.get(row).size()) {
                $coeffs.get(row).add(BigFraction.ZERO);
            }
            BigFraction val = (positive) ? $var2.coeff : $var2.coeff.negate();
            if (!maximize) val = val.negate();
            
            if ($varnames.containsKey($var2.name)) {
                int varcol = $varnames.get($var2.name);
                $coeffs.get(row).set(varcol, $coeffs.get(row).get(varcol).add(val));
            } else {
                $varnames.put($var2.name, ++col);
                $coeffs.get(row).add(val);
            }
        }
        )*
        CONSTRAINTSSECTION
        (
        (VARNAME ':')?
        var3=var {
            $coeffs.add(new ArrayList<BigFraction>());
            row++;
            while (col >= $coeffs.get(row).size()) {
                $coeffs.get(row).add(BigFraction.ZERO);
            }
            
            if ($varnames.containsKey($var3.name)) {
                int varcol = $varnames.get($var3.name);
                $coeffs.get(row).set(varcol, $coeffs.get(row).get(varcol).add($var3.coeff));
            } else {
                $varnames.put($var3.name, ++col);
                $coeffs.get(row).add($var3.coeff);
            }
        }
        (
        ('+' {positive = true;}| '-' {positive = false;})
        var4=var {
            BigFraction val = (positive) ? $var4.coeff : $var4.coeff.negate();
            if ($varnames.containsKey($var4.name)) {
                int varcol = $varnames.get($var4.name);
                $coeffs.get(row).set(varcol, $coeffs.get(row).get(varcol).add(val));
            } else {
                $varnames.put($var4.name, ++col);
                $coeffs.get(row).add(val);
            }
        }
        )*
        (
          '<' { sense = '<'; }
        | '<=' { sense = '<'; }
        | '=<' { sense = '<'; }
        | '>' { sense = '>'; }
        | '>=' { sense = '>'; }
        | '=>' { sense = '>'; }
        | '=' { sense = '='; }
        )
        term {
            if (sense == '<') {
                $rhs.add($term.val);
            }
            else if (sense == '=') {
                $coeffs.add(new ArrayList<BigFraction>());
                row++;
                
                for (BigFraction bf : $coeffs.get(row-1)) {
                    $coeffs.get(row).add(bf.negate());
                }
                $rhs.add($term.val);
                $rhs.add($term.val.negate());
            }
            else if (sense == '>') {
                for (int i = 0; i < $coeffs.get(row).size(); i++) {
                    $coeffs.get(row).set(i, $coeffs.get(row).get(i).negate());
                }
                $rhs.add($term.val.negate());
            }
        }
        )+
        END?
    ;

lpfromfile returns [LP lp]
    : lpfile {
        int cols = $lpfile.varnames.size();
        int rows = $lpfile.coeffs.size();
        
        /* Fill in zeroes where values are missing */
        for (int i = 0; i < rows; i++) {
            while ($lpfile.coeffs.get(i).size() < cols) {
                $lpfile.coeffs.get(i).add(BigFraction.ZERO);
            }
        }
        
        BigFraction[] cdata = $lpfile.coeffs.get(0).toArray(new BigFraction[0]);
        BigFraction[] bdata = $lpfile.rhs.toArray(new BigFraction[0]);
        
        BigFraction[][] Ndata = new BigFraction[rows-1][cols-1];
        for (int i = 1; i < rows; i++) {
            Ndata[i-1] = $lpfile.coeffs.get(i).toArray(new BigFraction[0]);
        }
        
        /* Invert the HashMap */
        HashMap<Integer, String> x = new HashMap<Integer, String>();
        for (Entry<String, Integer> entry : $lpfile.varnames.entrySet()) {
            x.put(entry.getValue(), entry.getKey());
        }
        $lp = new LP(new Array2DRowFieldMatrix<BigFraction>(Ndata),
                new ArrayFieldVector<BigFraction>(bdata),
                new ArrayFieldVector<BigFraction>(cdata), x);
    }
    ;
////////////////////////// END LP SPECIFIC STUFF //////////////////////////
