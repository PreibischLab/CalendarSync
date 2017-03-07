/*
 * Copyright (c) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package sync;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

/**
 * @author spreibi@google.com (Your Name Here)
 *
 */
public class GoogleCalendar implements ListMyEvents< Event >
{
	final String APPLICATION_NAME = "BIMSBCalendarSync";
	final java.io.File DATA_STORE_DIR = new java.io.File( System.getProperty( "user.home" ), ".store/calendar_sample" );
	final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	final com.google.api.services.calendar.Calendar client;
	final CalendarListEntry bimsbCal;

	public GoogleCalendar()
	{
		try
		{
			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory( DATA_STORE_DIR );
			Credential credential = authorize( httpTransport, dataStoreFactory, JSON_FACTORY );

			// set up global Calendar instance
			client = new com.google.api.services.calendar.Calendar.Builder( httpTransport, JSON_FACTORY, credential ).setApplicationName(APPLICATION_NAME).build();

			CalendarList feed = client.calendarList().list().execute();
			bimsbCal = getBIMSBCalendar( feed );

			if ( bimsbCal != null )
				System.out.println( "Found: " + bimsbCal.getSummary() + " [" + bimsbCal.getDescription() + "]" );
			else
				throw new RuntimeException( "Could not find BIMSB Calendar." );
		}
		catch ( Exception e )
		{
			throw new RuntimeException( "An error occured: " + e );
		}
	}

	public void deleteEvent( final Event e )
	{
		try
		{
			client.events().delete( bimsbCal.getId(), e.getId() ).execute();
		}
		catch ( IOException e1 )
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public boolean deleteEventsBatch( final Collection< Event > events )
	{
		if ( events == null || events.size() == 0 )
			return true;

		try
		{
			BatchRequest batch = client.batch();
	
			for ( Event e : events )
			{
				client.events().delete( bimsbCal.getId(), e.getId() ).queue(batch, new JsonBatchCallback< Void >()
				{
					@Override
					public void onSuccess( final Void content, final HttpHeaders responseHeaders){ System.out.println("Deleted: " + content ); }
				
					@Override
					public void onFailure( final GoogleJsonError e, final HttpHeaders responseHeaders) { System.out.println("Error Message: " + e.getMessage() ); }
				});
			}
			
			batch.execute();

			return true;
		}
		catch ( IOException e1 )
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
	}

	public Event addEvent( final Event e )
	{
		try
		{
			return client.events().insert( bimsbCal.getId(), e ).execute();
		}
		catch ( IOException e1 )
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
	}

	public boolean addEventsBatch( final Collection< Event > events )
	{
		if ( events == null || events.size() == 0 )
			return true;

		try
		{
			BatchRequest batch = client.batch();
	
			for ( Event e : events )
			{
				client.events().insert( bimsbCal.getId(), e ).queue(batch, new JsonBatchCallback<Event>()
				{
					@Override
					public void onSuccess( final Event content, final HttpHeaders responseHeaders){ System.out.println("Inserted: " + content ); }
				
					@Override
					public void onFailure( final GoogleJsonError e, final HttpHeaders responseHeaders) { System.out.println("Error Message: " + e.getMessage() ); }
				});
			}
			
			batch.execute();

			return true;
		}
		catch ( IOException e1 )
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
	}

	public ArrayList< MyEvent< Event > > allEvents( final Date startDate, final Date endDate )
	{
		try
		{
			final ArrayList< MyEvent< Event > > events = new ArrayList< MyEvent< Event > >();
			final Events googleEvents = client.events().list( bimsbCal.getId() ).setTimeMin( new DateTime( startDate ) ).setTimeMax( new DateTime( endDate ) ).execute();

			for ( final Event e : googleEvents.getItems() )
			{
				events.add(
					new MyEvent< Event >(
						e.getSummary(),
						e.getLocation(),
						getStart( e ),
						getEnd( e ),
						isAllDay( e ),
						e.getDescription(),
						e ) );
			}

			return events;
		}
		catch ( IOException e1 )
		{
			e1.printStackTrace();
			return null;
		}
	}

	public static void main( String[] args ) throws Exception
	{
		GoogleCalendar g = new GoogleCalendar();

		Date startDate = MyEvent.parseDate( "2017-02-27 12:00:00" );
		Date endDate = MyEvent.parseDate( "2017-02-27 22:30:00" );

		for ( final MyEvent< Event > e : g.allEvents( startDate, endDate ) )
			System.out.println( e );

		/*
		Events events = g.client.events().list( g.bimsbCal.getId() ).setTimeMin( new DateTime( startDate ) ).setTimeMax( new DateTime( endDate ) ).execute();
		for ( final Event e : events.getItems() )
		{
			System.out.println( e.getSummary() );
			System.out.println( e.getLocation() );
			//System.out.println( e.getStart().getDateTime() + " >> " + e.getEnd().getDateTime() );
			//System.out.println( e.getStart().getDate() + " >> " + e.getEnd().getDate() );
			System.out.println( getStart( e ) );
			System.out.println( getEnd( e ) );
		    System.out.println( "isAll-day: " + isAllDay( e ) );
			System.out.println( e.getDescription() );
			System.out.println( e );
			System.out.println();
		}*/
	}

	public static Date getStart( final Event e )
	{
		if ( isAllDay( e ) )
		{
			return MyEvent.parseDate( e.getStart().getDate().toString() + " 00:00:00" );
		}
		else
		{
			final String date = e.getStart().getDateTime().toString();
			return MyEvent.parseDate( date.substring( 0, 10 ) + " " + date.substring( 11, 19 ) );
		}
	}

	public static Date getEnd( final Event e )
	{
		if ( isAllDay( e ) )
		{
			return MyEvent.parseDate( e.getEnd().getDate().toString().substring( 0, 10 ) + " 00:00:00" );
		}
		else
		{
			final String date = e.getEnd().getDateTime().toString();
			return MyEvent.parseDate( date.substring( 0, 10 ) + " " + date.substring( 11, 19 ) );
		}
	}

	public static boolean isAllDay( final Event e )
	{
		if ( e.getStart().getDateTime() == null || e.getEnd().getDateTime() == null )
			return true;
		else
			return false;
	}

	public static CalendarListEntry getBIMSBCalendar( CalendarList feed )
	{
		for ( final CalendarListEntry s : feed.getItems() )
			if ( s.getSummary().toLowerCase().startsWith( "bimsb calendar" ) ) //if ( s.getSummary().toLowerCase().startsWith( "preibisch lab" ) ) 
				return s;

		return null;
	}

	public static Credential authorize(
			final HttpTransport httpTransport,
			final FileDataStoreFactory dataStoreFactory,
			final JsonFactory JSON_FACTORY ) throws Exception
	{
		// load client secrets
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
				JSON_FACTORY,
				new InputStreamReader(
					new FileInputStream(
						new File(
							"client_secret_google.json"))));

		// set up authorization code flow
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder( httpTransport,
			JSON_FACTORY, clientSecrets, Collections.singleton(CalendarScopes.CALENDAR))
				.setDataStoreFactory( dataStoreFactory ).build();

		// authorize
		return new AuthorizationCodeInstalledApp( flow, new LocalServerReceiver() ).authorize( "user" );
	}
}
