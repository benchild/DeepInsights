package Invariant;
import java.io.*;
import java.util.*;

public abstract class Invariant
{
	public abstract String toString();
	public abstract String toPrefix();
	public double error = 0;
}