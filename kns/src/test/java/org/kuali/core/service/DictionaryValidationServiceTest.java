/*
 * Copyright 2007 The Kuali Foundation.
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
package org.kuali.core.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.core.UserSession;
import org.kuali.core.util.ErrorMap;
import org.kuali.core.util.GlobalVariables;
import org.kuali.rice.KNSServiceLocator;
import org.kuali.rice.TestBase;
import org.kuali.rice.kns.test.document.AccountRequestDocument;

/**
 * This class tests the DictionaryValidationService (currently only recursive validation is tested).
 *
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 *
 */
public class DictionaryValidationServiceTest extends TestBase {

    public DictionaryValidationServiceTest() {
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        GlobalVariables.setErrorMap(new ErrorMap());
        GlobalVariables.setUserSession(new UserSession("quickstart"));
    }

    @After
    public void tearDown() throws Exception {
        GlobalVariables.setErrorMap(new ErrorMap());
        GlobalVariables.setUserSession(null);
        super.tearDown();
    }

    /**
     * This method tests recursive validation at a depth of zero
     *
     * @throws Exception
     */
    @Test public void testRecursiveValidation() throws Exception {
        AccountRequestDocument travelDocument = (AccountRequestDocument) KNSServiceLocator.getDocumentService().getNewDocument("AccountRequest");
        // set all required fields except 1
        travelDocument.getDocumentHeader().setFinancialDocumentDescription("test document");
        travelDocument.setReason1("reason1");
        travelDocument.setReason2("reason2");
        travelDocument.setRequester("requester");

        GlobalVariables.setErrorMap(new ErrorMap());
        KNSServiceLocator.getDictionaryValidationService().validateDocumentAndUpdatableReferencesRecursively(travelDocument, 0, true);
        ErrorMap errorMap = GlobalVariables.getErrorMap();
        int recursiveZeroErrorMapSize = errorMap.size();

        assertEquals("We should only get four errors here", 4, recursiveZeroErrorMapSize);
    }

    /**
     * This method tests recursive validation comparing multiple levels of recursion
     *
     * @throws Exception
     */
    @Test public void testRecursiveValidationMultiple() throws Exception {
        AccountRequestDocument travelDocument = (AccountRequestDocument) KNSServiceLocator.getDocumentService().getNewDocument("AccountRequest");
        // set all required fields except 1
        travelDocument.getDocumentHeader().setFinancialDocumentDescription("test document");
        travelDocument.setReason1("reason1");
        travelDocument.setReason2("reason2");
        travelDocument.setRequester("requester");

        GlobalVariables.setErrorMap(new ErrorMap());
        KNSServiceLocator.getDictionaryValidationService().validateDocumentAndUpdatableReferencesRecursively(travelDocument, 0, true);
        ErrorMap errorMap = GlobalVariables.getErrorMap();
        int recursiveZeroErrorMapSize = errorMap.size();

        GlobalVariables.setErrorMap(new ErrorMap());
        KNSServiceLocator.getDictionaryValidationService().validateDocumentAndUpdatableReferencesRecursively(travelDocument, 5, true);
        ErrorMap errorMap2 = GlobalVariables.getErrorMap();
        int recursiveFiveErrorMapSize = errorMap2.size();

        assertEquals("We should get the same number of errors no matter how deeply we recursively validate for this document", recursiveZeroErrorMapSize, recursiveFiveErrorMapSize);
    }

}
