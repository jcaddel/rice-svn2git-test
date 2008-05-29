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
package org.kuali.core.bo;

import java.io.Serializable;

import javax.persistence.Column;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * This Compound Primary Class has been generated by the rice ojb2jpa Groovy script.  Please
 * note that there are no setter methods, only getters.  This is done purposefully as cpk classes
 * can not change after they have been created.  Also note they require a public no-arg constructor.
 * TODO: Implement the equals() and hashCode() methods. 
 */
public class ParameterId implements Serializable {

    private static final long serialVersionUID = -8210189691273413060L;
    
	@Column(name="SH_PARM_NMSPC_CD")
    private String parameterNamespaceCode;
    @Column(name="SH_PARM_DTL_TYP_CD")
    private String parameterDetailTypeCode;
    @Column(name="SH_PARM_NM")
    private String parameterName;

    public ParameterId() {}

    public String getParameterNamespaceCode() { return parameterNamespaceCode; }

    public String getParameterDetailTypeCode() { return parameterDetailTypeCode; }

    public String getParameterName() { return parameterName; }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ParameterId)) return false;
        if (o == null) return false;
        ParameterId pk = (ParameterId) o;
        return getParameterNamespaceCode() != null && getParameterDetailTypeCode() != null && getParameterNamespaceCode().equals(pk.getParameterNamespaceCode()) && getParameterDetailTypeCode().equals(pk.getParameterDetailTypeCode());        
    }

    public int hashCode() {
    	return new HashCodeBuilder().append(parameterDetailTypeCode).append(parameterNamespaceCode).toHashCode();
	}
}

