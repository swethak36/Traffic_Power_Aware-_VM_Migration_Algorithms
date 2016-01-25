package com.csdl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

class LIPAlgorithm
{
	public static float[] sumOfcpuUtilizations;
	public static float[] threshold;
	public static double hostCreationEnergy = 0.0;
	public static double migrationEnergy = 0.0;
	public static int tempAdditionalHost =0;
	public static ArrayList<Integer> additionalHostsLIP = new ArrayList<Integer>();
	public static int additionalHosts ;
	static HashMap<Integer,Double> migrationPowerMapping = new HashMap<Integer,Double>();

	public static ArrayList<String> totalMigratedVms = new ArrayList<String>();
	// public static Host[] LIPHosts;

	public static void migrateVirtualMachines(Host[] hosts)
	{
		System.out.println("Checking for overloaded hosts........\n");
		// Determine host is overloaded or under loaded
		sumOfcpuUtilizations = new float[hosts.length];
		threshold = new float[hosts.length];
		for (int hostIndex = 0; hostIndex < hosts.length; hostIndex++)
		{
			computeThreshold(hosts, hostIndex);
		}

		// to print graph un-comment this
		// for (int hostIndex = 0; hostIndex < hosts.length; hostIndex++)
		// {
		// System.out.println("-----------------------------Connection between VMs in " + hosts[hostIndex].id +
		// "-----------------------------------");
		// for (int vmIndex = 0; vmIndex < hosts[hostIndex].virtualMachine.length; vmIndex++)
		// {
		// if (hosts[hostIndex].virtualMachine[vmIndex].connectedVMs != null)
		// {
		// for (int i = 0; i < hosts[hostIndex].virtualMachine[vmIndex].connectedVMs.length; i++)
		// {
		// System.out.println(hosts[hostIndex].virtualMachine[vmIndex].id + "-->" +
		// hosts[hostIndex].virtualMachine[(hosts[hostIndex].virtualMachine[vmIndex].connectedVMs[i][0])].id + "--> (" +
		// hosts[hostIndex].virtualMachine[vmIndex].connectedVMs[i][1] + ")");
		// }
		// }
		// }
		// }

		// cluster the overloaded hosts
		boolean shouldCluster = true;
		for (int hostIndex = 0; hostIndex < hosts.length; hostIndex++)
		{
			// if CPU utilization is more than the threshold cluster the host
			if (sumOfcpuUtilizations[hostIndex] > threshold[hostIndex])
			{
				String hostID = hosts[hostIndex].id;
				if (shouldCluster)
				{
					clusterHost(hosts, hostID);
				}
				hosts[hostIndex].clusters = null;
				computeClusters(hosts, hostID);

				int clusterIndex = selectCluster(hosts[hostIndex]);
				boolean isAllocationSuccess = allocateClusterToHost(hosts, hosts[hostIndex], clusterIndex);

				shouldCluster = true;
				if (isAllocationSuccess && hosts[hostIndex].virtualMachine.length > 3)
				{
					System.out.println("\nchecking if the host is` still overloaded...");
					computeThreshold(hosts, hostIndex);
					if (sumOfcpuUtilizations[hostIndex] > threshold[hostIndex])
					{
						System.out.println("As the host is still overloaded re-itereating allocation policy");
						shouldCluster = false;
						hostIndex--;

						Host host = hosts[hostIndex + 1];
						System.out.println("");
						System.out.println("******************************  Available clusters in overloaded " + host.id + " ****************************" );
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
            
						System.out.println("");
						System.out.println("Independent VMs of this host");
						for (int vmIndex = 0; vmIndex < host.virtualMachine.length; vmIndex++)
						{
							if (host.virtualMachine[vmIndex].connectedVMs == null && !host.virtualMachine[vmIndex].isCommunicatingWithVM)
							{
								System.out.println(host.virtualMachine[vmIndex].id);
							}
						}
					} else
					{
						shouldCluster = true;
						System.out.println("As the host is not overloaded moving on to next overloaded host");
					}
				}
			}
		}
	} // migratevirtualmachines

