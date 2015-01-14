package Invariant;

import java.io.*;
import java.util.*;
import Invariant.Invariant;

//unary
//x = a or x >= a or x <= a or x != 0 or x mod a=b or x mod a!=b(only if x mod a takes on every value besides a_
public class constantInv extends Invariant
{
	private int x;
	private int value;
	private int remain = -1;
	private String type;
	
	public constantInv(int i, String s, int a)
	{
		x = i;
		type = s;
		value = a;
	}
	
	public constantInv(int i, int j, String s, int a)
	{
		x = i;
		type = s;
		value = j;
		remain = a;
	}
	
	@Override
	public String toString() 
	{
		if (remain == -1) 
			return "A("+x+")"+" "+type+" "+value;
		else return "A("+x+")"+" mod "+value+" "+type+" "+remain;
	}
	
	public String toPrefix() 
	{
		if (remain == -1) 
			if (type == "!=")
				return "not (= "+"a"+x+" "+value+")";
			else return type+" "+"a"+x+" "+value;
		else if (type == "=")
			return "= "+remain+" (mod "+"a"+x+" "+value+")";
		else
			return "not (= "+remain+" (mod "+"a"+x+" "+value+"))";
	}
}