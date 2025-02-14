package org.cc.solver.bean;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Node{
	
	private String id="";
	private String fn="";
	private String find="";
	private List<String> args;
	private Set<String> ccpar;
	private Set<String> forbidden_set;
	
	/**
	* Constructs a node with its id and fn fields
	*@param id the node's unique identificator number
	*@param fn the constant or function symbol
	*/
	public Node(String id, String fn){ 
		this.id = id;
		this.fn = fn; 
		this.find=id;
		args=new LinkedList<String>();
		ccpar=new HashSet<String>();
		forbidden_set =new HashSet<String>();
		
	}
	
	public String getId(){
	  	return id;
	}
	public String getFn(){
	  	return fn;
	}
	
	public String getFind(){
		return find;
	}
	public void setFind(String find){
		this.find=find;
	}
	
	public List<String> getArgs(){
		return args;
	}
	public void setArgs(List<String> arguments){
		args=arguments;
	}
	public void addArg(String argument){
		//if(!args.contains(argument))
			args.add(argument);
	}
	
	public Set<String> getCcpars(){
		return ccpar;
	}
	public void setCcpars(Set<String> parents){
		ccpar=parents;
	}
	public void addCcpars(String parent){
		ccpar.add(parent); 
	}
	
	public Set<String> getForbidden_set(){
		return forbidden_set;
	}
	public void setForbidden_set(Set<String> b){
		forbidden_set =b;
	}
	public void addForbidden_set(String b){
		forbidden_set.add(b);
	}

	public String toString(){
	 	return id + "\n\t arguments:\t" + args + "\n\t parents:\t" + ccpar + "\n\t find:\t\t" + find + "\n";
	}
	
}
