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
package org.kuali.core.bo;

import javax.persistence.FetchType;
import javax.persistence.Basic;
import javax.persistence.Lob;
import javax.persistence.Version;
import javax.persistence.TemporalType;
import javax.persistence.Temporal;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.CascadeType;
import javax.persistence.Table;
import javax.persistence.Entity;

@Entity
@Table(name="FS_LOOKUP_RESULTS_MT")
public class LookupResults extends MultipleValueLookupMetadata {
    @Lob
	@Basic(fetch=FetchType.LAZY)
	@Column(name="SERIALIZED_LOOKUP_RESULTS")
	private String serializedLookupResults;

    public String getSerializedLookupResults() {
        return serializedLookupResults;
    }

    public void setSerializedLookupResults(String serializedLookupResults) {
        this.serializedLookupResults = serializedLookupResults;
    }
}

