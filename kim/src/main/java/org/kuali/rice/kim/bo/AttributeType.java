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
package org.kuali.rice.kim.bo;

import javax.persistence.Version;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.CascadeType;
import javax.persistence.Table;
import javax.persistence.Entity;
import java.util.LinkedHashMap;

import org.kuali.core.bo.PersistableBusinessObjectBase;
import org.kuali.rice.kim.dto.AttributeTypeDTO;

/**
 * This class defines the concept of an attribute type.  Given KIM's need for dynamic lists of attributes attached
 * to various entities in the system, this class organizes or categorizes the type of attribute.  For example,
 * one instance of attribute type could be "text" or "radio button".  Every attribute attached to a person, group, etc
 * in KIM will need to have an attribute type associated with it.
 *
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 */
@Entity
@Table(name="KIM_ATTRIBUTE_TYPES_T")
public class AttributeType extends PersistableBusinessObjectBase {

	private static final long serialVersionUID = -3856630570406063764L;
	@Id
	@Column(name="ID")
	private Long id;
	@Column(name="NAME")
	private String attributeTypeName;
	@Column(name="DESCRIPTION")
	private String description;

	/**
	 * This method retrieves the attribute type name.
	 *
	 * @return String
	 */
	public String getAttributeTypeName() {
		return attributeTypeName;
	}

	/**
	 * This method sets the attribute type name.
	 *
	 * @param attributeTypeName
	 */
	public void setAttributeTypeName(String attributeTypeName) {
		this.attributeTypeName = attributeTypeName;
	}

	/**
	 * This method retrieves the unique id (primary key) for an attribute type instance.
	 *
	 * @return Long
	 */
	public Long getId() {
		return id;
	}

	/**
	 * This method sets the unique id (primary key) for an attribute type instance.
	 *
	 * @param id
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
	    return this.description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
	    this.description = description;
	}

	/**
	 * @see org.kuali.core.bo.BusinessObjectBase#toStringMapper()
	 */
    	protected LinkedHashMap<String, Object> toStringMapper() {
            LinkedHashMap<String, Object> propMap = new LinkedHashMap<String, Object>();
            propMap.put("id", getId());
            propMap.put("attributeTypeName", getAttributeTypeName());
            return propMap;
    	}

    	/**
    	 * @see org.kuali.core.bo.BusinessObject#refresh()
    	 */
	public void refresh() {
		// not doing this unless needed
	}

	/**
	 *
	 * This method creates a DTO from a BO
	 *
	 * @param AttributeType
	 * @return AttributeTypeDTO
	 */
	public static AttributeTypeDTO toDTO(final AttributeType at) {
	    final AttributeTypeDTO dto = new AttributeTypeDTO();
	    dto.setAttributeTypeName(at.getAttributeTypeName());
	    dto.setDescription(at.getDescription());
	    dto.setId(at.getId());

	    return dto;
	}
}
