package kr.soen.myapplication;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

public class NeuralNetwork {
	private  Context context;
	public NeuralNetwork(Context current){
		this.context = current;
	}
	private static int inputnum  = 13;
	private static int hiddennum = 10;
	private static int outputnum = 2;
	private static int maxinputnum = 5000;
	private static double ymin = -1;
	
	public String NN(double[][] sourceDataArray,int loopcnt) throws IOException { /////중요: main을 nn으로 수정후 매개변수에 배열의 참조값받기 참조값은getData에 인수로넘김

	double[][] wh = new double[hiddennum][inputnum]; //히든층 가중치
	double[][] wo = new double[outputnum][hiddennum]; //출력층 가중치
	double[] bias1 = new double[hiddennum]; // 히든층 바이어스
	double[] bias2 = new double[outputnum]; //츨력층 바이어스
	double[][] data = new double[maxinputnum][inputnum]; //입력데이터
	double[] hi = new double[hiddennum]; //중간층 출력
	double[] xoffset = new double[inputnum]; // 입력정규화에 사용
	double[] gain = new double[inputnum]; //입력정규화에 사용
	int n_of_e;
	int i,j;
	int cnt1=0;
	int cnt2=0;
	InputStream inputStream = context.getResources().openRawResource(R.raw.wh);
		BufferedReader whBr = new BufferedReader(new InputStreamReader(inputStream));
		Scanner whSc = new Scanner(whBr); //히든층 가중치

	InputStream inputStream2 = context.getResources().openRawResource(R.raw.wo);
		BufferedReader woBr = new BufferedReader(new InputStreamReader(inputStream2));
		Scanner woSc = new Scanner(woBr); //출력층 가중치

	InputStream inputStream3 = context.getResources().openRawResource(R.raw.hbias);
		BufferedReader hbiasBr = new BufferedReader(new InputStreamReader(inputStream3));
		Scanner hbiasSc = new Scanner(hbiasBr);	//히든층 바이어스

	InputStream inputStream4 = context.getResources().openRawResource(R.raw.obias);
		BufferedReader obiasBr = new BufferedReader(new InputStreamReader(inputStream4));
		Scanner obiasSc = new Scanner(obiasBr);	//히든층 바이어스

	InputStream inputStream5 = context.getResources().openRawResource(R.raw.xoffset);
		BufferedReader xoffsetBr = new BufferedReader(new InputStreamReader(inputStream5));
		Scanner xoffsetSc = new Scanner(xoffsetBr);	//히든층 바이어스

	InputStream inputStream6 = context.getResources().openRawResource(R.raw.gain);
		BufferedReader gainBr = new BufferedReader(new InputStreamReader(inputStream6));
		Scanner gainSc = new Scanner(gainBr);	//히든층 바이어스

	initwh(whSc,hbiasSc,wh,bias1);
	initwo(woSc,obiasSc,wo,bias2);
	//print(wh,wo,bias1,bias2);
	n_of_e = getData(sourceDataArray,xoffsetSc,gainSc,data,xoffset,gain,loopcnt); //sourceData 에 넘겨받은 배열의 참조값 전달 sourceData -> data 로 값대입
	double[][] o1 = new double[n_of_e][outputnum];
	double[] result = new double[inputnum]; //to bsxfun
	System.out.println("n_of_e :"+" "+n_of_e);
	for(i=0; i<n_of_e;i++)
	{
		bsxfun(inputnum,result, data[i], xoffset, gain);
		forward(wh,wo,hi,result,bias1,bias2,o1[i]);
	}
	double sub=0;
	for(i=0;i<n_of_e;i++)
	{
		//System.out.println(i+" "+o1[i][0]+" "+o1[i][1]);
		//if(o1[i][0]>o1[i][1])	cnt1++;
		//else if(o1[i][0]<o1[i][1]) cnt2++;
		sub = (o1[i][0] - o1[i][1]) * (o1[i][0] - o1[i][1]);
		System.out.println("output"+i+" "+o1[i][0]+" "+o1[i][1] +" " + "sub : " + sub);
		if((o1[i][0]>o1[i][1]) && (o1[i][0]>0 && o1[i][1]<0))   cnt1++;
		else if((o1[i][0]<o1[i][1]) && (o1[i][0]<0 && o1[i][1]>0)) cnt2++;
	}
	whBr.close();
	woBr.close();
	hbiasBr.close();
	obiasBr.close();
	xoffsetBr.close();
	gainBr.close();
	System.out.println("cnt1: "+cnt1+" cnt2: "+cnt2);
	double real = (double)cnt1/(double)(cnt1+cnt2) * 100.0;
	double fake = (double)cnt2/(double)(cnt1+cnt2) * 100.0;
	if(real>fake) {
		System.out.print("real ");
		System.out.printf("%.2f",real);
		System.out.println("%");
		return "accept";
	}
	else {
		System.out.print("fake ");
		System.out.printf("%.2f",fake);
		System.out.println("%");
		return "reject";
	}
}

		
	public static void forward(double[][] wh,double[][] wo, double[] hi, double[] data,double[] bias1,double[] bias2,double[] o)
	{
		int i,j;
		double u,u1; //가중합
		for(i=0; i<hiddennum;i++)
		{	u=0;
			for(j=0; j<inputnum;j++)
			{
				u += data[j]*wh[i][j];
			}
			u += bias1[i];
			hi[i] = tansig(u);
		}
		for(i=0; i<outputnum;i++) 
		{	u1=0;
			for(j=0; j<hiddennum; j++)
			{
				u1 += hi[j]*wo[i][j];
			}
			u1 += bias2[i];
			o[i] = tansig(u1);
		}

	}
	public static double tansig(double u)
	{
		return (2.0)/(1.0+Math.exp(-2*u))-1;  //tansig
	}
	public static int getData(double[][] sourceData, Scanner sc1,Scanner sc2, double[][] data, double[] xoffset, double[] gain,int loopcnt) throws FileNotFoundException
	{ /////바로녹음되어 추출된 mfcc배열의 참조값을받음
		int n_of_e=0;
		for(int i=0; i<loopcnt; i++) /////mfcc배열의 길이
		{
			//if(i >= 70) break;
			for(int j=0; j<inputnum;j++) {
				data[i][j] = sourceData[i][j];    //값 대입
				System.out.print(data[i][j]);
			}
			System.out.println();
			n_of_e++;
		}
			for(int j=0; j<inputnum;j++)
			{
				xoffset[j] = sc1.nextDouble();
				//System.out.print(xoffset[j]);
				if(sc1.hasNextDouble()==false)	break;
			}
			//System.out.println();
			for(int j=0; j<inputnum;j++)
			{
				gain[j] = sc2.nextDouble();
				//System.out.print(gain[j]);
				if(sc2.hasNextDouble()==false)	break;
			}
			//System.out.println();
		sc1.close();
		sc2.close();
		return n_of_e;
	}

