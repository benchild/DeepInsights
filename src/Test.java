import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import Invariant.Invariant;

public class Test 
{
	public static class relation
	{
		public int num = 0, aim = 0, cons = 0;
		public int[] termX;
		public int[] termY; 
		public int[] type; 
		public int[] coe;
		public relation(int num, int aim)
		{
			this.num = num;
			this.aim = aim;
			termX = new int[num];
			termY = new int[num];
			type = new int[num];
			coe = new int[num];
		}
		
		public String toString() 
		{
			String s = "A("+aim+") = ";
			for (int i=0; i<num; i++)
			{
				if (i > 0) s = s+" + ";
				switch (type[i]) {
					case 0: s = s+coe[i]+"A("+termX[i]+")"; break;
					case 1: s = s+coe[i]+"/A("+termX[i]+")"; break;
					case 2: s = s+coe[i]+"A("+termX[i]+")*A("+termY[i]+")"; break;
					case 3: s = s+coe[i]+"A("+termX[i]+")/A("+termY[i]+")"; break;
					case 4: s = s+coe[i]+"log A("+termX[i]+")"; break;
					default: break;
				}
			}
			return s+ " + "+cons;
		}
	}
	

	public static int varNum = 10;
	public static int createVarNum = 5;
	public static int lineNum = 100;
	public static List<relation> rel = new ArrayList<relation>();
	
	public static void main(String[] args) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter("input9.txt"));
		writer.write(Integer.toString(varNum+createVarNum)+'\n');
		writer.write(Integer.toString(lineNum)+'\n');
		
		createRelation();
		
		long startTime=System.currentTimeMillis();
		Random random = new Random(startTime);
		double[] now = new double[varNum+createVarNum];
		for (int i=0; i<lineNum; i++)
		{
			for (int j=0; j<varNum; j++)
			{
				now[j] = random.nextInt(10000)/100.0+1;
				writer.write(String.format("%.2f", now[j])+" "); 
			}
			for (int j=0; j<createVarNum; j++)
			{
				double ans = 0;
				relation re = rel.get(j);
				for (int k=0; k<re.num; k++)
					switch (re.type[k]) {
						case 0: ans += re.coe[k]*now[re.termX[k]]; break;
						case 1: ans += re.coe[k]/now[re.termX[k]]; break;
						case 2: ans += re.coe[k]*now[re.termX[k]]*now[re.termY[k]]; break;
						case 3: ans += re.coe[k]*now[re.termX[k]]/now[re.termY[k]]; break;
						case 4: ans += re.coe[k]*Math.log(now[re.termX[k]])/Math.log(2); break;
						default: break;
					}
				System.out.println(random.nextGaussian());
				ans = ans+re.cons+ans/100*random.nextGaussian();
				now[j+varNum] = ans;
				if (j != createVarNum-1) writer.write(String.format("%.2f", ans)+" "); 
				else writer.write(String.format("%.2f", ans)+'\n');
			}
		}
		for (relation re : rel)
			writer.write(re.toString()+'\n');
		writer.close();
		System.out.println("Finish");
	}
	
	public static void createRelation()
	{
		long startTime=System.currentTimeMillis();
		Random random = new Random(startTime);
		for (int i=0; i<createVarNum; i++)
		{
			//int term = random.nextInt(3)+1;
			int type = i;
			relation re;
			if (type == 0) re = new relation(2, i+10); 
				else re = new relation(1, i+10);
			re.coe[0] = random.nextInt(20)-10;
			while (re.coe[0] == 0) re.coe[0] = random.nextInt(20)-10;
			re.termX[0] = random.nextInt(varNum);
			//0:self; 1:rec; 2:mul; 3:div; 4:log
			re.type[0] = type;
			re.termY[0] = random.nextInt(varNum);
			if (re.type[0] == 2 || re.type[0] == 3)
				while (re.termY[0] == re.termX[0]) re.termY[0] = random.nextInt(varNum);
			
			if (re.type[0] == 0)
			{
				re.coe[1] = random.nextInt(20)-10;
				while (re.termX[0] == re.termX[1]) re.termX[1] = random.nextInt(varNum);
			}
			//re.cons = random.nextInt(20)-10;
			rel.add(re);
		}
	}
}