package com.csdl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

class BFVAlgorithm // Swetha changed
{
	public static float[] sumOfcpuUtilizations;
	public static float[] threshold;
    static ArrayList<String> tempOHList = new ArrayList<String>();
    static ArrayList<String> tempUHList = new ArrayList<String>();
    static ArrayList<String> tempMigrating_VMCL = new ArrayList<String>();
    static ArrayList<Integer> tempSize_VMCL = new ArrayList<Integer>();
    static ArrayList<Integer> tempCpuValue_VMCL = new ArrayList<Integer>();
   	private static String[] overloadedHostList = new String[30];
	private static String[] underloadedHostList = new String[30];
	private static int[] underloadedHostRemainingSize = new int[30];
	private static int i; //swetha overloaded & underloaded host array elements
	private static int j=0; //swetha - overloaded & underloaded host array elements
	private static int rs=0;
	private static int mp =0; //for knapsack
	private static ArrayList<Integer> unSelected = new ArrayList<Integer>(); // knapsack
	private static String[] vmClusterMigratingList = new String[20]; //swetha
	private static String[] vmClusterMigratingListCopy = new String[20]; //swetha
	private static int[] vmClusterMigratingListSize = new int[20]; //swetha 
	private static int[] vmClusterMigratingListCpu = new int[20]; //swetha 
	//private static int m=0; //swetha
	public int hostCpu ;
	
