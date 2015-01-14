package Invariant;

import java.io.*;
import java.util.*;
import Invariant.Invariant;

//binary
//x<y, x<=y, x>y, x>=y, x=y, x!=y
public class orderInv extends Invariant
{
	private int x, y;
	private String type;
	
	public orderInv(int i, String s, int j)
	{
		x = i;
		type = s;
		y = j;
	}
	
	@Override
	public String toString() 
	{
		return "A("+x+")"+" "+type+" "+"A("+y+")";
	}
	
	public String toPrefix() 
	{
		if (type == "!=")
			return "not (=  "+"a"+x+" "+"a"+y+")";
		else return type+" "+"a"+x+" "+"a"+y;
	}
}