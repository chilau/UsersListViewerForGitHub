package com.torikos.data.collections;


import com.torikos.data.User;

import java.util.ArrayList;
import java.util.List;


public class UserArrayList extends ArrayList<User> {
	
	
	public UserArrayList() {
		super();
	}
	
	public UserArrayList(List<User> source) {
		super(source);
	}
	
}
