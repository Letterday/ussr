package sunTron.futures;

import sunTron.API.ISunTronAPI;

public class FutureExtend extends Future{
	int connectorNo;
	int i = 0;
	public FutureExtend(int i,ISunTronAPI atronDelegateAPI) {
		this.sunTronAPI = atronDelegateAPI;
		connectorNo = i;
	}



	public boolean isCompleted() {
		boolean state = false;
		// TODO Auto-generated method stub
		if(i > 100) state  = true; 
		i++;
		
		return state;
		
	}



	public String getKey() {
		// TODO Auto-generated method stub
		return null;
	}






}