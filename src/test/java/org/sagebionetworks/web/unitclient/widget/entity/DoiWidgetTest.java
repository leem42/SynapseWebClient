package org.sagebionetworks.web.unitclient.widget.entity;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.doi.Doi;
import org.sagebionetworks.repo.model.doi.DoiStatus;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.DoiWidget;
import org.sagebionetworks.web.client.widget.entity.DoiWidgetView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class DoiWidgetTest {

	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	DoiWidgetView mockView;
	String entityId = "syn123";
	DoiWidget doiWidget;
	Doi testDoi;
	@Mock
	AuthenticationController mockAuthenticationController;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	SynapseAlert mockSynAlert;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		testDoi = new Doi();
		testDoi.setId(entityId);
		testDoi.setDoiStatus(DoiStatus.CREATED);
		AsyncMockStubber.callSuccessWith(testDoi).when(mockSynapseClient).getEntityDoi(anyString(), anyLong(), any(AsyncCallback.class));
		doiWidget = new DoiWidget(mockView, mockGlobalApplicationState, mockAuthenticationController, mockSynapseClient, mockSynAlert);
	}
	
	@Test
	public void testConfigureReadyStatus() throws Exception {
		testDoi.setDoiStatus(DoiStatus.READY);
		doiWidget.configure(testDoi, entityId);
		verify(mockView).setVisible(false);
		verify(mockView).clear();
		verify(mockView).showDoiCreated(doiWidget.getDoi(DoiWidget.DOI_PREFIX, false));
	}

	@Test
	public void testConfigureErrorStatus() throws Exception {
		testDoi.setDoiStatus(DoiStatus.ERROR);
		doiWidget.configure(testDoi, entityId);
		verify(mockView).setVisible(false);
		verify(mockView).clear();
		verify(mockView).showDoiError();
	}

	@Test(expected=UnsatisfiedLinkError.class)
	public void testConfigureInProcessStatus() throws Exception {
		testDoi.setDoiStatus(DoiStatus.IN_PROCESS);
		doiWidget.configure(testDoi, entityId);
		verify(mockView).showDoiInProgress();
	}
	
	@Test
	public void testConfigureNotFound() throws Exception {
		doiWidget.configure(null, entityId);
		verify(mockView).setVisible(false);
		verify(mockView).clear();
	}
	
	@Test
	public void testGetDoiLink() throws Exception {
		String prefix = "10.5072/FK2.";
		Long version = 42l;
		testDoi.setObjectVersion(version);
		doiWidget.configure(testDoi, entityId);
		String link = doiWidget.getDoi(prefix, true);
		assertTrue(link.contains(entityId));
		assertTrue(link.contains(version.toString()));
		assertTrue(link.contains(prefix));
	}

	@Test
	public void testGetDoiLinkNoPrefix() throws Exception {
		String prefix = "";
		Long version = 42l;
		testDoi.setObjectVersion(version);
		doiWidget.configure(testDoi, entityId);
		String link = doiWidget.getDoi(prefix, true);
		assertTrue(link.length() == 0);
		link = doiWidget.getDoi(null, true);
		assertTrue(link.length() == 0);
	}

	@Test(expected=UnsatisfiedLinkError.class)
	public void testGetEntityDoi() {
		// when ready
		testDoi.setDoiStatus(DoiStatus.READY);
		doiWidget.getEntityDoi(entityId, null);
		verify(mockView).setVisible(false);
		verify(mockView).clear();
		verify(mockView).showDoiCreated(doiWidget.getDoi(DoiWidget.DOI_PREFIX, false));
		Mockito.reset(mockView);
		// when in error
		testDoi.setDoiStatus(DoiStatus.ERROR);
		doiWidget.getEntityDoi(entityId, null);
		verify(mockView).setVisible(false);
		verify(mockView).clear();
		verify(mockView).showDoiError();
		Mockito.reset(mockView);
		// when in process, should throw UnsatisfiedLinkError
		testDoi.setDoiStatus(DoiStatus.IN_PROCESS);
		doiWidget.getEntityDoi(entityId, null);
		verify(mockView).setVisible(false);
		verify(mockView).clear();
		verify(mockView).showDoiInProgress();
	}
	
}
