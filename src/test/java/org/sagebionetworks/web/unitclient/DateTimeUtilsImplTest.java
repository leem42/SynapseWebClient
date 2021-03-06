package org.sagebionetworks.web.unitclient;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.sagebionetworks.web.client.DateTimeUtilsImpl.*;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.DateTimeUtilsImpl;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.Moment;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.datepicker.client.CalendarUtil;

public class DateTimeUtilsImplTest {
	
	@Mock
	Moment mockMoment;
	@Mock
	GWTWrapper mockGWT;
	@Mock
	DateTimeFormat mockDateOnlyFormat;
	@Mock
	DateTimeFormat mockDateOnlyFormatUTC;
	@Mock
	DateTimeFormat mockSmallDateFormat;
	@Mock
	DateTimeFormat mockSmallDateFormatUTC;
	@Mock
	DateTimeFormat mockLongDateFormat;
	@Mock
	DateTimeFormat mockLongDateFormatUTC;
	@Mock
	DateTimeFormat mockISO8601Format;
	
	DateTimeUtilsImpl dateTimeUtils;
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
		when(mockGWT.getFormat(DATE_ONLY_FORMAT_STRING)).thenReturn(mockDateOnlyFormat);
		when(mockGWT.getFormat(DATE_ONLY_FORMAT_STRING + UTC)).thenReturn(mockDateOnlyFormatUTC);
		when(mockGWT.getFormat(SMALL_DATE_FORMAT_STRING)).thenReturn(mockSmallDateFormat);
		when(mockGWT.getFormat(SMALL_DATE_FORMAT_STRING + UTC)).thenReturn(mockSmallDateFormatUTC);
		when(mockGWT.getFormat(LONG_DATE_FORMAT_STRING)).thenReturn(mockLongDateFormat);
		when(mockGWT.getFormat(LONG_DATE_FORMAT_STRING + UTC)).thenReturn(mockLongDateFormatUTC);
		when(mockGWT.getFormat(PredefinedFormat.ISO_8601)).thenReturn(mockISO8601Format);
		dateTimeUtils = new DateTimeUtilsImpl(mockMoment, mockGWT);
	}	

	@Test
	public void testGetDateString() {
		Date d = new Date();
		dateTimeUtils.getDateString(d);
		verify(mockDateOnlyFormat).format(d);
		
		dateTimeUtils.setShowUTCTime(true);
		dateTimeUtils.getDateString(d);
		verify(mockDateOnlyFormatUTC).format(d);
		
		dateTimeUtils.setShowUTCTime(false);
		dateTimeUtils.getDateString(d);
		verify(mockDateOnlyFormat, times(2)).format(d);
	}
	
	@Test
	public void testRelativeTime() {
		Date d = new Date();
		CalendarUtil.addDaysToDate(d, -2);
		dateTimeUtils.getRelativeTime(d);
		verifyZeroInteractions(mockMoment);
		verify(mockDateOnlyFormat).format(d);
		
		d = new Date();
		CalendarUtil.addDaysToDate(d, 2);
		dateTimeUtils.getRelativeTime(d);
		verify(mockDateOnlyFormat).format(d);
		verifyZeroInteractions(mockMoment);
		
		d = new Date();
		dateTimeUtils.getRelativeTime(d);
		verify(mockMoment).getRelativeTime(anyString());
	}
}






