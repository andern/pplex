/*
 * Copyright (C) 2012 Andreas Halle
 *
 * This file is part of lpped.
 *
 * lpped is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * lpped is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public license
 * along with lpped. If not, see <http://www.gnu.org/licenses/>.
 */
package lpped;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;

class CLI {
	/* Save history for each linear program. */
	private ArrayList<LP> lps = new ArrayList<LP>();

	/* Save command history. */
	private ArrayList<String> log = new ArrayList<String>();

	/* Index pointer for the current linear program. */
	private int p = 0;

	/* Index pointer for undo/redo operations. */
	private int redo = 0;

	/* These commands do operations on an LP. */
	private String[] reqProg = new String[] {
			Data.primal,
			Data.dual,
			Data.pivot,
			Data.solution,
			Data.replace,
			Data.update,
			Data.status
	};



	private boolean requireProgram(String cmd) {
		for (String s : reqProg)
			if (cmd.equals(s))
				return true;
		return false;
	}



	private String parseCmd(String cmd) {
		if (cmd.equals("q") || cmd.equals("exit") || cmd.equals("quit")) System.exit(0);

		String[] args = cmd.split(" ");
		String s = "";

		/* Valid command for the current state? */
		if (p == 0 && requireProgram(args[0])) {
			String f = "Cannot execute command '%s' without a"
					               + " a current linear program.%n";
			return String.format(f, args[0]);
		}

		if      (args[0].equals(Data.read))     { s = parseRead(args); }
		else if (args[0].equals(Data.help))     { s = parseHelp(args); }
		else if (args[0].equals(Data.primal))   { s = parsePrimal(args); }
		else if (args[0].equals(Data.dual))     { s = parseDual(args); }
		else if (args[0].equals(Data.pivot))    { s = parsePivot(args); }
		else if (args[0].equals(Data.solution)) { s = parseSolution(args); }
		else if (args[0].equals(Data.undo))     { s = parseUndo(args); }
		else if (args[0].equals(Data.redo))     { s = parseRedo(args); }
		else if (args[0].equals(Data.replace))  { s = parseReplace(args); }
		else if (args[0].equals(Data.log))      { s = parseLog(args); }
		else if (args[0].equals(Data.update))   { s = parseUpdate(args); }
        else if (args[0].equals(Data.status))   { s = parseStatus(args); }

		else return String.format("Invalid command %s%n", args[0]);

		/* Only log valid commands. */
		log.add(cmd);
		return s;
	}



    private String parseStatus(String[] args) {
        StringBuilder sb = new StringBuilder();
        LP lp = lps.get(p-1);

        if (lp.feasible(false)) sb.append("Primal problem is feasible.\n");
        else sb.append("Primal problem is infeasible.\n");

        if (lp.feasible(true)) sb.append("Dual problem is feasible.\n");
        else sb.append("Dual problem is infeasible.\n");

        if (lp.optimal(false)) sb.append("Primal problem is optimal\n");
        else sb.append("Primal problem is not optimal.\n");

        if (lp.optimal(true)) sb.append("Dual problem is optimal\n");
        else sb.append("Dual problem is not optimal.\n");

        return sb.toString();
    }



	private String parseUpdate(String[] args) {
		LP lp = lps.get(p-1).updateObj();
		lps.add(p, lp);
		p++;
		return lp.toString();
	}



	private String parseLog(String[] args) {
		if (log.size() == 0) return "No command history available.";

		StringBuilder sb = new StringBuilder();

		sb.append("Command history:\n");
		String f = "%d: %s%n";

		/* Show the last 10 commands. */
		int k = log.size()-10;
		if (k < 0) k = 0;
		for (; k < log.size(); k++) {
			sb.append(String.format(f, k, log.get(k)));
		}
		return sb.toString();
	}



	private String parseReplace(String[] args) {
		String syntax = Data.SYNTAX.get(Data.replace);

		LP lp;
		int size = lps.get(p-1).getNoBasic();

		if (args.length == 1){
			lp = lps.get(p-1).phaseOneObj();
		}
		else if (args.length == (size +1 )) {
			double[] coeff = new double[size];

			for (int i = 0; i < size; i++) {
				try {
					coeff[i] = Double.parseDouble(args[i+1]);
				} catch (NumberFormatException err) {
					return Data.SYNTAX.get(Data.replace);
				}
			}
			lp = lps.get(p-1).replaceObj(coeff);
		}
		else {
			return syntax;
		}
		lps.add(p++, lp);
		return lp.toString();
	}



	private String parseRedo(String[] args) {
		if (redo == 0) return "Nothing to redo.\n";

		redo--;
		p++;
		return "";
	}



	private String parseUndo(String[] args) {
		if (p == 0) return "Nothing to undo.";

		p--;
		redo++;
		return "";
	}