	public static ArrayList<String> totalMigratedVms = new ArrayList<String>();
	public static int tempAdditionalHost =0;
	public static int additionalHosts = 1;
	public static ArrayList<Integer> additionalHostsBFV = new ArrayList<Integer>();
	public static double hostCreationEnergyBFV = 80.0;
	public static double migrationEnergyBFV ;
	static HashMap<Integer, Double> migrationPowerMappingBFV = new HashMap<Integer, Double>();

//	public static void main(String[] args) // removing main and moving to topology
	public static void migrateVirtualMachines(Host[] hosts)
	{
		/* BFVAlgorithm cd = new BFVAlgorithm(); //swe changed
		int minHosts = ClustersConstants.MIN_NO_OF_HOSTS;
		int maxHosts = ClustersConstants.MAX_NO_OF_HOSTS;
		
		// number of hosts computation
		Random rHosts = new Random();
		int noOfHosts = rHosts.nextInt(maxHosts - minHosts + 1) + minHosts;
		Host[] hosts = new Host[noOfHosts]; //added as hostsBFH , LIP , BFV
		System.out.println("Number Of Hosts::::  " + noOfHosts);
		
		// number of vms per host computation
		int index = 0;
		int minVms = ClustersConstants.MIN_NO_OF_VMS_PER_HOST;
		int maxVms = ClustersConstants.MAX_NO_OF_VMS_PER_HOST;
		Random rVms = new Random();
		
		// initializing the Weight of VMs on Host
		int minVmsize = ClustersConstants.MIN_SIZE_VM_GB ; //swetha - add in topology main
		int maxVmsize = ClustersConstants.MAX_SIZE_VM_GB ; //swetha - add in topology main
				
		int[] noOfVmsPerHost = new int[noOfHosts];
		do
		{
			int noOfVms = noOfVmsPerHost[index] = rVms.nextInt(maxVms - minVms + 1) + minVms; // added differently in do loop- V:1 
			hosts[index] = new Host(); // modified differently : 	hostsBFH[index] = new Host();
			hosts[index].id = "Host_" + (index + 1); // added in initHost method
			hosts[index].virtualMachine = new VirtualMachine[noOfVms]; // added in initHost method

			System.out.println("Number of VMS for " + hosts[index].id + "::::  " + noOfVms);
			System.out.println("CPU utilization and DiskSize of VMs::"); //edited swetha
			int totalVmsSize = 0 ; //swetha - added in topology : 72
			//hosts[index].virtualMachine[0].totalVmSize =0; 
			// calculating CPU utilization and Size in GB for each VM on the host

			int vi ;
			for ( vi = 0; vi < noOfVms; vi++) //vi becomes i in topology
			{
				hosts[index].virtualMachine[vi] = new VirtualMachine(); //vi is "i" and done for 3 policies
				hosts[index].virtualMachine[vi].id = hosts[index].id + "_VM_" + (vi + 1); //added in initVM method-v: 176 :V
				hosts[index].virtualMachine[vi].cpuUtilization = rVms.nextDouble() * 100; // v:80
				hosts[index].virtualMachine[vi].vmSize =  (rVms.nextInt(maxVmsize - minVmsize + 1) + minVmsize); //swetha added - 82
				System.out.print(hosts[index].virtualMachine[vi].cpuUtilization + "% -->"); //v:92
				System.out.print(hosts[index].virtualMachine[vi].vmSize + "GB\t"); //swetha added 93
			//	hosts[index].virtualMachine[vi].totalVmSize += hosts[index].virtualMachine[vi].vmSize; //swetha
				totalVmsSize += hosts[index].virtualMachine[vi].vmSize; //swetha added 94
			} 
			
			hosts[index].totalVmSize =totalVmsSize;
			System.out.print("\nSize of VMs on "+ hosts[index].id + " is " +hosts[index].totalVmSize + " GB"); //swetha
		//	System.out.println("Total Size***********" +totalVmsSize);
			
			// Size calculation of Hosts & calculating the remaining size after VMsSize
			 int maxHostSize= ClustersConstants.MAX_HOST_SIZE; //swetha - max size of any host is set to 3 TB added in V
			 int minHostSize = ClustersConstants.MIN_HOST_SIZE; //swetha - min size of any host is set to TB  added in V
			
			Random rHostSize = new Random(); //added in V
			hosts[index].size = rHostSize.nextInt((maxHostSize - minHostSize) + 1) + minHostSize; //added in V
			  
			 System.out.println("\nTotal Size of " + hosts[index].id + " is "+ hosts[index].size + " GB"); //swetha added in V
			
			hosts[index].remainingHostSize = hosts[index].size - hosts[index].totalVmSize; //added in V
			 
			 System.out.println("Remaining Size of "+ hosts[index].id + " is " +hosts[index].remainingHostSize + " GB" ); //added in V
			System.out.println("\n"); 
			index++; //already in V

		} while (index < noOfHosts); //already in V */
		
		// Until This part is being commented as its already checked and moved to Topology

		System.out.println("Checking for overloaded hosts........\n");
		// Determine host is overloaded or under loaded
		sumOfcpuUtilizations = new float[hosts.length];
		threshold = new float[hosts.length];
		for (int hostIndex = 0; hostIndex < hosts.length; hostIndex++)
		{
			computeThreshold(hosts, hostIndex);
			
			
		}
		System.out.println("*********** OverLoaded Host Array ***********");
		
		for(int m=0;m<i;m++)
		{
			
			System.out.println(overloadedHostList[m]);
			tempOHList.add(overloadedHostList[m]);
					
		}
		
		 migrationEnergyBFV = tempOHList.size()*6.0;
	//	System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$ Contents of tempOHList" +tempOHList);
		
		//System.out.println(" ");
		System.out.println("*********** Underloaded Host Array  ***********");
		
		for(int n=0;n<j;n++)
		{
			
			System.out.println(underloadedHostList[n]);
			tempUHList.add(underloadedHostList[n]);
		//	System.out.println("      " +hosts[n].remainingHostSize);
			// 
					
		} 
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$ UnderloadedHostList Length" +tempUHList.size());
		
		
		
		System.out.println("*********** Available Hosts List with Remaining Sizes & CPUutilization  ***********");
		for(int n=0;n<rs;n++)
		{
		// avgCpuHosts(hosts,n); // NOT REQUIRED to print
			System.out.println(underloadedHostList[n] +"		" +underloadedHostRemainingSize[n] +" GB" +"		" +avgCpuHosts(hosts,n) +" %" + "        " + round((10-(avgCpuHosts(hosts,n)*0.12)) ,1));
		
		} 
		//
	/*	// cluster formation for all hosts
	 * //This part is added in Topology creator :clusterFormations  : Changes are marked in that file too
		Random vmConnections = new Random();

		do
		{
			for (int hostIndex = 0; hostIndex < hosts.length; hostIndex++)
			{
				// initial node requires a minimum of one connection
				int minLinks = 1;
				int maxLinks = 2;

				for (int vmIndex = 0; vmIndex < hosts[hostIndex].virtualMachine.length; vmIndex++)
				{
					if (hosts[hostIndex].virtualMachine[vmIndex].connectedVMs == null)
					{
						int numLinks = vmConnections.nextInt(maxLinks - minLinks) + minLinks;
						for (int linkIndex = 0; linkIndex < numLinks; linkIndex++)
						{
							int establishedConnection = vmConnections.nextInt(hosts[hostIndex].virtualMachine.length);
							// minimum weight -- 1
							int linkWeight = vmConnections.nextInt(100) + 1;
							// make sure this link is not made already
							boolean isConnected = false;
							if (vmIndex > establishedConnection && hosts[hostIndex].virtualMachine[establishedConnection].connectedVMs != null)
							{
								for (int i = 0; i < hosts[hostIndex].virtualMachine[establishedConnection].connectedVMs.length; i++)
								{
									if (hosts[hostIndex].virtualMachine[establishedConnection].connectedVMs[i][0] == vmIndex)
									{
										isConnected = true;
										break;
									}
								}
							}

							if (!isConnected && (establishedConnection == vmIndex || (!hosts[hostIndex].virtualMachine[vmIndex].addVMConnection(establishedConnection, linkWeight))))
							{
								linkIndex--;
								continue;
							}

							hosts[hostIndex].virtualMachine[establishedConnection].isCommunicatingWithVM = true;
						}
						minLinks = 0;
						maxLinks = 2;
					}
				}
			}
		} while (!areAllConnectionsMade(hosts)); //This part is added in Topology creator :clusterFormations */ 

		

		
		// cluster the overloaded hosts
		boolean shouldCluster = true;
		for (int hostIndex = 0, hostEmpty = 0, hostFill = 0; hostIndex < hosts.length; hostIndex++)
		{
			tempSize_VMCL.clear();
			tempCpuValue_VMCL.clear();
			tempMigrating_VMCL.clear();
			if (hostIndex == hostEmpty) {
				// System.out.println("Empty VMClusterMigrating List");
				for( int l=0;l<vmClusterMigratingList.length;l++)
				{
					vmClusterMigratingList[l] = null;
					// tempSize_VMCL.remove(l);
				}
				hostEmpty++;
			
			}
			
			
			// if CPU utilization is more than the threshold cluster the host
			if (sumOfcpuUtilizations[hostIndex] > threshold[hostIndex])
			{
				String hostID = hosts[hostIndex].id;
				if (shouldCluster)
				{
					clusterHostBFV(hosts, hostID); //swetha
				}
				
				hosts[hostIndex].clusters = null;
				computeClusters(hosts, hostID);

				int clusterIndex = selectCluster(hosts[hostIndex]);
				//System.out.println("##########################Calling AllocateClusterTohost");
				boolean isAllocationSuccess = allocateClusterToHost(hosts, hosts[hostIndex], clusterIndex);
	
				shouldCluster = true;
				if (isAllocationSuccess)
				{
			//ss		System.out.println("\nchecking if the host is still overloaded...");
					computeThreshold(hosts, hostIndex);
					if (sumOfcpuUtilizations[hostIndex] > threshold[hostIndex])
					{
			//ss			System.out.println("As the host is still overloaded re-itereating allocation policy");
						shouldCluster = false;
						hostIndex--;

						Host host = hosts[hostIndex + 1];
					//SS	System.out.println("-----------------------------Clustering " + host.id + " since it is is overloaded. -----------------------------------");
						for (int vmIndex = 0; vmIndex < host.virtualMachine.length; vmIndex++)
						{
							if (host.virtualMachine[vmIndex].connectedVMs != null)
							{
								for (int i = 0; i < host.virtualMachine[vmIndex].connectedVMs.length; i++)
								{
							//SS		System.out.println(host.virtualMachine[vmIndex].id + "-->" + host.virtualMachine[(host.virtualMachine[vmIndex].connectedVMs[i][0])].id + "--> (" + host.virtualMachine[vmIndex].connectedVMs[i][1] + ")");
								}
							}
						}

					// ss	System.out.println("------------------------------Independent VMs of this host-------------------------------");
						for (int vmIndex = 0; vmIndex < host.virtualMachine.length; vmIndex++)
						{
							if (host.virtualMachine[vmIndex].connectedVMs == null && !host.virtualMachine[vmIndex].isCommunicatingWithVM)
							{
						//ss		System.out.println(host.virtualMachine[vmIndex].id);
							}
						}
					} else
					{
						shouldCluster = true;
					//	System.out.println();
						System.out.println("-------------------This host becomes Normal Utilized by migrating the following -----------------");
						System.out.println("------------------------- VMCluster(s) to VMClusterMigrating List -------------------------------");
						System.out.println();
						System.out.println("The VMClusterMigrating List Details are ");
						System.out.println("VMCluster" + "        " + "Size (GB)" + "        " +"CPU Utilization" );
						System.out.println();
					//	System.out.println("*************************************************************************************************************************************************");
						
					}
				}
			}
			if (hostFill == hostIndex)
			{
			//	System.out.println("Filled VMClusterMigrating List "+ hostFill + "  " + hostIndex ); 
				//The following is the output - printed under "And the VMClusterMigrating List is "
				for( int l=0;l<vmClusterMigratingList.length;l++)
				{
					if (vmClusterMigratingList[l] != null ) 
					{
						System.out.println(vmClusterMigratingList[l] +"        " +vmClusterMigratingListSize[l] +"        "+vmClusterMigratingListCpu[l] );
						
						tempSize_VMCL.add(vmClusterMigratingListSize[l]);
						tempCpuValue_VMCL.add(vmClusterMigratingListCpu[l]);
						tempMigrating_VMCL.add(vmClusterMigratingList[l]);
						
						
					}
					
					
				}
				hostFill++;
				
				// System.out.println ("%%%%%%%%%%%%%%%%%% Size of vmcl" +tempSize_VMCL);
				// copying the array inorder to eliminate null values and print the length.
				System.arraycopy(vmClusterMigratingList, 0, vmClusterMigratingListCopy, 0, 10);
				
		/*	System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$Copied Array");
				for(int k=0;k<10;k++)
				{
				System.out.println(vmClusterMigratingListCopy[k] + "   ");
					
				} //*/
				
				getLength(vmClusterMigratingListCopy);
					
			} // if hostfill = host index
			
		 	 
		//	System.out.println ("%%%%%%%%%%%%%%%%%% Size of vmcl" +tempSize_VMCL);
		//	System.out.println ("%%%%%%%%%%%%%%%%%% CPU Value of vmcl" +tempCpuValue_VMCL);
		//	System.out.println ("%%%%%%%%%%%%%%%%%% Size of vmcl" +tempMigrating_VMCL);
			
			/*ArrayList to Array Conversion */
			/*Displaying Array elements*/
			
		//	System.out.println("&&&&&&&&&&&&&&&&&&&&&&& Printing Array to Array list - Size of VMCL ");
			int realSize_VMCL[] = new int[tempSize_VMCL.size() + 1];
			for(int j =1;j<=tempSize_VMCL.size();j++){ // changed it for knapsack j =1
				realSize_VMCL[j] = (int) tempSize_VMCL.get(j-1);
			//	System.out.println(realSize_VMCL[j]);
				}

		//	System.out.println("&&&&&&&&&&&&&&&&&&&&&&& Printing Array to Array list - CPUValue of VMCL ");
			int realCpuValue_VMCL[] = new int[tempCpuValue_VMCL.size() + 1];
			for(int j =1;j<=tempCpuValue_VMCL.size();j++){ // changed it for knapsack j=1
				realCpuValue_VMCL[j] = tempCpuValue_VMCL.get(j-1);
			//	System.out.println(realCpuValue_VMCL[j]);
				}	
			
	//		System.out.println("The Cpu factor for these clusters is " +sumAll(realCpuValue_VMCL));
		//	System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@Printing the length again");
			//getLength(vmClusterMigratingListCopy);
			// the count N is nothing but the size of the array LIst
			
		//	System.out.println(" The Count is "+tempSize_VMCL.size());
			
			
			int realCount = tempSize_VMCL.size();
			
		//	int remSize = underloadedHostRemainingSize[0];
			if(realCount>0)
			{
				if(underloadedHostRemainingSize != null)
				{
				int remSize = underloadedHostRemainingSize[mp];
			knapSack(realSize_VMCL,realCpuValue_VMCL,remSize,realCount); // swetha made it static
			System.out.println("The above VM clusters are migrated to the "+underloadedHostList[mp]);
			mp++;
				}
				else
				{
					System.out.println("As there is no host available to accomodate the above clusters , a new host is being created ");
					tempAdditionalHost ++ ;
					additionalHostsBFV.add(tempAdditionalHost);
					//	System.out.println("The above clusters are moved to the new " +"Host" +(noOfHosts + 1));
				}
			}
		}
		
		
	} // migratevirtualmachines
	
public static int sumAll(int[] realCpuValue_VMCL)
	{
	int result = 0;
	 int[] realCpuValue = new int[realCpuValue_VMCL.length];
	for(int i = 0 ; i<realCpuValue_VMCL.length;i++){
	  result+=realCpuValue_VMCL[i];
	} 
	return result;
	
		
	}

/*	private static double sumof(ArrayList<Integer> tempSize_VMCL2)
	{
		ArrayList<Integer> s = new ArrayList<Integer>();
		
		int result = 0;
		 for(int i = 0 ; i<tempSize_VMCL2.size();i++){
		  result+=numbers[i];
		} 
		return result;
		return 0;
	} */

