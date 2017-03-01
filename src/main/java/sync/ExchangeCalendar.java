package sync;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.folder.CalendarFolder;
import microsoft.exchange.webservices.data.core.service.item.Appointment;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.FolderId;
import microsoft.exchange.webservices.data.property.complex.Mailbox;
import microsoft.exchange.webservices.data.search.CalendarView;
import microsoft.exchange.webservices.data.search.FindItemsResults;

public class ExchangeCalendar implements ListMyEvents< Appointment >
{
	static
	{
		Logger root = (Logger) org.slf4j.LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.WARN);
	}

	final ExchangeService service;

	public ExchangeCalendar()
	{
		try
		{
			BufferedReader in = new BufferedReader( new FileReader( new File( "exchange.pwd" ) ) );
			final String user = Crypt.decrypt( in.readLine() );
			final String pass = Crypt.decrypt( in.readLine() );
			in.close();

			this.service = new ExchangeService();
			ExchangeCredentials credentials = new WebCredentials( user, pass );
			service.setUrl( new URI( "https://mdcmailapp.mdc-berlin.de/ews/exchange.asmx" ) );//https://casarray.mdc-berlin.net/ews/exchange.asmx"));
			service.setCredentials( credentials );
			service.setTraceEnabled( false );
		}
		catch ( Exception e ) { throw new RuntimeException( "An error occured: " + e ); }

		/*
		EmailMessage msg= new EmailMessage(service);
		msg.setSubject("Hello world!");
		msg.setBody(MessageBody.getMessageBodyFromText("Sent using the EWS Java API."));
		msg.getToRecipients().add("stephanpreibisch@gmail.com");
		msg.send();*/
	}

	public ArrayList< MyEvent< Appointment > > allEvents( final Date startDate, final Date endDate )
	{
		final ArrayList< MyEvent< Appointment > > events = new ArrayList< MyEvent< Appointment > >();

		try
		{
			//final FindItemsResults< Appointment > exchangeEvens = findAppointmentsMainCalendar( service, startDate, endDate );
			final FindItemsResults< Appointment > exchangeEvens = findAppointmentsDelegate( service, "Bimsb.Calendar@mdc-berlin.de", startDate, endDate );

			for ( final Appointment appt : exchangeEvens.getItems() )
			{
				appt.load();

				events.add(
					new MyEvent< Appointment >(
						appt.getSubject(),
						appt.getLocation(),
						appt.getStart(),
						appt.getEnd(),
						isAllDay( appt.getStart(), appt.getEnd() ),
						appt.getBody().toString(),
						appt ) );
			}

			return events;
		}
		catch ( Exception e )
		{
			e.printStackTrace();
			return null;
		}
	}

	public static void main( String[] args ) throws ServiceLocalException, Exception
	{
		final ExchangeCalendar c = new ExchangeCalendar();

		//Folder inbox = Folder.bind( service, WellKnownFolderName.Inbox );
		//System.out.println("messages: " + inbox.getTotalCount());

		Date startDate = MyEvent.parseDate( "2017-02-28 05:00:00" );
		Date endDate = MyEvent.parseDate( "2017-02-28 18:00:00" );

		for ( final MyEvent< Appointment > e : c.allEvents( startDate, endDate ) )
			System.out.println( e );

		for ( final Appointment appt : findAppointmentsMainCalendar( c.service, startDate, endDate ).getItems() )
		{
			appt.load();
		    System.out.println("SUBJECT====="+appt.getSubject());
		    System.out.println( appt.getLocation() );
		    System.out.println( appt.getStart() );
		    System.out.println( appt.getEnd() );
		    System.out.println( "isAll-day: " + isAllDay( appt.getStart(), appt.getEnd() ) );
		    System.out.println( appt.getBody() );

		    Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
		    calendar.setTime( appt.getStart() );  
		    System.out.println( calendar.get(Calendar.HOUR_OF_DAY) + " " + calendar.get(Calendar.MINUTE ) + " " + calendar.get(Calendar.SECOND) );
		    calendar.setTime( appt.getEnd() ); 
		    System.out.println( calendar.get(Calendar.HOUR_OF_DAY) + " " + calendar.get(Calendar.MINUTE ) + " " + calendar.get(Calendar.SECOND) );
		}
		/*
		for ( final Appointment appt : findAppointmentsDelegate( service, "Bimsb.Calendar@mdc-berlin.de", startDate, endDate ).getItems() )
		{
			appt.load();
			System.out.println("SUBJECT====="+appt.getSubject());
			System.out.println( appt.getLocation() );
			System.out.println( appt.getStart() );
			System.out.println( appt.getEnd() );
			System.out.println( appt.getBody() );
		}*/

	}

	public static boolean isAllDay( final Date start, final Date end )
	{
		Calendar calendar = GregorianCalendar.getInstance();

		calendar.setTime( start );
		if ( calendar.get( Calendar.HOUR_OF_DAY ) != 0 ) return false;
		if ( calendar.get( Calendar.MINUTE ) != 0 ) return false;
		if ( calendar.get( Calendar.SECOND ) != 0 ) return false;

		calendar.setTime( end );
		if ( calendar.get( Calendar.HOUR_OF_DAY ) != 0 ) return false;
		if ( calendar.get( Calendar.MINUTE ) != 0 ) return false;
		if ( calendar.get( Calendar.SECOND ) != 0 ) return false;

		return true;
	}

	public static FindItemsResults< Appointment > findAppointmentsDelegate( final ExchangeService service, final String email, final Date startDate, final Date endDate ) throws Exception
	{
		final FolderId id = new FolderId(WellKnownFolderName.Calendar, Mailbox.getMailboxFromString( email ) );

		return findAppointments( CalendarFolder.bind( service, id ), startDate, endDate );
	}

	public static FindItemsResults< Appointment > findAppointmentsMainCalendar( final ExchangeService service, final Date startDate, final Date endDate ) throws Exception
	{
		return findAppointments( CalendarFolder.bind( service, WellKnownFolderName.Calendar ), startDate, endDate );
	}

	public static FindItemsResults< Appointment > findAppointments( final CalendarFolder folder, final Date startDate, final Date endDate ) throws Exception
	{
		return folder.findAppointments( new CalendarView( startDate, endDate ) );
	}
}
