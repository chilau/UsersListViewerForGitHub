package com.torikos.data.collections;


import com.torikos.data.Repository;
import com.torikos.data.User;

import java.util.ArrayList;
import java.util.List;


public class RepositoriesArrayList extends ArrayList<Repository> {
	
	private User _user;
	
	
	public RepositoriesArrayList() {
		super();
	}
	
	public RepositoriesArrayList(List<Repository> source) {
		super(source);
	}
	
	
	public void setUser(User user) {
		_user = user;
	}
	
	public User getUser() {
		return _user;
	}
	
}
