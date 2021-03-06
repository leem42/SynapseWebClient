package org.sagebionetworks.web.unitclient.widget.table.v2.results.cell;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import javax.annotation.meta.When;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EnumCellEditorImpl;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.ListCellEdtiorView;

public class EnumCellEditorImplTest {
	
	ListCellEdtiorView mockView;
	EnumCellEditorImpl editor;
	@Mock
	SynapseJSNIUtils mockSynapseJSNIUtils;
	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		mockView = Mockito.mock(ListCellEdtiorView.class);
		editor = new EnumCellEditorImpl(mockView, mockSynapseJSNIUtils);
	}

	@Test
	public void testConfigure(){
		List<String> values = Arrays.asList("one", "two");
		editor.configure(values);
		verify(mockView).configure(Arrays.asList(EnumCellEditorImpl.NOTHING_SELECTED, "one", "two"));
	}
	
	@Test
	public void testSetNull(){
		List<String> values = Arrays.asList("one", "two");
		editor.configure(values);
		editor.setValue(null);
		verify(mockView).setValue(0);
	}
	
	@Test
	public void testSetEmpty(){
		List<String> values = Arrays.asList("one", "two");
		editor.configure(values);
		editor.setValue("");
		verify(mockView).setValue(0);
	}
	
	@Test
	public void testSetValue(){
		List<String> values = Arrays.asList("one", "two");
		editor.configure(values);
		editor.setValue("one");
		// second value (first is 'nothing selected')
		verify(mockView).setValue(1);
	}
	
	@Test
	public void testSetInvalidValue(){
		List<String> values = Arrays.asList("one", "two");
		editor.configure(values);
		editor.setValue("three");
		verify(mockView).setValue(0);
		verify(mockSynapseJSNIUtils).consoleError(anyString());
	}

	
	@Test
	public void testGetNull(){
		when(mockView.getValue()).thenReturn(0);
		List<String> values = Arrays.asList("one", "two");
		editor.configure(values);
		assertEquals(null, editor.getValue());
	}
	
	@Test
	public void testGetValue(){
		when(mockView.getValue()).thenReturn(2);
		List<String> values = Arrays.asList("one", "two");
		editor.configure(values);
		assertEquals("two", editor.getValue());
	}
	
	
}
