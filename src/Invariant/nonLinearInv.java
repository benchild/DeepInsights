package Invariant;

import java.io.*;
import java.util.*;

import Invariant.Invariant;
import Invariant.Term;

public class nonLinearInv extends Invariant
{
	public LinkedList<Term> list = new LinkedList<Term>();
	double[] par;
	public nonLinearInv(LinkedList<Term> li, double[] res) 
	{
		for (int i=0; i<li.size(); i++)
			list.add(li.get(i));
		par = new double[res.length];
		for (int i=0; i<par.length; i++) par[i] = res[i];
	}
	
	@Override
	public String toString()
	{
		String s = list.get(0).toString() + " = " + String.format("%.2f", par[1]) + "*" + list.get(1).toString();
		for (int i=2; i<par.length; i++)
			s = s + " + " + String.format("%.2f", par[i]) + "*" + list.get(i).toString();
		if (Math.abs(par[0])>0.005) 
			s = s + " + " + String.format("%.2f", par[0]);
		return s;
	}

	@Override
	public String toPrefix() {
		switch (list.get(1).type) {
			case "two": return "= "+"a"+list.get(0).x+" (+ (* "+String.format("%.2f", par[1])+" a"+list.get(1).x+" a"+list.get(1).y+") "+par[0]+")";
			case "rec": return "= "+"a"+list.get(0).x+" (+ (/ "+String.format("%.2f", par[1])+" a"+list.get(1).x+") "+par[0]+")";
			case "div": return "= "+"a"+list.get(0).x+" (+ (/ (* "+String.format("%.2f", par[1])+" a"+list.get(1).x+") a"+list.get(1).y+") "+par[0]+")";
			default: return "";
		}
	}
}