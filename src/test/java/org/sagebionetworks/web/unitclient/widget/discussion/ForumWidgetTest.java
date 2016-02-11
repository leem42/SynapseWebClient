package org.sagebionetworks.web.unitclient.widget.discussion;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.sagebionetworks.web.client.widget.discussion.ForumWidget.DEFAULT_MODERATOR_MODE;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.discussion.DiscussionThreadBundle;
import org.sagebionetworks.repo.model.discussion.Forum;
import org.sagebionetworks.web.client.DiscussionForumClientAsync;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.place.ParameterizedToken;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadListWidget;
import org.sagebionetworks.web.client.widget.discussion.DiscussionThreadWidget;
import org.sagebionetworks.web.client.widget.discussion.ForumWidget;
import org.sagebionetworks.web.client.widget.discussion.ForumWidgetView;
import org.sagebionetworks.web.client.widget.discussion.modal.NewDiscussionThreadModal;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.tabs.Tab;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class ForumWidgetTest {
	@Mock
	ForumWidgetView mockView;
	@Mock
	DiscussionThreadListWidget mockAvailableThreadListWidget;
	@Mock
	CallbackP<Tab> mockOnClickCallback;
	@Mock
	NewDiscussionThreadModal mockNewDiscussionThreadModal;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	DiscussionForumClientAsync mockDiscussionForumClient;
	@Mock
	Forum mockForum;
	@Mock
	AuthenticationController mockAuthController;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	PlaceChanger mockPlaceChanger;
	@Mock
	DiscussionThreadWidget mockDiscussionThreadWidget;
	@Mock
	DiscussionThreadBundle mockDiscussionThreadBundle;
	
	ForumWidget forumWidget;
	private boolean canModerate = false;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		forumWidget = new ForumWidget(mockView, mockSynAlert, mockDiscussionForumClient,
				mockAvailableThreadListWidget, mockNewDiscussionThreadModal,
				mockAuthController, mockGlobalApplicationState, mockDiscussionThreadWidget);
		when(mockAuthController.isLoggedIn()).thenReturn(true);
		when(mockGlobalApplicationState.getPlaceChanger()).thenReturn(mockPlaceChanger);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setThreadList(any(Widget.class));
		verify(mockView).setNewThreadModal(any(Widget.class));
		verify(mockView).setPresenter(forumWidget);
		verify(mockView).setAlert(any(Widget.class));
		verify(mockView).setSingleThread(any(Widget.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureSuccess() {
		when(mockForum.getId()).thenReturn("123");
		AsyncMockStubber.callSuccessWith(mockForum).when(mockDiscussionForumClient)
				.getForumMetadata(anyString(), any(AsyncCallback.class));

		String entityId = "syn1"; 
		String areaToken = "a=b&c=d";
		ParameterizedToken param = new ParameterizedToken(areaToken);
		Callback callback = null;
		forumWidget.configure(entityId, param, canModerate, callback);

		verify(mockDiscussionForumClient).getForumMetadata(anyString(), any(AsyncCallback.class));
		verify(mockNewDiscussionThreadModal).configure(anyString(), any(Callback.class));
		verify(mockAvailableThreadListWidget).configure(anyString(), eq(DEFAULT_MODERATOR_MODE));
		verify(mockView).setModeratorModeContainerVisibility(canModerate);
		verify(mockView).setSingleThreadUIVisible(false);
		verify(mockView).setThreadListUIVisible(true);
		verify(mockSynAlert).clear();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureFailure() {
		when(mockForum.getId()).thenReturn("123");
		AsyncMockStubber.callFailureWith(new Exception()).when(mockDiscussionForumClient)
				.getForumMetadata(anyString(), any(AsyncCallback.class));

		String entityId = "syn1"; 
		String areaToken = "foo=bar";
		ParameterizedToken param = new ParameterizedToken(areaToken);
		Callback callback = null;
		forumWidget.configure(entityId, param, canModerate, callback);


		verify(mockDiscussionForumClient).getForumMetadata(anyString(), any(AsyncCallback.class));
		verify(mockNewDiscussionThreadModal, never()).configure(anyString(), any(Callback.class));
		verify(mockView).setModeratorModeContainerVisibility(canModerate);
		verify(mockSynAlert).handleException(any(Exception.class));
		verify(mockSynAlert).clear();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureSuccessWithModerator() {
		when(mockForum.getId()).thenReturn("123");
		AsyncMockStubber.callSuccessWith(mockForum).when(mockDiscussionForumClient)
				.getForumMetadata(anyString(), any(AsyncCallback.class));

		String entityId = "syn1";
		String areaToken = "";
		canModerate = true;
		ParameterizedToken param = new ParameterizedToken(areaToken);
		Callback callback = null;
		forumWidget.configure(entityId, param, canModerate, callback);

		verify(mockAvailableThreadListWidget).configure(anyString(), eq(DEFAULT_MODERATOR_MODE));
		verify(mockView).setModeratorModeContainerVisibility(canModerate);
		verify(mockSynAlert).clear();
	}

	@Test
	public void onClickNewThreadTest() {
		forumWidget.onClickNewThread();
		verify(mockNewDiscussionThreadModal).show();
	}

	@Test
	public void onClickNewThreadAnonymousTest() {
		when(mockAuthController.isLoggedIn()).thenReturn(false);
		forumWidget.onClickNewThread();
		verify(mockNewDiscussionThreadModal, never()).show();
		verify(mockGlobalApplicationState).getPlaceChanger();
		verify(mockView).showErrorMessage(anyString());
	}

	@Test
	public void testOnModeratorModeChange() {
		when(mockView.getModeratorMode()).thenReturn(true);
		forumWidget.onModeratorModeChange();
		verify(mockAvailableThreadListWidget).configure(anyString(), eq(true));
		verify(mockNewDiscussionThreadModal).configure(anyString(), any(Callback.class));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureSingleThreadSuccess() {
		//verify that collapsed thread is automatically toggled when showing a single thread
		when(mockDiscussionThreadWidget.isThreadCollapsed()).thenReturn(true);
		AsyncMockStubber.callSuccessWith(mockDiscussionThreadBundle).when(mockDiscussionForumClient)
				.getThread(anyString(), any(AsyncCallback.class));

		String entityId = "syn1";
		String threadId = "007";
		String areaToken = ForumWidget.THREAD_ID_KEY + "=" + threadId;
		ParameterizedToken param = new ParameterizedToken(areaToken);
		Callback callback = null;
		forumWidget.configure(entityId, param, canModerate, callback);

		
		verify(mockDiscussionForumClient).getThread(eq(threadId), any(AsyncCallback.class));
		verify(mockDiscussionThreadWidget).configure(eq(mockDiscussionThreadBundle), eq(canModerate), any(Callback.class));
		verify(mockDiscussionThreadWidget).toggleThread();
		verify(mockAvailableThreadListWidget, never()).configure(anyString(), anyBoolean());
		verify(mockView).setSingleThreadUIVisible(true);
		verify(mockView).setThreadListUIVisible(false);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testConfigureSingleThreadFailure() {
		Exception ex = new Exception("error");
		AsyncMockStubber.callFailureWith(ex).when(mockDiscussionForumClient)
				.getThread(anyString(), any(AsyncCallback.class));

		String entityId = "syn1";
		String threadId = "007";
		String areaToken = ForumWidget.THREAD_ID_KEY + "=" + threadId;
		ParameterizedToken param = new ParameterizedToken(areaToken);
		Callback callback = null;
		forumWidget.configure(entityId, param, canModerate, callback);

		
		verify(mockSynAlert).handleException(ex);
		verify(mockView).setSingleThreadUIVisible(true);
		verify(mockView).setThreadListUIVisible(false);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testOnClickShowAllThreads() {
		String entityId = "syn1";
		String threadId = "007";
		String areaToken = ForumWidget.THREAD_ID_KEY + "=" + threadId;
		ParameterizedToken param = new ParameterizedToken(areaToken);
		Callback callback = null;
		forumWidget.configure(entityId, param, canModerate, callback);
		forumWidget.onClickShowAllThreads();

		//attempts to show full thread list
		verify(mockDiscussionForumClient).getForumMetadata(anyString(), any(AsyncCallback.class));
		verify(mockView).setSingleThreadUIVisible(false);
		verify(mockView).setThreadListUIVisible(true);
		verify(mockSynAlert, times(2)).clear();
	}
}
