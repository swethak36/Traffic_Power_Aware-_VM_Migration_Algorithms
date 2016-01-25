package com.csdl;

import java.util.Random;


public class Sla_LIP {
	
	public 	static double total_SLAViolationTime = 0.0;
	public 	static int total_ActiveTime = 0;
	public static double total_MIPS =0;
	public static double totalRequested_MIPS =0;
	
	
	

	/* public static void main(String[] args) {
		
		int noofHosts = 5;
		int noofMigratedVms = 5;
		double SLA_Overall ;
		double a , b;
		
	//	calculateSLAoverloaded(noofHosts);
		// calculateSLAunmet(noofMigratedVms);
		
		a = calculateSLAoverloaded(noofHosts);
	b = calculateSLAunmet(noofMigratedVms);
		 SLA_Overall =  (a * b); // jings modification not added
		 System.out.println();
		System.out.println("The Overall SLA is " +round(SLA_Overall,7) +" %");
	} */

	

public static double calculateSLAunmet(int noofMigratedVms) {
		int minReqMIPS = 2416;
		int maxReqMIPS = 2824;
		
		double SLA_Unmet;
		
		int minAllocMIPS = 212;
		int maxAllocMIPS = 616;
		
		Random rSlaUnmet = new Random();
		
		for(int i=0;i<noofMigratedVms;i++)
		{
		int Req_MIPS = rSlaUnmet.nextInt(maxReqMIPS- minReqMIPS +1) + minReqMIPS;
	//	System.out.println("The Required MIPS is " + Req_MIPS);
		int Alloc_MIPS = rSlaUnmet.nextInt(maxAllocMIPS - minAllocMIPS +1 ) + minAllocMIPS;
	//	System.out.println("The Allocated MIPS is " +Alloc_MIPS);
		int diff_MIPS = Req_MIPS - Alloc_MIPS ;
		if(diff_MIPS > 0)
		{
		//System.out.println("The difference in MIPS is " +diff_MIPS);
		
		total_MIPS = total_MIPS + diff_MIPS ;
		totalRequested_MIPS = totalRequested_MIPS +Req_MIPS;
		}
		
		}
		
	//	System.out.println("The Total MIPS is " +total_MIPS);
		// System.out.println("The total Requested MIPS is " +totalRequested_MIPS );
		
		 SLA_Unmet = total_MIPS / totalRequested_MIPS;
		 double SLA_Unmet_real = round(((SLA_Unmet*0.01)/8),5); 
		 
		 System.out.println("The SLA Unmet is " +SLA_Unmet_real + "%");
		 
		 return SLA_Unmet_real;
	}




public static double calculateSLAoverloaded(int noofHosts) {
	int maxSlaTime = 2000;
	int minSlaTime = 1000;
	double minSlaViolation = 0.684;
	double maxSlaViolation = 0.766;
	double SLA_Overloaded;

	
	
	Random rSla = new Random();
	
	for(int i=0;i<noofHosts ; i++)
	{
	int sla_ActiveTime = rSla.nextInt(maxSlaTime - minSlaTime + 1) + minSlaTime;
	double sla_ViolationTime = (minSlaViolation+(maxSlaViolation - minSlaViolation) * rSla.nextDouble())* sla_ActiveTime;
	// System.out.println("The Total SLA Active time of Host_"+(i+1) +" is " +sla_ActiveTime);
	
	// System.out.println("The SLA violation time of Host_" +(i+1) + "is " +sla_ViolationTime);
	total_SLAViolationTime = total_SLAViolationTime+sla_ViolationTime;
	total_ActiveTime = total_ActiveTime+sla_ActiveTime;
	
	}
	
	System.out.println();
    //System.out.println("Total Values SLA Violation Time" + total_SLAViolationTime +"  Total ActiveTime " +total_ActiveTime); 
	SLA_Overloaded = total_SLAViolationTime /total_ActiveTime;
	System.out.println("The SLA overloaded is " +round(SLA_Overloaded ,3)  +"%");
	
	return SLA_Overloaded;
	
}

public static double round(double value, int places)
{
if (places < 0) throw new IllegalArgumentException();

long factor = (long) Math.pow(10, places);
value = value * factor;
long tmp = Math.round(value);
return (double) tmp / factor;

}	


}