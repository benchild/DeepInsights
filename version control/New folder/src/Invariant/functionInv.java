package Invariant;

import java.io.*;
import java.util.*;

import Invariant.Invariant;

//binary
//x = abs(y), x = neg(y), x mod y = remain, x*y=remain
//x mod y = z
public class functionInv extends Invariant
{
	private int x, y, z;
	private double remain;
	private String type;
	private String label = "";
	
	public functionInv(int i, String s, int j)
	{
		label = "binary";
		x = i;
		type = s;
		y = j;
	}
	
	public functionInv(int i, String s, int j, double k)
	{
		label = "binary_const";
		x = i;
		type = s;
		y = j;
		remain = k;
	}
	
	public functionInv(int i, int j, String s, int k)
	{
		label = "tenary";
		x = i;
		type = s;
		y = j;
		z = k;
	}
	
	@Override
	public String toString() 
	{
		if (label == "binary")
			return "A("+x+")"+" = "+type+"("+"A("+y+")"+")";
		else if (label == "binary_const") 
			return "A("+x+")"+" "+type+" "+"A("+y+")"+" = "+String.format("%.2f", remain);
		else if (type == "max" || type == "min")
			return "A("+x+")"+" = "+type+"{"+"A("+y+")"+" , "+"A("+z+")"+"}";
		else 
			return "A("+x+")"+" = "+"A("+y+")"+" "+type+" "+"A("+z+")";
	}
	
	public String toPrefix() 
	{
		if (label == "binary")
			if (type == "abs")
				return "= "+"a"+x+" (abs "+"a"+y+")";
			else return "= "+"a"+x+" -"+"a"+y;
		else if (label == "binary_const") 
			if (type == "mod")
				return "= "+remain+" (mod "+"a"+x+" "+"a"+y+")";
			else return "= "+remain+" (* "+"a"+x+" "+"a"+y+")";
		else if (type == "max" || type == "min")
			return "";
		else 
			return "= "+"a"+x+" ("+type+" "+"a"+y+" "+"a"+z+")";
	}
}