package com.csdl;

public class VirtualMachine
{	
	public String id;
	public double cpuUtilization = 0.0;
	public double ramUtilization = 0.0;
	public int[][] connectedVMs = null;
	public boolean isCommunicatingWithVM = false;
	public boolean isClustered = false;
	public int vmSize ; // swetha
	
	public boolean addVMConnection(int vmIndex, int linkWeight)
	{
		if (connectedVMs == null)
		{
			connectedVMs = new int[1][2];
			connectedVMs[0][0] = vmIndex;
			connectedVMs[0][1] = linkWeight;
		} else
		{
			// if this link is already made don't add it
			for (int i = 0; i < connectedVMs.length; i++)
			{
				if (connectedVMs[i][0] == vmIndex)
				{
					return false;
				}
			}
			
			int[][] temp = new int[connectedVMs.length][2];
			System.arraycopy(connectedVMs, 0, temp, 0, (connectedVMs.length  >> 1));
			
			connectedVMs = new int[temp.length + 1][2];
			System.arraycopy(temp, 0, connectedVMs, 0, (temp.length >> 1));
			connectedVMs[temp.length][0] = vmIndex;
			connectedVMs[temp.length][1] = linkWeight;
		}		
		
		return (isCommunicatingWithVM = true);
	}

	public void removeConnectionWithVM(int vmIndex)
	{
		if (connectedVMs.length <= 1)
		{
			connectedVMs = null;
			isCommunicatingWithVM = false;
			return;
		}
		
		int[][] temp = new int[connectedVMs.length - 1][2];
		// copy values before the index
		System.arraycopy(connectedVMs, 0, temp, 0, (vmIndex >> 1));
		// copy values after index
		System.arraycopy(connectedVMs, vmIndex + 1, temp, vmIndex, (temp.length - vmIndex >> 1));
		
		connectedVMs = new int[temp.length][2];
		//re-copy
		System.arraycopy(temp, 0, connectedVMs, 0, (temp.length >> 1));
	}	
}
