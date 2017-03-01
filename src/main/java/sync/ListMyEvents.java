package sync;

import java.util.ArrayList;
import java.util.Date;

public interface ListMyEvents< O >
{
	ArrayList< MyEvent< O > > allEvents( final Date startDate, final Date endDate );
}
