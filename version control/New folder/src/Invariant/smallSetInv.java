package Invariant;

import java.io.*;
import java.util.*;

import Invariant.Invariant;

//unary
//x is in a small set {a, b} or {a,b,c}
public class smallSetInv extends Invariant
{
	private int x;
	private Double val_a, val_b, val_c;
	private boolean isTwoElements = false;
	public smallSetInv(int i, Double a, Double b)
	{
		x = i;
		val_a = a;
		val_b = b;
		isTwoElements = true;
	}
	
	public smallSetInv(int i, Double a, Double b, Double c)
	{
		x = i;
		val_a = a;
		val_b = b;
		val_c = c;
	}
	
	@Override
	public String toString() 
	{
		if (isTwoElements)
			return "A("+x+")"+" in {"+String.format("%.2f", val_a)+","+String.format("%.2f", val_b)+"}";
			else 
			return "A("+x+")"+" in {"+String.format("%.2f", val_a)+","+String.format("%.2f", val_b)+","+String.format("%.2f", val_c)+"}";
	}
	
	public String toPrefix() 
	{
		return "";
	}
}