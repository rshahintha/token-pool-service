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

package com.wso2telco.dep.tpservice.pool.alltimefirst;

import com.wso2telco.dep.tpservice.dao.RetryConnectionDAO;
import com.wso2telco.dep.tpservice.dao.WhoDAO;
import com.wso2telco.dep.tpservice.manager.EmailManager;
import com.wso2telco.dep.tpservice.model.RetryConnectionDTO;
import com.wso2telco.dep.tpservice.model.TokenDTO;
import com.wso2telco.dep.tpservice.model.WhoDTO;
import com.wso2telco.dep.tpservice.pool.TokenReGenarator;
import com.wso2telco.dep.tpservice.util.exception.BusinessException;
import com.wso2telco.dep.tpservice.util.exception.GenaralError;
import com.wso2telco.dep.tpservice.util.exception.ThrowableError;
import com.wso2telco.dep.tpservice.util.exception.TokenException;
import org.slf4j.LoggerFactory;

class MasterModeTp extends AbstrController {

	private TokenReGenarator regenarator;
	RetryConnectionDTO retryDTO;
	RetryConnectionDAO retryDAO;
	private  final String MAIL_BODY_CONNECTION_LOSS = "Token genaration failed. Retry attempt :";
	private final String MAIL_SUBJECT_CONNECTION_LOSS = "[Token Genaration Failed]- Error occurd while connecting to ";
	
	private  final String MAIL_BODY_INVALID_CREDENTIALS = "Token genaration failed.";
	private final String MAIL_SUBJECT_INVALID_CREDENTIALS = "[Token Genaration Failed]- Credentials ";
	private final String MAIL_SUBJECT_END_OF_RETRY =  "The reach the maximum retry attempts reached .Make sure the token endpoint is up and running"
		+ "Re start the owners token pool manyally.";
	
	protected EmailManager manager;
	protected MasterModeTp(WhoDTO whoDTO,TokenDTO tokenDTO) throws TokenException {
		super(whoDTO,tokenDTO);
		log = LoggerFactory.getLogger(MasterModeTp.class);
		this.regenarator = new TokenReGenarator();
		this.manager = EmailManager.getInstance();
	}
	
	public void removeToken(final TokenDTO token) throws TokenException {
			super.removeToken(token);
			log.debug("remove form the DB "+token);
			tokenManager.invalidate(token);
	}
	
	@Override
	protected TokenDTO reGenarate() throws TokenException {
		TokenDTO newTokenDTO = null;
		newTokenDTO = new TokenDTO();
		WhoDAO whodao = new WhoDAO();
		String emailId = String.valueOf(whoDTO.getId());
		try {
			// generating new token
			newTokenDTO = regenarator.reGenarate(whoDTO, tokenDTO);

			tokenManager.saveToken(whoDTO, newTokenDTO);
			
		} catch (TokenException e) {
			ThrowableError x = e.getErrorType();
			if (x.getCode().equals(TokenException.TokenError.CONNECTION_LOSS.getCode())) {

				// int attCount = whodao.getRetryAttempt(whoDTO.getOwnerId());
				int attCount = whodao.incrimentRetryAttempt(whoDTO.getOwnerId());

				try {
					manager.sendConnectionFailNotification(whoDTO,MAIL_SUBJECT_CONNECTION_LOSS + whoDTO.getOwnerId(), MAIL_BODY_CONNECTION_LOSS+attCount,  e);
				
				
				if (attCount >= whoDTO.getMaxRetryCount()) {
					log.error("You have reach the maximum retry attempts :"+whoDTO);
					manager.sendConnectionFailNotification(whoDTO,MAIL_SUBJECT_CONNECTION_LOSS + whoDTO.getOwnerId(),MAIL_SUBJECT_END_OF_RETRY,  e);
					throw new TokenException(TokenException.TokenError.REACH_MAX_RETRY_ATTEMPT);

				}
				} catch (BusinessException e2) {
					log.error("reGenarate ",e2);
					throw new TokenException(GenaralError.INTERNAL_SERVER_ERROR);
				}
				
				// do the mailng,
				int number = whoDTO.getId();
				String url = whoDTO.getTokenUrl();

				// int maxCount = retryDTO.getRetryMax();
				int delay = whoDTO.getRetryDelay();
				attCount += 1;
				// regenarator.reGenarate(whoDTO, tokenDTO);

				try {
					Thread.sleep(delay);

				} catch (InterruptedException e1) {
					log.error("reGenarate ",e1);
					throw new TokenException(GenaralError.INTERNAL_SERVER_ERROR);
				}

				reGenarate();

			} else {

				try {
					manager.sendConnectionFailNotification(whoDTO,MAIL_SUBJECT_INVALID_CREDENTIALS, MAIL_BODY_INVALID_CREDENTIALS,  e);
				} catch (BusinessException e1) {
					log.error("reGenarate ",e1);
					throw new  TokenException(TokenException.TokenError.EMAIL_SENDING_FAIL);
				}
				
				throw new TokenException(TokenException.TokenError.INVALID_REFRESH_CREDENTIALS);

			}
		}
		// throw new TokenException(e.getErrorType());
		return newTokenDTO;

	}

}



