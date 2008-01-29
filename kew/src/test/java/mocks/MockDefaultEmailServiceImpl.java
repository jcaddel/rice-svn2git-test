/*
 * Copyright 2007 The Kuali Foundation
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
package mocks;

import org.apache.log4j.Logger;

import edu.iu.uis.eden.mail.DefaultEmailService;
import edu.iu.uis.eden.mail.EmailBody;
import edu.iu.uis.eden.mail.EmailFrom;
import edu.iu.uis.eden.mail.EmailSubject;
import edu.iu.uis.eden.mail.EmailTo;

/**
 * This class is used to disallow email sending for KEW tests 
 * 
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 *
 */
public class MockDefaultEmailServiceImpl extends DefaultEmailService {
    private static final Logger LOG = Logger.getLogger(MockDefaultEmailServiceImpl.class);

    /**
     * MOCK METHOD USED TO OVERRIDE EMAIL DELIVERY
     */
    @Override
    public void sendEmail(EmailFrom from, EmailTo to, EmailSubject subject, EmailBody body, boolean htmlMessage) {
        // do nothing
    }
}