	public static double round(double value, int places)
		{
		if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
		
	}

	public static void knapSack(int[] wt, int[] val, int W, int N) // changed it to static - Swetha //wt - size of VMCLs , val : cpu of VMCLs, W - Available host size , N- count of VMCLs
    {
       System.out.println("Printing Weight" +W +"Value N "+N);
      
       
		int NEGATIVE_INFINITY = Integer.MIN_VALUE;
        int[][] m = new int[N + 1][W + 1];
        int[][] sol = new int[N + 1][W + 1];
 
        for (int i = 1; i <= N; i++)
        {
            for (int j = 0; j <= W; j++)
            {
                int m1 = m[i - 1][j];
                int m2 = NEGATIVE_INFINITY; 
             //   System.out.println("  Printing i and J " +i +"	"+j);
                if (j >= wt[i])
                    m2 = m[i - 1][j - wt[i]] + val[i];
                /** select max of m1, m2 **/
                m[i][j] = Math.max(m1, m2);
                sol[i][j] = m2 > m1 ? 1 : 0;
            }
        }        
        /** make list of what all items to finally select **/
        int[] selected = new int[N + 1];
        for (int n = N, w = W; n > 0; n--)
        {
            if (sol[n][w] != 0)
            {
                selected[n] = 1;
                w = w - wt[n];
            }
            else
               selected[n] = 0;
           
            
            
        }
        /** Print finally selected items **/
        System.out.println("VMClusters selected for migration are  : ");
        for (int i = 1; i < N + 1; i++)
        {
            if (selected[i] == 1)
            {
                System.out.print(i +" ");
                System.out.println();
            }
         /*   else 
            {
               	unSelected.add(i);
            } */
        }
    /*   if(unSelected.size()>0)
        {
         System.out.print("The Clusters  "); 
          int unSelected_VMCL[] = new int[unSelected.size()+1];
        int wt_VMCL[] = new int[unSelected.size() +1];
          int cpu_VMCL[] = new int[unSelected.size() +1];
      	
  		for(int j =1;j<=unSelected.size();j++)
  		{
  			unSelected_VMCL[j] = unSelected.get(j-1);
  		  System.out.println(unSelected_VMCL[j] + "	");
  		  }
  			System.out.println("The Cluster(s) cannot be migrated to this host as it gets overloaded");
  			System.out.println("A new host is being created and the clusters are moved to it ");
  			
  	     
  		
        } */
        
    }
	
