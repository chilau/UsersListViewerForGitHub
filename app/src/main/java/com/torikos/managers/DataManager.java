package com.torikos.managers;


import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.torikos.data.collections.RepositoriesArrayList;
import com.torikos.data.Repository;
import com.torikos.data.User;
import com.torikos.data.collections.UserArrayList;

import java.util.Arrays;


public class DataManager {
	
	public static final int SUCCESS = 0;
	public static final int ERROR = 1;
	
	private RequestManager _requestManager;
	
	private String _rawResponse;
	
	
	public void init(Context context) {
		_requestManager = new RequestManager();
		_requestManager.init(context);
	}
	
	public void setRawResponse(String value) {
		_rawResponse = value;
	}
	
	
	public void getUsers(long since, final Handler completeHandler) {
		if (_requestManager == null) {
			return;
		}
		
		_requestManager.getUsers(
				since,
				_rawResponse,
				new Handler(new Handler.Callback() {
					@Override
					public boolean handleMessage(Message message) {
						switch (message.what) {
							case RequestManager.SUCCESS:
								String users_json = message.obj.toString();
								User[] usersArray = new Gson().fromJson(users_json, User[].class);
								UserArrayList users = new UserArrayList(Arrays.asList(usersArray));
								
								if (completeHandler != null) {
									completeHandler.obtainMessage(SUCCESS, -1, -1, users).sendToTarget();
								}
								break;
							case RequestManager.ERROR:
								if (completeHandler != null) {
									completeHandler.obtainMessage(ERROR, -1, -1, null).sendToTarget();
								}
								
								break;
						}
						
						return true;
					}
				})
		);
	}
	
	public void getUsersRepositories(final User user, final Handler completeHandler) {
		if (_requestManager == null) {
			return;
		}
		
		_requestManager.getUsersRepositories(
				user.login,
				_rawResponse,
				new Handler(new Handler.Callback() {
					@Override
					public boolean handleMessage(Message message) {
						switch (message.what) {
							case RequestManager.SUCCESS:
								String repositories_json = message.obj.toString();
								Repository[] repositoriesArray = new Gson().fromJson(repositories_json, Repository[].class);
								RepositoriesArrayList repositories = new RepositoriesArrayList(Arrays.asList(repositoriesArray));
								repositories.setUser(user);
								
								if (completeHandler != null) {
									completeHandler.obtainMessage(SUCCESS, -1, -1, repositories).sendToTarget();
								}
								break;
							case RequestManager.ERROR:
								if (completeHandler != null) {
									completeHandler.obtainMessage(ERROR, -1, -1, null).sendToTarget();
								}
								
								break;
						}
						
						return true;
					}
				})
		);
	}
	
}
