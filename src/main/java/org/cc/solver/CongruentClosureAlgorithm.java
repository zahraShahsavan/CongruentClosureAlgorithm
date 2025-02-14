package org.cc.solver;

import org.cc.solver.bean.Node;
import org.cc.solver.bean.TermPair;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class CongruentClosureAlgorithm{
	/**
	 * Contains the Direct Acyclic Graph as a map of nodes
	 */
	private static Map<String, Node> dag;

	/**
	 * Contains the ID of the termines that are pairwise equals
	 */
	private static Set<TermPair> equalPred;


	/**
	 * Contains the ID of the termines that are atom's arguments
	 */
	private static Set<String> atomPred;

	/**
	 * Contains the ID of the cons node in DAG
	 */
	private static Set<String> consTerm;


	/**
	 * Enum type to select if perform heyuristics or not heuristic
	 * version of that algorithm
	 */
	private static Enum.Heuristics eur;

	/**
	 * Contains the ID of the termines that are pairwise not equals
	 */
	private static Set<TermPair> notEqualPred;


	public static TermPair solver(
			Map<String,Node> Dag, Set<TermPair> EqualPred,
			Set<TermPair> NotEqualPred, Set<String> AtomPred,
			Set<String> ConsTerm, Enum.Heuristics e) throws Exception{

		dag=Dag;
		equalPred=EqualPred;
		notEqualPred=NotEqualPred;
		atomPred=AtomPred;
		consTerm=ConsTerm;
		eur=e;
		switch (eur){
			case ENABLE:
				return solver_h();
			case DISABLE:
				return solver_();
			default:
				return solver_();
		}
	}

	private static TermPair solver_() throws Exception{
		// Step 2:
		Node nTemp;
		for(String s: consTerm){
			nTemp=new Node("car(" + s + ")", "car");
			nTemp.addArg(s);
			node(s).addCcpars(nTemp.getId());
			dag.put(nTemp.getId(), nTemp);
			//System.out.println("\t\t\t" + nTemp.getId() + " = " + node(s).getArgs().get(0));
			merge(nTemp.getId(), node(s).getArgs().get(0));

			nTemp=new Node("cdr(" + s + ")", "cdr");
			nTemp.addArg(s);
			node(s).addCcpars(nTemp.getId());
			dag.put(nTemp.getId(), nTemp);
			//System.out.println("\t\t\t" + nTemp.getId() + " = " + node(s).getArgs().get(1));
			merge(nTemp.getId(), node(s).getArgs().get(1));
		}
		// Sep 3
		float step=100/((float) equalPred.size()),
				count=0;
		int perc=0;
		for(TermPair tp: equalPred){
			count+=step;
			for(;count>=perc && perc<=100; perc++)
				System.out.print("\rExecuting Congruent Closure Algorithm\t"
						+ perc + "%");

			merge(tp.getFirst(),tp.getSecond());
		}
		System.out.println("\rExecuting Congruent Closure Algorithm\t100%");
		// Step 4
		for(TermPair tp: notEqualPred){
			if(find(tp.getFirst()).equals(find(tp.getSecond())))
				return tp;
		}
		// Step 5
		for(String s1: atomPred){
			for(String s2: consTerm)
				if(find(s1).equals(find(s2)))
					return new TermPair("atom(" + s1 + ")", s2);
		}
		// Step 6: satisfiable
		return null;
	}
	private static TermPair solver_h() throws Exception{
		System.out.print("\rExecuting Congruent Closure Algorithm\t0%");
		// atom axiom: a consTerm cannot be the argument of an atom predicate
		for(String id: atomPred)
			if(consTerm.contains(id)){
				System.out.println("\rExecuting Congruent Closure Algorithm\t0%");
				return new TermPair("atom(" + id + ")", id);
			}
		// an atom term cannot be in the same congruent class of a term term
		for(String s: atomPred)
			node(s).getForbidden_set().addAll(consTerm);
		// a cons term cannot be in the same congruent class of an atom term
		for(String s: consTerm)
			node(s).getForbidden_set().addAll(atomPred);

		// Step 2: car/cdr projection axioms
		Node nTemp;
		TermPair ct=null;		// the two terms that are in conflict
		for(String s: consTerm){
			nTemp=new Node("car(" + s + ")", "car");
			nTemp.addArg(s);
			node(s).addCcpars(nTemp.getId());
			dag.put(nTemp.getId(), nTemp);
			if((ct=merge(nTemp.getId(), node(s).getArgs().get(0))) != null){
				System.out.println("\rExecuting Congruent Closure Algorithm\t0%");
				return ct;
			}
			nTemp=new Node("cdr(" + s + ")", "cdr");
			nTemp.addArg(s);
			node(s).addCcpars(nTemp.getId());
			dag.put(nTemp.getId(), nTemp);
			//System.out.println("\t\t\t" + nTemp.getId() + " = " + node(s).getArgs().get(1));
			if((ct=merge(nTemp.getId(), node(s).getArgs().get(1))) != null){
				System.out.println("\rExecuting Congruent Closure Algorithm\t0%");
				return ct;
			}
		}

		// Sep 3: the equality terms must be in the same congruent class
		float step=100/((float)equalPred.size()),
				count=0;
		int perc=0;
		for(TermPair tp: equalPred){
			count+=step;
			for(;count>=perc && perc<=100; perc++)
				System.out.print("\rExecuting Congruent Closure Algorithm\t"
						+ perc + "%"); // "\r" --> backspace
			if((ct=merge(tp.getFirst(),tp.getSecond()))!=null){
				System.out.println();
				return ct;
			}
		}
		System.out.println("\rExecuting Congruent Closure Algorithm\t100%");
		// Step 6: satisfiable
		return null;
	}


	private static TermPair merge(String id1, String id2) throws Exception{
		switch (eur){
			case ENABLE:
				return merge_h(id1, id2);
			case DISABLE:
				merge_(id1, id2);
				return null;
			default:
				merge_(id1, id2);
				return null;
		}
	}

	private static void merge_(String id1, String id2) throws Exception{
		String rId1=find(id1);
		String rId2=find(id2);
		if(!rId1.equals(rId2)){
			Object[] p1= ccpar(rId1).toArray();
			Object[] p2= ccpar(rId2).toArray();
			union(rId1, rId2);
			String t1, t2;
			for(int i=0;i<p1.length;i++){
				t1=(String) p1[i];
				for(int j=0;j<p2.length;j++){
					t2=(String) p2[j];
					if(!find(t1).equals(find(t2)) && congruent(t1,t2))
						merge_(t1,t2);
				}
			}
		}
	}

	private static TermPair merge_h(String id1, String id2) throws Exception{
		if(!find(id1).equals(find(id2))){

			if(node(find(id1)).getForbidden_set().contains(node(find(id2)).getFind())
					|| node(find(id2)).getForbidden_set().contains(node(find(id1)).getFind()) )
				return new TermPair(id1,id2);


			Object[] p1= ccpar(id1).toArray();
			Object[] p2= ccpar(id2).toArray();


			union(id1, id2);
			String t1, t2;
			TermPair conflictTerms;
			for(int i=0;i<p1.length;i++){
				t1=(String) p1[i];
				for(int j=0;j<p2.length;j++){
					t2=(String) p2[j];
					if(!find(t1).equals(find(t2)) && congruent(t1,t2))
						if((conflictTerms=merge(t1,t2))!=null)
							return conflictTerms;
				}
			}
		}
		return null;
	}


	private static boolean congruent(String id1, String id2) throws Exception{
		// equals between the two arguments' lists
		Node n1=node(id1);
		Node n2=node(id2);
		if(n1.getFn().equals(n2.getFn())){
			List<String> arg1=n1.getArgs();
			List<String> arg2=n2.getArgs();
			if(arg1.size()==arg2.size()){
				for(int i=0;i<arg1.size();i++)
					if(!find(arg1.get(i)).equals(find(arg2.get(i))))
						return false;
				return true;
			}
		}
		return false;
	}


	private static Node node(String id) throws Exception{
		Node n=dag.get(id);
		if(n==null)
			throw new NullPointerException("Does not exist any node with ID: " + id);
		return n;
	}

	private static Set<String> ccpar(String id) throws Exception{
		return node(find(id)).getCcpars();
	}

	private static String find(String id) throws Exception{
		switch (eur){
			case ENABLE:
				return find_h(id);
			case DISABLE:
				return find_(id);
			default:
				return find_(id);
		}
	}

	private static String find_(String id) throws Exception{
		Node n=node(id);
		return (id.equals(n.getFind()))? id : find_(n.getFind());
	}
	private static String find_h(String id) throws Exception{
		Node n=node(id);
		return n.getFind();
	}


	private static void union(String id1, String id2) throws Exception{
		switch (eur){
			case ENABLE:
				union_h(id1, id2);
				break;
			case DISABLE:
				union_(id1, id2);
				break;
			default:
				union_(id1, id2);
				break;
		}
	}

	private static void union_h(String id1, String id2) throws Exception{
		Node n1=node(find(id1));
		Node n2=node(find(id2));


		if ( n1.getCcpars().size() > n2.getCcpars().size()){
			for ( String id : dag.keySet() ){
				if ( dag.get(id).getFind() == n2.getFind() ){
					n2.setFind(n1.getFind());
				}
			}

			n1.setCcpars(Stream.concat(n1.getCcpars().stream(), n2.getCcpars().stream())
					.collect(Collectors.toSet()));
			n2.setCcpars(new HashSet<>());

			n1.setForbidden_set(Stream.concat(n1.getForbidden_set().stream(), n2.getForbidden_set().stream())
					.collect(Collectors.toSet()));
			n2.setForbidden_set(new HashSet<>());

		} else {
			for ( String id : dag.keySet() ){
				if ( dag.get(id).getFind() == n1.getFind() ){
					n2.setFind(n2.getFind());
				}
			}
			n2.setCcpars(Stream.concat(n1.getCcpars().stream(), n2.getCcpars().stream())
					.collect(Collectors.toSet()));
			n1.setCcpars(new HashSet<>());
			n2.setForbidden_set(Stream.concat(n1.getForbidden_set().stream(), n2.getForbidden_set().stream())
					.collect(Collectors.toSet()));
			n1.setForbidden_set(new HashSet<>());

		}

		n2.setFind(n1.getFind());
		n1.getCcpars().addAll(n2.getCcpars());
		n2.setCcpars(new HashSet<String>());
	}

	private static void union_(String id1, String id2) throws Exception{
		Node n1=node(find(id1));
		Node n2=node(find(id2));
		n2.setFind(n1.getFind());
		n1.getCcpars().addAll(n2.getCcpars());
		n2.setCcpars(new HashSet<String>());

	}


}