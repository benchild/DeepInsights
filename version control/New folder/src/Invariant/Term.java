package Invariant;
import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Term 
{
	public int x, y, z;
	public String type;
	public Term(int i)
	{
		x = i;
		type = "one";
	}
	
	public Term(int i, int j)
	{
		x = i;
		y = j;
		type = "two";
	}
	
	public Term(int i, String s)
	{
		x = i;
		type = s;
	}
	
	public Term(int i, int j, String s)
	{
		x = i;
		y = j;
		type = s;
	}
	
	public Term(int i, int j, int k, String s)
	{
		x = i;
		y = j;
		z = k;
		type = s;
	}
	
	@Override
	public String toString()
	{
		switch (type) {
		case "rec":
			return "1/"+"A("+x+")";
		case "one":
			return ""+"A("+x+")";
		case "two":
			return "A("+x+")*"+"A("+y+")";
		case "log+":
			return "log("+"A("+x+")"+" + "+"A("+y+"))";
		case "logx":
			return "A("+z+")*log("+"A("+x+")"+" + "+"A("+y+"))";
		case "div":
			return "A("+x+")/"+"A("+y+")";
		default:
			return type+"("+"A("+x+"))";
		}
	}
}
