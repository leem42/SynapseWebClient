package org.sagebionetworks.web.client.widget.entity.act;

import com.google.gwt.user.client.ui.IsWidget;

public interface ApproveUserAccessModalView extends IsWidget {

	void setPresenter(Presenter presenter);
	void setEvaluationName(String name);
	String getEvaluationName();
	void show();
	void hide();
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onSave();
	}

}
