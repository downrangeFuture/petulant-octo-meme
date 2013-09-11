package com.vstargauge.navigation;

public class DirectionsUrlException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8475465748929427010L;

	
	public DirectionsUrlException(){
		super();
	}
	
	public DirectionsUrlException(String msg){
		super(msg);
	}
	
	public DirectionsUrlException(String msg, Throwable throwable){
		super(msg, throwable);
	}
}
