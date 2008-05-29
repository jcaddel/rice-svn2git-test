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
package org.kuali.core.dao.jpa;

import java.util.ArrayList;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;
import org.kuali.core.bo.Attachment;
import org.kuali.core.bo.Note;
import org.kuali.core.dao.NoteDao;
import org.kuali.rice.jpa.criteria.Criteria;
import org.kuali.rice.jpa.criteria.QueryByCriteria;
import org.springframework.dao.DataAccessException;

/**
 * This class is the JPA implementation of the NoteDao interface.
 * 
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 */
public class NoteDaoJpa implements NoteDao {

	private static Logger LOG = Logger.getLogger(NoteDaoJpa.class);

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Saves a note to the DB using JPA.
	 */
	public void save(Note note) throws DataAccessException {
		if (note != null && note.getNoteIdentifier() == null && note.getAttachment() != null) {
			Attachment attachment = note.getAttachment();
			note.setAttachment(null);
			// store without attachment
			entityManager.merge(note);
			attachment.setNoteIdentifier(note.getNoteIdentifier());
			// put attachment back
			note.setAttachment(attachment);
		}
		entityManager.merge(note);
		
	}

	/**
	 * Deletes a note from the DB using JPA.
	 */
	public void deleteNote(Note note) throws DataAccessException {
		entityManager.remove(note.getAttachment());
		note.setAttachment(null);
		entityManager.remove(note);
	}

	/**
	 * Retrieves document associated with a given object using JPA.
	 */
	public ArrayList findByremoteObjectId(String remoteObjectId) {
		Criteria criteria = new Criteria(Note.class.getName());
		// TODO: Notes - Chris move remoteObjectId string to constants
		criteria.eq("remoteObjectIdentifier", remoteObjectId);
		criteria.orderBy("notePostedTimestamp", true);
		return new ArrayList(new QueryByCriteria(entityManager, criteria).toQuery().getResultList());
	}

}