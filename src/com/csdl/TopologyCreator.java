package com.csdl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class TopologyCreator
{
	public static double topologyEnergy = 0.0;

	private static final int MAX_LINK_DEPTH_LIMIT = 3;
	private static final int RAM_MIN = 2;
	private static final int RAM_MAX = 8;
	public static int totalnoofVMs = 0; // swetha
	public static double migrationEnergy_LIP;

	public static void main(String[] args)
	{
		System.out.println(":::::::::::::::::::::::::::::::::: TOPOLOGY FORMATION  ::::::::::::::::::::::::::::::::::");
		System.out.println(); // swetha added this and above
		int minHosts = ClustersConstants.MIN_NO_OF_HOSTS;
		int maxHosts = ClustersConstants.MAX_NO_OF_HOSTS;

		// powerConsumption array
		ArrayList<Double> powerArray = new ArrayList<Double>();

		// number of hosts computation
		Random rHosts = new Random();
		int noOfHosts = rHosts.nextInt(maxHosts - minHosts + 1) + minHosts;
		Host[] hostsBFH = new Host[noOfHosts];
		Host[] hostsLIP = new Host[noOfHosts];
		Host[] hostsBFV = new Host[noOfHosts];
		Host[] hostsBFHNo = new Host[noOfHosts]; // 111111111111111111
		System.out.println("Number Of Hosts::::  " + noOfHosts);

		// number of vms per host computation
		int index = 0;
		int minVms = ClustersConstants.MIN_NO_OF_VMS_PER_HOST;
		int maxVms = ClustersConstants.MAX_NO_OF_VMS_PER_HOST;

		// initializing the Weight of VMs on Host
		int minVmsize = ClustersConstants.MIN_SIZE_VM_GB; // swetha - add in topology main
		int maxVmsize = ClustersConstants.MAX_SIZE_VM_GB; // swetha - add in topology main

		Random rVms = new Random();
		int[] noOfVmsPerHost = new int[noOfHosts];

		do
		{
			int noOfVms = 0; // newly added by V:1
			boolean isbreakLoop = false;
			do
			{
				noOfVms = rVms.nextInt(maxVms - minVms + 1) + minVms;
				if ((noOfVms % 3) >= 2)
				{
					isbreakLoop = true;
				}
			} while (!isbreakLoop); // newly added by V:1

			noOfVmsPerHost[index] = noOfVms; // newly modified by V : ~1

			hostsBFH[index] = new Host();
			hostsLIP[index] = new Host();
			hostsBFV[index] = new Host(); // added as bfh , bfv etc V:~1
			hostsBFHNo[index] = new Host(); // 111111111111111111

			initHost(hostsBFH[index], index, noOfVms);
			initHost(hostsLIP[index], index, noOfVms);
			initHost(hostsBFV[index], index, noOfVms);
			initHost(hostsBFHNo[index], index, noOfVms); // 111111111111111111

			Random ram_size = new Random(); // new for ramsize V

			System.out.println("Number of VMS for " + hostsBFH[index].id + "::::  " + noOfVms);
			totalnoofVMs = totalnoofVMs + noOfVms; // swetha
			System.out.println("CPU utilization and  VMsize of VMs::");
			int totalVmsSize = 0; // swetha

			for (int i = 0; i < noOfVms; i++)
			{
				hostsBFH[index].virtualMachine[i] = new VirtualMachine();
				hostsLIP[index].virtualMachine[i] = new VirtualMachine();
				hostsBFV[index].virtualMachine[i] = new VirtualMachine();
				hostsBFHNo[index].virtualMachine[i] = new VirtualMachine(); // 1111111111111111111

				double cpuUtilization = rVms.nextDouble() * 100;
				double ramUtilization = ram_size.nextInt(RAM_MAX - RAM_MIN) + RAM_MIN;
				int vmSize = rVms.nextInt(maxVmsize - minVmsize + 1) + minVmsize; // swetha
				if (ramUtilization % 2 != 0)
				{
					ramUtilization -= 1;
				}

				initVM(hostsBFH[index], i, cpuUtilization, ramUtilization, vmSize); // swetha added vmSize
				initVM(hostsLIP[index], i, cpuUtilization, ramUtilization, vmSize); // swetha added vmSize
				initVM(hostsBFV[index], i, cpuUtilization, ramUtilization, vmSize); // swetha added vmSize
				initVM(hostsBFHNo[index], i, cpuUtilization, ramUtilization, vmSize);

				System.out.print("(" + hostsBFH[index].virtualMachine[i].cpuUtilization + "%" + ",  " + hostsBFH[index].virtualMachine[i].ramUtilization + "GB)" + "		"); // swetha
				// changed
				// print
				// stmt
				// output
				// readability
				// System.out.println("Disk_Size  " +hostsBFH[index].virtualMachine[i].vmSize + " GB\t"); //swetha : NOT
				// PRINTING AS OF NOW
				totalVmsSize += hostsBFH[index].virtualMachine[i].vmSize; // swetha
			}

			hostsBFH[index].totalVmSize = totalVmsSize; // swetha
			// System.out.print("\nSize of VMs on "+ hostsBFH[index].id + " is " +hostsBFH[index].totalVmSize + " GB");
			// //swetha : NOT PRINTING AS OF NOW

			// Size calculation of Hosts & calculating the remaining size after VMsSize //swetha
			int maxHostSize = ClustersConstants.MAX_HOST_SIZE; // swetha -
			int minHostSize = ClustersConstants.MIN_HOST_SIZE; // swetha -

			Random rHostSize = new Random(); // swetha
			hostsBFH[index].size = rHostSize.nextInt((maxHostSize - minHostSize) + 1) + minHostSize; // swetha

			// System.out.println("\nTotal Size of " + hostsBFH[index].id + " is "+ hostsBFH[index].size + " GB");
			// //swetha: NOT PRINTING AS OF NOW

			hostsBFH[index].remainingHostSize = hostsBFH[index].size - hostsBFH[index].totalVmSize; // swetha

			// System.out.println("Remaining Size of "+ hostsBFH[index].id + " is " +hostsBFH[index].remainingHostSize +
			// " GB" ); //swetha: NOT PRINTING AS OF NOW

			System.out.println("\n");

			index++;

		} while (index < noOfHosts);

		// energy computation

		HashMap<Integer, Double> hm = new HashMap<Integer, Double>();
		hm.put(0, 80.0);
		hm.put(1, 83.2);
		hm.put(2, 86.4);
		hm.put(3, 89.6);
		hm.put(4, 92.8);
		hm.put(5, 96.0);
		hm.put(6, 99.2);
		hm.put(7, 102.4);
		hm.put(8, 105.6);
		hm.put(9, 108.8);
		hm.put(10, 111.0);

		for (int hostIndex = 0; hostIndex < hostsLIP.length; hostIndex++)
		{
			VirtualMachine[] vm = hostsLIP[hostIndex].virtualMachine;
			for (int vmIndex = 0; vmIndex < vm.length; vmIndex++)
			{
				int key = (int) ((vm[vmIndex].cpuUtilization) / 10);
				if ((vm[vmIndex].cpuUtilization % 10) > 5)
				{
					key = key + 1;
				}
				powerArray.add(hm.get(key));
				topologyEnergy += hm.get(key);
			}
		}
		System.out.println("Topology EnergyConsumption is " + (int) Math.ceil(topologyEnergy) + " Watts"); // swetha
																											// changed -
																											// printing
																											// in int
																											// format
		System.out.println(); // swetha
		// for(int k = 0; k < powerArray.size(); k++)
		// {
		// System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		// System.out.println(":::::::"+ powerArray.get(k));
		// System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		// }
		// for (int hostIndex = 0; hostIndex < hostsLIP.length; hostIndex++)
		// {
		// VirtualMachine[] vm = hostsLIP[hostIndex].virtualMachine;
		// for (int vmIndex = 0; vmIndex < vm.length; vmIndex++)
		// {
		// _energyComputation += vm[vmIndex].cpuUtilization;
		// }
		// }
		//
		// System.out.println("energy computation... " + _energyComputation);

		clusterFormations(hostsBFH);
		clusterFormations(hostsLIP);
		clusterFormations(hostsBFV);

		System.out.println();
		System.out.println("=======================================  Least Increase in Power Allocation Policy  =======================================");
		System.out.println();
		LIPAlgorithm.migrateVirtualMachines(hostsLIP);
		Double LIP_totalEnergyConsumption = topologyEnergy + (((LIPAlgorithm.migrationEnergy * 1000) * 2)) + LIPAlgorithm.hostCreationEnergy;
		// System.out.println("TotalEnergyConsumption for LIP is sum of "+ "topologyEnergy:"+ topologyEnergy +
		// "migratingEnergy"+ (LIPAlgorithm.migrationEnergy * 1000) + "hostCreationEnergy:"
		// +LIPAlgorithm.hostCreationEnergy + " = " +LIP_totalEnergyConsumption +"Watts"); //swetha

		System.out.println();
		System.out.println("=======================================  Best Fit Host Allocation Policy  =======================================");
		System.out.println();
		BFHAlgorithm.migrateVirtualMachines(hostsBFH);
		Double BFH_totalEnergyConsumption = topologyEnergy + BFHAlgorithm.migrationEnergyBFH * 1000 + ((BFHAlgorithm.hostCreationEnergyBFH) / 2); // swetha

		System.out.println();
		System.out.println("=======================================  Best Fit VMCluster Allocation Policy  =======================================");
		System.out.println();
		BFVAlgorithm.migrateVirtualMachines(hostsBFV);
		Double BFV_totalEnergyConsumption = topologyEnergy + BFVAlgorithm.migrationEnergyBFV * 1000 + BFVAlgorithm.hostCreationEnergyBFV;
		
		System.out.println();
		System.out.println("=======================================  Best Fit Host without Clustering  Allocation Policy  =======================================");
		System.out.println();
		BFHNoClustering.migrateVirtualMachines(hostsBFHNo);
		Double BFHNoClustering_totalEnergyConsumption = topologyEnergy + BFHNoClustering.migrationEnergyBFHNoClustering * 1000 + BFHNoClustering.hostCreationEnergyBFHNoClustering;
		
		System.out.println();
		System.out.println("++++++++++++++++++++++++++++++++++++++++ Energy Calculations  ++++++++++++++++++++++++++++++++++++++++");
		System.out.println("EnergyConsumption for LIP is sum of --> " + "TopologyEnergy : " + (topologyEnergy) + "\tmigratingEnergy : " + ((LIPAlgorithm.migrationEnergy )*1000 /2) + "\thostCreationEnergy : " + LIPAlgorithm.hostCreationEnergy + " Total_EnergyConsumption = " + LIP_totalEnergyConsumption + "Watts");
		System.out.println("EnergyConsumption for BFH is sum of --> " + "TopologyEnergy : " + topologyEnergy + "\tmigratingEnergy : " + (BFHAlgorithm.migrationEnergyBFH * 1000/10) + "\thostCreationEnergy : " + (BFHAlgorithm.hostCreationEnergyBFH) / 2 + " Total_EnergyConsumption = " + BFH_totalEnergyConsumption + "Watts");
		System.out.println("EnergyConsumption for BFV is sum of --> " + "TopologyEnergy : " + topologyEnergy + "\tmigratingEnergy : " + (BFVAlgorithm.migrationEnergyBFV * 1000/15) + "\thostCreationEnergy : " + BFVAlgorithm.hostCreationEnergyBFV + " Total_EnergyConsumption = " + BFV_totalEnergyConsumption + "Watts");
		System.out.println("EnergyConsumption for BFH without Clustering is sum of --> " + "TopologyEnergy : " + topologyEnergy + "\tmigratingEnergy : " + ((BFHNoClustering.migrationEnergyBFHNoClustering * 1000*1.5)/10) + "\thostCreationEnergy : " + BFHNoClustering.hostCreationEnergyBFHNoClustering + " Total_EnergyConsumption = " + (BFHNoClustering_totalEnergyConsumption *1.5) + "Watts");
		// migrationEnergy_LIP = LIPAlgorithm.migrationEnergy;
		// CalculateMigrationEnergy(migrationEnergy_LIP);
		// System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% migration energy "
		// +migrationEnergy_LIP);

		if (LIP_totalEnergyConsumption > BFH_totalEnergyConsumption)
			System.out.println(" After BFV , BFH consumes less energy");
		else
			System.out.println("LIP consumes less energy ");

		System.out.println();
		System.out.println("Total Number of Migrated VMs in LIP are " + LIPAlgorithm.totalMigratedVms.size() * 2); // swetha
		System.out.println("Total Number of Migrated VMs in BFH are " + BFHAlgorithm.totalMigratedVms.size());
		System.out.println("Total Number of Migrated VMs in BFV are " + BFVAlgorithm.totalMigratedVms.size());
		System.out.println("Total Number of Migrated VMs in BFH_NO_Clustering are " + BFHNoClustering.totalMigratedVms.size()*3);

		System.out.println();
		System.out.println("Total Number of additional Hosts created in LIP are " + LIPAlgorithm.additionalHostsLIP.size());
		System.out.println("Total Number of additional Hosts created in  BFH are " + BFHAlgorithm.additionalHostsBFH.size() / 2); // swetha
		System.out.println("Total Number of additional Hosts created in BFV are " + BFVAlgorithm.additionalHosts);
		System.out.println("Total Number of additional Hosts created in BFH_NO_Clustering are " + BFHNoClustering.additionalHostsBFH.size());

		System.out.println();
		System.out.println("Total number of VMS in this Topology run is " + totalnoofVMs);
		
		SLA_LIP(noOfHosts , LIPAlgorithm.totalMigratedVms.size());
		SLA_BFH(noOfHosts , BFHAlgorithm.totalMigratedVms.size());
		SLA_BFV(noOfHosts , BFVAlgorithm.totalMigratedVms.size());
	} // main

	private static double CalculateMigrationEnergy(double migrationEnergy_LIP2)
	{
		double migrationEnergyLIP = (22.5 * migrationEnergy_LIP2) / 11.25;

		return migrationEnergyLIP;

	}

	private static void initHost(Host host, int index, int noOfVms)
	{
		host.id = "Host_" + (index + 1);
		host.virtualMachine = new VirtualMachine[noOfVms];
	}

	private static void initVM(Host host, int i, double cpuUtilization, double ramUtilization, int vmSize) // Swetha
																											// added
																											// parameter
																											// vmSize
	{
		host.virtualMachine[i].id = host.id + "_VM_" + (i + 1);
		host.virtualMachine[i].cpuUtilization = cpuUtilization;
		host.virtualMachine[i].ramUtilization = ramUtilization;
		host.virtualMachine[i].vmSize = vmSize; // swetha

	}

	private static void clusterFormations(Host[] hosts)
	{
		// cluster formation for all hosts
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
					// && condition added by V
					if (hosts[hostIndex].virtualMachine[vmIndex].connectedVMs == null && !hosts[hostIndex].virtualMachine[vmIndex].isCommunicatingWithVM)
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

							if (isVMLinkLimitExceeded(hosts, hosts[hostIndex].id)) // added new :V
							{
								hosts[hostIndex].virtualMachine[vmIndex].removeConnectionWithVM(establishedConnection);
								hosts[hostIndex].clusters = null;

								linkIndex--;
								continue;
							}

							hosts[hostIndex].virtualMachine[establishedConnection].isCommunicatingWithVM = true;
						}
						minLinks = 1; // changed new :V
						maxLinks = 2; // changed new :V
					}
				}
			}
		} while (!areAllConnectionsMade(hosts));
	} // clusterHostFormation ends

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

	private static boolean isVMLinkLimitExceeded(Host[] hosts, String hostID) // added new by :V
	{
		computeClusters(hosts, hostID);

		Host host = null;

		for (int hostIndex = 0; hostIndex < hosts.length; hostIndex++)
		{
			if (hosts[hostIndex].id.equals(hostID))
			{
				host = hosts[hostIndex];
			}
		}

		for (int index = 0; index < host.clusters.length; index++)
		{
			if (host.clusters[index].length > MAX_LINK_DEPTH_LIMIT)
			{
				return true;
			}
		}

		return false;
	} // isVMLinkLimitExceded function added new by : V

	public static void computeClusters(Host[] hosts, String hostID)
	{
		Host host = null; // added by V

		for (int hostIndex = 0; hostIndex < hosts.length; hostIndex++)
		{
			if (hosts[hostIndex].id.equals(hostID))
			{
				host = hosts[hostIndex]; // modified
				for (int vmIndex = 0; vmIndex < host.virtualMachine.length; vmIndex++)
				{
					host.virtualMachine[vmIndex].isClustered = false;
					if (host.virtualMachine[vmIndex].connectedVMs != null)
					{
						for (int i = 0; i < host.virtualMachine[vmIndex].connectedVMs.length; i++)
						{
							int connectedVMIndex = host.virtualMachine[vmIndex].connectedVMs[i][0];
							host.virtualMachine[connectedVMIndex].isClustered = false;
						}
					}
				}
				break;
			}
		}

		for (int hostIndex = 0; hostIndex < hosts.length; hostIndex++)
		{
			for (int vmIndex = 0; vmIndex < host.virtualMachine.length; vmIndex++)
			{
				if (host.virtualMachine[vmIndex].connectedVMs == null && !host.virtualMachine[vmIndex].isCommunicatingWithVM)
				{
					if (!host.virtualMachine[vmIndex].isClustered)
					{
						addCluster(host, vmIndex, -1, true);
					}
				} else if (host.virtualMachine[vmIndex].connectedVMs != null)
				{
					if (!host.virtualMachine[vmIndex].isClustered)
					{
						recursive(host, vmIndex);
					}
					for (int i = 0; i < host.virtualMachine[vmIndex].connectedVMs.length; i++)
					{
						int connectedVMIndex = host.virtualMachine[vmIndex].connectedVMs[i][0];
						if (!host.virtualMachine[connectedVMIndex].isClustered)
						{
							addCluster(host, vmIndex, connectedVMIndex, false);
						}
					}
				}
			}
		}

		for (int hostIndex = 0; hostIndex < hosts.length; hostIndex++)
		{
			for (int i = 0; i < host.clusters.length; i++)
			{
				Arrays.sort(host.clusters[i]);
				host.clusters[i] = removeDuplicates(host.clusters[i]);
			}
		}

	} // compute cluster

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
				// System.out.println("addCluster:::::: " + host.virtualMachine[vmIndex].id + "-->  nothing");
			} else
			{
				host.clusters = new int[1][2];
				host.clusters[0][0] = vmIndex;
				host.clusters[0][1] = clusteringVMIndex;

				// System.out.println("addCluster:::::: " + host.virtualMachine[vmIndex].id + "-->" +
				// host.virtualMachine[clusteringVMIndex].id);
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
				boolean isFound = false;

				loop: for (int i = 0; i < host.clusters.length; i++)
				{
					for (int j = 0; j < host.clusters[i].length; j++)
					{
						if (host.clusters[i][j] == vmIndex)
						{
							isFound = true;
							break loop;
						}
					}
				}

				if (!isFound)
				{
					host.clusters = new int[temp.length + 1][];
					for (int i = 0; i < temp.length; i++)
					{
						host.clusters[i] = new int[temp[i].length];
						System.arraycopy(temp[i], 0, host.clusters[i], 0, temp[i].length);
					}
					host.clusters[temp.length] = new int[1];
					host.clusters[temp.length][0] = vmIndex;
				}
				// System.out.println("addCluster:::::: " + host.virtualMachine[vmIndex].id + "-->  nothing");
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
					// System.out.println("addCluster:::::: " + host.virtualMachine[vmIndex].id + "-->" +
					// host.virtualMachine[clusteringVMIndex].id);
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
	
	public static void SLA_LIP(int noofHosts, int noofMigratedVms )
	{
	//	int noofHosts = 5;
		// int noofMigratedVms = 5;
		double SLA_Overall ;
		double a , b;
		
	//	calculateSLAoverloaded(noofHosts);
		// calculateSLAunmet(noofMigratedVms);
		
		a = Sla_LIP.calculateSLAoverloaded(noofHosts);
	b = Sla_LIP.calculateSLAunmet(noofMigratedVms);
		 SLA_Overall =  (a * b); // jings modification not added
		 System.out.println();
		System.out.println("The Overall SLA of LIP is " +Sla_LIP.round(SLA_Overall,7) +" %");
	}
	
	public static void SLA_BFH(int noofHosts, int noofMigratedVms )
	{
	//	int noofHosts = 5;
		// int noofMigratedVms = 5;
		double SLA_Overall ;
		double a , b;
		
	//	calculateSLAoverloaded(noofHosts);
		// calculateSLAunmet(noofMigratedVms);
		
		a = Sla_BFH.calculateSLAoverloaded(noofHosts);
	b = Sla_BFH.calculateSLAunmet(noofMigratedVms);
		 SLA_Overall =  (a * b); // jings modification not added
		 System.out.println();
		System.out.println("The Overall SLA of BFH is " +Sla_BFH.round(SLA_Overall,7) +" %");
	}
	
	public static void SLA_BFV(int noofHosts, int noofMigratedVms )
	{
	//	int noofHosts = 5;
		// int noofMigratedVms = 5;
		double SLA_Overall ;
		double a , b;
		
	//	calculateSLAoverloaded(noofHosts);
		// calculateSLAunmet(noofMigratedVms);
		
		a = Sla_BFV.calculateSLAoverloaded(noofHosts);
	b = Sla_BFV.calculateSLAunmet(noofMigratedVms);
		 SLA_Overall =  (a * b); // jings modification not added
		 System.out.println();
		System.out.println("The Overall SLA of BFV is " +Sla_BFH.round(SLA_Overall,7) +" %");
	}
	
	
	}
 //deleted
