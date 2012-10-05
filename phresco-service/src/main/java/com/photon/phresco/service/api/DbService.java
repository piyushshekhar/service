/*
 * ###
 * Phresco Service
 * 
 * Copyright (C) 1999 - 2012 Photon Infotech Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ###
 */

package com.photon.phresco.service.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.document.mongodb.MongoOperations;
import org.springframework.data.document.mongodb.query.Criteria;
import org.springframework.data.document.mongodb.query.Query;

import com.photon.phresco.commons.model.ApplicationInfo;
import com.photon.phresco.commons.model.ArtifactGroup;
import com.photon.phresco.commons.model.DownloadInfo;
import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.service.converters.ConvertersFactory;
import com.photon.phresco.service.dao.ApplicationInfoDAO;
import com.photon.phresco.service.dao.ArtifactGroupDAO;
import com.photon.phresco.service.dao.DownloadsDAO;
import com.photon.phresco.util.ServiceConstants;

public class DbService implements ServiceConstants {

	private static final Logger S_LOGGER = Logger.getLogger(DbService.class);
	private static Boolean isDebugEnabled = S_LOGGER.isDebugEnabled();
	
	private static final String MONGO_TEMPLATE = "mongoTemplate";
	protected MongoOperations mongoOperation;

	protected DbService() {
		ApplicationContext ctx = new AnnotationConfigApplicationContext(MongoConfig.class);
    	mongoOperation = (MongoOperations)ctx.getBean(MONGO_TEMPLATE);
	}

	protected Query createCustomerIdQuery(String customerId) {
		List<String> customerIds = new ArrayList<String>();
		customerIds.add(customerId);
		
		if(!customerId.equals(DEFAULT_CUSTOMER_NAME)) {
			customerIds.add(DEFAULT_CUSTOMER_NAME);
		}

		Criteria criteria = Criteria.where(DB_COLUMN_CUSTOMERIDS).in(customerIds.toArray());
		Query query = new Query(criteria);

	    if (isDebugEnabled) {
	        S_LOGGER.debug("query.getQueryObject() " + query.getQueryObject());
	    }
	    
	    return query;
	}
	
	protected List<ArtifactGroup> convertArtifactDAOs(List<ArtifactGroupDAO> artifactGroupDAOs) throws PhrescoException {
		Converter<ArtifactGroupDAO, ArtifactGroup> converter = 
			(Converter<ArtifactGroupDAO, ArtifactGroup>) ConvertersFactory.getConverter(ArtifactGroupDAO.class);
		List<ArtifactGroup> artifactGroups = new ArrayList<ArtifactGroup>();
		for (ArtifactGroupDAO artifactDAO : artifactGroupDAOs) {
			artifactGroups.add(converter.convertDAOToObject(artifactDAO, mongoOperation));
		}
		return artifactGroups;
	}
	
	protected List<DownloadInfo> convertDownloadDAOs(List<DownloadsDAO> downloadsDAOs) throws PhrescoException {
		List<DownloadInfo> infos = new ArrayList<DownloadInfo>();
		Converter<DownloadsDAO, DownloadInfo> converter = 
			(Converter<DownloadsDAO, DownloadInfo>) ConvertersFactory.getConverter(DownloadsDAO.class);
		for (DownloadsDAO downloadsDAO : downloadsDAOs) {
			infos.add(converter.convertDAOToObject(downloadsDAO, mongoOperation));
		}
		return infos;
	}
	
	protected ArtifactGroup convertArtifactDAO(ArtifactGroupDAO artifactGroupDAO) throws PhrescoException {
		Converter<ArtifactGroupDAO, ArtifactGroup> converter = 
			(Converter<ArtifactGroupDAO, ArtifactGroup>) ConvertersFactory.getConverter(ArtifactGroupDAO.class);
		return converter.convertDAOToObject(artifactGroupDAO, mongoOperation);
	}
	
	protected ApplicationInfo convertApplicationDAO(ApplicationInfoDAO applicationInfoDAO) throws PhrescoException {
		Converter<ApplicationInfoDAO, ApplicationInfo> appConverter = 
			(Converter<ApplicationInfoDAO, ApplicationInfo>) ConvertersFactory.getConverter(ApplicationInfoDAO.class);
		return appConverter.convertDAOToObject(applicationInfoDAO, mongoOperation);
	}
}
