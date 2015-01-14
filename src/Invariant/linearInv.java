package Invariant;

import java.io.*;
import java.util.*;
import Invariant.Invariant;

//binary
//ay=bx+c
//
public class linearInv extends Invariant
{
	private int x, y, z;
	private double a, b, c, d;
	int[] var;
	double[] par;
	String type = "";
	public int hash = 0;
	
	public linearInv(int inx, int iny, Double ina, Double inb, Double inc)
	{
		type = "binary";
		x = inx;
		y = iny;
		a = ina; 
		b = inb;
		c = inc;
		double com = gcd(gcd(a, b), c);
		if (Math.abs(com)<0.00001) return;
		a = a / com;
		b = b / com;
		c = c / com;
	}
	
	public linearInv(int inx, int iny, int inz, double ina, double inb, double inc, double ind)
	{
		type = "tenary";
		x = inx;
		y = iny;
		z = inz;
		a = ina; 
		b = inb;
		c = inc;
		d = ind; 
		double com = gcd(gcd(gcd(a, b), c), d);
		if (Math.abs(com)<0.00001) return;
		a = a / com;
		b = b / com;
		c = c / com;
		d = d / com;
	}
	/*
	public linearInv(int[] e, int[] res) 
	{
		type = "function";
		par = new int[res.length];
		var = new int[e.length];
		for (int i=0; i<par.length; i++) par[i] = res[i];
		for (int i=0; i<var.length; i++) var[i] = e[i];
	}*/
	
	public linearInv(int[] e, double[] res, String ty, int h) 
	{
		hash = h;
		type = ty;
		par = new double[res.length];
		var = new int[e.length];
		for (int i=0; i<par.length; i++) par[i] = res[i];
		for (int i=0; i<var.length; i++) var[i] = e[i];
	}
	
	@Override
	public String toString() 
	{
		//String s = hash+" ";
		String s = "";
		if (type == "binary") {
			if (a == 1) s = s+"A("+x+")";
			else s = s+a+"A("+x+")";
			s = s+" = ";
			if (b == 1) s = s+"A("+y+")";
			else s = s+b+"A("+y+")";
			if (c > 0) s = s+" + "+c;
			else if (c < 0) s = s+" - "+(-c);
			return s;
		} else if (type == "tenary") {
			if (a == 0) s = "";
			else if (a > 0)
				if (a == 1) s = s+ "A("+x+")";
				else s = s+a+"A("+x+")";
			else 
				if (a == -1) s = s+ "-"+"A("+x+")";
				else s = s+a+"A("+x+")";
			s = s+simplify(b, y)+simplify(c, z);
			s = s+" = "+d;
			return s;
		} else if (type == "function"){
			if (par[0] == 0) s = "";
			else if (par[0] > 0)
				if (par[0] == 1) s = s+ "A("+var[0]+")";
				else s = s+ par[0] +"A("+var[0]+")";
			else 
				if (par[0] == -1) s = s+ "-"+"A("+var[0]+")";
				else s = s+ par[0] +"A("+var[0]+")";
			for (int i=1; i<par.length-1; i++)
				s = s+simplify(par[i], var[i]); 
			s = s+" = "+par[par.length-1]; 
			return s;
		} else {
			s = s+"A("+var[0]+")"+" = ";
			for (int i=1; i<par.length; i++)
				s = s+simplify(par[i], var[i]);
			if (Math.abs(par[0]) < 0.005) ;
			else if (par[0] > 0.00001)
				s+= " + "+String.format("%.2f", par[0]);
			else s+= " - "+String.format("%.2f", -par[0]); 
			if (s.contains("=  +"))
				s = s.substring(0, s.indexOf("=  +")+1)+s.substring(s.indexOf("=  +")+4);
			return s;
		}
	}
	
	public String simplify(int a, int b) 
	{
		if (a == 0) return "";
		if (a > 0)
			if (a == 1) return " + "+"A("+b+")";
			else return " + "+a+"A("+b+")";
		else 
			if (a == -1) return " - "+"A("+b+")";
			else return " - "+(-a)+"A("+b+")"; 
	}
	
	public String simplify(double a, int b) 
	{
		if (Math.abs(a) < 0.005) return "";
		if (a > 0.00001)
			if (Math.abs(a-1) < 0.005) return " + "+"A("+b+")";
			else return " + "+String.format("%.2f", a)+"A("+b+")";
		else 
			if (Math.abs(a+1) < 0.005) return " - "+"A("+b+")";
			else return " - "+String.format("%.2f", -a)+"A("+b+")"; 
	}
	
	public String toPrefix() 
	{
		String s = "";
		if (type == "binary") {
			s = "= (* "+a+" "+"a"+x+" ) (+ (* "+b+" "+"a"+y+") "+c+")";
			return s;
		} else if (type == "tenary")
		{
			s = "= "+d+" (+ (* "+a+" "+"a"+x+") (* "+b+" "+"a"+y+") (* "+c+" "+"a"+z+"))";
			return s;
		} else if (type == "function"){
			s = "= "+par[par.length-1]+" (+";
			for (int i=0; i<par.length-1; i++)
				s = s+"(* "+par[i]+" "+"a"+var[i]+")";
			s = s+")";
			return s;
		} else {
			s = "= "+"a"+var[0]+" (+";
			for (int i=1; i<par.length; i++)
				s = s+"(* "+String.format("%.2f", par[i])+" "+"a"+var[i]+") ";
			s = s+par[0]+")";
			return s;
			
			//return "";
		}
	}
	
	public double gcd(double a, double b)
	{
		if (Math.abs(b)<0.00001) return a;
		else return gcd(b, a % b);
	}
}