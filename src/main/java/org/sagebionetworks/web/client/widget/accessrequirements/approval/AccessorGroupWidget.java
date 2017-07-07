package org.sagebionetworks.web.client.widget.accessrequirements.approval;

import java.util.Iterator;
import java.util.List;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.dataaccess.AccessorGroup;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.accessrequirements.AccessRequirementWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AccessorGroupWidget implements AccessorGroupView.Presenter, IsWidget {
	
	public static final String ARE_YOU_SURE = "Are you sure?";
	public static final String REVOKE_ACCESS_TO_GROUP = "Revoke access to group?";
	private AccessorGroupView view;
	SynapseAlert synAlert;
	AccessorGroup accessorGroup;
	PortalGinInjector ginInjector;
	PopupUtilsView popupUtils;
	AccessRequirementWidget accessRequirementWidget;
	DataAccessClientAsync dataAccessClient;
	SynapseClientAsync synapseClient;
	Callback onRevokeCallback;
	DateTimeUtils dateTimeUtils;
	
	@Inject
	public AccessorGroupWidget(AccessorGroupView view, 
			SynapseAlert synAlert,
			PortalGinInjector ginInjector,
			PopupUtilsView popupUtils,
			AccessRequirementWidget accessRequirementWidget,
			DataAccessClientAsync dataAccessClient,
			SynapseClientAsync synapseClient,
			DateTimeUtils dateTimeUtils) {
		this.view = view;
		this.synAlert = synAlert;
		this.ginInjector = ginInjector;
		this.popupUtils = popupUtils;
		this.accessRequirementWidget = accessRequirementWidget;
		this.dataAccessClient = dataAccessClient;
		this.synapseClient = synapseClient;
		this.dateTimeUtils = dateTimeUtils;
		view.setPresenter(this);
		view.setSynAlert(synAlert);
		view.setAccessRequirementWidget(accessRequirementWidget);
	}
	
	public void configure(AccessorGroup accessorGroup) {
		synAlert.clear();
		this.accessorGroup = accessorGroup;
		addAccessorUserBadges(accessorGroup.getAccessorIds());
		UserBadge badge = ginInjector.getUserBadgeWidget();
		badge.configure(accessorGroup.getSubmitterId());
		if (accessorGroup.getExpiredOn() != null && accessorGroup.getExpiredOn().getTime() > 0) {
			view.setExpiresOn(dateTimeUtils.convertDateToSmallString(accessorGroup.getExpiredOn()));	
		} else {
			view.setExpiresOn("");
		}
		
		view.setSubmittedBy(badge);
	}
	
	public void setOnRevokeCallback(Callback onRevokeCallback) {
		this.onRevokeCallback = onRevokeCallback;
	}
	
	public void addAccessorUserBadges(List<String> accessorIds) {
		view.clearAccessors();
		for (String userId : accessorIds) {
			UserBadge badge = ginInjector.getUserBadgeWidget();
			badge.configure(userId);
			view.addAccessor(badge);
		}
	}
	@Override
	public void onShowEmails() {
		//get the profiles, to get the usernames
		synapseClient.listUserProfiles(accessorGroup.getAccessorIds(), new AsyncCallback<List<UserProfile>>() {
			@Override
			public void onSuccess(List<UserProfile> userProfiles) {
				StringBuilder sb = new StringBuilder();
				for (Iterator it = userProfiles.iterator(); it.hasNext();) {
					UserProfile profile = (UserProfile) it.next();
					sb.append(profile.getUserName() + "@synapse.org");
					if (it.hasNext()) {
						sb.append(", ");
					}
				}
				view.setEmailAddresses(sb.toString());
				view.showEmailAddressesDialog();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
	
	public void addStyleNames(String styleNames) {
		view.addStyleNames(styleNames);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public void setVisible(boolean visible) {
		view.setVisible(visible);
	}
	
	@Override
	public void onShowAccessRequirement() {
		accessRequirementWidget.configure(accessorGroup.getAccessRequirementId());
		view.showAccessRequirementDialog();
	}
	
	@Override
	public void onRevoke() {
		popupUtils.showConfirmDialog(REVOKE_ACCESS_TO_GROUP, ARE_YOU_SURE, new Callback() {
			@Override
			public void invoke() {
				onRevokeAfterConfirm();
			}
		});
	}
	
	public void onRevokeAfterConfirm() {
		dataAccessClient.revokeGroup(accessorGroup.getAccessRequirementId(), accessorGroup.getSubmitterId(), new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				popupUtils.showInfo("Successfully revoked access.", "");
				if (onRevokeCallback != null) {
					onRevokeCallback.invoke();
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
}