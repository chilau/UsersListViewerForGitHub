package com.torikos.managers;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.torikos.R;
import com.torikos.managers.enums.HttpRequestEventEnum;
import com.torikos.managers.enums.RequestEventEnum;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class RequestManager {
	
	private static final String TAG = RequestManager.class.getSimpleName();
	
	public static final int SUCCESS = 0;
	public static final int ERROR = 1;
	
	private static final boolean LOG_ENABLE = true;
	
	private Context _context;
	
	
	public void init(Context context) {
		_context = context;
	}
	
	
	public void getUsers(long since, String rawResponse, final Handler completeHandler) {
		if (_context == null) {
			return;
		}
		
		if (since < 0) {
			since = 0;
		}
		
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(_context.getString(R.string.users_api_url));
		stringBuilder.append("?since=");
		stringBuilder.append(String.valueOf(since));
		
		if (rawResponse != null && !rawResponse.isEmpty()) {
			stringBuilder.append("&");
			stringBuilder.append(rawResponse);
		}
		
		sendRequestToServer(
				stringBuilder.toString(),
				new Handler(new Handler.Callback() {
					public boolean handleMessage(Message message) {
						switch (message.what) {
							case RequestEventEnum.SUCCESS:
								if (completeHandler != null) {
									String users_json = (String) message.obj;
									completeHandler.obtainMessage(SUCCESS, -1, -1, users_json).sendToTarget();
								}
								
								break;
							default:
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
	
	public void getUsersRepositories(String userLogin, String rawResponse, final Handler completeHandler) {
		if (_context == null) {
			return;
		}
		
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(_context.getString(R.string.users_api_url));
		stringBuilder.append("/");
		stringBuilder.append(userLogin);
		stringBuilder.append("/repos");
		
		if (rawResponse != null && !rawResponse.isEmpty()) {
			stringBuilder.append("?");
			stringBuilder.append(rawResponse);
		}
		
		sendRequestToServer(
				stringBuilder.toString(),
				new Handler(new Handler.Callback() {
					public boolean handleMessage(Message message) {
						switch (message.what) {
							case RequestEventEnum.SUCCESS:
								if (completeHandler != null) {
									String users_json = (String) message.obj;
									completeHandler.obtainMessage(SUCCESS, -1, -1, users_json).sendToTarget();
								}
								
								break;
							default:
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
	
	
	private void sendRequestToServer(final String urlString, final Handler completeHandler) {
		sendRequest(
				urlString,
				new Handler(new Handler.Callback() {
					public boolean handleMessage(Message message) {
						switch (message.what) {
							case HttpRequestEventEnum.SUCCESS:
								if (message.obj != null) {
									String response = message.obj.toString();
									if (response.contentEquals("")) {
										if (completeHandler != null) {
											completeHandler.obtainMessage(RequestEventEnum.EMPTY_RESPONSE, -1, -1, null).sendToTarget();
										}
										
										return true;
									}
									
									if (completeHandler != null) {
										completeHandler.obtainMessage(RequestEventEnum.SUCCESS, -1, -1, response).sendToTarget();
									}
								} else {
									if (completeHandler != null) {
										completeHandler.obtainMessage(RequestEventEnum.EMPTY_RESPONSE, -1, -1, null).sendToTarget();
									}
								}
								break;
							case HttpRequestEventEnum.FAIL:
								if (message.obj != null) {
									Exception exception = (Exception) message.obj;
									if (completeHandler != null) {
										completeHandler.obtainMessage(RequestEventEnum.RESPONSE_EXCEPTION, -1, -1, exception).sendToTarget();
									}
								} else {
									int responseCode = message.arg1;
									if (completeHandler != null) {
										completeHandler.obtainMessage(RequestEventEnum.RESPONSE_ERROR, responseCode, -1, null).sendToTarget();
									}
								}
								
								break;
						}
						
						return true;
					}
				})
		);
	}
	
	private void sendRequest(final String urlString, final Handler completeHandler) {
		Thread sendRequestThread = new Thread(
				new Runnable() {
					@Override
					public void run() {
						HttpURLConnection urlConnection = null;
						
						String responseString = "";
						Exception exception = null;
						int responseCode = HttpsURLConnection.HTTP_OK;
						
						try {
							URL url = new URL(urlString);
							
							if (LOG_ENABLE) {
								String logMessage = "   Send request to URL            - " + urlString;
								Log.i(TAG, logMessage);
								
								Log.i(TAG, " ");
							}
							
							urlConnection = (HttpURLConnection) url.openConnection();
							urlConnection.setRequestMethod("GET");
							urlConnection.setRequestProperty("Accept", "application/vnd.github.v3+json");
							
							responseCode = urlConnection.getResponseCode();
							if (responseCode == HttpsURLConnection.HTTP_OK) {
								BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
								String inputLine;
								StringBuilder response = new StringBuilder();
								
								while ((inputLine = bufferedReader.readLine()) != null) {
									response.append(inputLine);
								}
								bufferedReader.close();
								
								responseString = response.toString();
							} else {
								responseString = "";
								
								if (LOG_ENABLE) {
									String logMessage = "   Empty response with code  - " + responseCode;
									Log.i(TAG, logMessage);
									
									Log.i(TAG, " ");
								}
							}
						} catch (Exception ignored) {
							exception = ignored;
						} finally {
							if (urlConnection != null) {
								urlConnection.disconnect();
							}
						}
						
						if (completeHandler != null) {
							if (exception != null) {
								completeHandler.obtainMessage(HttpRequestEventEnum.FAIL,	-1,				-1,	exception).sendToTarget();
							} else if (responseCode == HttpsURLConnection.HTTP_OK) {
								completeHandler.obtainMessage(HttpRequestEventEnum.SUCCESS,	-1,				-1,	responseString).sendToTarget();
							} else { //responseCode != HttpsURLConnection.HTTP_OK
								completeHandler.obtainMessage(HttpRequestEventEnum.FAIL,	responseCode,	-1,	null).sendToTarget();
							}
						}
					}
				}
		);
		sendRequestThread.start();
	}
	
}
