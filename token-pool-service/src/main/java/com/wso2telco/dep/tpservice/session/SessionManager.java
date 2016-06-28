/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wso2telco.dep.tpservice.session;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wso2telco.dep.tpservice.manager.WhoManager;
import com.wso2telco.dep.tpservice.model.TokenDTO;
import com.wso2telco.dep.tpservice.model.WhoDTO;
import com.wso2telco.dep.tpservice.util.exception.BusinessException;

final class SessionManager implements Runnable {

	private Logger log = LoggerFactory.getLogger(SessionManager.class);
	private WhoManager whoService;

	private static SessionManager instance;

	private SessionManager() {
		whoService = new WhoManager();
	}

	public static SessionManager getInstance() {
		if (instance == null) {
			instance = new SessionManager();
		}
		return instance;
	}

	@Override
	public void run() {

	}

	private void loadConfig() throws BusinessException {
		log.info("INITIALIZE TOKEN DATA ");
		
		ArrayList<WhoDTO> whoDTOs = whoService.getAllOwners();
		for (WhoDTO whoDTO : whoDTOs) {
			
		}
	
		
	}

}
