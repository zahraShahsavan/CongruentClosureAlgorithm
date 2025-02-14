package org.cc.solver;

import org.cc.solver.parser.Parser;
import org.cc.solver.bean.CCobject;
import org.cc.solver.bean.TermPair;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class Main {
	public static void main(String[] args) throws Exception {
		String input = "formulas";
		Enum.Heuristics eur = Enum.Heuristics.DISABLE;

		// Load properties from app.properties
		Properties properties = new Properties();
		try (FileInputStream fis = new FileInputStream("application.properties")) {
			properties.load(fis);
			String heuristicsValue = properties.getProperty("heuristics", "DISABLE").toUpperCase();
			eur = Enum.Heuristics.valueOf(heuristicsValue);
		} catch (IOException | IllegalArgumentException e) {
			log("Error loading heuristics from properties file, using default: DISABLE");
		}

		log("Heuristic is " + eur);

		try (BufferedReader in = new BufferedReader(new FileReader(input))) {
			log("Input interpreted as path of a file with the formula inside.");
			input = "";
			String s;
			while ((s = in.readLine()) != null) input += s;
			if (input.length() <= 1500)
				log("FORMULA:\n" + input);
			else
				log("The formula is displayed only if it is less than 1500 characters");
		} catch (FileNotFoundException e) {
			log("Can not open file. Maybe path is wrong or file does not exist.");
			log("Try to interpret the input string as a formula.");
		} catch (IOException e) {
			throw new IOException("Failed to open the file");
		}

		CCobject ccObj = new CCobject();
		long parserUser = 0, algoUser = 0, startUser = 0, start = 0, parserTime = 0, algoTime = 0;

		try {
			log("Parsing...");
			start = System.currentTimeMillis();
			startUser = getUserTime();
			Parser.parsing(input, ccObj);
			parserUser = TimeUnit.NANOSECONDS.toMillis(getUserTime() - startUser);
			parserTime = System.currentTimeMillis() - start;
		} catch (Throwable e) {
			log(e.getMessage());
		}

		if (ccObj.dag != null && ccObj.dag.size() > 0) {
			log("Created a DAG with " + ccObj.dag.size() + " nodes and " + ccObj.numEdges() + " edges in " +
					String.format("%.3f", (parserTime / 1000d)) + "s");
			log("Executing Congruent Closure Algorithm");

			start = System.currentTimeMillis();
			startUser = getUserTime();
			TermPair term = CongruentClosureAlgorithm.solver(ccObj.dag, ccObj.equalTerm, ccObj.notEqualTerm,
					ccObj.atomTerm, ccObj.consTerm, eur);
			algoUser = TimeUnit.NANOSECONDS.toMillis(getUserTime() - startUser);
			algoTime = System.currentTimeMillis() - start;

			if (term == null)
				log("SATISFIABLE");
			else {
				log("UNSATISFIABLE");
				log("First conflict is between these terms and/or predicates:\n\t" + term.getFirst() + "\n\t" + term.getSecond());
			}

			log("Time for parsing: " + formatTime(parserTime, parserUser));
			log("Time for CCAlgorithm: " + formatTime(algoTime, algoUser));
			log("Total time: " + formatTime(parserTime + algoTime, parserUser + algoUser));
		}
	}

	private static long getUserTime() {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		return bean.isCurrentThreadCpuTimeSupported() ? bean.getCurrentThreadUserTime() : 0L;
	}

	private static void log(String message) {
		System.out.println(message);
		try (FileWriter fw = new FileWriter("output.txt", true);
			 BufferedWriter bw = new BufferedWriter(fw);
			 PrintWriter out = new PrintWriter(bw)) {
			out.println(message);
		} catch (IOException e) {
			System.err.println("Error writing to output file: " + e.getMessage());
		}
	}

	private static String formatTime(long clockTime, long cpuTime) {
		return "Clock time: " + (clockTime / 1000) + "s, CPU time: " + (cpuTime / 1000) + "ms";
	}
}
