/*
 * Copyright 2005-2007 The Kuali Foundation.
 *
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.kew.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.core.exception.RiceRuntimeException;
import org.kuali.rice.core.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kew.actionitem.ActionItem;
import org.kuali.rice.kew.actionrequest.ActionRequestValue;
import org.kuali.rice.kew.actionrequest.KimPrincipalRecipient;
import org.kuali.rice.kew.actionrequest.Recipient;
import org.kuali.rice.kew.actiontaken.ActionTakenValue;
import org.kuali.rice.kew.definition.AttributeDefinition;
import org.kuali.rice.kew.docsearch.DocSearchCriteriaDTO;
import org.kuali.rice.kew.docsearch.DocumentSearchResultComponents;
import org.kuali.rice.kew.doctype.bo.DocumentType;
import org.kuali.rice.kew.dto.ActionItemDTO;
import org.kuali.rice.kew.dto.ActionRequestDTO;
import org.kuali.rice.kew.dto.ActionTakenDTO;
import org.kuali.rice.kew.dto.DTOConverter;
import org.kuali.rice.kew.dto.DocumentContentDTO;
import org.kuali.rice.kew.dto.DocumentDetailDTO;
import org.kuali.rice.kew.dto.DocumentSearchCriteriaDTO;
import org.kuali.rice.kew.dto.DocumentSearchResultDTO;
import org.kuali.rice.kew.dto.DocumentTypeDTO;
import org.kuali.rice.kew.dto.PropertyDefinitionDTO;
import org.kuali.rice.kew.dto.ReportCriteriaDTO;
import org.kuali.rice.kew.dto.RouteHeaderDTO;
import org.kuali.rice.kew.dto.RouteNodeInstanceDTO;
import org.kuali.rice.kew.dto.RuleDTO;
import org.kuali.rice.kew.dto.RuleExtensionDTO;
import org.kuali.rice.kew.dto.RuleReportCriteriaDTO;
import org.kuali.rice.kew.dto.WorkflowAttributeDefinitionDTO;
import org.kuali.rice.kew.dto.WorkflowAttributeValidationErrorDTO;
import org.kuali.rice.kew.engine.ActivationContext;
import org.kuali.rice.kew.engine.CompatUtils;
import org.kuali.rice.kew.engine.RouteContext;
import org.kuali.rice.kew.engine.node.KeyValuePair;
import org.kuali.rice.kew.engine.node.RouteNode;
import org.kuali.rice.kew.engine.node.RouteNodeInstance;
import org.kuali.rice.kew.engine.simulation.SimulationCriteria;
import org.kuali.rice.kew.engine.simulation.SimulationEngine;
import org.kuali.rice.kew.engine.simulation.SimulationResults;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.rice.kew.rule.FlexRM;
import org.kuali.rice.kew.rule.RuleBaseValues;
import org.kuali.rice.kew.rule.WorkflowAttribute;
import org.kuali.rice.kew.rule.WorkflowAttributeValidationError;
import org.kuali.rice.kew.rule.WorkflowAttributeXmlValidator;
import org.kuali.rice.kew.rule.xmlrouting.GenericXMLRuleAttribute;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kew.service.WorkflowUtility;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kew.util.Utilities;
import org.kuali.rice.kew.web.session.UserSession;
import org.kuali.rice.kim.bo.entity.KimPrincipal;
import org.kuali.rice.kim.service.KIMServiceLocator;
import org.kuali.rice.kns.util.KNSConstants;

@SuppressWarnings({"deprecation","unchecked"})
public class WorkflowUtilityWebServiceImpl implements WorkflowUtility {

    private static final Logger LOG = Logger.getLogger(WorkflowUtilityWebServiceImpl.class);

    public RouteHeaderDTO getRouteHeaderWithPrincipal(String principalId, Long documentId) throws WorkflowException {
        if (documentId == null) {
            LOG.error("null routeHeaderId passed in.  Throwing RuntimeExcpetion");
            throw new RuntimeException("Null documentId passed in.");
        }
        if (principalId == null) {
            LOG.error("null principalId passed in.");
            throw new RuntimeException("null principalId passed in");
        }
        if ( LOG.isDebugEnabled() ) {
            LOG.debug("Fetching RouteHeaderVO [id="+documentId+", user="+principalId+"]");
        }
        DocumentRouteHeaderValue document = loadDocument(documentId);
        RouteHeaderDTO routeHeaderVO = DTOConverter.convertRouteHeader(document, principalId);
        if (routeHeaderVO == null) {
        	LOG.error("Returning null RouteHeaderVO [id=" + documentId + ", user=" + principalId + "]");
        }
        if ( LOG.isDebugEnabled() ) {
            LOG.debug("Returning RouteHeaderVO [id=" + documentId + ", user=" + principalId + "]");
        }
        return routeHeaderVO;
    }

    public RouteHeaderDTO getRouteHeader(Long documentId) throws WorkflowException {
        if (documentId == null) {
            LOG.error("null routeHeaderId passed in.");
            throw new RuntimeException("null routeHeaderId passed in");
        }
        if ( LOG.isDebugEnabled() ) {
            LOG.debug("Fetching RouteHeaderVO [id="+documentId+"]");
        }
        DocumentRouteHeaderValue document = loadDocument(documentId);
        String principalId = UserSession.getAuthenticatedUser().getPrincipalId();
        RouteHeaderDTO routeHeaderVO = DTOConverter.convertRouteHeader(document, principalId);
        if (routeHeaderVO == null) {
        	LOG.error("Returning null RouteHeaderVO [id=" + documentId + "]");
        }
        if ( LOG.isDebugEnabled() ) {
            LOG.debug("Returning RouteHeaderVO [id=" + documentId + "]");
        }
        return routeHeaderVO;
    }

    public String getDocumentStatus(Long documentId) throws WorkflowException {
	if (documentId == null) {
	    LOG.error("null documentId passed in.");
            throw new IllegalArgumentException("null documentId passed in");
	}
	String documentStatus = KEWServiceLocator.getRouteHeaderService().getDocumentStatus(documentId);
	if (StringUtils.isEmpty(documentStatus)) {
	    throw new WorkflowException("Could not locate a document with the ID " + documentId);
	}
	return documentStatus;
    }

    public DocumentDetailDTO getDocumentDetail(Long documentId) throws WorkflowException {
        if (documentId == null) {
            LOG.error("null documentId passed in.");
            throw new RuntimeException("null documentId passed in");
        }
        if ( LOG.isDebugEnabled() ) {
        	LOG.debug("Fetching DocumentDetailVO [id="+documentId+"]");
        }
        DocumentRouteHeaderValue document = loadDocument(documentId);
        DocumentDetailDTO documentDetailVO = DTOConverter.convertDocumentDetail(document);
        if (documentDetailVO == null) {
        	LOG.error("Returning null DocumentDetailVO [id=" + documentId + "]");
        }
        if ( LOG.isDebugEnabled() ) {
        	LOG.debug("Returning DocumentDetailVO [id=" + documentId + "]");
        }
        return documentDetailVO;
    }

    public RouteNodeInstanceDTO getNodeInstance(Long nodeInstanceId) throws WorkflowException {
        if (nodeInstanceId == null) {
            LOG.error("null nodeInstanceId passed in.");
            throw new RuntimeException("null nodeInstanceId passed in");
        }
        if ( LOG.isDebugEnabled() ) {
        	LOG.debug("Fetching RouteNodeInstanceVO [id="+nodeInstanceId+"]");
        }
        RouteNodeInstance nodeInstance = KEWServiceLocator.getRouteNodeService().findRouteNodeInstanceById(nodeInstanceId);
        return DTOConverter.convertRouteNodeInstance(nodeInstance);
    }

    public DocumentTypeDTO getDocumentType(Long documentTypeId) throws WorkflowException {
        if (documentTypeId == null) {
            LOG.error("null documentTypeId passed in.");
            throw new RuntimeException("null documentTypeId passed in.");
        }
        if ( LOG.isDebugEnabled() ) {
        	LOG.debug("Fetching DocumentTypeVO [documentTypeId="+documentTypeId+"]");
        }
        return KEWServiceLocator.getDocumentTypeService().getDocumentTypeVO(documentTypeId);
    }

    public DocumentTypeDTO getDocumentTypeByName(String documentTypeName) throws WorkflowException {
        if (documentTypeName == null) {
            LOG.error("null documentTypeName passed in.");
            throw new RuntimeException("null documentTypeName passed in");
        }
        if ( LOG.isDebugEnabled() ) {
        	LOG.debug("Fetching DocumentTypeVO [documentTypeName="+documentTypeName+"]");
        }
        DocumentTypeDTO documentType = KEWServiceLocator.getDocumentTypeService().getDocumentTypeVO(documentTypeName);
        return documentType;
    }

    public Long getNewResponsibilityId() {
    	LOG.debug("Getting new responsibility id.");
        Long rid = KEWServiceLocator.getResponsibilityIdService().getNewResponsibilityId();
        if ( LOG.isDebugEnabled() ) {
        	LOG.debug("returning responsibility Id " + rid);
        }
        return rid;
    }

    public Integer getUserActionItemCount(String principalId) throws WorkflowException {
        return Integer.valueOf(KEWServiceLocator.getActionListService().getCount(principalId));
    }
    
	public ActionItemDTO[] getActionItemsForPrincipal(String principalId) throws WorkflowException {
        //added by Derek
        Collection actionItems = KEWServiceLocator.getActionListService().getActionList(principalId, null);
        ActionItemDTO[] actionItemVOs = new ActionItemDTO[actionItems.size()];
        int i = 0;
        for (Iterator iterator = actionItems.iterator(); iterator.hasNext(); i++) {
            ActionItem actionItem = (ActionItem) iterator.next();
            actionItemVOs[i] = DTOConverter.convertActionItem(actionItem);
        }
        return actionItemVOs;
    }

    public ActionItemDTO[] getActionItems(Long routeHeaderId) throws WorkflowException {
        Collection actionItems = KEWServiceLocator.getActionListService().getActionListForSingleDocument(routeHeaderId);
        ActionItemDTO[] actionItemVOs = new ActionItemDTO[actionItems.size()];
        int i = 0;
        for (Iterator iterator = actionItems.iterator(); iterator.hasNext(); i++) {
            ActionItem actionItem = (ActionItem) iterator.next();
            actionItemVOs[i] = DTOConverter.convertActionItem(actionItem);
        }
        return actionItemVOs;
    }

    public ActionItemDTO[] getActionItems(Long routeHeaderId, String[] actionRequestedCodes) throws WorkflowException {
        List<String> actionRequestedCds = Arrays.asList(actionRequestedCodes);
        ActionItemDTO[] actionItems = getActionItems(routeHeaderId);
        List<ActionItemDTO> matchingActionitems = new ArrayList<ActionItemDTO>();
        for (int i = 0; i < actionItems.length; i++) {
            ActionItemDTO actionItemVO = actionItems[i];
            if (actionRequestedCds.contains(actionItemVO.getActionRequestCd())) {
                matchingActionitems.add(actionItemVO);
            }
        }
        ActionItemDTO[] returnActionItems = new ActionItemDTO[matchingActionitems.size()];
        int j = 0;
        for (ActionItemDTO actionItemVO : matchingActionitems) {
            returnActionItems[j] = actionItemVO;
            j++;
        }
        return returnActionItems;
    }

    public ActionRequestDTO[] getActionRequests(Long routeHeaderId) throws WorkflowException {
        return getActionRequests(routeHeaderId, null, null);
    }

    public ActionRequestDTO[] getActionRequests(Long routeHeaderId, String nodeName, String principalId) throws WorkflowException {
        if (routeHeaderId == null) {
            LOG.error("null routeHeaderId passed in.");
            throw new RuntimeException("null routeHeaderId passed in.");
        }
        if ( LOG.isDebugEnabled() ) {
        	LOG.debug("Fetching ActionRequestVOs [docId="+routeHeaderId+"]");
        }
        List actionRequests = KEWServiceLocator.getActionRequestService().findAllActionRequestsByRouteHeaderId(routeHeaderId);
        List matchingActionRequests = new ArrayList();
        for (Iterator iterator = actionRequests.iterator(); iterator.hasNext();) {
            ActionRequestValue actionRequestValue = (ActionRequestValue) iterator.next();
            if (actionRequestMatches(actionRequestValue, nodeName, principalId)) {
                matchingActionRequests.add(actionRequestValue);
            }
        }
        ActionRequestDTO[] actionRequestVOs = new ActionRequestDTO[matchingActionRequests.size()];
        int i = 0;
        for (Iterator iter = matchingActionRequests.iterator(); iter.hasNext(); i++) {
            ActionRequestValue actionRequest = (ActionRequestValue) iter.next();
            actionRequestVOs[i] = DTOConverter.convertActionRequest(actionRequest);
        }
        return actionRequestVOs;
    }

    private boolean actionRequestMatches(ActionRequestValue actionRequest, String nodeName, String principalId) throws WorkflowException {
        boolean matchesUserId = true;  // assume a match in case user is empty
        boolean matchesNodeName = true;  // assume a match in case node name is empty
        if (StringUtils.isNotBlank(nodeName)) {
            matchesNodeName = nodeName.equals(actionRequest.getPotentialNodeName());
        }
        if (principalId != null) {
            matchesUserId = actionRequest.isRecipientRoutedRequest(principalId);
        }
        return matchesNodeName && matchesUserId;
    }

    public ActionTakenDTO[] getActionsTaken(Long routeHeaderId) throws WorkflowException {
        if (routeHeaderId == null) {
            LOG.error("null routeHeaderId passed in.");
            throw new RuntimeException("null routeHeaderId passed in.");
        }
        if ( LOG.isDebugEnabled() ) {
        	LOG.debug("Fetching ActionTakenVOs [docId="+routeHeaderId+"]");
        }
        Collection actionsTaken = KEWServiceLocator.getActionTakenService().findByRouteHeaderId(routeHeaderId);
        ActionTakenDTO[] actionTakenVOs = new ActionTakenDTO[actionsTaken.size()];
        int i = 0;
        for (Iterator iter = actionsTaken.iterator(); iter.hasNext(); i++) {
            ActionTakenValue actionTaken = (ActionTakenValue) iter.next();
            actionTakenVOs[i] = DTOConverter.convertActionTaken(actionTaken);
        }
        return actionTakenVOs;
    }

    /**
     * This work is also being done in the bowels of convertDocumentContentVO in DTOConverter so some code
     * could be reduced.
     *
     * @param definition
     * @return WorkflowAttributeValidationErrorVO[] errors from client input into attribute
     */
    public WorkflowAttributeValidationErrorDTO[] validateWorkflowAttributeDefinitionVO(WorkflowAttributeDefinitionDTO definition) throws WorkflowException {
        if (definition == null) {
            LOG.error("null definition passed in.");
            throw new RuntimeException("null definition passed in.");
        }
        if ( LOG.isDebugEnabled() ) {
        	LOG.debug("Validating WorkflowAttributeDefinitionVO [attributeName="+definition.getAttributeName()+"]");
        }
        AttributeDefinition attributeDefinition = DTOConverter.convertWorkflowAttributeDefinitionVO(definition, null);
        WorkflowAttribute attribute = null;
        if (attributeDefinition != null) {
        	attribute = (WorkflowAttribute) GlobalResourceLoader.getObject(attributeDefinition.getObjectDefinition());
        }
        if (attribute instanceof GenericXMLRuleAttribute) {
            Map<String, String> attributePropMap = new HashMap<String, String>();
            GenericXMLRuleAttribute xmlAttribute = (GenericXMLRuleAttribute)attribute;
            xmlAttribute.setRuleAttribute(attributeDefinition.getRuleAttribute());
            for (int i = 0; i < definition.getProperties().length; i++) {
		PropertyDefinitionDTO property = definition.getProperties()[i];
		attributePropMap.put(property.getName(), property.getValue());
	    }
            xmlAttribute.setParamMap(attributePropMap);
	}
        //validate inputs from client application if the attribute is capable
        if (attribute instanceof WorkflowAttributeXmlValidator) {
            List errors = ((WorkflowAttributeXmlValidator)attribute).validateClientRoutingData();
            WorkflowAttributeValidationErrorDTO[] errorVOs = new WorkflowAttributeValidationErrorDTO[errors.size()];
            for (int i = 0; i < errorVOs.length; i++) {
                errorVOs[i] = DTOConverter.convertWorkflowAttributeValidationError((WorkflowAttributeValidationError)errors.get(i));
            }
            return errorVOs;
        } else {
            // WORKAROUND: if it is not validatable, then just quietly succeed
            return new WorkflowAttributeValidationErrorDTO[0];
        }
    }

    public RouteNodeInstanceDTO[] getDocumentRouteNodeInstances(Long documentId) throws WorkflowException {
    	if ( LOG.isDebugEnabled() ) {
    		LOG.debug("Fetching RouteNodeInstanceVOs [docId=" + documentId + "]");
    	}
    	return convertRouteNodeInstances(KEWServiceLocator.getRouteNodeService().getFlattenedNodeInstances(loadDocument(documentId), true));
    }

    public RouteNodeInstanceDTO[] getActiveNodeInstances(Long documentId) throws WorkflowException {
    	if ( LOG.isDebugEnabled() ) {
    		LOG.debug("Fetching active RouteNodeInstanceVOs [docId=" + documentId + "]");
    	}
        loadDocument(documentId);
        return convertRouteNodeInstances(KEWServiceLocator.getRouteNodeService().getActiveNodeInstances(documentId));
    }

    public RouteNodeInstanceDTO[] getTerminalNodeInstances(Long documentId) throws WorkflowException {
    	if ( LOG.isDebugEnabled() ) {
    		LOG.debug("Fetching terminal RouteNodeInstanceVOs [docId=" + documentId + "]");
    	}
    	loadDocument(documentId);
        return convertRouteNodeInstances(KEWServiceLocator.getRouteNodeService().getTerminalNodeInstances(documentId));
    }

    public RouteNodeInstanceDTO[] getCurrentNodeInstances(Long documentId) throws WorkflowException {
    	if ( LOG.isDebugEnabled() ) {
    		LOG.debug("Fetching current RouteNodeInstanceVOs [docId=" + documentId + "]");
    	}
    	loadDocument(documentId);
    	return convertRouteNodeInstances(KEWServiceLocator.getRouteNodeService().getCurrentNodeInstances(documentId));
    }

    private RouteNodeInstanceDTO[] convertRouteNodeInstances(List nodeInstances) throws WorkflowException {
        RouteNodeInstanceDTO[] nodeInstanceVOs = new RouteNodeInstanceDTO[nodeInstances.size()];
        int i = 0;
        for (Iterator iter = nodeInstances.iterator(); iter.hasNext(); ) {
            nodeInstanceVOs[i++] = DTOConverter.convertRouteNodeInstance((RouteNodeInstance) iter.next());
        }
        return nodeInstanceVOs;
    }

    public boolean isUserInRouteLog(Long routeHeaderId, String principalId, boolean lookFuture) throws WorkflowException {
        if (routeHeaderId == null) {
            LOG.error("null routeHeaderId passed in.");
            throw new RuntimeException("null routeHeaderId passed in.");
        }
        if (principalId == null ){
            LOG.error("null principalId passed in.");
            throw new RiceRuntimeException("null principalId passed in.");
        }
        boolean authorized = false;
        if ( LOG.isDebugEnabled() ) {
        	LOG.debug("Evaluating isUserInRouteLog [docId=" + routeHeaderId + ", principalId=" + principalId + ", lookFuture=" + lookFuture + "]");
        }
        DocumentRouteHeaderValue routeHeader = loadDocument(routeHeaderId);
        KimPrincipal principal = KEWServiceLocator.getIdentityHelperService().getPrincipal(principalId);
        List actionsTaken = KEWServiceLocator.getActionTakenService().findByRouteHeaderIdWorkflowId(routeHeaderId, principal.getPrincipalId());
        
        if(routeHeader.getInitiatorWorkflowId().equals(principal.getPrincipalId())){
        	return true;
        }

        if (actionsTaken.size() > 0) {
        	LOG.debug("found action taken by user");
        	authorized = true;
        }

        List actionRequests = KEWServiceLocator.getActionRequestService().findAllActionRequestsByRouteHeaderId(routeHeaderId);
        if (actionRequestListHasPrincipal(principal, actionRequests)) {
        	authorized = true;
        }

        if (!lookFuture) {
        	return authorized;
        }


        SimulationEngine simulationEngine = new SimulationEngine();
        SimulationCriteria criteria = new SimulationCriteria(routeHeaderId);
        criteria.setDestinationNodeName(null); // process entire document to conclusion
        criteria.getDestinationRecipients().add(new KimPrincipalRecipient(principal));
        
        try {
        	SimulationResults results = simulationEngine.runSimulation(criteria);
        	if (actionRequestListHasPrincipal(principal, results.getSimulatedActionRequests())) {
        		authorized = true;
        	}
        } catch (Exception e) {
        	throw new RiceRuntimeException(e);
        }

        return authorized;
    }

    /**
     * @see org.kuali.rice.kew.service.WorkflowUtility#getPrincipalIdsInRouteLog(java.lang.Long, boolean)
     */
    public List<String> getPrincipalIdsInRouteLog(Long routeHeaderId, boolean lookFuture) throws WorkflowException {
        if (routeHeaderId == null) {
            LOG.error("null routeHeaderId passed in.");
            throw new RuntimeException("null routeHeaderId passed in.");
        }
    	List<String> principalIds = new ArrayList<String>();
        try {
        	if ( LOG.isDebugEnabled() ) {
        		LOG.debug("Evaluating isUserInRouteLog [docId=" + routeHeaderId + ", lookFuture=" + lookFuture + "]");
        	}
            DocumentRouteHeaderValue routeHeader = loadDocument(routeHeaderId);
            List<ActionTakenValue> actionsTakens =
            	(List<ActionTakenValue>)KEWServiceLocator.getActionTakenService().findByRouteHeaderId(routeHeaderId);
            //TODO: confirm that the initiator is not already there in the actionstaken
            principalIds.add(routeHeader.getInitiatorWorkflowId());
            for(ActionTakenValue actionTaken: actionsTakens){
            	principalIds.add(actionTaken.getPrincipalId());
            }
            List<ActionRequestValue> actionRequests =
            	KEWServiceLocator.getActionRequestService().findAllActionRequestsByRouteHeaderId(routeHeaderId);
            for(ActionRequestValue actionRequest: actionRequests){
            	principalIds.add(actionRequest.getPrincipalId());
            }
            if (!lookFuture) {
                return principalIds;
            }
            SimulationEngine simulationEngine = new SimulationEngine();
            SimulationCriteria criteria = new SimulationCriteria(routeHeaderId);
            criteria.setDestinationNodeName(null); // process entire document to conclusion
            SimulationResults results = simulationEngine.runSimulation(criteria);
            actionRequests = (List<ActionRequestValue>)results.getSimulatedActionRequests();
            for(ActionRequestValue actionRequest: actionRequests){
                principalIds.add(actionRequest.getPrincipalId());
            }
        } catch (Exception ex) {
            LOG.warn("Problems getting principalIds in Route Log for routeHeaderId: "+routeHeaderId+". Exception:"+ex.getMessage(),ex);
        }
        return principalIds;
    }

    /***
     * @see org.kuali.rice.kew.service.WorkflowUtility#getPrincipalIdsWithPendingActionRequestByActionRequestedAndDocId(java.lang.String, java.lang.Long)
     */
    public List<String> getPrincipalIdsWithPendingActionRequestByActionRequestedAndDocId(String actionRequestedCd, Long routeHeaderId){
    	return KEWServiceLocator.getActionRequestService().
    				getPrincipalIdsWithPendingActionRequestByActionRequestedAndDocId(actionRequestedCd, routeHeaderId);
    }

    private boolean actionRequestListHasPrincipal(KimPrincipal principal, List actionRequests) throws WorkflowException {
        for (Iterator iter = actionRequests.iterator(); iter.hasNext();) {
            ActionRequestValue actionRequest = (ActionRequestValue) iter.next();
            if (actionRequest.isRecipientRoutedRequest(new KimPrincipalRecipient(principal))) {
                return true;
            }
        }
        return false;
    }

    private boolean isRecipientRoutedRequest(ActionRequestValue actionRequest, List<Recipient> recipients) throws WorkflowException {
        for (Recipient recipient : recipients) {
            if (actionRequest.isRecipientRoutedRequest(recipient)) {
                return true;
            }
        }
        return false;
    }


    /**
     * @deprecated use {@link #documentWillHaveAtLeastOneActionRequest(ReportCriteriaDTO, String[], boolean)} instead
     *
     * @see org.kuali.rice.kew.service.WorkflowUtility#documentWillHaveAtLeastOneActionRequest(org.kuali.rice.kew.dto.ReportCriteriaDTO, java.lang.String[])
     */
    public boolean documentWillHaveAtLeastOneActionRequest(ReportCriteriaDTO reportCriteriaDTO, String[] actionRequestedCodes) {
        return documentWillHaveAtLeastOneActionRequest(reportCriteriaDTO, actionRequestedCodes, false);
    }

    /**
     * @see org.kuali.rice.kew.service.WorkflowUtility#documentWillHaveAtLeastOneActionRequest(org.kuali.rice.kew.dto.ReportCriteriaDTO, java.lang.String[], boolean)
     */
    public boolean documentWillHaveAtLeastOneActionRequest(ReportCriteriaDTO reportCriteriaDTO, String[] actionRequestedCodes, boolean ignoreCurrentActionRequests) {
        try {
	        SimulationEngine simulationEngine = new SimulationEngine();
	        SimulationCriteria criteria = DTOConverter.convertReportCriteriaDTO(reportCriteriaDTO);
	        // set activate requests to true by default so ignore previous works correctly
	        criteria.setActivateRequests(Boolean.TRUE);
	        SimulationResults results = simulationEngine.runSimulation(criteria);
            List actionRequestsToProcess = results.getSimulatedActionRequests();
            if (!ignoreCurrentActionRequests) {
                actionRequestsToProcess.addAll(results.getDocument().getActionRequests());
            }
            for (Iterator iter = actionRequestsToProcess.iterator(); iter.hasNext();) {
				ActionRequestValue actionRequest = (ActionRequestValue) iter.next();
                if (actionRequest.isDone()) {
                    // an action taken has eliminated this request from being active
                    continue;
                }
				// if no action request codes are passed in.... assume any request found is
		    	if ( (actionRequestedCodes == null) || (actionRequestedCodes.length == 0) ) {
		    		// we found an action request
		    		return true;
		    	}
		    	// check the action requested codes passed in
		    	for (int i = 0; i < actionRequestedCodes.length; i++) {
					String requestedActionRequestCode = actionRequestedCodes[i];
					if (requestedActionRequestCode.equals(actionRequest.getActionRequested())) {
					    boolean satisfiesDestinationUserCriteria = (criteria.getDestinationRecipients().isEmpty()) || (isRecipientRoutedRequest(actionRequest,criteria.getDestinationRecipients()));
					    if (satisfiesDestinationUserCriteria) {
					        if (StringUtils.isBlank(criteria.getDestinationNodeName())) {
					            return true;
					        } else if (StringUtils.equals(criteria.getDestinationNodeName(),actionRequest.getNodeInstance().getName())) {
					            return true;
					        }
					    }
					}
				}
			}
	        return false;
        } catch (Exception ex) {
        	String error = "Problems evaluating documentWillHaveAtLeastOneActionRequest: " + ex.getMessage();
            LOG.error(error,ex);
            if (ex instanceof RuntimeException) {
            	throw (RuntimeException)ex;
            }
            throw new RuntimeException(error, ex);
        }
    }

    public boolean isLastApproverInRouteLevel(Long routeHeaderId, String principalId, Integer routeLevel) throws WorkflowException {
        if (routeLevel == null) {
            LOG.error("null routeLevel passed in.");
            throw new RuntimeException("null routeLevel passed in.");
        }
        if ( LOG.isDebugEnabled() ) {
        	LOG.debug("Evaluating isLastApproverInRouteLevel [docId=" + routeHeaderId + ", principalId=" + principalId + ", routeLevel=" + routeLevel + "]");
        }
        DocumentRouteHeaderValue document = loadDocument(routeHeaderId);
        RouteNode node = CompatUtils.getNodeForLevel(document.getDocumentType(), routeLevel);
        if (node == null) {
            throw new RuntimeException("Cannot resolve given route level to an approriate node name: " + routeLevel);
        }
        return isLastApproverAtNode(routeHeaderId, principalId, node.getRouteNodeName());
    }

    public boolean isLastApproverAtNode(Long routeHeaderId, String principalId, String nodeName) throws WorkflowException {
        if (routeHeaderId == null) {
            LOG.error("null routeHeaderId passed in.");
            throw new RuntimeException("null routeHeaderId passed in.");
        }
        if (principalId == null ){
            LOG.error("null principalId passed in.");
            throw new RuntimeException("null principalId passed in.");
        }
        if ( LOG.isDebugEnabled() ) {
        	LOG.debug("Evaluating isLastApproverAtNode [docId=" + routeHeaderId + ", principalId=" + principalId + ", nodeName=" + nodeName + "]");
        }
        loadDocument(routeHeaderId);
        // If this app constant is set to true, then we will attempt to simulate activation of non-active requests before
        // attempting to deactivate them, this is in order to address the ignore previous issue reported by EPIC in issue
        // http://fms.dfa.cornell.edu:8080/browse/KULWF-366
        boolean activateFirst = Utilities.getKNSParameterBooleanValue(KEWConstants.KEW_NAMESPACE, KNSConstants.DetailTypes.FEATURE_DETAIL_TYPE, KEWConstants.IS_LAST_APPROVER_ACTIVATE_FIRST_IND);

        List requests = KEWServiceLocator.getActionRequestService().findPendingByDocRequestCdNodeName(routeHeaderId, KEWConstants.ACTION_REQUEST_APPROVE_REQ, nodeName);
        if (requests == null || requests.isEmpty()) {
            return false;
        }
        ActivationContext activationContext = new ActivationContext(ActivationContext.CONTEXT_IS_SIMULATION);
        for (Iterator iterator = requests.iterator(); iterator.hasNext();) {
            ActionRequestValue request = (ActionRequestValue) iterator.next();
            if (activateFirst && !request.isActive()) {
                KEWServiceLocator.getActionRequestService().activateRequest(request, activationContext);
            }
            if (request.isUserRequest() && request.getPrincipalId().equals(principalId)) {
                KEWServiceLocator.getActionRequestService().deactivateRequest(null, request, activationContext);
            } else if (request.isGroupRequest() && KIMServiceLocator.getIdentityManagementService().isMemberOfGroup(principalId, request.getGroup().getGroupId())) {
                KEWServiceLocator.getActionRequestService().deactivateRequest(null, request, activationContext);
            }
        }
        boolean allDeactivated = true;
        for (Iterator iter = requests.iterator(); iter.hasNext();) {
            ActionRequestValue actionRequest = (ActionRequestValue) iter.next();
            allDeactivated = allDeactivated && actionRequest.isDeactivated();
        }
        return allDeactivated;
    }

    /**
     * Used to determine if a given route level will produce Approve Action Requests.
     *
     * @deprecated use routeNodeHasApproverActionRequest instead
     */
    public boolean routeLevelHasApproverActionRequest(String documentTypeName, String docContent, Integer routeLevel) throws WorkflowException {
        if (documentTypeName == null) {
            LOG.error("null document type name passed in.");
            throw new RuntimeException("null document type passed in.");
        }
        if (routeLevel == null) {
            LOG.error("null routeLevel passed in.");
            throw new RuntimeException("null routeLevel passed in.");
        }
        if ( LOG.isDebugEnabled() ) {
        	LOG.debug("Evaluating routeLevelHasApproverActionRequest [docTypeName=" + documentTypeName + ", routeLevel=" + routeLevel + "]");
        }
        DocumentType documentType = KEWServiceLocator.getDocumentTypeService().findByName(documentTypeName);
        if (!CompatUtils.isRouteLevelCompatible(documentType)) {
            throw new WorkflowException("The given document type is not route level compatible: " + documentTypeName);
        }
        RouteNode routeNode = CompatUtils.getNodeForLevel(documentType, routeLevel);
        return routeNodeHasApproverActionRequest(documentType, docContent, routeNode, routeLevel);
    }

    public boolean routeNodeHasApproverActionRequest(String documentTypeName, String docContent, String nodeName) throws WorkflowException {
        if (documentTypeName == null) {
            LOG.error("null docType passed in.");
            throw new RuntimeException("null docType passed in.");
        }
        if (nodeName == null) {
            LOG.error("null nodeName passed in.");
            throw new RuntimeException("null nodeName passed in.");
        }
        if ( LOG.isDebugEnabled() ) {
        	LOG.debug("Evaluating routeNodeHasApproverActionRequest [docTypeName=" + documentTypeName + ", nodeName=" + nodeName + "]");
        }
        DocumentType documentType = KEWServiceLocator.getDocumentTypeService().findByName(documentTypeName);
        RouteNode routeNode = KEWServiceLocator.getRouteNodeService().findRouteNodeByName(documentType.getDocumentTypeId(), nodeName);
        return routeNodeHasApproverActionRequest(documentType, docContent, routeNode, new Integer(KEWConstants.INVALID_ROUTE_LEVEL));
    }

    /**
     * Really this method needs to be implemented using the routingReport functionality (the SimulationEngine).
     * This would get rid of the needs for us to call to FlexRM directly.
     */
    private boolean routeNodeHasApproverActionRequest(DocumentType documentType, String docContent, RouteNode node, Integer routeLevel) throws WorkflowException {
        if (documentType == null) {
            LOG.error("could not locate document type.");
            throw new RuntimeException("could not locate document type.");
        }
        if (docContent == null) {
            LOG.error("null docContent passed in.");
            throw new RuntimeException("null docContent passed in.");
        }
        if (node == null) {
            LOG.error("could not locate route node.");
            throw new RuntimeException("could not locate route node.");
        }

        DocumentRouteHeaderValue routeHeader = new DocumentRouteHeaderValue();
        routeHeader.setRouteHeaderId(new Long(0));
        routeHeader.setDocumentTypeId(documentType.getDocumentTypeId());
        routeHeader.setDocRouteLevel(routeLevel);
        routeHeader.setDocVersion(new Integer(KEWConstants.CURRENT_DOCUMENT_VERSION));

        if (node.getRuleTemplate() != null && node.isFlexRM()) {
            String ruleTemplateName = node.getRuleTemplate().getName();
            routeHeader.setDocContent(docContent);
            routeHeader.setDocRouteStatus(KEWConstants.ROUTE_HEADER_INITIATED_CD);
            FlexRM flexRM = new FlexRM();
    		RouteContext context = RouteContext.getCurrentRouteContext();
    		context.setDocument(routeHeader);
    		try {
    			List actionRequests = flexRM.getActionRequests(routeHeader, node, null, ruleTemplateName);
    			for (Iterator iter = actionRequests.iterator(); iter.hasNext();) {
    				ActionRequestValue actionRequest = (ActionRequestValue) iter.next();
    				if (actionRequest.isApproveOrCompleteRequest()) {
    					return true;
    				}
    			}
    		} finally {
    			RouteContext.clearCurrentRouteContext();
    		}
        }
        return false;
    }

    private void incomingParamCheck(Object object, String name) {
        if (object == null) {
            LOG.error("null " + name + " passed in.");
            throw new RuntimeException("null " + name + " passed in.");
        }
    }

    public void reResolveRole(String documentTypeName, String roleName, String qualifiedRoleNameLabel) throws WorkflowException {
        incomingParamCheck(documentTypeName, "documentTypeName");
        incomingParamCheck(roleName, "roleName");
        incomingParamCheck(qualifiedRoleNameLabel, "qualifiedRoleNameLabel");
        if ( LOG.isDebugEnabled() ) {
        	LOG.debug("Re-resolving Role [docTypeName=" + documentTypeName + ", roleName=" + roleName + ", qualifiedRoleNameLabel=" + qualifiedRoleNameLabel + "]");
        }
    	DocumentType documentType = KEWServiceLocator.getDocumentTypeService().findByName(documentTypeName);
    	if (Utilities.isEmpty(qualifiedRoleNameLabel)) {
    		KEWServiceLocator.getRoleService().reResolveRole(documentType, roleName);
    	} else {
    		KEWServiceLocator.getRoleService().reResolveQualifiedRole(documentType, roleName, qualifiedRoleNameLabel);
    	}
    }

    public void reResolveRoleByDocumentId(Long documentId, String roleName, String qualifiedRoleNameLabel) throws WorkflowException {
        incomingParamCheck(documentId, "documentId");
        incomingParamCheck(roleName, "roleName");
        incomingParamCheck(qualifiedRoleNameLabel, "qualifiedRoleNameLabel");
        if ( LOG.isDebugEnabled() ) {
        	LOG.debug("Re-resolving Role [documentId=" + documentId + ", roleName=" + roleName + ", qualifiedRoleNameLabel=" + qualifiedRoleNameLabel + "]");
        }
        DocumentRouteHeaderValue routeHeader = loadDocument(documentId);
    	if (Utilities.isEmpty(qualifiedRoleNameLabel)) {
    		KEWServiceLocator.getRoleService().reResolveRole(routeHeader, roleName);
    	} else {
    		KEWServiceLocator.getRoleService().reResolveQualifiedRole(routeHeader, roleName, qualifiedRoleNameLabel);
    	}
    }

    public DocumentDetailDTO routingReport(ReportCriteriaDTO reportCriteria) throws WorkflowException {
        incomingParamCheck(reportCriteria, "reportCriteria");
        if ( LOG.isDebugEnabled() ) {
        	LOG.debug("Executing routing report [docId=" + reportCriteria.getRouteHeaderId() + ", docTypeName=" + reportCriteria.getDocumentTypeName() + "]");
        }
        SimulationCriteria criteria = DTOConverter.convertReportCriteriaDTO(reportCriteria);
        return DTOConverter.convertDocumentDetail(KEWServiceLocator.getRoutingReportService().report(criteria));
    }

    public boolean isFinalApprover(Long routeHeaderId, String principalId) throws WorkflowException {
        incomingParamCheck(routeHeaderId, "routeHeaderId");
        incomingParamCheck(principalId, "principalId");
        if ( LOG.isDebugEnabled() ) {
        	LOG.debug("Evaluating isFinalApprover [docId=" + routeHeaderId + ", principalId=" + principalId + "]");
        }
        DocumentRouteHeaderValue routeHeader = loadDocument(routeHeaderId);
        List requests = KEWServiceLocator.getActionRequestService().findPendingByDoc(routeHeaderId);
        List finalApproverNodes = KEWServiceLocator.getRouteNodeService().findFinalApprovalRouteNodes(routeHeader.getDocumentType().getDocumentTypeId());
        if (finalApproverNodes.isEmpty()) {
        	if ( LOG.isDebugEnabled() ) {
        		LOG.debug("Could not locate final approval nodes for document " + routeHeaderId);
        	}
            return false;
        }
        Set finalApproverNodeNames = new HashSet();
        for (Iterator iterator = finalApproverNodes.iterator(); iterator.hasNext();) {
            RouteNode node = (RouteNode) iterator.next();
            finalApproverNodeNames.add(node.getRouteNodeName());
        }

        int approveRequest = 0;
        for (Iterator iter = requests.iterator(); iter.hasNext();) {
            ActionRequestValue request = (ActionRequestValue) iter.next();
            RouteNodeInstance nodeInstance = request.getNodeInstance();
            if (nodeInstance == null) {
            	if ( LOG.isDebugEnabled() ) {
            		LOG.debug("Found an action request on the document with a null node instance, indicating EXCEPTION routing.");
            	}
                return false;
            }
            if (finalApproverNodeNames.contains(nodeInstance.getRouteNode().getRouteNodeName())) {
                if (request.isApproveOrCompleteRequest()) {
                    approveRequest++;
                    if ( LOG.isDebugEnabled() ) {
                    	LOG.debug("Found request is approver " + request.getActionRequestId());
                    }
                    if (! request.isRecipientRoutedRequest(principalId)) {
                    	if ( LOG.isDebugEnabled() ) {
                    		LOG.debug("Action Request not for user " + principalId);
                    	}
                        return false;
                    }
                }
            }
        }

        if (approveRequest == 0) {
            return false;
        }
        if ( LOG.isDebugEnabled() ) {
        	LOG.debug("Principal "+principalId+" is final approver for document " + routeHeaderId);
        }
        return true;
    }

    public boolean isSuperUserForDocumentType(String principalId, Long documentTypeId) throws WorkflowException {
    	if ( LOG.isDebugEnabled() ) {
    		LOG.debug("Determining super user status [principalId=" + principalId + ", documentTypeId=" + documentTypeId + "]");
    	}
    	DocumentType documentType = KEWServiceLocator.getDocumentTypeService().findById(documentTypeId);
    	boolean isSuperUser = KEWServiceLocator.getDocumentTypePermissionService().canAdministerRouting(principalId, documentType);
    	if ( LOG.isDebugEnabled() ) {
    		LOG.debug("Super user status is " + isSuperUser + ".");
    	}
    	return isSuperUser;
    }

    private DocumentRouteHeaderValue loadDocument(Long documentId) {
        return KEWServiceLocator.getRouteHeaderService().getRouteHeader(documentId);
    }

    public DocumentContentDTO getDocumentContent(Long routeHeaderId) throws WorkflowException {
    	if ( LOG.isDebugEnabled() ) {
    		LOG.debug("Fetching document content [docId=" + routeHeaderId + "]");
    	}
    	DocumentRouteHeaderValue document = KEWServiceLocator.getRouteHeaderService().getRouteHeader(routeHeaderId);
    	return DTOConverter.convertDocumentContent(document.getDocContent(), routeHeaderId);
    }

	public String[] getPreviousRouteNodeNames(Long documentId) throws WorkflowException {
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("Fetching previous node names [docId=" + documentId + "]");
		}
		DocumentRouteHeaderValue document = KEWServiceLocator.getRouteHeaderService().getRouteHeader(documentId);
		//going conservative for now.  if the doc isn't enroute or exception nothing will be returned.
		if (document.isEnroute() || document.isInException()) {

			List activeNodeInstances = KEWServiceLocator.getRouteNodeService().getActiveNodeInstances(document);
			long largetActivatedNodeId = 0;
			for (Iterator iter = activeNodeInstances.iterator(); iter.hasNext();) {
				RouteNodeInstance routeNodeInstance = (RouteNodeInstance) iter.next();
				if (routeNodeInstance.getRouteNode().getRouteNodeId().longValue() > largetActivatedNodeId) {
					largetActivatedNodeId = routeNodeInstance.getRouteNode().getRouteNodeId().longValue();
				}
			}

			List routeNodes = KEWServiceLocator.getRouteNodeService().getFlattenedNodeInstances(document, false);
			List nodeNames = new ArrayList();

			for (Iterator iter = routeNodes.iterator(); iter.hasNext();) {
				RouteNodeInstance routeNode = (RouteNodeInstance) iter.next();
				if (routeNode.isComplete() && !nodeNames.contains(routeNode.getName())) {
					//if the prototype of the nodeInstance we're analyzing is less than the largest id of all our active prototypes
					//then add it to the list.  This is an attempt to account for return to previous hitting a single node multiple times
					if (routeNode.getRouteNode().getRouteNodeId().longValue() < largetActivatedNodeId) {
						nodeNames.add(routeNode.getName());
					}
				}
			}
			return (String[]) nodeNames.toArray(new String[nodeNames.size()]);
		} else {
			return new String[0];
		}
	}

    public RuleDTO[] ruleReport(RuleReportCriteriaDTO ruleReportCriteria) throws WorkflowException {
        incomingParamCheck(ruleReportCriteria, "ruleReportCriteria");
        if (ruleReportCriteria == null) {
            throw new IllegalArgumentException("At least one criterion must be sent in a RuleReportCriteriaDTO object");
        }
        if ( LOG.isDebugEnabled() ) {
        	LOG.debug("Executing rule report [responsibleUser=" + ruleReportCriteria.getResponsiblePrincipalId() + ", responsibleWorkgroup=" +
                    ruleReportCriteria.getResponsibleGroupId() + "]");
        }
        Map extensionValues = new HashMap();
        if (ruleReportCriteria.getRuleExtensionVOs() != null) {
            for (int i = 0; i < ruleReportCriteria.getRuleExtensionVOs().length; i++) {
                RuleExtensionDTO ruleExtensionVO = ruleReportCriteria.getRuleExtensionVOs()[i];
                KeyValuePair ruleExtension = DTOConverter.convertRuleExtensionVO(ruleExtensionVO);
                extensionValues.put(ruleExtension.getKey(), ruleExtension.getValue());
            }
        }
        Collection<String> actionRequestCodes = null;
        if ( (ruleReportCriteria.getActionRequestCodes() != null) && (ruleReportCriteria.getActionRequestCodes().length != 0) ) {
            actionRequestCodes = Arrays.asList(ruleReportCriteria.getActionRequestCodes());
        }
        Collection rulesFound = KEWServiceLocator.getRuleService().search(ruleReportCriteria.getDocumentTypeName(),ruleReportCriteria.getRuleTemplateName(),
                ruleReportCriteria.getRuleDescription(), ruleReportCriteria.getResponsibleGroupId(),
                ruleReportCriteria.getResponsiblePrincipalId(),ruleReportCriteria.getResponsibleRoleName(),
                ruleReportCriteria.isConsiderWorkgroupMembership(),ruleReportCriteria.isIncludeDelegations(),
                ruleReportCriteria.isActiveIndicator(),extensionValues,actionRequestCodes);
        RuleDTO[] returnableRules = new RuleDTO[rulesFound.size()];
        int i = 0;
        for (Iterator iter = rulesFound.iterator(); iter.hasNext();) {
            RuleBaseValues rule = (RuleBaseValues) iter.next();
            returnableRules[i] = DTOConverter.convertRule(rule);
            i++;
        }
        return returnableRules;
    }

    public DocumentSearchResultDTO performDocumentSearch(DocumentSearchCriteriaDTO criteriaVO) throws WorkflowException {
        return performDocumentSearch(null, criteriaVO);
    }

    public DocumentSearchResultDTO performDocumentSearch(String principalId, DocumentSearchCriteriaDTO criteriaVO) throws WorkflowException {
        DocSearchCriteriaDTO criteria = DTOConverter.convertDocumentSearchCriteriaDTO(criteriaVO);
        criteria.setOverridingUserSession(true);
        KEWServiceLocator.getIdentityHelperService().validatePrincipalId(principalId);
        DocumentSearchResultComponents components = KEWServiceLocator.getDocumentSearchService().getListRestrictedByCriteria(principalId, criteria);
        DocumentSearchResultDTO resultVO = DTOConverter.convertDocumentSearchResultComponents(components);
        resultVO.setOverThreshold(criteria.isOverThreshold());
        resultVO.setSecurityFilteredRows(Integer.valueOf(criteria.getSecurityFilteredRows()));
        return resultVO;
    }
    
    /**
     * @see org.kuali.rice.kew.service.WorkflowUtility#getDocumentInitiatorPrincipalId(java.lang.Long)
     */
    public String getDocumentInitiatorPrincipalId(Long routeHeaderId)
    		throws WorkflowException {
        if (routeHeaderId == null) {
            LOG.error("null routeHeaderId passed in.");
            throw new RuntimeException("null routeHeaderId passed in.");
        }
    	
        DocumentRouteHeaderValue header = KEWServiceLocator.getRouteHeaderService().getRouteHeader(routeHeaderId, false);
        if ( header == null) {
        	return null;
        }
    	return header.getInitiatorWorkflowId();
    }
    /**
     * @see org.kuali.rice.kew.service.WorkflowUtility#getDocumentRoutedByPrincipalId(java.lang.Long)
     */
    public String getDocumentRoutedByPrincipalId(Long routeHeaderId)
    		throws WorkflowException {
        if (routeHeaderId == null) {
            LOG.error("null routeHeaderId passed in.");
            throw new RuntimeException("null routeHeaderId passed in.");
        }
    	
        DocumentRouteHeaderValue header = KEWServiceLocator.getRouteHeaderService().getRouteHeader(routeHeaderId, false);
        if ( header == null) {
        	return null;
        }
    	return header.getRoutedByUserWorkflowId();
    }
}