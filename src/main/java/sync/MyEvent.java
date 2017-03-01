package sync;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

public class MyEvent< O > implements Comparable< MyEvent< ? > >
{
	public static SimpleDateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
	public static SimpleDateFormat formatterDateOnly = new SimpleDateFormat( "yyyy-MM-dd" );

	final String subject, location, body;
	final Date start, end;
	final boolean isAllDay;

	final O object;

	public MyEvent( final String subject, final String location, final Date start, final Date end, final boolean isAllDay, final String body, final O object )
	{
		if ( subject == null )
			this.subject = "";
		else
			this.subject = subject;

		if ( location == null )
			this.location = "";
		else
			this.location = location;

		this.start = start;
		this.end = end;
		this.isAllDay = isAllDay;

		if ( body == null )
			this.body = "";
		else
			this.body = body;

		this.object = object;
	}

	// makes a google event
	public Event event()
	{
		final Event event = new Event();
		event.setSummary( this.subject );
		event.setLocation( this.location );
		event.setDescription( this.body );

		String tzStart = "CET";
		String tzEnd = "CET";

		if ( this.start.toString().contains( "CEST" ))
			tzStart = "CEST";

		if ( this.end.toString().contains( "CEST" ))
			tzEnd = "CEST";

		if ( isAllDay )
		{
			DateTime s = new DateTime( formatterDateOnly.format( start ) );
			event.setStart( new EventDateTime().setDate( s ) );

			DateTime e = new DateTime( formatterDateOnly.format( end ) );
			event.setEnd( new EventDateTime().setDate( e ) );
		}
		else
		{
			DateTime s = new DateTime( start, TimeZone.getTimeZone( tzStart ) );
			event.setStart( new EventDateTime().setDateTime( s ) );

			DateTime e = new DateTime( end, TimeZone.getTimeZone( tzEnd ) );
			event.setEnd( new EventDateTime().setDateTime( e ) );
		}

		return event;
	}

	public static Date parseDate( final String date )
	{
		try
		{
			return formatter.parse( date ); // e.g. "2017-02-28 05:00:00"
		}
		catch ( ParseException e )
		{
			e.printStackTrace();
			return null;
		} 
	}

	public String toString()
	{
		String r = "Appointment '" + subject + "', location '" + location + "', from '" + start + "' to '" + end + "' [all-day:" + isAllDay + "], details '" + body.substring( 0, Math.min( 30, body.length() ) ) + " ...'";
		return r.replaceAll( "\n", " " );
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( body == null ) ? 0 : body.hashCode() );
		result = prime * result + ( ( end == null ) ? 0 : end.hashCode() );
		result = prime * result + ( isAllDay ? 1231 : 1237 );
		result = prime * result
				+ ( ( location == null ) ? 0 : location.hashCode() );
		result = prime * result + ( ( start == null ) ? 0 : start.hashCode() );
		result = prime * result
				+ ( ( subject == null ) ? 0 : subject.hashCode() );
		return result;
	}

	@Override
	public boolean equals( Object obj )
	{
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		MyEvent< ? > other = (MyEvent< ? >) obj;
		if ( body == null )
		{
			if ( other.body != null )
				return false;
		} else if ( !body.equals( other.body ) )
			return false;
		if ( end == null )
		{
			if ( other.end != null )
				return false;
		} else if ( !end.equals( other.end ) )
			return false;
		if ( isAllDay != other.isAllDay )
			return false;
		if ( location == null )
		{
			if ( other.location != null )
				return false;
		} else if ( !location.equals( other.location ) )
			return false;
		if ( start == null )
		{
			if ( other.start != null )
				return false;
		} else if ( !start.equals( other.start ) )
			return false;
		if ( subject == null )
		{
			if ( other.subject != null )
				return false;
		} else if ( !subject.equals( other.subject ) )
			return false;
		return true;
	}

	@Override
	public int compareTo( final MyEvent< ? > o )
	{
		return start.compareTo( o.start );
	}
}
