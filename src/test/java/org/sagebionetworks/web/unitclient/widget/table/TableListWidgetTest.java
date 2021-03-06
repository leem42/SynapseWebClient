package org.sagebionetworks.web.unitclient.widget.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityChildrenRequest;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityType;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.entity.Direction;
import org.sagebionetworks.repo.model.entity.SortBy;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.TableListWidget;
import org.sagebionetworks.web.client.widget.table.TableListWidgetView;
import org.sagebionetworks.web.client.widget.table.modal.fileview.CreateTableViewWizard;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class TableListWidgetTest {

	private static final String ENTITY_ID = "syn123";
	private TableListWidgetView mockView;
	private TableListWidget widget;
	private EntityBundle parentBundle;
	private UserEntityPermissions permissions;
	@Mock
	CreateTableViewWizard mockCreateTableViewWizard;
	@Mock
	CookieProvider mockCookies;
	@Mock
	LoadMoreWidgetContainer mockLoadMoreWidgetContainer;
	@Mock
	EntityChildrenResponse mockResults;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	CallbackP<EntityHeader> mockTableClickedCallback;
	@Mock
	EntityHeader mockEntityHeader;
	List<EntityHeader> searchResults;
	
	
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		permissions = new UserEntityPermissions();
		permissions.setCanEdit(true);
		Project project = new Project();
		project.setId(ENTITY_ID);
		parentBundle = new EntityBundle();
		parentBundle.setEntity(project);
		parentBundle.setPermissions(permissions);
		mockView = Mockito.mock(TableListWidgetView.class);
		widget = new TableListWidget(mockView, mockSynapseJavascriptClient, mockLoadMoreWidgetContainer, mockSynAlert);
		AsyncMockStubber.callSuccessWith(mockResults).when(mockSynapseJavascriptClient).getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class));
		searchResults = new ArrayList<EntityHeader>();
		when(mockResults.getPage()).thenReturn(searchResults);
		when(mockCookies.getCookie(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY)).thenReturn("true");
	}
	
	@Test
	public void testCreateQuery(){
		String parentId = ENTITY_ID;
		EntityChildrenRequest query = widget.createQuery(parentId);
		assertEquals(parentId, query.getParentId());
		assertTrue(query.getIncludeTypes().contains(EntityType.entityview));
		assertTrue(query.getIncludeTypes().contains(EntityType.table));
		assertEquals(SortBy.CREATED_ON, query.getSortBy());
		assertEquals(Direction.DESC, query.getSortDirection());
	}
	
	@Test
	public void testConfigureUnderPageSize(){
		widget.configure(parentBundle);
		verify(mockView, times(2)).hideLoading();
		verify(mockView).setSortUI(TableListWidget.DEFAULT_SORT_BY, TableListWidget.DEFAULT_DIRECTION);
		verify(mockLoadMoreWidgetContainer).setIsMore(false);
	}
	
	@Test
	public void testConfigureOverPageSize(){
		when(mockResults.getNextPageToken()).thenReturn("ismore");
		widget.configure(parentBundle);
		verify(mockView).setSortUI(TableListWidget.DEFAULT_SORT_BY, TableListWidget.DEFAULT_DIRECTION);
		verify(mockLoadMoreWidgetContainer).setIsMore(true);
	}
	
	@Test
	public void testConfigureFailure(){
		parentBundle.getPermissions().setCanEdit(false);
		String error = "an error";
		Throwable th = new Throwable(error);
		AsyncMockStubber.callFailureWith(th).when(mockSynapseJavascriptClient).getEntityChildren(any(EntityChildrenRequest.class), any(AsyncCallback.class));
		widget.configure(parentBundle);
		verify(mockSynAlert).handleException(th);
	}
	
	@Test
	public void testOnTableClicked() {
		widget.setTableClickedCallback(mockTableClickedCallback);
		widget.onTableClicked(mockEntityHeader);
		
		verify(mockView).showLoading();
		verify(mockView).clearTableWidgets();
		verify(mockTableClickedCallback).invoke(mockEntityHeader);
	}
}
