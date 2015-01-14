import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Collections;

import com.microsoft.z3.*;

import Invariant.*;

public class DeepInsights
{
	static int maxLineNum = 1000, maxVarNum = 100, maxAnswer = 10000;
	static double maximum = 1000000.0, minimum = -1000000.0;
	static double alpha = 0.04;
	static double limit = 0.000001;
	static double confidence = 1.0;
	static int varNum = 0;
	static int lineNum = 0;
	static double[][] map = new double[maxVarNum][maxLineNum];
	static double[] min = new double[maxVarNum], max = new double[maxVarNum], stu = new double[maxLineNum];
	static boolean[] use = new boolean[maxAnswer], pos = new boolean[maxVarNum];
	static int maxn = 999983;
	static int[] ha = new int[maxVarNum];
	static boolean[] hash = new boolean[999983];
	static List<Invariant> result = new ArrayList<Invariant>();
	static double[][] alp = new double[maxAnswer][maxVarNum];
	static int num_a = 0;
	static double[][] pearson = new double[maxVarNum][maxVarNum], spearman = new double[maxVarNum][maxVarNum], kendall = new double[maxVarNum][maxVarNum];
	
	static int currentNum = 2;
	public static class InvariantCompare implements Comparator 
	{
		@Override
		public int compare(Object a, Object b) 
		{
			if (((Invariant)a).error > ((Invariant)b).error) return 1;
			else return -1;
		}
	}
	
