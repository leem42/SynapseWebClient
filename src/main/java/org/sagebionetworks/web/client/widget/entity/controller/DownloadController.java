package org.sagebionetworks.web.client.widget.entity.controller;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;

/**
 * Can the user download from this entity.
 * 
 * @author jhill
 *
 */
public interface DownloadController {

	/**
	 * Can the user download from the entity.
	 * @param uploadTo
	 * @param callback
	 */
	public void checkDownloadFromEntity(EntityBundle uploadTo, Callback callback);
}
