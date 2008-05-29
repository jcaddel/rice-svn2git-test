/*
 * Copyright 2005-2008 The Kuali Foundation.
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
package edu.iu.uis.eden.workgroup;

import java.io.Serializable;

import javax.persistence.Column;

/**
 * This Compound Primary Class has been generated by the rice ojb2jpa Groovy script.  Please
 * note that there are no setter methods, only getters.  This is done purposefully as cpk classes
 * can not change after they have been created.  Also note they require a public no-arg constructor.
 * TODO: Implement the equals() and hashCode() methods. 
 */
public class BaseWorkgroupId implements Serializable {

    @Column(name="WRKGRP_ID")
    protected Long workgroupId;
    @Column(name="WRKGRP_VER_NBR")
    protected Integer versionNumber;

    public BaseWorkgroupId() {}

    public Long getWorkgroupId() { return workgroupId; }
    public Integer getVersionNumber() { return versionNumber; }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof BaseWorkgroupId)) return false;
        if (o == null) return false;
        BaseWorkgroupId pk = (BaseWorkgroupId) o;
        // TODO: Finish implementing this method.  Compare o to pk and return true or false.
        throw new UnsupportedOperationException("Please implement me!");
    }

    public int hashCode() {
        // TODO: Implement this method
        throw new UnsupportedOperationException("Please implement me!");
    }

}