	public static void main(String[] args) throws IOException, Z3Exception, InterruptedException
	{
		String input = "input"+currentNum+".txt";
		BufferedReader reader = new BufferedReader(new FileReader(input));
		//only int now
		varNum = Integer.parseInt(reader.readLine());
		lineNum = Integer.parseInt(reader.readLine());       
		
		for (int i=0; i<lineNum; i++) {
			String[] s = reader.readLine().split(" ");
			for (int j=0; j<varNum; j++) {
				if (s[j].charAt(0) == '-') map[j][i] = -Double.parseDouble(s[j].substring(1));
				else map[j][i] = Double.parseDouble(s[j]); 
			}
		}
		reader.close();
		
		long startTime=System.currentTimeMillis();   //获取开始时间
		Random random = new Random(startTime);
		for (int i=0; i<maxVarNum; i++)
			ha[i] = Math.abs(random.nextInt()) % maxn;
		
		preprocess();
		enumerate();

		Collections.sort(result, new InvariantCompare());
		for (int i=0; i<result.size(); i++) use[i] = true;
		
		//test_pair();
		String output_all = "output_all"+currentNum+".txt";
		BufferedWriter writer = new BufferedWriter(new FileWriter(output_all));
		for (int i=0; i<result.size(); i++)
		if (use[i] && result.get(i).toPrefix() != "")
		{
			writer.write(result.get(i).toString()+'\n');
			test(i);
			String s = runCMD();
			if (s.charAt(0) == 'u' && s.charAt(2) == 's') {
				use[i] = false;
				System.out.println("unsat");
			} else System.out.println("sat");
		}
		writer.close();
		
		long endTime=System.currentTimeMillis(); //获取结束时间
		print(endTime-startTime);
	}
	                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
	public static String runCMD() {
		List<String> command = new ArrayList<String>();
		command.add("z3");
		command.add("input.smt2");
		ProcessBuilder pb=new ProcessBuilder(command);
		pb.redirectErrorStream(true);
		try {
			Process process=pb.start();
			BufferedReader inStreamReader = new BufferedReader(
				 new InputStreamReader(process.getInputStream())); 
			return inStreamReader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void preprocess()
	{
		for (int i=0; i<varNum; i++)
		{
		
			min[i] = maximum;
			max[i] = minimum;
			for (int j=0; j<lineNum; j++)
			{
				if (map[i][j]<min[i]) min[i] = map[i][j];
				if (map[i][j]>max[i]) max[i] = map[i][j];
			}
		}
	}     
	
	static int[] e;
	public static void enumerate() 
	{
		//e = new int[6];
		//if (varNum>=6) search(0, 0); 
		//e = new int[5];
		//if (varNum>=5) search(0, 0);
		e = new int[4];
		if (varNum>=4) search(0, 0);
		e = new int[3];
		if (varNum>=3) search(0, 0);
		e = new int[2];
		if (varNum>=2) search(0, 0);
		
		//unary
		for (int i=0; i<varNum; i++)
		{
			checkConstantInv(i);
			checkSmallSetInv(i);
		}
		//binary
		for (int i=0; i<varNum; i++)
			for (int j=i+1; j<varNum; j++)
			{
				//checkLinearInv(i, j);
				checkOrderInv(i, j);
				checkFunctionInv(i, j);
			}
		//ternary
		for (int i=0; i<varNum; i++)
			for (int j=i+1; j<varNum; j++)
				for (int k=j+1; k<varNum; k++)
				{
					checkFunctionInv(i, j, k);
					checkFunctionInv(j, i, k);
					checkFunctionInv(k, i, j);
				}
			
		e = new int[10];
		regression(0, 0);
		search_nonlinear(0, 0);
	}
	
	public static int num = 0;
	public static void search(int i, int pos)
	{
		if (i>=e.length) {
			System.out.println("Linear:");
			num++;
			//for (int j=0; j<e.length; j++) System.out.print(e[j]+" ");
			System.out.println(num);
			checkLinearInv(e);
			return;
		}
		for (int j=pos; j<varNum; j++)
		{
			e[i] = j;
			search(i+1, j+1);
		}
	}
	
	static LinkedList<Term> list = new LinkedList<Term>();
	
	static int unique = 0;
	public static void search_nonlinear(int i, int pos)
	{
		if (i>1) checkNonlinearInv(i);
		num++;
		System.out.println(num+" "+i+" "+pos);
		for (int j=0; j<list.size(); j++) System.out.print(list.get(j).toString()+" ");;
		System.out.println();
		
		//now the search range is 4： total variable number
		if (i>1) return;

		if (i == 0) 
			for (int j=0; j<varNum; j++) 
			{
				list.add(new Term(j));
				unique = j;
				search_nonlinear(i+1, 0);
				list.remove(i);
			}
		else
		for (int j=pos; j<varNum; j++)   
		if (j != unique)
		{
			//list.add(new Term(j));
			//search_nonlinear(i+1, j+1);
			//list.remove(i);
			
			list.add(new Term(j, "log"));
			search_nonlinear(i+1, j+1);
			list.remove(i);
			
			for (int k=j+1; k<varNum; k++)
			{
				list.add(new Term(j, k, "log+"));
				search_nonlinear(i+1, j+1);
				list.remove(i);
				
				for (int kk = 0; kk<varNum; kk++)
				if (kk != unique)
				{
					list.add(new Term(j, k, kk, "logx")); 
					search_nonlinear(i+1, j+1);
					list.remove(i);
				}
			}
			/*
			list.add(new Term(j, "exp"));
			search_nonlinear(i+1, j+1);
			list.remove(i);
			list.add(new Term(j, "sin"));
			search_nonlinear(i+1, j+1);
			list.remove(i);
			list.add(new Term(j, "cos"));
			search_nonlinear(i+1, j+1);
			list.remove(i);*/
			list.add(new Term(j, "rec"));
			search_nonlinear(i+1, j+1);
			list.remove(i);
			for (int k=j; k<varNum; k++)
			if (k != unique)
			{
				list.add(new Term(j, k));
				search_nonlinear(i+1, j+1);
				list.remove(i);
				if (k!=j) 
				{
					list.add(new Term(j, k, "div"));
					search_nonlinear(i+1, j+1);
					list.remove(i);
				}
			}
		}
	}
	
	static double log2 = Math.log((double)2); 
	public static void checkNonlinearInv(int size)
	{
		if (list.get(0).x == 14 && list.get(1).x == 6 && list.get(1).type == "log")
		{
			int k = 10;
			k = 20;
		}
		double[][] ma = new double[size][lineNum];
		try {
			for (int i=0; i<size; i++)
				for (int j=0; j<lineNum; j++)
				{
					switch (list.get(i).type) {
						case "log":ma[i][j] = Math.log(map[list.get(i).x][j])/log2;break;
						case "exp":ma[i][j] = Math.exp(map[list.get(i).x][j]);break;
						case "sin":ma[i][j] = Math.sin(map[list.get(i).x][j]);break;
						case "cos":ma[i][j] = Math.cos(map[list.get(i).x][j]);break;
						case "rec":ma[i][j] = 1/map[list.get(i).x][j];break;
						case "one":ma[i][j] = map[list.get(i).x][j];break;
						case "two":ma[i][j] = map[list.get(i).x][j]*map[list.get(i).y][j];break;
						case "div":ma[i][j] = map[list.get(i).x][j]/map[list.get(i).y][j];break;
						case "log+":ma[i][j] = Math.log(map[list.get(i).x][j]+map[list.get(i).y][j])/log2;break;
						case "logx":ma[i][j] = Math.log(map[list.get(i).x][j]+map[list.get(i).y][j])/log2*map[list.get(i).z][j];break;
						default: break;
					}
					if (Double.isNaN(ma[i][j])) return;
				}
		} catch (Exception e) {
			System.out.println("ERROR");
			return;
		}
		
		boolean[] label = new boolean[lineNum];
		double[] par = new double[size];
		double[] now = new double[size];
		double temp = 0, error = 0, first_error = 0, old_error = 0;
		int slineNum = 10;
		if (lineNum<slineNum) slineNum = lineNum;
		alpha = 0.0001;
		for (int i=0; i<size; i++) 
		{ 
			par[i] = 0; 
			now[i] = 0; 
		}
		
		for (int k=0; k<100; k++)
		{
			error = 0;
			for (int i=0; i<slineNum; i++)
			{
				temp = par[0];
				for (int j=1; j<size; j++)
					temp += ma[j][i]*par[j];
				temp = ma[0][i]-temp;  
				error += temp*temp;
			}
			if (k == 0) first_error = error;
			if (error > 2*old_error) alpha = alpha/10;
			else if (k<50 && (old_error-error)/error<0.1) alpha = alpha*10;
			old_error = error;
			for (int i=0; i<slineNum; i++)
			{
				temp = par[0];
				for (int j=1; j<size; j++)
					temp += ma[j][i]*par[j];
				temp = ma[0][i]-temp;
				now[0] += temp*alpha;
				for (int j=1; j<size; j++)
					now[j] += temp*alpha*ma[j][i];
			}
			for (int i=0; i<size; i++) 
			{
				par[i] = now[i];
				if (Double.isNaN(par[i])) return;
			}
		}
		if (first_error/100 < error) { left++; return; } else right++;
		
		//gradient algorithm
		old_error = 0;
		alpha = 0.0001;
		for (int i=0; i<size; i++) 
		{ 
			par[i] = 0; 
			now[i] = 0; 
		}
		for (int k=0; k<1000; k++)
		{
			error = 0;
			for (int i=0; i<lineNum; i++)
			{
				temp = par[0];
				for (int j=1; j<size; j++)
					temp += ma[j][i]*par[j];
				temp = ma[0][i]-temp;  
				error += temp*temp;
			}
			if (error > 2*old_error) alpha = alpha/10;
			else if (k<500 && (old_error-error)/error<0.1) alpha = alpha*10;
			old_error = error;
			for (int i=0; i<lineNum; i++)
			{
				temp = par[0];
				for (int j=1; j<size; j++)
					temp += ma[j][i]*par[j];
				temp = ma[0][i]-temp;
				now[0] += temp*alpha;
				for (int j=1; j<size; j++)
					now[j] += temp*alpha*ma[j][i];
			}
			for (int i=0; i<size; i++) 
			{
				par[i] = now[i];
				if (Double.isNaN(par[i])) return;
			}
		}
		/*
		int iflag = 0;
		for (int i=0; i<lineNum; i++)
		{
			temp = par[0];
			for (int j=1; j<size; j++)
				temp += ma[j][i]*par[j];
			error = Math.abs(ma[0][i]-temp)/(Math.abs(ma[0][i]));
			if (error > 0.2) {
				label[i] = true;
				iflag++;
			}
		}
		if (iflag>lineNum*0.4) return;
		
		//gradient algorithm without error data
		alpha = 0.001;
		for (int i=0; i<size; i++) 
		{ 
			par[i] = 0; 
			now[i] = 0; 
		}
		for (int k=0; k<1000; k++)
		{
			error = 0;
			for (int i=0; i<lineNum; i++) if (!label[i])
			{
				temp = par[0];
				for (int j=1; j<size; j++)
					temp += ma[j][i]*par[j];
				temp = ma[0][i]-temp;  
				error += temp*temp;
			}
			if (error > 2*old_error) alpha = alpha/10;
			else if (k<500 && (old_error-error)/error<0.1) alpha = alpha*10;
			old_error = error;
			for (int i=0; i<lineNum; i++) if (!label[i])
			{
				temp = par[0];
				for (int j=1; j<size; j++)
					temp += ma[j][i]*par[j];
				temp = ma[0][i]-temp;
				now[0] += temp*alpha;
				for (int j=1; j<size; j++)
					now[j] += temp*alpha*ma[j][i];
			}
			for (int i=0; i<size; i++) 
			{
				par[i] = now[i];
				if (Double.isNaN(par[i])) return;
			}
		}
		*/
		//Simulated Annealing
		/*
		old_error = first_error;
		double tem = 1000;
		for (int i=0; i<size; i++) 
		{ 
			par[i] = 0; 
			now[i] = 0; 
		}
		Random random = new Random(System.currentTimeMillis());
		for (int k=0; k<10000; k++)
		{
			error = 0;
			for (int i=0; i<size; i++)
				now[i] = par[i] + (random.nextDouble()-0.5)*old_error;
			for (int i=0; i<lineNum; i++)
			{
				temp = now[0];
				for (int j=1; j<size; j++)
					temp += ma[j][i]*now[j];
				temp = ma[0][i]-temp;  
				error += temp*temp;
			}
			if (error < old_error || random.nextDouble()<Math.exp((old_error-error)/tem))
				for (int i=0; i<size; i++) 
				{
					par[i] = now[i];
					if (Double.isNaN(par[i])) return;
				}
			old_error = error;
			tem = 0.8*tem;
		}
		*/
		for (int i=1; i<size; i++)
			if (Math.abs(par[i]) < 0.005) return;
		int iflag = 0;
		double total_error = 0, q = 0, q0 = 0;
		double[] err = new double[maxVarNum];
		for (int i=0; i<lineNum; i++)// if (!label[i])
		{
			temp = par[0];
			for (int j=1; j<size; j++)
				temp += ma[j][i]*par[j];
			q += (temp-ma[0][i])*(temp-ma[0][i]);
			error = Math.abs(ma[0][i]-temp)/(Math.abs(ma[0][i]));
			temp -= par[0];
			//q0 += (temp-ma[0][i])*(temp-ma[0][i]);
			temp += par[0];
			if ((Math.round(par[0])-par[0])/temp>0.1) err[0]++;
			for (int j=1; j<size; j++)
				if (Math.abs(ma[j][i]*(Math.round(par[j])-par[j])/temp)>0.1) err[j]++;
			total_error += error;
			if (error > 0.2) iflag++;
		}
		if (iflag < lineNum*0.2+2) {
			double[][] mat = new double[size][size+size];
			double[] mean = new double[size];
			for (int i=1; i<size; i++)
			{
				for (int j=0; j<lineNum; j++)
					mean[i] += ma[i][j];
				mean[i] = mean[i-1]/lineNum;
			}
			for (int i=1; i<size; i++)
				for (int j=1; j<size; j++)
					for (int k=0; k<lineNum; k++)
						mat[i][j] += (ma[i][k]-mean[i])*(ma[j][k]-mean[j]);
			for (int i=1; i<size; i++)
				mat[i][size-1+i] = 1;
			Converse(mat);
			
			//判定误差系数为0.1， 即常数影响不超过0.1就删去
			//if (Math.abs(q0-q)/q < 0.1) par[0] = 0;
			if (err[0]<lineNum*0.0+25) par[0] = Math.round(par[0]);
			
			q = Math.sqrt(q/(lineNum-varNum-1));
			double t = 0;
			int total_alpha = 0;
			for (int i=1; i<size; i++)
			{
				t = par[i]/(Math.sqrt(mat[i][size-1+i])*q);
				//这里取检验水平a为0.01
				System.out.println("alpha: "+i+" "+par[i]+" "+t);
				total_alpha += Math.abs(t);
				if (Math.abs(t)<8.86) par[i] = 0;
				if (err[i]<lineNum*0.05+2) par[i] = Math.round(par[i]);
				alp[result.size()][i] = t;
			}
			for (int i=1; i<size; i++)
				//if (alp[result.size()][i]/total_alpha < 0.1) par[i] = 0;
				if (Math.abs(par[i])<0.005) return;
			/*
			int h = ha[0];
			for (int i=1; i<size; i++)
			{
				h+=ha[count]* % maxn;
				count++;
			}
			h = h % maxn;
			if (hash[h]) return;
			else hash[h] = true;
			*/
			//if (total_error>lineNum*0.1) return;
			result.add(new unLinearInv(list, par));
			result.get(result.size()-1).error = total_error;
			System.out.println(result.get(result.size()-1).toString());
		}

	}
	
	public static boolean[] isuse = new boolean[maxVarNum];
	public static void regression(int i, int pos) 
	{
		if (i>1) 
			descent(i);
		System.out.println(num++);
		for (int k=0; k<i; k++) System.out.print(e[k]+" ");
		System.out.println();
			//getEquation(i);
		if (i>2) return;
		for (int j=pos; j<varNum; j++) if (!isuse[j])
		{
			e[i] = j;
			isuse[j] = true;
			regression(i+1, 0);
			isuse[j] = false;
		}
	}
	
	static int left = 0, right = 0;
	public static void descent(int size) 
	{
		if (size == 3 && e[0] == 0 && e[1] == 7 && e[2] == 10) 
		{
			int k;
			k = 100;
		}
		boolean[] label = new boolean[lineNum]; 
		double[] par = new double[size];
		double[] now = new double[size];
		double temp = 0, error = 0, old_error = 0, first_error = 0;
		alpha = 0.0001;
		for (int i=0; i<size; i++) 
		{ 
			par[i] = 0; 
			now[i] = 0; 
		}
		int slineNum = 10;
		if (lineNum>slineNum) slineNum = lineNum;
		for (int k=0; k<100; k++)
		{
			error = 0;
			int error_time = 0;
			for (int i=0; i<slineNum; i++)
			{
				temp = par[0];
				for (int j=1; j<size; j++)
					temp += map[e[j]][i]*par[j];
				temp = map[e[0]][i]-temp;  
				error += temp*temp;
				if (Math.abs(temp/map[e[0]][i])>0.1) error_time++;
			}
			if (k == 0) first_error = error;
			if (error > 2*old_error) alpha = alpha/10;
			else if (k<50 && (old_error-error)/error<0.1) alpha = alpha*10;
			old_error = error;
			for (int i=0; i<slineNum; i++)
			{
				temp = par[0];
				for (int j=1; j<size; j++)
					temp += map[e[j]][i]*par[j];
				temp = map[e[0]][i]-temp;
				now[0] += temp*alpha;
				for (int j=1; j<size; j++)
					now[j] += temp*alpha*map[e[j]][i];
			}
			for (int i=0; i<size; i++) 
			{
				par[i] = now[i];
				if (Double.isNaN(par[i])) return;
			}
			boolean flag = true;
			for (int i=0; i<size; i++)                                                                                                                
			{
				if (Math.abs((par[i]-now[i])/now[i])>0.1) flag = false; 
				par[i] = now[i];
				if (Double.isNaN(par[i])) return;
			}
			if (flag && error_time<1) { error = 0; break; }
		}
		//System.out.println(first_error+" "+error);
		if (first_error/100 < error) { left ++; return; } else right++;
		
		//gradient descent algorithm
		old_error = 0;
		alpha = 0.0001;
		for (int k=0; k<1000; k++)
		{
			error = 0;
			int error_time = 0;
			for (int i=0; i<lineNum; i++)
			{
				temp = par[0];
				for (int j=1; j<size; j++)
					temp += map[e[j]][i]*par[j];
				temp = map[e[0]][i]-temp;  
				error += temp*temp;
				if (Math.abs(temp/map[e[0]][i])>0.1) error_time++;
			}
			//if (k<5000)
			if (error > 2*old_error) alpha = alpha/10;
			else if ((old_error-error)/error<0.1) alpha = alpha*10;
			old_error = error;
			for (int i=0; i<lineNum; i++)
			{
				temp = par[0];
				for (int j=1; j<size; j++)
					temp += map[e[j]][i]*par[j];
				temp = map[e[0]][i]-temp;
				now[0] += temp*alpha;
				for (int j=1; j<size; j++)
					now[j] += temp*alpha*map[e[j]][i];
			}
			boolean flag = true;
			for (int i=0; i<size; i++)                                                                                                                
			{
				if (Math.abs((par[i]-now[i])/now[i])>0.1) flag = false; 
				par[i] = now[i];
				if (Double.isNaN(par[i])) return;
			}
			if (flag && error_time<lineNum*0.2+2) break;
		}
		/*
		int iflag = 0;
		for (int i=0; i<lineNum; i++)
		{
			temp = par[0];
			for (int j=1; j<size; j++)
				temp += map[e[j]][i]*par[j];
			error = Math.abs(map[e[0]][i]-temp)/(Math.abs(map[e[0]][i]));
			if (error > 0.2) 
			{
				iflag ++;
				label[i] = true;
			}
		}
		if (iflag>lineNum*0.4) return;
	
		//gradient descent algorithm without error data
		alpha = 0.0001;//delete or not?
		for (int k=0; k<1000; k++)
		{
			error = 0;
			int error_time = 0;
			for (int i=0; i<lineNum; i++) if (!label[i])
			{
				temp = par[0];
				for (int j=1; j<size; j++)
					temp += map[e[j]][i]*par[j];
				temp = map[e[0]][i]-temp;  
				error += temp*temp;
				if (Math.abs(temp/map[e[0]][i])>0.1) error_time++;
			}
			//if (k<5000)
			if (error > old_error) alpha = alpha/10;
			else if ((old_error-error)/error<0.1) alpha = alpha*10;
			old_error = error;
			for (int i=0; i<lineNum; i++) if (!label[i])
			{
				temp = par[0];
				for (int j=1; j<size; j++)
					temp += map[e[j]][i]*par[j];
				temp = map[e[0]][i]-temp;
				now[0] += temp*alpha;
				for (int j=1; j<size; j++)
					now[j] += temp*alpha*map[e[j]][i];
			}
			boolean flag = true;
			for (int i=0; i<size; i++)                                                                                                                
			{
				if (Math.abs((par[i]-now[i])/now[i])>0.1) flag = false; 
				par[i] = now[i];
				if (Double.isNaN(par[i])) return;
			}
			if (flag && error_time<lineNum*0.2) break;
		}
		*/
		//Simulated Annealing
		/*
		old_error = first_error;
		double tem = 1000;
		for (int i=0; i<size; i++) 
		{ 
			par[i] = 0; 
			now[i] = 0; 
		}
		Random random = new Random(System.currentTimeMillis());
		for (int k=0; k<10000; k++)
		{
			error = 0;
			for (int i=0; i<size; i++)
				now[i] = par[i] + (random.nextDouble()-0.5)*old_error*tem;
			for (int i=0; i<lineNum; i++)
			{
				temp = now[0];
				for (int j=1; j<size; j++)
					temp += map[e[j]][i]*now[j];
				temp = map[e[0]][i]-temp;  
				error += temp*temp;
			}
			if (error < old_error || random.nextDouble()<Math.exp((old_error-error)/tem))
				for (int i=0; i<size; i++) 
				{
					par[i] = now[i];
					if (Double.isNaN(par[i])) return;
				}
			old_error = error;
			tem = 0.2*tem;
		}*/
		
		for (int i=1; i<size; i++)
			if (Math.abs(par[i]) < 0.005) return;
		int iflag = 0;
		double q = 0, total_error = 0, q0 = 0;
		int[] err = new int[maxVarNum];
		for (int i=0; i<lineNum; i++)// if (!label[i])
		{
			temp = par[0];
			for (int j=1; j<size; j++)
				temp += map[e[j]][i]*par[j];
			q += (temp-map[e[0]][i])*(temp-map[e[0]][i]);
			temp -= par[0];
			//q0 += (temp-map[e[0]][i])*(temp-map[e[0]][i]);
			temp += par[0];
			if ((Math.round(par[0])-par[0])/temp>0.1) err[0]++;
			for (int j=1; j<size; j++)
				if (Math.abs(map[e[j]][i]*(Math.round(par[j])-par[j])/temp)>0.1) err[j]++;
			error = Math.abs(map[e[0]][i]-temp)/(Math.abs(map[e[0]][i]));
			total_error += error;
			if (error > 0.2) iflag++;
		}
		if (iflag < lineNum*0.2+2) {
			double[][] mat = new double[size][size+size];
			double[] mean = new double[size];
			for (int i=1; i<size; i++)
			{
				for (int j=0; j<lineNum; j++)
					mean[i] += map[e[i]][j];
				mean[i] = mean[i-1]/lineNum;
			}
			for (int i=1; i<size; i++)
				for (int j=1; j<size; j++)
					for (int k=0; k<lineNum; k++)
						mat[i][j] += (map[e[i]][k]-mean[i])*(map[e[j]][k]-mean[j]);
			for (int i=1; i<size; i++)
				mat[i][size-1+i] = 1;
			Converse(mat);
			
			//判定误差系数为0.1， 即常数影响不超过0.1就删去
			//if (Math.abs(q0-q)/q < 0.1) par[0] = 0;
			if (err[0]<lineNum*0.05+2) par[0] = Math.round(par[0]);
			if (lineNum>varNum+1) q = Math.sqrt(q/(lineNum-varNum-1)); else q = Math.sqrt(q);
			double t = 0;
			int total_alpha = 0;
			for (int i=1; i<size; i++)
			{
				t = par[i]/(Math.sqrt(mat[i][size-1+i])*q);
				//这里取检验水平a为0.01
				System.out.println("alpha: "+e[i]+" "+par[i]+" "+t);
				total_alpha += Math.abs(t);
				//if (Math.abs(t)<8.86) par[i] = 0;
				if (err[i]<lineNum*0.05+2) par[i] = Math.round(par[i]);
				alp[result.size()][i] = t;
			}
			for (int i=1; i<size; i++)
				if (Math.abs(par[i])<0.005) return;
			
			int h = 0;
			for (int i=0; i<size; i++)
				h+=ha[i]*e[i] % maxn;
			h = h % maxn;
			if (hash[h]) return;
			else hash[h] = true;
			result.add(new linearInv(e, par, "regression", h));
			result.get(result.size()-1).error = total_error;
			System.out.println(result.get(result.size()-1).toString());
		}
	}
	
	public static void Converse(double[][] mat)
	{
		int size = mat[1].length / 2;
		int width = size+size-1;
		for (int i=1; i<size; i++)
		{
			for (int j=width-1; j>0; j--) mat[i][j] /= mat[i][i];
			for (int j=i+1; j<size; j++)
				for (int k=i+1; k<width; k++)
					mat[j][k] -= mat[i][k]*mat[j][i];
		}
		for (int i=size-2; i>0; i--)
			for (int j=i+1; j<size; j++)
				for (int k=size; k<width; k++)
					mat[i][k] -= mat[j][k]*mat[i][j];
	}
	
	public static void getEquation(int i)
	{
		double[][] mat = new double[maxVarNum][maxVarNum];
		for (int j=0; j<i; j++)
			for (int k=0; k<i; k++)
				for (int l=0; l<lineNum; l++)
					mat[j][k]+=map[e[j]][l]*map[e[k]][l];
		Gaussian(mat);
	}
	
	public static double[] Gaussian(double[][] de)
	{
		int size = de[0].length;
		double[][] der = new double[size][size];
		for (int i=0; i<size; i++)
			for (int j=0; j<size; j++)
				der[i][j] = de[i][j];
		
		int line = -1;
		double mul = 1;
		for (int k=1; k<size-1; k++) 
		{
			line = -1;
			for (int i=k; i<size; i++)
				if (Math.abs(der[i][k])>0.00001) {
					line = i;
					if (i!=k)
						for (int j=1; j<size; j++)
						{
							double temp = der[i][j];
							der[i][j] = der[k][j];
							der[k][j] = temp;
						}
					mul *= der[k][k];
					break;
				}
			if (line != k) mul = -mul;
			
			for (int i=k+1; i<size; i++)
			{
				if (Math.abs(der[i][k])<0.00001) continue;
				for (int j=k+1; j<size; j++)
					der[i][j] = -der[k][j]*der[i][k]/der[k][k]+der[i][j];
			}
		}
		
		mul = Math.rint(mul*der[size-1][size-1]);
		if (Math.abs(mul) > 0.00001) {
			double[] res = new double[size+1];
			double tot;
			for (int i=0; i<size; i++) res[i] = 1;
			for (int i=size-1; i>0; i--)
			{
				tot = 0;
				for (int j=i+1; j<size; j++)
					tot += der[i][j]*res[j];
				res[i] = (der[i][0]-tot)/der[i][i];
			}
			return res;
		} else return null;
	}
	
	public static void newton(int size)
	{
		double[][] de = new double[varNum][varNum];
		//int size = de[0].length;
		double[][] der = new double[size][size];
		for (int i=0; i<size; i++)
			for (int j=0; j<size; j++)
				der[i][j] = de[i][j];
		
		int line = -1;
		double mul = 1;
	}
	
	public static void checkConstantInv(int i) 
	{
		/*
		Invariant temp;
		if (max[i]==min[i]) 
		{
			temp = new constantInv(i, "=", min[i]);
			result.add(temp);
		} else 
		{
			//x<=a
			temp = new constantInv(i, "<=", max[i]);
			result.add(temp);
			
			//x>=a
			temp = new constantInv(i, ">=", min[i]);
			result.add(temp);
			
			//x!=0
			boolean flag = true;
			for (int p=0; p<lineNum; p++)
				if (map[i][p]==0) { 
					flag = false;
					break;
				}
			if (flag) {
				temp = new constantInv(i, "!=", 0);
				result.add(temp);
			}
			
			//x mod a = b or x mod a != b
			
			for (int p=2; p<=10; p++)
			{
				boolean[] record = new boolean[10000];
				for (int q=0; q<p; q++) record[q] = false;
				for (int q=0; q<lineNum; q++) record[map[i][q] % p] = true;
				
				int count = 0, unique = -1;
				for (int q=0; q<p; q++) 
					if (record[q]) count++; else unique = q;
				if (count == 1) {
					temp = new constantInv(i, p, "=", map[i][0] % p);
					result.add(temp);
				} else if (count == p-1) {
					temp = new constantInv(i, p, "!=", unique);
					result.add(temp);
				}
			}
		}*/
	}

	public static void checkSmallSetInv(int i) 
	{
		Invariant temp;
		if (min[i]!=max[i])
		{
			Double mid = 0.0;
			int flag = 0;
			for (int p=0; p<lineNum; p++)
				if (map[i][p]!=min[i] && map[i][p]!=max[i])
					if (flag == 0) {
						flag = 1;
						mid = map[i][p];
					} else if (flag == 1 && map[i][p]!=mid) {
						flag = 2;
						break;
					}
			if (flag == 0) {
				temp = new smallSetInv(i, min[i], max[i]);
				result.add(temp);
			} else if (flag == 1) {
				temp = new smallSetInv(i, min[i], mid, max[i]);
				result.add(temp);
			}
		}
	}

	public static void checkLinearInv(int i, int j) 
	{
		if (lineNum <= 1 || min[j] == max[j]) return;
		Double a = map[i][1]-map[i][0];
		Double b = map[j][1]-map[j][0];
		Double c = (map[i][1]-map[i][0])*map[j][0]-(map[j][1]-map[j][0])*map[i][0];
		boolean flag = true;
		for (int p=2; p<lineNum; p++)
			if (a*map[j][p] != b*map[i][p]+c) {
				flag = false;
				break;
			}
		if (flag) {
			Invariant temp = new linearInv(j, i, a, b, c);
			result.add(temp);
		}
	}

	public static void checkOrderInv(int i, int j) 
	{
		Invariant temp;
		/*
		boolean flag = true;
		for (int p=0; p<lineNum; p++)
			if (map[i][p] == map[j][p]) { flag = false; break; }
		if (flag) {
			temp = new orderInv(i, "!=", j);
			result.add(temp);
		}
		
		flag = true;
		for (int p=0; p<lineNum; p++)
			if (map[i][p] > map[j][p]) { flag = false; break; }
		if (flag) {
			temp = new orderInv(i, "<=", j);
			result.add(temp);
		}
		
		flag = true;
		for (int p=0; p<lineNum; p++)
			if (map[i][p] >= map[j][p]) { flag = false; break; }
		if (flag) {
			temp = new orderInv(i, "<", j);
			result.add(temp);
		}
		
		flag = true;
		for (int p=0; p<lineNum; p++)
			if (map[i][p] < map[j][p]) { flag = false; break; }
		if (flag) {
			temp = new orderInv(i, ">=", j);
			result.add(temp);
		}
		
		flag = true;
		for (int p=0; p<lineNum; p++)
			if (map[i][p] <= map[j][p]) { flag = false; break; }
		if (flag) {
			temp = new orderInv(i, ">", j);
			result.add(temp);
		}
		
		boolean flag = true;
		for (int p=0; p<lineNum; p++)
			if (map[i][p] != map[j][p]) { flag = false; break; }
		if (flag) {
			temp = new orderInv(i, "=", j);
			result.add(temp);
		}*/
		
	}

	public static void checkFunctionInv(int i, int j) 
	{
		if (lineNum <= 2) return;
		
		Invariant temp;
		/*
		//absolute value
		boolean flag = true;
		for (int p=0; p<lineNum; p++)
			if (map[i][p] != Math.abs(map[j][p])) {
				flag = false;
				break;
			}
		if (flag) {
			temp = new functionInv(i, "abs", j);
			result.add(temp);
		}
		
		flag = true;
		for (int p=0; p<lineNum; p++)
			if (map[j][p] != Math.abs(map[i][p])) {
				flag = false;
				break;
			}
		if (flag) {
			temp = new functionInv(j, "abs", i);
			result.add(temp);
		}
		
		//negation
		flag = true;
		for (int p=0; p<lineNum; p++)
			if (map[i][p] != -map[j][p]) {
				flag = false;
				break;
			}
		if (flag) {
			temp = new functionInv(i, "-", j);
			result.add(temp);
		}
		
		//x mod y = remain
		flag = true;
		int remain = 0;
		if (map[j][0] == 0) flag = false; else remain = map[i][0] % map[j][0];
		for (int p=1; p<lineNum; p++)
			if (map[j][p] == 0 || map[i][p] % map[j][p] != remain) {
				flag = false;
				break;
			}
		if (flag) {
			temp = new functionInv(i, "neg", j, remain);
			result.add(temp);
		}	

		flag = true;
		if (map[j][0] == 0) flag = false; else remain = map[i][0] % map[j][0];
		for (int p=1; p<lineNum; p++)
			if (map[i][p] == 0 || map[j][p] % map[i][p] != remain) {
				flag = false;
				break;
			}
		if (flag) {
			temp = new functionInv(j, "mod", i, remain);
			result.add(temp);
		}	
		*/
		
		//x * y = remain
		boolean flag = true;
		double remain = map[i][0]*map[j][0];
		for (int p=1; p<lineNum; p++)
			if (map[i][p] * map[j][p] != remain) {
				flag = false;
				break;
			}
		if (flag) {
			temp = new functionInv(i, "*", j, remain);
			result.add(temp);
		}	
	}

	public static void checkLinearInv(int i, int j, int k) 
	{
		if (lineNum <= 3) return;
		double vecx = map[i][1]-map[i][0], vecy = map[j][1]-map[j][0], vecz = map[k][1]-map[k][0];
		double tx, ty, tz, vx = 0, vy = 0, vz = 0;
		int tp = -1;
		for (int p = 2; p<lineNum; p++)
		{
			tx = map[i][p]-map[i][0];
			ty = map[j][p]-map[j][0];
			tz = map[k][p]-map[k][0];
			vx = vecy*tz-vecz*ty;
			vy = vecz*tx-vecx*tz;
			vz = vecx*ty-vecy*tx;
			if (vx!=0 || vy!=0 || vz!=0)  {
				tp = p;
				break;
			}
		}
		if (tp == -1) return;
		boolean flag = true;
		for (int p = 2; p<lineNum; p++)
			if (map[i][p]*vx+map[j][p]*vy+map[k][p]*vz != 0) {
				flag = false;
				break;
			}
		if (flag) {
			double a = (map[j][0]-map[j][1])*(map[k][0]-map[k][tp])-(map[j][0]-map[j][tp])*(map[k][0]-map[k][1]);
			double b = (map[i][0]-map[i][tp])*(map[k][0]-map[k][1])-(map[i][0]-map[i][1])*(map[k][0]-map[k][tp]);
			double c = (map[i][0]-map[i][1])*(map[j][0]-map[j][tp])-(map[i][0]-map[i][tp])*(map[j][0]-map[j][1]);
			double d = a*map[i][0]+b*map[j][0]+c*map[k][0];
			Invariant temp = new linearInv(i, j, k, a, b, c, d);
			result.add(temp);
		}
	}

	public static void checkFunctionInv(int i, int j, int k) 
	{
		Invariant temp;
		/*
		//max
		boolean flag = true;
		for (int p=0; p<lineNum; p++)
			if (Math.max(map[j][p], map[k][p]) != map[i][p]) {
				flag = false;
				break;
			}
		if (flag) {
			temp = new functionInv(i, j, "max", k);
			result.add(temp);
		}
		//min
		flag = true;
		for (int p=0; p<lineNum; p++)
			if (Math.min(map[j][p], map[k][p]) != map[i][p]) {
				flag = false;
				break;
			}
		if (flag) {
			temp = new functionInv(i, j, "min", k);
			result.add(temp);
		}
		*/
		//mul
		boolean flag = true;
		for (int p=0; p<lineNum; p++)
			if (map[j][p] * map[k][p] != map[i][p]) {
				flag = false;
				break;
			}
		if (flag) {
			temp = new functionInv(i, j, "*", k);
			result.add(temp);
		}
		/*
		//and
		flag = true;
		for (int p=0; p<lineNum; p++)
			if ((map[j][p] & map[k][p]) != map[i][p]) {
				flag = false;
				break;
			}
		if (flag) {
			temp = new functionInv(i, j, "and", k);
			result.add(temp);
		}
		//or
		flag = true;
		for (int p=0; p<lineNum; p++)
			if ((map[j][p] | map[k][p]) != map[i][p]) {
				flag = false;
				break;
			}
		if (flag) {
			temp = new functionInv(i, j, "or", k);
			result.add(temp);
		}
		
		//div
		flag = true;
		for (int p=0; p<lineNum; p++)
			if (map[k][p] == 0 || map[j][p] / map[k][p] != map[i][p]) {
				flag = false;
				break;
			}
		if (flag) {
			temp = new functionInv(i, j, "div", k);
			result.add(temp);
		}
		flag = true;
		for (int p=0; p<lineNum; p++)
			if (map[j][p] == 0 || map[k][p] / map[j][p] != map[i][p]) {
				flag = false;
				break;
			}
		if (flag) {
			temp = new functionInv(i, k, "div", j);
			result.add(temp);
		}
		//mod
		flag = true;
		for (int p=0; p<lineNum; p++)
			if (map[k][p] == 0 || map[j][p] % map[k][p] != map[i][p]) {
				flag = false;
				break;
			}
		if (flag) {
			temp = new functionInv(i, j, "mod", k);
			result.add(temp);
		}
		flag = true;
		for (int p=0; p<lineNum; p++)
			if (map[j][p] == 0 || map[k][p] % map[j][p] != map[i][p]) {
				flag = false;
				break;
			}
		if (flag) {
			temp = new functionInv(i, k, "mod", j);
			result.add(temp);
		}
		*/
	}
	
	//static int[] res;
	public static void checkLinearInv(int[] e) 
	{
		int size = e.length;
		if (varNum < size || lineNum <= size) return;
		double de[][] = new double[size][size];
		for (int i=0; i<size; i++)
			for (int j=0; j<size; j++)
				de[j][i] = map[e[i]][j+1]-map[e[i]][0];
		double[] res = determinants(de);
		if (res == null) return;
		
		for (int i=0; i<size; i++)
			if (Math.abs(res[i])<0.00001) return;
		double com = res[0];
		for (int i=1; i<size; i++)
			com = gcd(com, res[i]);
		if (com != 0)
		for (int i=0; i<size; i++)
			res[i] = res[i] / com;
		res[size] = 0;
		for (int i=0; i<size; i++) res[size]+=map[e[i]][0]*res[i];
		boolean flag = true;
		for (int i=size+1; i<lineNum; i++)
		{
			int ans = 0;
			for (int j=0; j<size; j++) ans+=res[j]*map[e[j]][i];
			if (ans != res[size]) {
				flag = false;
				break;
			}
		}
		if (flag) {
			int h = 0;
			for (int i=0; i<size; i++)
				h+=ha[i]*e[i] % maxn;
			h = h % maxn;
			if (hash[h]) return;
			else hash[h] = true;
			Invariant temp = new linearInv(e, res, "function", h);
			result.add(temp);
		} else checkLinear_error(e);
	}
	
	public static void checkLinear_error(int[] e)
	{
	}
	
	public static double gcd(double a, double b)
	{
		if (Math.abs(b) < 0.00001) return a;
		else return gcd(b, a % b);
	}
	
	public static double[] determinants(double[][] de) 
	{
		int size = de[0].length;
		double[][] der = new double[size][size];
		for (int i=0; i<size; i++)
			for (int j=0; j<size; j++)
				der[i][j] = de[i][j];
		
		int line = -1;
		double mul = 1;
		for (int k=0; k<size-1; k++) 
		{
			line = -1;
			for (int i=k; i<size; i++)
				if (Math.abs(der[i][k])>0.00001) {
					line = i;
					if (i!=k)
						for (int j=0; j<size; j++)
						{
							double temp = der[i][j];
							der[i][j] = der[k][j];
							der[k][j] = temp;
						}
					mul *= der[k][k];
					break;
				}
			if (line != k) mul = -mul;
			
			for (int i=k+1; i<size; i++)
			{
				if (Math.abs(der[i][k])<0.00001) continue;
				for (int j=k+1; j<size; j++)
					der[i][j] = -der[k][j]*der[i][k]/der[k][k]+der[i][j];
			}
		}
		
		mul = Math.rint(mul*der[size-1][size-1]);
		if (Math.abs(mul) < 0.00001) {
			double[] res = new double[size+1];
			double tot;
			for (int i=0; i<size; i++) res[i] = 1;
			for (int i=size-1; i>=0; i--)
			{
				tot = 0;
				int k = i;
				while (k<size && Math.abs(der[i][k]) < 0.00001) k++;
				for (int j=k+1; j<size; j++)
					tot+=der[i][j]*res[j];
				if (k < size)
					res[k] = -tot/der[i][k];
			}
			return res;
		} else return null;
	}
	
	public static double[] getResult(double[][] de) 
	{
		int size = de[0].length;
		double[][] des= new double[size-1][size-1];
		double[] res = new double[size+1];
		for (int i=0; i<size; i++) 
		{
			for (int j=1; j<size; j++) 
			{
				for (int k=0; k<i; k++)
					des[j-1][k] = de[j][k];
				for (int k=i+1; k<size; k++)
					des[j-1][k-1] = de[j][k];
			}
			//if (i % 2==0) res[i] = determinants(des);
			//else res[i] = -determinants(des);
		}
		return res;
	}
	
	public static void print(long time) throws IOException
	{
		String output = "output"+currentNum+".txt";
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		int total = 0;
		for (int i=0; i<result.size(); i++)
			if (use[i] && result.get(i).error<lineNum*0.05) {
			total++;
			writer.write(result.get(i).toString()+'\n');
			writer.write(String.format("%.2f", result.get(i).error)+'\n');
			System.out.println(result.get(i).toString());
			System.out.println(String.format("%.2f", result.get(i).error));
		}
		System.out.println(total);
		//System.out.println(left+" "+right);
		System.out.println("Total running time: "+time+"ms\n");
		writer.write(Integer.toString(total)+'\n');
		writer.write("Total running time: "+time+"ms\n");
		writer.close();
	}
	
	//compare each pair to delete some redundant variants
	public static void test_pair() throws IOException
	{
		BufferedWriter writer;
		for (int i=0; i<result.size(); i++)
			if (use[i])
			for (int j=0; j<result.size(); j++)
				if (i!=j && use[j]) {
					writer = new BufferedWriter(new FileWriter("input.smt2"));
					for (int k=0; k<varNum; k++)
						writer.write("(declare-const "+"a"+k+" Real)\n");
					writer.write("(assert ("+result.get(i).toPrefix()+"))\n");
					writer.write("(assert (not ("+result.get(j).toPrefix()+")))\n");
					writer.write("(check-sat)\n");
					writer.close();
					if (runCMD().charAt(0) == 'u') use[j] = false;
				}
	}
	
	//delete redundant invariants by z3
	public static void test(int p) throws IOException 
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter("input.smt2"));
		for (int i=0; i<varNum; i++) 
			writer.write("(declare-const "+"a"+i+" Real)\n");
		for (int i=0; i<result.size(); i++)
			if (i != p && use[i])
			{
				String s = result.get(i).toPrefix();
				if (s != "") writer.write("(assert ("+s+"))\n");
				if (result.get(i) instanceof unLinearInv) 
				{
					String type = ((unLinearInv)(result.get(i))).list.get(1).type;
					if (type == "rec" || type == "div")
						writer.write("(assert (not (= a"+((unLinearInv)(result.get(i))).list.get(1).y+" 0)))\n");
				}
			} else if (i == p)
			{
				String s = result.get(i).toPrefix();
				if (s != "") writer.write("(assert (not ("+s+")))\n");
				if (result.get(i) instanceof unLinearInv) 
				{
					String type = ((unLinearInv)(result.get(i))).list.get(1).type;
					if (type == "rec" || type == "div")
						writer.write("(assert (not (= a"+((unLinearInv)(result.get(i))).list.get(1).y+" 0)))\n");
				}
			}
		writer.write("(check-sat)\n");
        writer.close();
	}
	
}