	public static int getLength(String[] vmClusterMigratingListCopy2){
	    int count = 0;
	    for(int index=0;index <vmClusterMigratingListCopy2.length ; index++ )
	        if (vmClusterMigratingListCopy[index] != null)
	            ++count;
	    if(count>0)
	    {
	    	System.out.println();
	    	System.out.println("The number of VMClusters is " +count);
	    }
		return count;
	}
	
	private static void computeThreshold(Host[] hosts, int hostIndex)
	{
		int noOfVms = hosts[hostIndex].virtualMachine.length;
	    if(noOfVms == 0)
	    {
	    	sumOfcpuUtilizations[hostIndex] = threshold[hostIndex] = 0;
	    	return;
	    }

	    float medianOfcpuUtilizations = 0.0f;
		float[] temp_cpuUtilization_values = new float[noOfVms];//swetha
		for (int j = 0; j < noOfVms; j++)
		{
			temp_cpuUtilization_values[j] = (float) (hosts[hostIndex].virtualMachine[j].cpuUtilization / 100);
			sumOfcpuUtilizations[hostIndex] += temp_cpuUtilization_values[j];
		}

		// applying the factor
		float factor = 0.1f;
		sumOfcpuUtilizations[hostIndex] *= factor;

		Arrays.sort(temp_cpuUtilization_values);

		if (noOfVms % 2 == 0)
		{
			medianOfcpuUtilizations = temp_cpuUtilization_values[noOfVms / 2];
		} else
		{
		//	medianOfcpuUtilizations = (temp_cpuUtilization_values[noOfVms / 2] + temp_cpuUtilization_values[(noOfVms + 1) / 2]) / 2; //swetha commented and added below from BFH/LIP to solve arrayoutofbound issue
			medianOfcpuUtilizations = (noOfVms == 1) ? temp_cpuUtilization_values[0] : (temp_cpuUtilization_values[noOfVms / 2] + temp_cpuUtilization_values[(noOfVms / 2) + 1]) / 2; // added from BFH/LIP:Veena
		}

		// absolute difference with the median and median evaluation
		for (int k = 0; k < noOfVms; k++)
		{
			temp_cpuUtilization_values[k] = Math.abs(temp_cpuUtilization_values[k] - medianOfcpuUtilizations);
		}

		Arrays.sort(temp_cpuUtilization_values);

		if (noOfVms % 2 == 0)
		{
			medianOfcpuUtilizations = temp_cpuUtilization_values[noOfVms / 2];
		} else
		{
		//	medianOfcpuUtilizations = (temp_cpuUtilization_values[noOfVms / 2] + temp_cpuUtilization_values[(noOfVms / 2) + 1]) / 2; Swetha commented and added below to solve arrayoutof bound exception
			medianOfcpuUtilizations = (noOfVms == 1) ? temp_cpuUtilization_values[0] : (temp_cpuUtilization_values[noOfVms / 2] + temp_cpuUtilization_values[(noOfVms / 2) + 1]) / 2; // added from BFH/LIP:Veena
		}

		// System.out.println("Final Median Value:" +
		// medianOfcpuUtilizations);

		// determine threshold value
		float s = 2.5f; // safety parameter value predetermined for the
						// algorithm
		threshold[hostIndex] = Math.abs(1 - (s * medianOfcpuUtilizations));
		// System.out.println("Upper Threshold value is: " + threshold);

		// System.out.println("sum of cpuutilizations: " + (factor *
		// sumOfcpuUtilizations));
		// checking if sum of cpu utilizations is greater than threshold

		if (sumOfcpuUtilizations[hostIndex] > threshold[hostIndex])
		{
			overloadedHostList[i++]=hosts[hostIndex].id  ;//+ " is overloaded";
		} else
		{
			underloadedHostList[j++]=hosts[hostIndex].id ;//+ " is not overloaded";
			underloadedHostRemainingSize[rs++] = hosts[hostIndex].remainingHostSize;
			// System.out.println("REmaining%%%%%%%%%%% " + hosts[hostIndex].remainingHostSize);
		}
		
	}

