/*
 * ###
 * Service Web Archive
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
package com.photon.phresco.service.admin.actions.components;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.photon.phresco.exception.PhrescoException;
import com.photon.phresco.model.ProjectInfo;
import com.photon.phresco.model.Technology;
import com.photon.phresco.service.admin.actions.ServiceBaseAction;
import com.photon.phresco.service.admin.commons.LogErrorReport;
import com.photon.phresco.service.client.api.Content;
import com.photon.phresco.service.rest.api.ComponentService;
import com.photon.phresco.util.ServiceConstants;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.MultiPart;

public class PilotProjects extends ServiceBaseAction { 
	
	private static final long serialVersionUID = 6801037145464060759L;
	private static final Logger S_LOGGER = Logger.getLogger(PilotProjects.class);
	private static Boolean isDebugEnabled = S_LOGGER.isDebugEnabled();
	
	private String name = null;
	private String nameError = null;
	private String fileError = null;
	private boolean errorFound = false;
	
	private String description = null;
	private String projectId = null;
	private String fromPage = null;
	private String customerId = null;
	private String techId = null;
	private static byte[] pilotProByteArray = null;
	private static String pilotProJarName = null;

	public String list() throws PhrescoException {
        if (isDebugEnabled) {
            S_LOGGER.debug("Entering Method PilotProjects.list()");
        }

		try {
			List<ProjectInfo> pilotProjects = getServiceManager().getPilotProjects(customerId);
			getHttpRequest().setAttribute(REQ_PILOT_PROJECTS, pilotProjects);
			getHttpRequest().setAttribute(REQ_CUST_CUSTOMER_ID, customerId);
		} catch (PhrescoException e) {
			new LogErrorReport(e, PILOT_PROJECTS_LIST_EXCEPTION);
			
			return LOG_ERROR;	
		}
		
		return COMP_PILOTPROJ_LIST;
	}
	
    public String add() throws PhrescoException {
    	if (isDebugEnabled) {	
    		S_LOGGER.debug("Entering Method PilotProjects.add()");
    	}
    	
    	try {
    		List<Technology> technologies = getServiceManager().getArcheTypes(customerId);
    		getHttpRequest().setAttribute(REQ_ARCHE_TYPES, technologies);
    	} catch (PhrescoException e) {
			new LogErrorReport(e, PILOT_PROJECTS_ADD_EXCEPTION);
			
			return LOG_ERROR;	
		}
    	
    	return COMP_PILOTPROJ_ADD;
    }
	
    public String edit() throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entering Method PilotProjects.edit()");
    	}

    	try {
    		ProjectInfo pilotProjectInfo = getServiceManager().getPilotProject(projectId, customerId);
    		getHttpRequest().setAttribute(REQ_PILOT_PROINFO, pilotProjectInfo);
    		getHttpRequest().setAttribute(REQ_FROM_PAGE, REQ_EDIT);
    	} catch (PhrescoException e) {
			new LogErrorReport(e, PILOT_PROJECTS_EDIT_EXCEPTION);
			
			return LOG_ERROR;	
		}

    	return COMP_PILOTPROJ_ADD;
    }
    
    public String save() throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entering Method PilotProjects.save()");
    	}
    	
    	try {
    		MultiPart multiPart = new MultiPart();
    		
    		List<ProjectInfo> pilotProInfo = new ArrayList<ProjectInfo>();
    		ProjectInfo proInfo = new ProjectInfo();
    		proInfo.setName(name);
    		proInfo.setDescription(description);
    		proInfo.setTechId(techId);
    		proInfo.setCustomerId(customerId);
    		Technology technology = new Technology();
    		technology.setId(techId);
    		ComponentService cs = new ComponentService();
    		Response techName = cs.getTechnology(techId);
    		technology = (Technology) techName.getEntity();
    		technology.setName(technology.getName());
    		proInfo.setTechnology(technology);
    		
    		BodyPart jsonPart = new BodyPart();
		    jsonPart.setMediaType(MediaType.APPLICATION_JSON_TYPE);
		    jsonPart.setEntity(proInfo);
		    Content content = new Content("object", name, null, null, null, 0);
		    jsonPart.setContentDisposition(content);
		    multiPart.bodyPart(jsonPart);
		    
		    if (StringUtils.isNotEmpty(pilotProJarName)) {
				InputStream pilotProIs = new ByteArrayInputStream(pilotProByteArray);
				BodyPart binaryPart2 = getServiceManager().createBodyPart(name, FILE_FOR_APPTYPE, pilotProIs );
		        multiPart.bodyPart(binaryPart2);
			}
		    
    		pilotProInfo.add(proInfo);
    		ClientResponse clientResponse = getServiceManager().createPilotProjects(pilotProInfo, customerId);
    		if (clientResponse.getStatus() != ServiceConstants.RES_CODE_200 && clientResponse.getStatus() != ServiceConstants.RES_CODE_201  ) {
    			addActionError(getText(PLTPROJ_NOT_ADDED, Collections.singletonList(name)));
    		} else {
    			addActionMessage(getText(PLTPROJ_ADDED, Collections.singletonList(name)));
    		}
    	} catch (PhrescoException e) {
			new LogErrorReport(e, PILOT_PROJECTS_SAVE_EXCEPTION);
			
			return LOG_ERROR;	
		}

    	return list();
    }
    
    public String update() throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entering Method  PilotProjects.update()");
    	}

    	try {
    		MultiPart multiPart = new MultiPart();
    		
    		ProjectInfo pilotProInfo = new ProjectInfo();
    		pilotProInfo.setId(projectId);
    		pilotProInfo.setName(name);
    		pilotProInfo.setDescription(description);
    		pilotProInfo.setTechId(techId);
    		pilotProInfo.setCustomerId(customerId);
    		Technology technology = new Technology();
    		technology.setId(techId);
    		ComponentService cs = new ComponentService();
    		Response techName = cs.getTechnology(techId);
    		technology = (Technology) techName.getEntity();
    		technology.setName(technology.getName());
    		pilotProInfo.setTechnology(technology);
    		
    		BodyPart jsonPart = new BodyPart();
		    jsonPart.setMediaType(MediaType.APPLICATION_JSON_TYPE);
		    jsonPart.setEntity(pilotProInfo);
		    Content content = new Content("object", name, null, null, null, 0);
		    jsonPart.setContentDisposition(content);
		    multiPart.bodyPart(jsonPart);
		    
		    if (StringUtils.isNotEmpty(pilotProJarName)) {
				InputStream pilotProIs = new ByteArrayInputStream(pilotProByteArray);
				BodyPart binaryPart2 = getServiceManager().createBodyPart(name, FILE_FOR_APPTYPE, pilotProIs );
		        multiPart.bodyPart(binaryPart2);
			}
    		
    		getServiceManager().updatePilotProject(pilotProInfo, projectId, customerId);
    	} catch (PhrescoException e) {
			new LogErrorReport(e, PILOT_PROJECTS_UPDATE_EXCEPTION);
			
			return LOG_ERROR;	
		}

    	return list();
    }
	
    public String delete() throws PhrescoException {
    	if (isDebugEnabled) {
    		S_LOGGER.debug("Entering Method PilotProjects.delete()");
    	}

    	try {
    		String[] projectIds = getHttpRequest().getParameterValues("projectId");
    		if (projectIds != null) {
    			for (String projectID : projectIds) {
    				ClientResponse clientResponse =getServiceManager().deletePilotProject(projectID, customerId);
    				if (clientResponse.getStatus() != ServiceConstants.RES_CODE_200) {
    					addActionError(getText(PLTPROJ_NOT_DELETED));
    				}
    			}
    			addActionMessage(getText(PLTPROJ_DELETED));
    		}
    	}catch (PhrescoException e) {
			new LogErrorReport(e, PILOT_PROJECTS_DELETE_EXCEPTION);
			
			return LOG_ERROR;	
		}

    	return list();
    }
    
    public String uploadFile() throws PhrescoException {
    	if (isDebugEnabled) {
			S_LOGGER.debug("Entering Method  PilotProjects.uploadFile()");
		}
    	
    	PrintWriter writer = null;
		try {
            writer = getHttpResponse().getWriter();
	        pilotProJarName = getHttpRequest().getHeader(X_FILE_NAME);
	        if (pilotProJarName.endsWith(REQ_JAR_FILE_EXTENSION) || pilotProJarName.endsWith(REQ_ZIP_FILE_EXTENSION) 
	        		|| pilotProJarName.endsWith(REQ_TAR_GZ_FILE_EXTENSION)) {
	        	InputStream is = getHttpRequest().getInputStream();
	        	pilotProByteArray = IOUtils.toByteArray(is);
	            getHttpResponse().setStatus(getHttpResponse().SC_OK);
	            writer.print(SUCCESS_TRUE);
		        writer.flush();
		        writer.close();
	        }
		} catch (Exception e) {
			getHttpResponse().setStatus(getHttpResponse().SC_INTERNAL_SERVER_ERROR);
            writer.print(SUCCESS_FALSE);
			throw new PhrescoException(e);
		}
		
		return SUCCESS;
	}
	
	public void removeUploadedFile() {
		if (isDebugEnabled) {
			S_LOGGER.debug("Entering Method  PilotProjects.removeUploadedFile()");
		}
		pilotProJarName = null;
		pilotProByteArray = null;
	}
	
    public String validateForm() {
    	if (isDebugEnabled) {
			S_LOGGER.debug("Entering Method  PilotProjects.validateForm()");
		}
    	
    	boolean isError = false;
    	if (StringUtils.isEmpty(name)) {
    		setNameError(getText(KEY_I18N_ERR_NAME_EMPTY ));
    		isError = true;
    	}
    	
    	if (StringUtils.isEmpty(pilotProJarName) || pilotProJarName == null) {
    		setFileError(getText(KEY_I18N_ERR_PLTPROJ_EMPTY));
    		isError = true;
    	}
    	
    	if (isError) {
    		setErrorFound(true);
    	}

    	return SUCCESS;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNameError() {
		return nameError;
	}

	public void setNameError(String nameError) {
		this.nameError = nameError;
	}
	
	public String getFileError() {
		return fileError;
	}

	public void setFileError(String fileError) {
		this.fileError = fileError;
	}

	public boolean isErrorFound() {
		return errorFound;
	}

	public void setErrorFound(boolean errorFound) {
		this.errorFound = errorFound;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
	
	public String getFromPage() {
		return fromPage;
	}

	public void setFromPage(String fromPage) {
		this.fromPage = fromPage;
	}

	public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    
    public String getTechId() {
		return techId;
	}

	public void setTechId(String techId) {
		this.techId = techId;
	}
}