	private String parseSolution(String[] args) {
		LP lp = lps.get(p-1);
		double[] point = lp.point();
		double val = lp.objVal();

		int prec = 2;

		try {
			if (args.length == 2) prec = Integer.parseInt(args[1]);
		}
		catch (NumberFormatException e) {
			return Data.SYNTAX.get(Data.solution);
		}

		StringBuilder sb = new StringBuilder("(");

		String f = String.format("%%.%df", prec);
		for (int i = 0; i < point.length-1; i++) {
			sb.append(String.format((f + ", "), point[i]));
		}
		sb.append(String.format(f + ")", point[point.length-1]));

		return String.format(f + "at point %s%n", val, sb.toString());
	}



	private String parsePivot(String[] args) {
		String[] pr = new String[] {"primal"};
		String[] du = new String[] {"dual"};

		/* No args. Primal pivot with largest coefficients rule. */
		if (args.length == 1) {
			try {
				LP lp = lps.get(p-1).pivot(false);
				lps.add(p++, lp);
				redo = 0;
				return parsePrimal(pr);
			}
			catch (RuntimeException err) {
				return err.getLocalizedMessage();

			}
		}

		/* Dual arg. Dual pivot with largest coefficients rule. */
		else if (args.length == 2 && args[1].equals(Data.dual)) {
			try {
				LP lp = lps.get(p-1).pivot(true);
				lps.add(p++, lp);
				redo = 0;
				return parseDual(du);
			} catch (RuntimeException err) {
				return err.getLocalizedMessage();
			}
		}

		/* Pivot args. Primal pivot with given variables. */
		else if (args.length == 3) {
			int e;
			int l;

			try {
				e = Integer.parseInt(args[1]) - 1;
				l = Integer.parseInt(args[2]) - 1;


				int eSize = lps.get(p-1).getNoBasic();
				int lSize = lps.get(p-1).getNoNonBasic();

				if (e < 0 || l < 0 ||
				    e > eSize || l > lSize) return "Invalid index";

				LP lp = lps.get(p-1).pivot(e, l);
				lps.add(p++, lp);
				redo = 0;
				return parsePrimal(pr);
			}
			catch (NumberFormatException err) {
				return Data.SYNTAX.get(Data.pivot);
			}
			catch (RuntimeException err) {
				return err.getLocalizedMessage();
			}
		}

		/* Dual args and pivots. Dual pivot with given variables. */
		else if (args.length == 4) {
			int e;
			int l;

			try {
				e = Integer.parseInt(args[2]) - 1;
				l = Integer.parseInt(args[3]) - 1;

				boolean bDual = args[1].equals(Data.dual);
				if (!bDual) return Data.SYNTAX.get(Data.pivot);

				int eSize = lps.get(p-1).getNoBasic();
				int lSize = lps.get(p-1).getNoNonBasic();

				if (e < 0 || l < 0 ||
				    e > eSize || l > lSize) return "Invalid index";

				LP lp = lps.get(p-1).pivot(l, e);
				lps.add(p++, lp);
				redo = 0;
				return parseDual(du);
			}
			catch (NumberFormatException err) {
				return Data.SYNTAX.get(Data.pivot);
			}
			catch (RuntimeException err) {
				return err.getLocalizedMessage();
			}
		}
		/* Wrong syntax. */
		return Data.SYNTAX.get(Data.pivot);
	}



	private String parsePrimal(String[] args) {
		int prec = 2;
		if (args.length != 1) {
			try { prec = Integer.parseInt(args[1]); }
			catch (NumberFormatException e) { return Data.SYNTAX.get(Data.primal); }
		}

		try { return lps.get(p-1).toString(prec); }
		catch (IllegalArgumentException e) { return e.getLocalizedMessage(); }
	}



	private String parseDual(String[] args) {
		int prec = 2;
		if (args.length != 1) {
			try { prec = Integer.parseInt(args[1]); }
			catch (NumberFormatException e) { return Data.SYNTAX.get(Data.dual); }
		}

		try { return lps.get(p-1).dualToString(prec); }
		catch (IllegalArgumentException e) { return e.getLocalizedMessage(); }
	}



	private String parseRead(String[] args) {
		if (args.length == 2) {
			File file = new File(args[1]);

			try {
				lps.add(p, Parser.parse(file));
				p++;
				redo = 0;
				return "Read " + file + " OK.\n";
			}
			catch (FileNotFoundException e) {
				return "File " + file + " not found.\n";
			}
		}
		else {
			return Data.SYNTAX.get(Data.read);
		}
	}



	private String parseHelp(String[] args) {
		StringBuffer sb = new StringBuffer();

		if (args.length == 1) {
			sb.append("\n");
			Set<String> set = Data.SHELP.keySet();
			for (String s : set) {
				sb.append(String.format("%-20s %s%n", s, Data.SHELP.get(s)));
			}
			sb.append("\n");
			sb.append(Data.EHELP);
			sb.append("\n");
		}
		if (args.length == 2) {
			String h = Data.LHELP.get(args[1]);
			if (h != null) sb.append(h);
			else sb.append("No such command.");
		}
		return sb.toString();
	}



	void run() {
		Scanner s = new Scanner(System.in);
		System.out.println(Data.FWELCOME);
		for (;;) {
			System.out.print("lpped> ");
			String cmd = s.nextLine();
			System.out.println(parseCmd(cmd));
		}
	}
}
