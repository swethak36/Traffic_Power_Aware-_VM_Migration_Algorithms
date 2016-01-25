package com.csdl;

public class algorithm
{
	/*
	 * 1. creation of hosts and vms
	 * 2. Loop: for each host in availableHostList
	 *    {
	 *  	2.a check if (host is overloaded)
	 *        {
	 *  		2.b call clustering method //here clusters of VMs on that host are formed based on traffic factor
	 *  		2.c call selection method // here one cluster with high CPU utilization is selected 
	 *          2.d call allocation policy // there are 3 allocation policies.......
	 *          							  here cluster selected in above step will be migrated to another host in availableHostList
	 *          
	 *          2.e check if (this host is still overloaded)
	 *              {
	 *          	  repeat 2.c to 2.e until host reaches equilibrium
	 *              }      
	 *              else
	 *              {
	 *                remove this host from availableHostList
	 *                go to step 2
	 *              }
	 *         }
	 *         
	 *       2.f check if(host is underloaded)
	 *           {
	 *           	2.g select all vms on the host 
	 *              2.h migrate all vms on the host to another host (doubt............)
	 *              go to step 2
	 *           }
	 *    }
	 *   
	 */

}
/*
 * hosts - 	hosts[]
 * VMs  -  	virtualMachine[i].id
 * 			virtualMachine[i].cpuUtilization
 * 
 * 
 * 
 * 
 * 
 */