	private static int avgCpuHosts(Host hosts[] , int hostIndex )
	{
		//for (int hostIndex = 0; hostIndex < hosts.length; hostIndex++)
		//{
		//for(int n=0;n<j;n++){			
			float currHostUtilization = 0.0f;
			float avg ;
			int average;

			for (int vmIndex = 0; vmIndex < hosts[hostIndex].virtualMachine.length; vmIndex++)
			{
				currHostUtilization += hosts[hostIndex].virtualMachine[vmIndex].cpuUtilization;
			}
		
		avg=	(currHostUtilization)/(hosts[hostIndex].virtualMachine.length);
		average = (int)	Math.ceil(avg);
		
		return average;
		// System.out.println(" Average CPU Utilization of Underloaded Hosts is " +average);
		//}
	//}
	}
	
	private static void clusterHostBFV(Host[] hosts, String hostID)
	{
		float linkWeightAverage = 0.0f;
		int numOfLinks = 0;
		Host host = null;
		for (int hostIndex = 0; hostIndex < hosts.length; hostIndex++)
		{
			if (hosts[hostIndex].id.equals(hostID))
			{
				host = hosts[hostIndex];
				for (int vmIndex = 0; vmIndex < hosts[hostIndex].virtualMachine.length; vmIndex++)
				{
					if (hosts[hostIndex].virtualMachine[vmIndex].connectedVMs != null)
					{
						for (int i = 0; i < hosts[hostIndex].virtualMachine[vmIndex].connectedVMs.length; i++)
						{
							// System.out.println("before:::  " + vmIndex + ", " +
							// hosts[hostIndex].virtualMachine[vmIndex].connectedVMs[i][0]);
							linkWeightAverage += hosts[hostIndex].virtualMachine[vmIndex].connectedVMs[i][1];
							numOfLinks++;
						}
					}
				}

				break;
			}
		}
		// average of link weight
		linkWeightAverage /= numOfLinks;

		// System.out.println("link weight average for " + hostID + "::::: " +
		// linkWeightAverage);

		for (int vmIndex = 0; vmIndex < host.virtualMachine.length; vmIndex++)
		{
			if (host.virtualMachine[vmIndex].connectedVMs != null)
			{
				for (int i = 0; i < host.virtualMachine[vmIndex].connectedVMs.length; i++)
				{
					if (host.virtualMachine[vmIndex].connectedVMs[i][1] < linkWeightAverage)
					{
						host.virtualMachine[vmIndex].removeConnectionWithVM(i);

						if (host.virtualMachine[i].connectedVMs == null)
						{
							host.virtualMachine[i].isCommunicatingWithVM = false;
						}

						if (host.virtualMachine[vmIndex].connectedVMs == null)
						{
							break;
						}
					}
				}
			}
		}

		for (int vmIndex = 0; vmIndex < host.virtualMachine.length; vmIndex++)
		{
			if (host.virtualMachine[vmIndex].connectedVMs != null)
			{
				host.virtualMachine[vmIndex].isCommunicatingWithVM = true;

				for (int i = 0; i < host.virtualMachine[vmIndex].connectedVMs.length; i++)
				{
					// System.out.println("after:::  " + vmIndex + ", " +
					// host.virtualMachine[vmIndex].connectedVMs[i][0]);
					host.virtualMachine[host.virtualMachine[vmIndex].connectedVMs[i][0]].isCommunicatingWithVM = true;
				}
			}
		}

		// if CPU utilization is more than the threshold cluster the host
		System.out.println(" ");
		System.out.println("*************************************************************************************************************************************************");
		System.out.println(" ");
		System.out.println("-----------------------------Clustering " + host.id + " since it is is overloaded. -----------------------------------");
		for (int vmIndex = 0; vmIndex < host.virtualMachine.length; vmIndex++)
		{
			if (host.virtualMachine[vmIndex].connectedVMs != null)
			{
				for (int i = 0; i < host.virtualMachine[vmIndex].connectedVMs.length; i++)
				{
					System.out.println(host.virtualMachine[vmIndex].id + "-->" + host.virtualMachine[(host.virtualMachine[vmIndex].connectedVMs[i][0])].id + "--> (" + host.virtualMachine[vmIndex].connectedVMs[i][1] + ")");
				}
			}
		}

		System.out.println("------------------------------Independent VMs of this host-------------------------------");
		for (int vmIndex = 0; vmIndex < host.virtualMachine.length; vmIndex++)
		{
			if (host.virtualMachine[vmIndex].connectedVMs == null && !host.virtualMachine[vmIndex].isCommunicatingWithVM)
			{
				System.out.println(host.virtualMachine[vmIndex].id);
			}
		}
	}

