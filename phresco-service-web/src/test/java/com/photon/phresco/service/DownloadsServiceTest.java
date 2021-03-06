package com.photon.phresco.service;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Type;
import java.util.List;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.photon.phresco.commons.model.DownloadInfo;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.service.api.PhrescoServerFactory;
import com.photon.phresco.service.api.RepositoryManager;
import com.photon.phresco.service.util.ServerConstants;
import com.photon.phresco.util.ServiceConstants;

public class DownloadsServiceTest implements ServerConstants{

	private static final String SOFTWARE_REPO_PATH = "/softwares/info/1.0/info-1.0.json";
	@Test
	public void testGetAvailableDownloads() throws PhrescoException {
    	PhrescoServerFactory.initialize();
    	RepositoryManager repoMgr = PhrescoServerFactory.getRepositoryManager();
		String downloadInfoJSON = repoMgr.getArtifactAsString(SOFTWARE_REPO_PATH, ServiceConstants.DEFAULT_CUSTOMER_NAME);
    	Type type = new TypeToken<List<DownloadInfo>>() {}.getType();
		Gson gson = new Gson();
		List<DownloadInfo> downloadInfoList = gson.fromJson(downloadInfoJSON, type);
    	assertNotNull(downloadInfoList);
	}

}
