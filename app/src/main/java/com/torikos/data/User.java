package com.torikos.data;


import com.torikos.data.collections.RepositoriesArrayList;


public class User {
	
	public long id;
	public String login;
	public String avatar_url;
	
	private RepositoriesArrayList _repositories;
	private boolean _repositoriesLoaded;
	
	
	public User() {
		_repositories = new RepositoriesArrayList();
		_repositoriesLoaded = false;
	}
	
	
	public void setRepositories(RepositoriesArrayList repositories) {
		_repositories.clear();
		_repositories.addAll(repositories);
		
		_repositoriesLoaded = true;
	}
	public RepositoriesArrayList getRepositories() {
		return _repositories;
	}
	
	public boolean getRepositoriesLoaded() {
		return _repositoriesLoaded;
	}
	
}