	// compute clusters of a host
	public static void computeClusters(Host[] hosts, String hostID)
	{
		for (int hostIndex = 0; hostIndex < hosts.length; hostIndex++)
		{
			if (hosts[hostIndex].id.equals(hostID))
			{
				Host host = hosts[hostIndex];
				for (int vmIndex = 0; vmIndex < host.virtualMachine.length; vmIndex++)
				{
					if (host.virtualMachine[vmIndex].connectedVMs == null && !host.virtualMachine[vmIndex].isCommunicatingWithVM)
					{
						addCluster(host, vmIndex, -1, true);
					} else if (host.virtualMachine[vmIndex].connectedVMs != null)
					{
						recursive(host, vmIndex);
						for (int i = 0; i < host.virtualMachine[vmIndex].connectedVMs.length; i++)
						{
							int connectedVMIndex = host.virtualMachine[vmIndex].connectedVMs[i][0];
							addCluster(host, vmIndex, connectedVMIndex, false);
						}
					}
				}

				break;
			}
		}
		for (int hostIndex = 0; hostIndex < hosts.length; hostIndex++)
		{
			if (hosts[hostIndex].id.equals(hostID))
			{
				Host host = hosts[hostIndex];
				for (int i = 0; i < host.clusters.length; i++)
				{
					Arrays.sort(host.clusters[i]);
					host.clusters[i] = removeDuplicates(host.clusters[i]);
				}
			}
		}

		
	} //compute

	private static void recursive(Host host, int vmIndex)
	{
		if (host.virtualMachine[vmIndex].connectedVMs != null && !host.virtualMachine[vmIndex].isClustered)
		{
			host.virtualMachine[vmIndex].isClustered = true;
			int clusteringVMIndex = host.virtualMachine[vmIndex].connectedVMs[0][0];
			recursive(host, clusteringVMIndex);
			addCluster(host, vmIndex, clusteringVMIndex, false);
		}
	}

	private static void addCluster(Host host, int vmIndex, int clusteringVMIndex, boolean isIndependentCluster)
	{
		if (host.clusters == null)
		{
			if (isIndependentCluster)
			{
				host.clusters = new int[1][1];
				host.clusters[0][0] = vmIndex;
			} else
			{
				host.clusters = new int[1][2];
				host.clusters[0][0] = vmIndex;
				host.clusters[0][1] = clusteringVMIndex;
			}
		} else
		{
			int[][] temp = new int[host.clusters.length][];
			for (int i = 0; i < host.clusters.length; i++)
			{
				temp[i] = new int[host.clusters[i].length];
				System.arraycopy(host.clusters[i], 0, temp[i], 0, host.clusters[i].length);
			}

			if (isIndependentCluster)
			{
				host.clusters = new int[temp.length + 1][];
				for (int i = 0; i < temp.length; i++)
				{
					host.clusters[i] = new int[temp[i].length];
					System.arraycopy(temp[i], 0, host.clusters[i], 0, temp[i].length);
				}
				host.clusters[temp.length] = new int[1];
				host.clusters[temp.length][0] = vmIndex;
			} else
			{
				host.clusters = new int[temp.length][];
				boolean isFound = false;
				for (int i = 0; i < temp.length; i++)
				{
					if (!isFound)
					{
						for (int j = 0; j < temp[i].length; j++)
						{
							if (temp[i][j] == vmIndex || temp[i][j] == clusteringVMIndex)
							{
								host.clusters[i] = new int[temp[i].length + 1];
								System.arraycopy(temp[i], 0, host.clusters[i], 0, temp[i].length);
								if (temp[i][j] == vmIndex)
								{
									host.clusters[i][temp[i].length] = clusteringVMIndex;
								} else
								{
									host.clusters[i][temp[i].length] = vmIndex;
								}

								isFound = true;

								break;
							}
						}

						if (isFound)
						{
							continue;
						}
					}

					host.clusters[i] = new int[temp[i].length];
					System.arraycopy(temp[i], 0, host.clusters[i], 0, temp[i].length);
				}

				if (!isFound)
				{
					host.clusters = new int[temp.length + 1][];
					for (int i = 0; i < temp.length; i++)
					{
						host.clusters[i] = new int[temp[i].length];
						System.arraycopy(temp[i], 0, host.clusters[i], 0, temp[i].length);
					}
					host.clusters[temp.length] = new int[2];
					host.clusters[temp.length][0] = vmIndex;
					host.clusters[temp.length][1] = clusteringVMIndex;
				}
			}
		}
	}

	public static int[] removeDuplicates(int[] input)
	{
		int j = 0;
		int i = 1;
		// return if the array length is less than 2
		if (input.length < 2)
		{
			return input;
		}
		while (i < input.length)
		{
			if (input[i] == input[j])
			{
				i++;
			} else
			{
				input[++j] = input[i++];
			}
		}
		int[] output = new int[j + 1];
		for (int k = 0; k < output.length; k++)
		{
			output[k] = input[k];
		}

		return output;
	}