	/*중간층 가중치 및 임계치 초기화*/
	public static void initwh(Scanner sc1, Scanner sc2, double[][] wh,double[] bias1) throws FileNotFoundException
	{
		int i,j;

		for(i=0; i<hiddennum; i++) {
			for(j=0; j<inputnum;j++) {
				wh[i][j] = sc1.nextDouble();
				//System.out.print(wh[i][j]+ " ");
				}
			bias1[i] = sc2.nextDouble();
			//System.out.println();
			}
		sc1.close();
		sc2.close();
	}
	/*출력층 가중치 및 임계치 초기화*/
	public static void initwo(Scanner sc1, Scanner sc2, double[][] wo,double[] bias2) throws FileNotFoundException
	{
		int i,j;
		
		for(i=0;i<outputnum;i++)
		{
			for(j=0;j<hiddennum;j++)
			{	
				wo[i][j] = sc1.nextDouble();
			}
			bias2[i]= sc2.nextDouble();
		}
		sc1.close();
		sc2.close();
	}
	public static void print(double[][] wh, double[][] wo,double[] bias1,double[] bias2)
	{
		int i,j;
		for(i=0;i<hiddennum;i++)
		{
			for(j=0;j<inputnum;j++)
				System.out.print(wh[i][j]+" ");
			System.out.println();
		}
		System.out.println();
		for(i=0; i<outputnum; i++)
		{
			for(j=0; j<hiddennum; j++)
				System.out.print(wo[i][j]+" ");
			System.out.println();
		}
		System.out.println();
		
			for(j=0; j<hiddennum; j++)
				System.out.print(bias1[j]+" ");
			System.out.println();
			for(j=0; j<outputnum; j++)
				System.out.print(bias2[j]+" ");
			System.out.println();
	}
	public static void bsxfun(int iteration, double[] y,double[] data, double[] xoffset, double[] gain)
	{
		for(int i =0;i<iteration;i++)
		{
			y[i] = data[i]-xoffset[i];
		}
		for(int i=0;i<iteration;i++)
		{
			y[i] = y[i]*gain[i];
		}
		for(int i=0;i<iteration;i++)
		{
			y[i] = y[i] + ymin;
		}
	}
}