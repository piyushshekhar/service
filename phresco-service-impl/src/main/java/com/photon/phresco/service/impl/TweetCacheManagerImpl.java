package com.photon.phresco.service.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.photon.phresco.service.api.TweetCacheManager;
import com.photon.phresco.service.model.ServerConfiguration;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

public class TweetCacheManagerImpl implements TweetCacheManager {

	private static final String KEY = "tweet";
	//private static final String Default = "[{\"error\":\"Rate limit exceeded. Clients may not make more than 150 requests per hour.\",\"request\":\"\\/statuses\\/user_timeline\\/photoninfotech.json\"}]";
	private static final String DEFAULT_TWEET_MSG = "[{}]";
	private static final Logger S_LOGGER = Logger.getLogger(TweetCacheManagerImpl.class);
	private EHCacheManager manager;
	private ServerConfiguration config = null;

	public TweetCacheManagerImpl(ServerConfiguration config) {
		this.config = config;
		manager = new EHCacheManager();
		manager.addWidget(KEY, new Widget(DEFAULT_TWEET_MSG));
		cacheTweetMessage();
	}

	// Put the tweet message into the cache.
	public void cacheTweetMessage() {
		String responseString = getTweetMessageFromService();
		if (StringUtils.isNotEmpty(responseString)&& !responseString.equalsIgnoreCase(DEFAULT_TWEET_MSG)) {
			manager.addWidget(KEY, new Widget(responseString));
		}
	}

	// Get the tweet message from the cache.
	public String getTweetMessageFromCache() {
		return manager.getWidget(KEY).getName();
	}

	// Get News Update from the twitter
	private String getTweetMessageFromService() {
		String twitterMessage = DEFAULT_TWEET_MSG;
		try {
			Client client = Client.create();
//			//TODO: Read the twitter service url from server.config
			WebResource webResource = client.resource(config.getTwitterServiceURL());
			twitterMessage = webResource.get(String.class);
		} catch (java.lang.Exception e) {
			S_LOGGER.error("Error : " + e.getMessage());
		}
		return twitterMessage;
	}

}
