package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.attachment.AttachmentData;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.events.AttachmentSelectedEvent;
import org.sagebionetworks.web.client.events.AttachmentSelectedHandler;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedEvent;
import org.sagebionetworks.web.client.events.WidgetDescriptorUpdatedHandler;
import org.sagebionetworks.web.client.presenter.BaseEditWidgetDescriptorPresenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetRegistrar;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class Attachments implements AttachmentsView.Presenter,
		SynapseWidgetPresenter {

	private AttachmentsView view;
	private SynapseClientAsync synapseClient;
	private GlobalApplicationState globalApplicationState;
	private NodeModelCreator nodeModelCreator;
	private AuthenticationController authenticationController;
	private JSONObjectAdapter jsonObjectAdapter;
	private EventBus bus;
	private HandlerManager handlerManager;
	private WidgetRegistrar widgetRegistrar;
	private Entity entity;
	private boolean isEmpty = true;
	private BaseEditWidgetDescriptorPresenter widgetEditor;
	private String baseUrl;
	private boolean widgetAttachments;
	private List<AttachmentData> workingSet;
	
	@Inject
	public Attachments(AttachmentsView view, SynapseClientAsync synapseClient,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController,
			NodeModelCreator nodeModelCreator,
			JSONObjectAdapter jsonObjectAdapter,
			EventBus bus, WidgetRegistrar widgetRegistrar,
			BaseEditWidgetDescriptorPresenter widgetEditor) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.nodeModelCreator = nodeModelCreator;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.bus = bus;
		this.widgetRegistrar = widgetRegistrar;
		this.widgetEditor = widgetEditor;
		view.setPresenter(this);
	}
	
	@Override
	public void configure(String baseUrl, Entity entity, boolean widgetAttachments) {
		this.baseUrl = baseUrl;
		this.entity = entity;
		this.widgetAttachments = widgetAttachments;
		isEmpty = (entity.getAttachments() == null || entity.getAttachments().size() == 0) ? true : false;
		
		workingSet = new ArrayList<AttachmentData>();
		//show json entity attachments, or everything else
		List<AttachmentData> allAttachments = entity.getAttachments();
		if (allAttachments != null) {
			for (Iterator iterator = allAttachments.iterator(); iterator.hasNext();) {
				AttachmentData attachmentData = (AttachmentData) iterator.next();
				//determine if this is a widget attachment
				String contentType = attachmentData.getContentType();
				boolean isWidget = widgetRegistrar.isWidgetContentType(contentType);
				if (isWidget) {
					if (widgetAttachments)
						workingSet.add(attachmentData);
				} else if (!widgetAttachments)
					workingSet.add(attachmentData);
			}
		}
		view.configure(baseUrl, entity.getId(), workingSet, widgetAttachments);
	}

	public boolean isEmpty() {
		return isEmpty;
	}
	
	@Override
	public void clearHandlers() {
		handlerManager = new HandlerManager(this);
	}

	@Override
	public void editAttachment(String tokenId) {
		if(tokenId != null) {
			// find attachment via token
			AttachmentData found = null;
			for(AttachmentData data : workingSet) {
				if(tokenId.equals(data.getTokenId())) {
					found = data;
				}
			}

			if(found != null) {
				String attachmentName = found.getName();
				final AttachmentData editedAttachment = found;
				BaseEditWidgetDescriptorPresenter.editExistingWidget(widgetEditor, entity.getId(), attachmentName, entity.getAttachments(), new WidgetDescriptorUpdatedHandler() {
					@Override
					public void onUpdate(WidgetDescriptorUpdatedEvent event) {
						//update the view so that it has the right name for the attachment
						editedAttachment.setName(event.getName());
						try {
							Entity updatedEntity = nodeModelCreator.createEntity(event.getEntityWrapper());
							configure(baseUrl, updatedEntity, widgetAttachments);
						} catch (JSONObjectAdapterException e) {
							view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
						}
						handlerManager.fireEvent(event);
					}
				});
			}
		}
	}
	
	@Override
	public void addAttachmentSelectedHandler(AttachmentSelectedHandler handler) {
		handlerManager.addHandler(AttachmentSelectedEvent.getType(), handler);
	}
	
	@Override
	public void addAttachmentUpdatedHandler(WidgetDescriptorUpdatedHandler handler) {
		handlerManager.addHandler(WidgetDescriptorUpdatedEvent.getType(), handler);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	@Override
	public void attachmentClicked(String attachmentName, String tokenId, String previewTokenId) {
		handlerManager.fireEvent(new AttachmentSelectedEvent(attachmentName, tokenId, previewTokenId));
	}
	
	@Override
	public void deleteAttachment(final String tokenId) {
		List<AttachmentData> attachments = entity.getAttachments();
		if(tokenId != null) {
			// find attachment via token and remove it
			AttachmentData found = null;
			for(AttachmentData data : attachments) {
				if(tokenId.equals(data.getTokenId())) {
					found = data;
				}
			}

			if(found != null) {
				// save name and remove from entity
				final String deletedName = found.getName();
				attachments.remove(found);
				JSONObjectAdapter adapter = jsonObjectAdapter.createNew();
				try {
					entity.writeToJSONObject(adapter);
				} catch (JSONObjectAdapterException e) {
					view.showErrorMessage(DisplayConstants.ERROR_DELETING_ATTACHMENT);
					return;
				}

				// update entity minus attachment
				synapseClient.createOrUpdateEntity(adapter.toJSONString(), null, false, new AsyncCallback<String>() {

					@Override
					public void onSuccess(String result) {
						view.attachmentDeleted(tokenId, deletedName);
						bus.fireEvent(new EntityUpdatedEvent());
						//also tell the listeners that this descriptor has been deleted, via a widget descriptor update event (where the new name is null)
						WidgetDescriptorUpdatedEvent e = new WidgetDescriptorUpdatedEvent();
						e.setName(null);
						e.setOldName(deletedName);
						handlerManager.fireEvent(e);
					}

					@Override
					public void onFailure(Throwable caught) {
						if(!DisplayUtils.handleServiceException(caught, globalApplicationState.getPlaceChanger(), authenticationController.getLoggedInUser())) {
							view.showErrorMessage(DisplayConstants.ERROR_DELETING_ATTACHMENT);
						}
					}
				});
			} else {
				view.showErrorMessage(DisplayConstants.ERROR_DELETING_ATTACHMENT);
			}
		} else {
			view.showErrorMessage(DisplayConstants.ERROR_DELETING_ATTACHMENT);
		}
	}
	
	/**
	 * For unit testing purposes only
	 * @return
	 */
	public List<AttachmentData> getWorkingSet() {
		return workingSet;
	}
	
	@Override
	public void setAttachmentColumnWidth(int width) {
		view.setAttachmentColumnWidth(width);
	}

}
