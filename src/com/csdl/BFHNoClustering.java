package com.csdl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class BFHNoClustering
{

	// number of linear terms in the regression equation
	public static final int MAX_HISTORY_COLLECTED = 100;
	public static int noOfDataPoints;
	public static double[] weights;
	public static double[] time;
	public static double[][] historyOfcpuUtilizations;

	public static float[] predictedUtilization;

	public static float[] sumOfcpuUtilizations;
	public static float[] threshold;

	public static ArrayList<String> totalMigratedVms = new ArrayList<String>();
	public static int tempAdditionalHost = 0;
	public static ArrayList<Integer> additionalHostsBFH = new ArrayList<Integer>();
	public static int additionalHosts;
	private static LocalRegression regression = null;
	public static double hostCreationEnergyBFHNoClustering = 0.0;
	public static double migrationEnergyBFHNoClustering = 0.0;
	static HashMap<Integer, Double> migrationPowerMappingBFHNoClustering = new HashMap<Integer, Double>();
	
	

	// public static Host[] LIPHosts;

	public static void migrateVirtualMachines(Host[] hosts)
	{
		generateHistoryOfCPUUtilizations(hosts);
		System.out.println("Checking for overloaded hosts in BFH with no clustering........\n");
		// Determine host is overloaded or under loaded
		sumOfcpuUtilizations = new float[hosts.length];
		threshold = new float[hosts.length];
		for (int hostIndex = 0; hostIndex < hosts.length; hostIndex++)
		{
			computeThreshold(hosts, hostIndex);
		}

		for (int hostIndex = 0; hostIndex < hosts.length; hostIndex++)
		{
			// if CPU utilization is more than the threshold cluster the host
			if (sumOfcpuUtilizations[hostIndex] > threshold[hostIndex])
			{
				int vmIndex = selectVM(hosts[hostIndex]);
				boolean isAllocationSuccess = allocateClusterToHost(hosts, hosts[hostIndex], vmIndex);

				if (isAllocationSuccess)
				{
					System.out.println("\nchecking if the host is still overloaded...");
					computeThreshold(hosts, hostIndex);
					if (sumOfcpuUtilizations[hostIndex] > threshold[hostIndex])
					{
						System.out.println("As the host is still overloaded re-itereating allocation policy");
						hostIndex--;

						Host host = hosts[hostIndex + 1];

					} else
					{
						System.out.println("As the host is not overloaded moving on to next overloaded host");
					}
				}
			}
		}
	}

	private static void generateHistoryOfCPUUtilizations(Host[] hosts)
	{
		Random r = new Random();
		noOfDataPoints = hosts.length + r.nextInt(MAX_HISTORY_COLLECTED - hosts.length);
		weights = new double[noOfDataPoints];
		time = new double[noOfDataPoints];
		historyOfcpuUtilizations = new double[hosts.length][noOfDataPoints];
		for (int i = 0; i < hosts.length; i++)
		{
			for (int j = 0; j < noOfDataPoints; j++)
			{
				time[j] = j + 1;
				historyOfcpuUtilizations[i][j] = r.nextDouble() * 100;
			}
		}

		for (int i = 0; i < noOfDataPoints; i++)
		{
			double t = (time[noOfDataPoints - 1] - time[i]) / (time[noOfDataPoints - 1] - time[0]);
			t = (t * t * t);
			weights[i] = (1 - t) * (1 - t) * (1 - t);
		}
		// System.out.println("\n generateHistory is mtd called..........\n");
	}

	private static void computeThreshold(Host[] hosts, int hostIndex)
	{
		// System.out.println("\n computeThreshold is mtd called..........\n");
		int noOfVms = hosts[hostIndex].virtualMachine.length;
		if (noOfVms == 0)
		{
			sumOfcpuUtilizations[hostIndex] = threshold[hostIndex] = 0;
			return;
		}

		float medianOfcpuUtilizations = 0.0f;
		float[] temp_cpuUtilization_values = new float[noOfVms];
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
			medianOfcpuUtilizations = (noOfVms == 1) ? temp_cpuUtilization_values[0] : (temp_cpuUtilization_values[noOfVms / 2] + temp_cpuUtilization_values[(noOfVms + 1) / 2]) / 2;
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
			medianOfcpuUtilizations = (noOfVms == 1) ? temp_cpuUtilization_values[0] : (temp_cpuUtilization_values[noOfVms / 2] + temp_cpuUtilization_values[(noOfVms / 2) + 1]) / 2;
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
			System.out.println(hosts[hostIndex].id + " is overloaded");

		} else
		{
			System.out.println(hosts[hostIndex].id + " is not overloaded");
		}

	}

	/*
	 * selection algorithm -- minimum CPU-Utilization
	 * 
	 * host that is overloaded
	 */
	// public static int selectCluster(Host host)
	// {
	// int clusterIndex = -1;
	// float minCPUUtilization = 1000.0f;
	// for (int i = 0; i < host.virtualMachine.length; i++)
	// {
	// float currUtiliation = 0.0f;
	//
	// for (int j = 0; j < host.clusters[i].length; j++)
	// {
	// currClusterUtiliation += host.virtualMachine[host.clusters[i][j]].cpuUtilization;
	// }
	//
	// if (currClusterUtiliation < minCPUUtilization)
	// {
	// minCPUUtilization = currClusterUtiliation;
	// clusterIndex = i;
	// }
	// }
	//
	// return clusterIndex;
	// }

	// selection algorithm to select 1 VM
	public static int selectVM(Host host)
	{
		int vmIndex = -1;
		double minCPUUtilization = 1000.0f;
		double currUtiliation = 0.0f;
		for (int j = 0; j < host.virtualMachine.length; j++)
		{
			currUtiliation = host.virtualMachine[j].cpuUtilization;
			if (currUtiliation < minCPUUtilization)
			{
				minCPUUtilization = currUtiliation;
				vmIndex = j;
			}
		}
		return vmIndex;
	}

	/*
	 * allocation algorithm -- minimum CPU-Utilization
	 * 
	 * host represents overloaded host
	 * 
	 * clusterIndex is the cluster which needs to be allocated to least
	 * overloaded Host and remove those set of VMs from the original host
	 */
	public static boolean allocateClusterToHost(Host hosts[], Host host, int migratingVMIndex)
	{
		// BFH Allocation policy
		// System.out.println("\n allocateClusterToHost is mtd called..........\n");
		System.out.println(" ");
		System.out.println("ALLOCATION POLICY");

		float migratingVMcpuUtilization = (float) host.virtualMachine[migratingVMIndex].cpuUtilization;

		// time to compute least overloaded host - the one with highest predicted utilization
		for (int hostIndex = 0; hostIndex < hosts.length; hostIndex++)
		{
			// skip if the hosts are similar
			if (hosts[hostIndex].id.equals(host.id))
			{
				continue;
			}
			for (int i = 0; i < noOfDataPoints; i++)
			{
				historyOfcpuUtilizations[hostIndex][i] += migratingVMcpuUtilization;
			}
		}
		regression = new LocalRegression();
		regression.Regress(time, historyOfcpuUtilizations, weights);

		int leastOverloadedHostIndex = 0;
		double predictedUtilization = -1.0f;
		for (int hostIndex = 0; hostIndex < hosts.length; hostIndex++)
		{
			// skip if the hosts are similar
			if (hosts[hostIndex].id.equals(host.id))
			{
				continue;
			}

			if (regression.C == null)
			{
				System.out.println("yeah this is null!!!");
			}
			double tempPredictedUtilization = regression.C[hostIndex];

			if (tempPredictedUtilization > predictedUtilization)
			{
				leastOverloadedHostIndex = hostIndex;
				predictedUtilization = tempPredictedUtilization;
			}
		}

		System.out.println("least overloaded host::   " + hosts[leastOverloadedHostIndex].id);

		// --------------------------------------------------------------------------------------------//

		float sumOfCPUUtilizations = 0.0f;
		float tempThreshold = 0.0f;
		int noOfVms = hosts[leastOverloadedHostIndex].virtualMachine.length;

		// System.out.println("no.Of.VMs:::: " + noOfVms);

		if (noOfVms == 0)
		{
			sumOfCPUUtilizations = tempThreshold = 0;
		} else
		{
			float medianOfcpuUtilizations = 0.0f;
			float[] temp_cpuUtilization_values = new float[noOfVms];
			int cpuUtilIndex = 0;
			for (; cpuUtilIndex < hosts[leastOverloadedHostIndex].virtualMachine.length; cpuUtilIndex++)
			{
				temp_cpuUtilization_values[cpuUtilIndex] = (float) (hosts[leastOverloadedHostIndex].virtualMachine[cpuUtilIndex].cpuUtilization / 100);
				sumOfCPUUtilizations += temp_cpuUtilization_values[cpuUtilIndex];
			}

			// for (int index = 0; index < host.clusters[clusterIndex].length; index++)
			// {
			// temp_cpuUtilization_values[cpuUtilIndex] = (float)
			// (host.virtualMachine[host.clusters[clusterIndex][index]].cpuUtilization / 100);
			// sumOfCPUUtilizations += temp_cpuUtilization_values[cpuUtilIndex];
			// cpuUtilIndex++;
			// }

			// applying the factor
			float factor = 0.1f;
			sumOfCPUUtilizations *= factor;

			Arrays.sort(temp_cpuUtilization_values);

			if (noOfVms % 2 == 0)
			{
				medianOfcpuUtilizations = temp_cpuUtilization_values[noOfVms / 2];
			} else
			{
				medianOfcpuUtilizations = (noOfVms == 1) ? temp_cpuUtilization_values[0] : (temp_cpuUtilization_values[noOfVms / 2] + temp_cpuUtilization_values[(noOfVms + 1) / 2]) / 2;
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
				medianOfcpuUtilizations = (noOfVms == 1) ? temp_cpuUtilization_values[0] : (temp_cpuUtilization_values[noOfVms / 2] + temp_cpuUtilization_values[(noOfVms / 2) + 1]) / 2;
			}

			// safety parameter value predetermined for the algorithm
			float s = 2.5f;
			tempThreshold = Math.abs(1 - (s * medianOfcpuUtilizations));
		}

		if (sumOfCPUUtilizations > tempThreshold)
		{
			System.out.println("This host is getting overloaded on allocating the cluster. So creating a new host and allocating to it!");
			hosts = createNewHost(hosts);
			tempAdditionalHost++;
			additionalHostsBFH.add(tempAdditionalHost);
			System.out.println("Number of Hosts::: " + hosts.length);
			leastOverloadedHostIndex = hosts.length - 1;
			// System.out.println("{{{{{{{{{{{{{{{{{{{{{{{{{{{new host id"+ hosts[hostIndex].id);
		}

		// additionalHosts = additionalHostsBFH.size()/2; //swetha

		// mapping for migration energy
		migrationPowerMappingBFHNoClustering.put(2, 4.0);
		migrationPowerMappingBFHNoClustering.put(4, 4.7);
		migrationPowerMappingBFHNoClustering.put(6, 5.4);
		migrationPowerMappingBFHNoClustering.put(8, 6.0);

		System.out.print("following cluster is allocated to the least overloaded host:: ");
		// HashMap<Integer, String> map = new HashMap<Integer, String>();

		System.out.print(host.virtualMachine[migratingVMIndex].id);
		totalMigratedVms.add(host.virtualMachine[migratingVMIndex].id);
		// map.put(host.clusters[clusterIndex][index], host.virtualMachine[host.clusters[clusterIndex][index]].id);

		// computing migration energy
		// System.out.println("<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		migrationEnergyBFHNoClustering += migrationPowerMappingBFHNoClustering.get((int) host.virtualMachine[migratingVMIndex].ramUtilization);
		// System.out.println("vmsize:"+ host.virtualMachine[host.clusters[clusterIndex][index]].ramUtilization
		// + "migrationEnergy:"+ migrationPowerMappingBFH.get((int)
		// host.virtualMachine[host.clusters[clusterIndex][index]].ramUtilization)
		// + "summed migration energy"+ migrationEnergyBFH);
		// System.out.println("<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

		// for (int vI = 0; vI < host.virtualMachine.length; vI++)
		// {
		// map.put(vI, host.virtualMachine[vI].id);
		// }

		// System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		// Set set = map.entrySet();
		// Iterator iterator = set.iterator();
		// while (iterator.hasNext())
		// {
		// Map.Entry mentry = (Map.Entry) iterator.next();
		// System.out.print("key is: " + mentry.getKey() + " & Value is: ");
		// System.out.println(mentry.getValue());
		// System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		// }

		// remove the cluster from current host and allocate it to new host
		Host leastOverloadedHost = hosts[leastOverloadedHostIndex];
		// add vm to leastOverloadedHost
		if (leastOverloadedHost.virtualMachine == null)
		{
			leastOverloadedHost.virtualMachine = new VirtualMachine[1];
			leastOverloadedHost.virtualMachine[0] = host.virtualMachine[migratingVMIndex];
		} else
		{
			VirtualMachine[] tempLeastVM = new VirtualMachine[leastOverloadedHost.virtualMachine.length];
			System.arraycopy(leastOverloadedHost.virtualMachine, 0, tempLeastVM, 0, leastOverloadedHost.virtualMachine.length);

			leastOverloadedHost.virtualMachine = new VirtualMachine[tempLeastVM.length + 1];
			System.arraycopy(tempLeastVM, 0, leastOverloadedHost.virtualMachine, 0, tempLeastVM.length);
			leastOverloadedHost.virtualMachine[tempLeastVM.length] = host.virtualMachine[migratingVMIndex];
		}

		// allocate & copy the values till vmIndex into temp for overloaded host
		VirtualMachine[] temp = new VirtualMachine[host.virtualMachine.length - 1];
		System.arraycopy(host.virtualMachine, 0, temp, 0, migratingVMIndex);

		// copy the values after vmIndex into temp
		System.arraycopy(host.virtualMachine, migratingVMIndex + 1, temp, migratingVMIndex, temp.length - migratingVMIndex);

		// now reallocate & copy the updated temp into overloaded virtualmachine
		host.virtualMachine = new VirtualMachine[temp.length];
		System.arraycopy(temp, 0, host.virtualMachine, 0, temp.length);

		return true;
	}

	private static Host[] createNewHost(Host[] hosts)
	{
		hostCreationEnergyBFHNoClustering += 80; // swetha 80 --> 40 --> 20
		Host[] temp = new Host[hosts.length];
		System.arraycopy(hosts, 0, temp, 0, hosts.length);

		hosts = new Host[temp.length + 1];
		System.arraycopy(temp, 0, hosts, 0, temp.length);

		hosts[temp.length] = new Host();
		hosts[temp.length].id = "Host_" + (hosts.length);

		return hosts;
	}

}
