package sync;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import com.google.api.services.calendar.model.Event;

import microsoft.exchange.webservices.data.core.service.item.Appointment;

public class SyncCalendars
{
	public static void main( String[] args )
	{
		final GoogleCalendar gc = new GoogleCalendar();
		final ExchangeCalendar ec = new ExchangeCalendar();

		Date startDate = MyEvent.parseDate( "2017-02-27 17:30:00" );
		Date endDate = MyEvent.parseDate( "2017-12-31 23:30:00" );

		final HashSet< MyEvent< Appointment > > eEvents = new HashSet< MyEvent< Appointment > >();
		final HashSet< MyEvent< Event > > gEvents = new HashSet< MyEvent< Event > >();

		eEvents.addAll( ec.allEvents( startDate, endDate ) );
		gEvents.addAll( gc.allEvents( startDate, endDate ) );

		System.out.println( "Exchange calendar events (" + eEvents.size() + " in total):" );
		for ( final MyEvent< Appointment > e : eEvents )
			System.out.println( e );

		System.out.println( "\nGoogle calendar events (" + gEvents.size() + " in total):" );
		for ( final MyEvent< Event > e : gEvents )
			System.out.println( e );

		final HashSet< MyEvent< Appointment > > toSync = new HashSet< MyEvent< Appointment > >();
		final HashSet< MyEvent< Event > > toDelete = new HashSet< MyEvent< Event > >();

		// events present in exchange but not in google -- copy over
		for ( final MyEvent< Appointment > e : eEvents )
			if ( !gEvents.contains( e ) )
				toSync.add( e );

		// events present in google but not in exchange -- delete
		for ( final MyEvent< Event > e : gEvents )
			if ( !eEvents.contains( e ) )
				toDelete.add( e );

		System.out.println( "\ntoSync:" );
		System.out.println( "=======" );
		final ArrayList< Event > syncBatch = new ArrayList< Event >();
		for ( final MyEvent< Appointment > e : toSync )
		{
			System.out.println( e );
			syncBatch.add( e.event() );
			//System.out.println( gc.addEvent( e.event() ) );
		}
		gc.addEventsBatch( syncBatch );
		
		System.out.println( "\ntoDelete:" );
		System.out.println( "=========" );
		final ArrayList< Event > deleteBatch = new ArrayList< Event >();
		for ( final MyEvent< Event > e : toDelete )
		{
			System.out.println( e );
			deleteBatch.add( e.object );
			//gc.deleteEvent( e.object );
		}
		gc.deleteEventsBatch( deleteBatch );
	}
}