	private static void computeThreshold(Host[] hosts, int hostIndex)
	{
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

	private static void clusterHost(Host[] hosts, String hostID)
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
		
		System.out.println("\n-----------------------------Clustering " + host.id + " since it is is overloaded. -----------------------------------");
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

		System.out.println("\n-----------------------------Independent VMs of this host -----------------------------------");
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
		// System.out.println("###########computing clusters###############");
		Host host = null;

		for (int hostIndex = 0; hostIndex < hosts.length; hostIndex++)
		{
			if (hosts[hostIndex].id.equals(hostID))
			{
				host = hosts[hostIndex];
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
					// System.out.println("trying to add VM... " + host.virtualMachine[vmIndex].id);
					if (!host.virtualMachine[vmIndex].isClustered)
					{
						addCluster(host, vmIndex, -1, true);
					}
				} else if (host.virtualMachine[vmIndex].connectedVMs != null)
				{
					// System.out.println("trying to add VM with connectedVMs... " + host.virtualMachine[vmIndex].id);
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

		// boolean foundCulprit = false;
		// for (int i = 0; i < (host.clusters.length - 1); i++)
		// {
		// for (int j = i + 1; j < host.clusters.length; j++)
		// {
		// for (int x = 0; x < host.clusters[i].length; x++)
		// {
		// for (int y = 0; y < host.clusters[j].length; y++)
		// {
		// // System.out.println(host.clusters[i][x] + " == " + host.clusters[j][y]);
		// if (host.clusters[i][x] == host.clusters[j][y])
		// {
		// int[][] temp = new int[host.clusters.length][];
		// for (int tempIndex = 0; tempIndex < host.clusters.length; tempIndex++)
		// {
		// temp[tempIndex] = new int[host.clusters[tempIndex].length];
		// System.arraycopy(host.clusters[tempIndex], 0, temp[tempIndex], 0, host.clusters[tempIndex].length);
		// }
		//
		// host.clusters = new int[temp.length - 1][];
		// for (int tempIndex = 0; tempIndex < host.clusters.length; tempIndex++)
		// {
		// if (tempIndex == i)
		// {
		// host.clusters[tempIndex] = new int[temp[tempIndex].length + temp[tempIndex + 1].length];
		// System.arraycopy(temp[tempIndex], 0, host.clusters[tempIndex], 0, temp[tempIndex].length);
		// System.arraycopy(temp[tempIndex + 1], 0, host.clusters[tempIndex], temp[tempIndex].length, temp[tempIndex +
		// 1].length);
		// } else
		// {
		// host.clusters[tempIndex] = new int[temp[tempIndex].length];
		// System.arraycopy(temp[tempIndex], 0, host.clusters[tempIndex], 0, host.clusters[tempIndex].length);
		// }
		// }
		// foundCulprit = true;
		// i = -1;
		// break;
		// }
		// }
		// // start whole loop again
		// if (foundCulprit)
		// {
		// break;
		// }
		// }
		// // start whole loop again
		// if (foundCulprit)
		// {
		// break;
		// }
		// }
		// }
		//
		// // finally remove all the duplicates
		// for (int i = 0; i < host.clusters.length; i++)
		// {
		// Arrays.sort(host.clusters[i]);
		// host.clusters[i] = removeDuplicates(host.clusters[i]);
		// }

		// System.out.println("################ computed clusters #####################");
		// for (int hostIndex = 0; hostIndex < hosts.length; hostIndex++)
		// {
		// if (hosts[hostIndex].id.equals(hostID))
		// {
		// host = hosts[hostIndex];
		// for (int i = 0; i < host.clusters.length; i++)
		// {
		// System.out.println("");
		// for (int j = 0; j < host.clusters[i].length; j++)
		// {
		// System.out.print(host.clusters[i][j] + ", ");
		// }
		// }
		// }
		// }
	} // compute

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
		System.out.println("");
		System.out.println("ALLOCATION POLICY");
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

		System.out.println("least overloaded host::   " + hosts[leastOverloadedHostIndex].id);

		// --------------------------------------------------------------------------------------------//

		float sumOfCPUUtilizations = 0.0f;
		float tempThreshold = 0.0f;
		int noOfVms = hosts[leastOverloadedHostIndex].virtualMachine.length + host.clusters[clusterIndex].length;

//		System.out.println("no.Of.VMs:::: " + noOfVms);

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
			tempAdditionalHost ++ ;
			additionalHostsLIP.add(tempAdditionalHost);
			System.out.println("Number of Hosts::: " + hosts.length + "new host id"+ host.id);
			leastOverloadedHostIndex = hosts.length - 1;
		//	System.out.println("{{{{{{{{{{{{{{{{{{{{{{{{{{{new host id"+ hosts[hostIndex].id);
		}
		
	//	additionalHosts = additionalHostsLIP.size() * 2;

		// --------------------------------------------------------------------------------------------//
		// generating mappings for migration energy
		migrationPowerMapping.put(2, 4.0); //
		migrationPowerMapping.put(4, 4.7); //
		migrationPowerMapping.put(6, 5.4); //
		migrationPowerMapping.put(8, 6.0); // 
		
		System.out.print("following cluster is allocated to the least overloaded host:: ");
		HashMap<Integer, String> map = new HashMap<Integer, String>();

		for (int index = 0; index < host.clusters[clusterIndex].length; index++)
		{
			System.out.print(host.virtualMachine[host.clusters[clusterIndex][index]].id);
			totalMigratedVms.add(host.virtualMachine[host.clusters[clusterIndex][index]].id) ;
			map.put(host.clusters[clusterIndex][index], host.virtualMachine[host.clusters[clusterIndex][index]].id);
			
			if (index + 1 < host.clusters[clusterIndex].length)
			{
				System.out.println(", ");
			}
			
			//calculation of migration energy
//			System.out.println("<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			migrationEnergy += migrationPowerMapping.get((int) host.virtualMachine[host.clusters[clusterIndex][index]].ramUtilization); 
//			System.out.println("vmsize:"+ host.virtualMachine[host.clusters[clusterIndex][index]].ramUtilization
//					+ "migrationEnergy:"+  migrationPowerMapping.get((int) host.virtualMachine[host.clusters[clusterIndex][index]].ramUtilization) 
//				    + "summed migration energy"+ migrationEnergy);
//			System.out.println("<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		}
	//	System.out.println("############################################################### migration energy is "+migrationEnergy); // swetha
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ MIGRATION ENERGY$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		System.out.println("");
		System.out.println("$$$$$$ in KW" + migrationEnergy );
		
		System.out.println("");
		double x = 3.6e+6;
		System.out.println("$$$$$$ in Watt/sec" + migrationEnergy * x);
		
		
		
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
					if (leastOverloadedHost.virtualMachine == null)
					{
						leastOverloadedHost.virtualMachine = new VirtualMachine[1];
						leastOverloadedHost.virtualMachine[0] = host.virtualMachine[vmIndex];

						if (leastOverloadedHostNewCluster == null)
						{
							leastOverloadedHostNewCluster = new int[1];
							leastOverloadedHostNewCluster[0] = 0;
						}
					} else
					{
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
		
	//	System.out.println("############################################################### migration energy is "+migrationEnergy); // swetha
		return true;
		
		
	} // allocate cluster to host

	private static Host[] createNewHost(Host[] hosts)
	{
		hostCreationEnergy += 80.0; // swetha 80-->160
		Host[] temp = new Host[hosts.length];
		System.arraycopy(hosts, 0, temp, 0, hosts.length);
//		System.out.println("*********** new host created************************************************");
		hosts = new Host[temp.length + 1];
		System.arraycopy(temp, 0, hosts, 0, temp.length);

		hosts[temp.length] = new Host();
		hosts[temp.length].id = "Host_" + (hosts.length);

		return hosts;
	}
	

	
}