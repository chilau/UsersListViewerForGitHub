package com.torikos.managers.runnables;


public class GetAccessTokenRunnable implements Runnable {
	
	private String _code;
	
	
	@Override
	public void run() {
		
	}
	
	
	public void setCode(String code) {
		_code = code;
	}
	
	public String getCode() {
		return _code;
	}
	
}
