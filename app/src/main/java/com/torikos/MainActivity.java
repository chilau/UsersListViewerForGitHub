package com.torikos;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.torikos.data.User;
import com.torikos.data.collections.RepositoriesArrayList;
import com.torikos.data.collections.UserArrayList;
import com.torikos.listView.adapter.UserListViewBaseAdapter;
import com.torikos.managers.AvatarManager;
import com.torikos.managers.DataManager;
import com.torikos.listeners.LazyLoaderScrollListener;
import com.torikos.managers.runnables.GetAccessTokenRunnable;

import java.util.Random;


public class MainActivity extends AppCompatActivity {
	
	private static final int AVATARS_COLLECTION_MAX_COUNT = 60;
	
	private OAuth20Service _oAuth20Service;
	
	private DataManager _dataManager;
	private AvatarManager _avatarManager;
	
	private UserListViewBaseAdapter _adapter;
	
	private long _since;
	private long _loadInitReminder;
	
	private int _usersCount;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initOAuthService();
		
		ListView usersListView = (ListView) findViewById(R.id.usersListView);
		
		_dataManager = new DataManager();
		_dataManager.init(this);
		
		_avatarManager = new AvatarManager(AVATARS_COLLECTION_MAX_COUNT);
		
		_since = 0;
		_loadInitReminder = 0;
		_usersCount = 0;
		
		LazyLoaderScrollListener lazyLoaderScrollListener = new LazyLoaderScrollListener();
		lazyLoaderScrollListener.initOnScrollHandler(_lazyLoaderScrollListenerHandler);
		usersListView.setOnScrollListener(lazyLoaderScrollListener);
		
		_adapter = new UserListViewBaseAdapter(this, new UserArrayList(), _userListViewBaseAdapterHandler);
		usersListView.setAdapter(_adapter);
	}
	
	
	// region OAuth
	
	private void initOAuthService() {
		String clientId = getString(R.string.client_id);
		String clientSecret = getString(R.string.client_secret);
		String secretState = "secret" + new Random().nextInt(999_999);
		String callbackUrl =  String.format(getString(R.string.callback_url_pattern), getString(R.string.callback_scheme), getString(R.string.callback_host));
		
		_oAuth20Service = new ServiceBuilder(clientId)
			.apiSecret(clientSecret)
			.state(secretState)
			.callback(callbackUrl)
			.build(GitHubApi.instance());
		String authorizationUrl = _oAuth20Service.getAuthorizationUrl();
		
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(authorizationUrl)).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_FROM_BACKGROUND);
		startActivity(intent);
	}
	
	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Uri uri = intent.getData();
		if (uri == null) {
			return;
		}
		
		String code = uri.getQueryParameter("code");
		_getAccessTokenRunnable.setCode(code);
		Thread getAccessTokenThread = new Thread(_getAccessTokenRunnable);
		getAccessTokenThread.start();
	}
	
	private GetAccessTokenRunnable _getAccessTokenRunnable = new GetAccessTokenRunnable() {
		@Override
		public void run() {
			try {
				String code = getCode();
				OAuth2AccessToken accessToken = _oAuth20Service.getAccessToken(code);
				_dataManager.setRawResponse(accessToken.getRawResponse());
				
				loadMoreData();
			} catch (Exception ignored) {
			}
		}
	};
	
	// endregion OAuth
	
	// region LazeLoad
	
	private Handler _lazyLoaderScrollListenerHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message message) {
			if (message == null) {
				return false;
			}
			
			switch (message.what) {
				case LazyLoaderScrollListener.SCROLL:
					int firstVisibleItem = message.arg1;
					int visibleItemCount = message.arg2;
					
					if (firstVisibleItem + visibleItemCount >= _usersCount - _loadInitReminder) {
						loadMoreData();
					}
					
					break;
			}
			
			return true;
		}
	});
	
	private void loadMoreData() {
		runOnUiThread(new Runnable() {
			public void run() {
				_dataManager.getUsers(
						_since,
						_getUserCompleteHandler
				);
			}
		});
	}
	
	private Handler _getUserCompleteHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message message) {
			switch (message.what) {
				case DataManager.SUCCESS:
					UserArrayList users = null;
					if (message.obj != null && message.obj instanceof UserArrayList) {
						users = (UserArrayList) message.obj;
					}
					
					if (users != null) {
						if (_since == 0) {
							_loadInitReminder = (int)(users.size() / 2.0);
						}
						
						addToList(users);
						
						_usersCount += users.size();
						_since = users.get(users.size() - 1).id;
					}
					
					break;
				case DataManager.ERROR:
					
					break;
			}
			
			return true;
		}
	});
	
	// endregion LazeLoad
	
	
	private Handler _userListViewBaseAdapterHandler = new Handler(new Handler.Callback() {
		public boolean handleMessage(Message message) {
			switch (message.what) {
				case UserListViewBaseAdapter.NEED_UPDATE:
					if (message.obj == null || !message.obj.getClass().equals(User.class)) {
						return false;
					}
					
					User user = (User) message.obj;
					updateData(user);
					
					break;
				
			}
			
			return true;
		}
	});
	
	
	private void updateData(User user) {
		_avatarManager.loadUserAvatar(
			user,
			_avatarManagerLoadUserAvatarCompleteHandler
		);
		
		if (!user.getRepositoriesLoaded()) {
			_dataManager.getUsersRepositories(
				user,
				_dataManagerGetUsersRepositoriesCompleteHandler
			);
		}
	}
	
	private Handler _avatarManagerLoadUserAvatarCompleteHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message message) {
			switch (message.what) {
				case AvatarManager.SUCCESS:
					if (message.obj != null && message.obj.getClass().equals(User.class)) {
						User user = (User) message.obj;
						Bitmap bitmap = _avatarManager.getImage(user.avatar_url);
						_adapter.updateUserAvatar(user, bitmap);
					}
					
					break;
			}
			
			return true;
		}
	});
	
	private Handler _dataManagerGetUsersRepositoriesCompleteHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message message) {
			switch (message.what) {
				case DataManager.SUCCESS:
					RepositoriesArrayList repositories = null;
					if (message.obj != null && message.obj instanceof RepositoriesArrayList) {
						repositories = (RepositoriesArrayList) message.obj;
					}
					
					if (repositories != null) {
						User user = repositories.getUser();
						user.setRepositories(repositories);
						_adapter.updateUserRepositories(user);
					}
					break;
			}
			return true;
		}
	});
	
	
	private void addToList(final UserArrayList users) {
		_adapter.addToList(users);
		_adapter.notifyDataSetChanged();
	}
	
}
