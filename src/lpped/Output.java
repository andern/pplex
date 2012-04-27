package lpped;

public final class Output {
	/**
	 * Return a nicely formatted {@code String} that represents the
	 * matrix-vector product of this {@code Matrix} and the given
	 * vector given as an {@code Array}.
	 *
	 * @param  x
	 *         A vector as an {@code array} of {@code Strings}.
	 * @param  precision
	 *         Limit each double precision number to this many decimals.
	 *         Give a negative value to automatically set precision.
	 * @return
	 *         A nicely formatted {@code String}.
	 */
	public static String toString(Matrix A, String[] x, int precision) {
		StringBuilder sb = new StringBuilder();
		String[][] terms = niceTerms(A, x, precision);

		for (int i = 0; i < A.rows(); i++) {
			if (i != 0) {
				sb.append("\n");
			}
			sb.append(exprToString(terms[i], x, precision));
		}
		return sb.toString();
	}



	/*
	 * Return an array containing the length
	 * of the longest string in each column.
	 */
	private static int[] colSizes(String[][] elements) {
	    int[] colSizes = new int[elements[0].length];
	    for (int j = 0; j < elements[0].length; j++) {
	        int max = 1;
	        for (int i = 0; i < elements.length; i++) {
	            int len = elements[i][j].length();

	            if (len > max) {
	                max = len;
	            }
	        }
	        colSizes[j] = max;
	    }
	    return colSizes;
	}



	/*
	 * Nicely format each term in an expression.
	 */
	private static String[] expr(Matrix row, String[] el, int[] cols, boolean fcol) {
		String[] exprs = new String[el.length];

	    for (int j = 0; j < el.length; j++) {
	    	char sign = signify(row.get(0, j), j);

	    	String f = String.format("%%c %%%ds", cols[j]);
	    	if (j == 0 && fcol) {
	    		f = String.format("%%%ds", cols[j]);
	    		exprs[j] = String.format(f, el[j]);
	    	} else {
	    		exprs[j] = String.format(f, sign, el[j]);
	    	}
	    }
	    return exprs;
	}



	/*
	 * Return a nicely formatted String of an expression.
	 */
	private static String exprToString(String[] row, String[] x, int precision) {
		StringBuilder sb = new StringBuilder();
		for (int j = 0; j < row.length; j++) {
			if (j != 0) {
				sb.append(" ");
			}
			sb.append(row[j]);
		}
		return sb.toString();
	}



	/*
	 * Nicely format all the terms in the matrix-vector product.
	 */
	private static String[][] niceTerms(Matrix A, String[] x, int precision) {
		int m = A.rows();
		int n = A.cols();

		String[][] terms = terms(A, x, precision);
		int[] cols = colSizes(terms); // Find longest element in each column.
		boolean fcol = A.getCol(0).gte(0); // First column all positive?

		String[][] niceTerms = new String[m][n];
		for (int i = 0; i < m; i++) {
			niceTerms[i] = expr(A.getRow(i), terms[i], cols, fcol);
		}
		return niceTerms;
	}



	/*
     * Return the sign to put in front of a term.
     */
    private static char signify(double coeff, int column) {
        char sign = ' ';
        if (coeff > 0 && column != 0) {
            sign = '+';
        } else if (coeff < 0) {
            sign = '-';
        }
        return sign;
    }



    /*
     * Format a term as nicely as possible.
     */
    private static String term(double coeff, String var, int precision) {
        if (coeff == 0.0) {
            return "";
        } else if (coeff == 1.0 && !var.trim().equals("")) {
            return var;
        }

        String f;
        if (precision < 0) {
            f = "%s%s"; // Automatically set precision.
        } else {
            f = String.format("%%.%df%%s", precision);
        }

        return String.format(f, coeff, var);
    }



	/*
	 * Calculate the term of each element
	 * in the matrix-vector-product.
	 */
	private static String[][] terms(Matrix A, String[] x, int precision) {
		int m = A.rows();
		int n = A.cols();
	    String[][] elements = new String[m][n];

	    for (int j = 0; j < n; j++) {
	        for (int i = 0; i < m; i++) {
	            double coeff = Math.abs(A.get(i, j));
	            String element = term(coeff, x[j], precision);

	            elements[i][j] = element;
	        }
	    }
	    return elements;
	}
}