	/*
	 * selection algorithm -- minimum CPU-Utilization
	 * 
	 * host that is overloaded
	 */
	public static int selectCluster(Host host)
	{
		int clusterIndex = -1;
		float minCPUUtilization = 1000.0f;
		for (int i = 0; i < host.clusters.length; i++)
		{
			float currClusterUtiliation = 0.0f;

			for (int j = 0; j < host.clusters[i].length; j++)
			{
				currClusterUtiliation += host.virtualMachine[host.clusters[i][j]].cpuUtilization;
			}

			if (currClusterUtiliation < minCPUUtilization)
			{
				minCPUUtilization = currClusterUtiliation;
				clusterIndex = i;
			}
		}

		return clusterIndex;
	}

	/*
	 * allocation algorithm -- minimum CPU-Utilization
	 * 
	 * host represents overloaded host
	 * 
	 * clusterIndex is the cluster which needs to be allocated to least
	 * overloaded Host and remove those set of VMs from the original host
	 */
    public static boolean allocateClusterToHost(Host hosts[], Host host, int clusterIndex)
	{
		// LIP Allocation policy
	//swetha	System.out.println("-------- ALLOCATION POLICY-------------");
		// time to compute least overloaded host
		int leastOverloadedHostIndex = 0;
		float minUtilization = 1000.0f;
		for (int hostIndex = 0; hostIndex < hosts.length; hostIndex++)
		{
			// skip if the hosts are similar
			if (hosts[hostIndex].id.equals(host.id))
			{
				continue;
			}
			
			float currHostUtilization = 0.0f;

			for (int vmIndex = 0; vmIndex < hosts[hostIndex].virtualMachine.length; vmIndex++)
			{
				currHostUtilization += hosts[hostIndex].virtualMachine[vmIndex].cpuUtilization;
			}

			if (currHostUtilization < minUtilization)
			{
				leastOverloadedHostIndex = hostIndex;
				minUtilization = currHostUtilization;
			}
		}

	//swetha	System.out.println("least overloaded host::   " + hosts[leastOverloadedHostIndex].id); */

		// --------------------------------------------------------------------------------------------//

		int noOfVms = hosts[leastOverloadedHostIndex].virtualMachine.length + host.clusters[clusterIndex].length;
		float medianOfcpuUtilizations = 0.0f;
		float[] temp_cpuUtilization_values = new float[(noOfVms+50)]; //swetha
		float sumOfCPUUtilizations = 0.0f;
		float tempThreshold = 0.0f;
		int cpuUtilIndex = 0;
		for (; cpuUtilIndex < hosts[leastOverloadedHostIndex].virtualMachine.length; cpuUtilIndex++)
		{
			temp_cpuUtilization_values[cpuUtilIndex] = (float) (hosts[leastOverloadedHostIndex].virtualMachine[cpuUtilIndex].cpuUtilization / 100);
			sumOfCPUUtilizations += temp_cpuUtilization_values[cpuUtilIndex];
		}

		for (int index = 0; index < host.clusters[clusterIndex].length; index++)
		{
			temp_cpuUtilization_values[cpuUtilIndex] = (float) (host.virtualMachine[host.clusters[clusterIndex][index]].cpuUtilization / 100);
			sumOfCPUUtilizations += temp_cpuUtilization_values[cpuUtilIndex];
			cpuUtilIndex++;
		}

		// applying the factor
		float factor = 0.1f;
		sumOfCPUUtilizations *= factor;

		Arrays.sort(temp_cpuUtilization_values);

		if (noOfVms % 2 == 0)
		{
			medianOfcpuUtilizations = temp_cpuUtilization_values[noOfVms / 2];
		} else
		{
			medianOfcpuUtilizations = (temp_cpuUtilization_values[noOfVms / 2] + temp_cpuUtilization_values[(noOfVms + 1) / 2]) / 2;
		}

		// absolute difference with the median and median evaluation
		for (int k = 0; k < noOfVms; k++)
		{
			temp_cpuUtilization_values[k] = Math.abs(temp_cpuUtilization_values[k] - medianOfcpuUtilizations);
		}

		Arrays.sort(temp_cpuUtilization_values);

		if (noOfVms % 2 == 0)
		{
			medianOfcpuUtilizations = temp_cpuUtilization_values[noOfVms / 2];
		} else
		{
			medianOfcpuUtilizations = (temp_cpuUtilization_values[noOfVms / 2] + temp_cpuUtilization_values[(noOfVms / 2) + 1]) / 2;
		}

		// safety parameter value predetermined for the algorithm
		float s = 2.5f;
		tempThreshold = Math.abs(1 - (s * medianOfcpuUtilizations));

		/* swetha if (sumOfCPUUtilizations > tempThreshold)
		{
			System.out.println("This host is getting overloaded on allocating the cluster. So cannot be allocated");
			return false;
		} Swetha */

		// --------------------------------------------------------------------------------------------//

	//	 System.out.print("The following cluster is added to the Migrating VMClusterList :: ");
		// mapping for migration energy
				migrationPowerMappingBFV.put(2, 4.0);
				migrationPowerMappingBFV.put(4, 4.7);
				migrationPowerMappingBFV.put(6, 5.4);
				migrationPowerMappingBFV.put(8, 6.0);
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		int m = 0;
		while(vmClusterMigratingList[m] != null){
			m++;
		}
		for (int index = 0; index < host.clusters[clusterIndex].length; index++)
		{
			// not required to print as we are printing above
			// System.out.print(host.virtualMachine[host.clusters[clusterIndex][index]].id);
		//	System.out.println("The size of" +host.virtualMachine[host.clusters[clusterIndex][index]].id +" is " +host.virtualMachine[host.clusters[clusterIndex][index]].vmSize +" GB");
			 vmClusterMigratingList[m] = host.virtualMachine[host.clusters[clusterIndex][index]].id;
				totalMigratedVms.add(host.virtualMachine[host.clusters[clusterIndex][index]].id) ;
			 vmClusterMigratingListSize[m] = host.virtualMachine[host.clusters[clusterIndex][index]].vmSize;
			 vmClusterMigratingListCpu[m] = (int) Math.ceil(host.virtualMachine[host.clusters[clusterIndex][index]].cpuUtilization);
			 m++;
			 map.put(host.clusters[clusterIndex][index], host.virtualMachine[host.clusters[clusterIndex][index]].id);

			if (index + 1 < host.clusters[clusterIndex].length)
			{
			//	System.out.println(", ");
			}
			
		//	migrationEnergyBFV=	underloadedHostList.length * 6.0 ;
		//	migrationEnergyBFV += migrationPowerMappingBFV.get((int) host.virtualMachine[host.clusters[clusterIndex][index]].ramUtilization);
		}
		System.out.println(" "); //swetha
		/*System.out.println("VMClusterMigrating List");
		for(int l=0;l<m;l++)
		{
		System.out.println(vmClusterMigratingList[l]);
		}*/ //swetha

				
		for (int vI = 0; vI < host.virtualMachine.length; vI++)
		{
			map.put(vI, host.virtualMachine[vI].id);
		}

		// remove the cluster from current host and allocate it to new host
		Host leastOverloadedHost = hosts[leastOverloadedHostIndex];
		int[] leastOverloadedHostNewCluster = null;

		for (int index = 0; index < host.clusters[clusterIndex].length; index++)
		{
			for (int vmIndex = 0; vmIndex < host.virtualMachine.length; vmIndex++)
			{
				if (host.virtualMachine[vmIndex].id.equals(map.get(host.clusters[clusterIndex][index])))
				{
					// allocate this VM to least overloaded host
					VirtualMachine[] tempLeastVM = new VirtualMachine[leastOverloadedHost.virtualMachine.length];

					System.arraycopy(leastOverloadedHost.virtualMachine, 0, tempLeastVM, 0, leastOverloadedHost.virtualMachine.length);

					leastOverloadedHost.virtualMachine = new VirtualMachine[tempLeastVM.length + 1];
					System.arraycopy(tempLeastVM, 0, leastOverloadedHost.virtualMachine, 0, tempLeastVM.length);
					leastOverloadedHost.virtualMachine[tempLeastVM.length] = host.virtualMachine[vmIndex];

					if (leastOverloadedHostNewCluster == null)
					{
						leastOverloadedHostNewCluster = new int[1];
						leastOverloadedHostNewCluster[0] = tempLeastVM.length;
					} else
					{
						int[] tempCluster = new int[leastOverloadedHostNewCluster.length];
						System.arraycopy(leastOverloadedHostNewCluster, 0, tempCluster, 0, leastOverloadedHostNewCluster.length);

						leastOverloadedHostNewCluster = new int[tempCluster.length + 1];
						System.arraycopy(tempCluster, 0, leastOverloadedHostNewCluster, 0, tempCluster.length);

						leastOverloadedHostNewCluster[tempCluster.length] = tempLeastVM.length;
					}

					// allocate & copy the values till vmIndex into temp for overloaded host
					VirtualMachine[] temp = new VirtualMachine[host.virtualMachine.length - 1];
					System.arraycopy(host.virtualMachine, 0, temp, 0, vmIndex);

					// copy the values after vmIndex into temp
					System.arraycopy(host.virtualMachine, vmIndex + 1, temp, vmIndex, temp.length - vmIndex);

					// now reallocate & copy the updated temp into overloaded virtualmachine
					host.virtualMachine = new VirtualMachine[temp.length];
					System.arraycopy(temp, 0, host.virtualMachine, 0, temp.length);

					break;
				}
			}
		}
		
		for (int vI = 0; vI < host.virtualMachine.length; vI++)
		{
			if (host.virtualMachine[vI].connectedVMs != null)
			{
				for (int i = 0; i < host.virtualMachine[vI].connectedVMs.length; i++)
				{
					String id = map.get(host.virtualMachine[vI].connectedVMs[i][0]);
					for (int vmIndex = 0; vmIndex < host.virtualMachine.length; vmIndex++)
					{
						if (id.equals(host.virtualMachine[vmIndex].id))
						{
							host.virtualMachine[vI].connectedVMs[i][0] = vmIndex;
						}
					}
				}
			}
		}
		
		// update the connectedVMs accordingly for overloaded host
		for (int vmIndex = leastOverloadedHostNewCluster[0]; vmIndex < leastOverloadedHost.virtualMachine.length; vmIndex++)
		{
			if (leastOverloadedHost.virtualMachine[vmIndex].connectedVMs != null)
			{
				for (int i = 0; i < leastOverloadedHost.virtualMachine[vmIndex].connectedVMs.length; i++)
				{
					String id = map.get(leastOverloadedHost.virtualMachine[vmIndex].connectedVMs[i][0]);
					for (int connectedVMIndex = leastOverloadedHostNewCluster[0]; connectedVMIndex < leastOverloadedHost.virtualMachine.length; connectedVMIndex++)
					{
						if (id.equals(leastOverloadedHost.virtualMachine[connectedVMIndex].id))
						{
							leastOverloadedHost.virtualMachine[vmIndex].connectedVMs[i][0] = connectedVMIndex;
							break;
						}
					}
				}
			}
		}
		
		return true;
	} 

	private static boolean areAllConnectionsMade(Host[] hosts)
	{
		// if no connection is established between any vms then reiterate
		for (int hostIndex = 0; hostIndex < hosts.length; hostIndex++)
		{
			for (int vmIndex = 0; vmIndex < hosts[hostIndex].virtualMachine.length; vmIndex++)
			{
				if (hosts[hostIndex].virtualMachine[vmIndex].connectedVMs == null && !hosts[hostIndex].virtualMachine[vmIndex].isCommunicatingWithVM)
				{
					return false;
				}
			}
		}
		return true;
	} 
}