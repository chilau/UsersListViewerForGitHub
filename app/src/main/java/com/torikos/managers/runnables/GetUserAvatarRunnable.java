package com.torikos.managers.runnables;


import com.torikos.data.User;


public class GetUserAvatarRunnable implements Runnable {
	
	private User _user;
	
	
	public GetUserAvatarRunnable(User user) {
		_user = user;
	}
	
	
	@Override
	public void run() {
	}
	
	
	public User getUser() {
		return _user;
	}
	
}
