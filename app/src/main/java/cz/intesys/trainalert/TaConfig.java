package cz.intesys.trainalert;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import cz.intesys.trainalert.entity.Location;
import cz.intesys.trainalert.repository.PostgreSqlRepository;
import cz.intesys.trainalert.repository.Repository;
import cz.intesys.trainalert.repository.SimulatedRepository;

import cz.intesys.trainalert.R;

public class TaConfig {
    public static final int TRIP_STATUS_TIME_INTERVAL = 3000; // GetTripStatus service polling.
    public static final int TRIP_TIME_INTERVAL = 5000; // GetNextStops, GetPreviousStops, GetFinalStop services polling.
    public static final int GET_POIS_INTERVAL = 300000; // GetPois service polling (5min.).
    public static final int MAP_MOVEMENT_INTERVAL = 10000;

    public static final int OSMDROID_ANIMATION_DURATION = 1000; // This is taken from osmdroid library, cannot change

    public static final int LOCATION_UPDATE_ANIMATION = BuildConfig.GPS_TIME_INTERVAL;

    public static final int SERVER_TIMEOUT = 30000; // In miliseconds.
    public static final int GPS_TIMEOUT_DELAY = 15000; // Time interval of server inactivity which invoke loader animation
    public static final int LOCATION_INITIAL_DELAY = 5000;
    public static final int TRIP_FRAGMENT_TIME_PADDING = 5000; // How much time TripFragment should be shown after finish animation has ended.

    public static final int MAP_DEFAULT_ZOOM = 15; //

    public static final Repository REPOSITORY = (BuildConfig.USE_SIMULATED_REPOSITORY) ? SimulatedRepository.getInstance() : PostgreSqlRepository.getInstance();
    public static final Location DEFAULT_LOCATION = new Location(50.48365189588503, 14.039404579177328);
    public static final boolean OSMDROID_DEBUGGING = true;

    public static final String BASIC_DATE_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss"; // Used in PostgreSQL database and as basic string format for java.util.Date.
    public static final String ARRIVAL_DATE_FORMAT_STRING = "HH:mm"; // Used to display arrival of train.

    public static final SimpleDateFormat BASIC_DATE_FORMAT = new SimpleDateFormat(BASIC_DATE_FORMAT_STRING);
    public static final SimpleDateFormat ARRIVAL_DATE_FORMAT = new SimpleDateFormat(ARRIVAL_DATE_FORMAT_STRING);

    public static final int TRIP_FRAGMENT_PREVIOUS_STOP_COUNT = 1;
    public static final int TRIP_FRAGMENT_NEXT_STOP_COUNT = 2;


    public static final int ADMINISTRATOR_PASSWORD = 2003;

    public static final int[] SIMULATED_REPOSITORY_RESPONSE_DELAY_RANGE = {50, 500}; // [0, 0] or [x, y] where x, y > 0
    public static final int[] SIMULATED_REPOSITORY_GET_TRIPS_RESPONSE_DELAY_RANGE = {50, 500};
    public static final int[] SIMULATED_REPOSITORY_SET_TRIP_RESPONSE_DELAY_RANGE = {50, 500}; // [0, 0] or [x, y] where x, y > 0

    public static final long UPDATE_INTERVAL = 5;
    public static final TimeUnit UPDATE_INTERVAL_UNIT = TimeUnit.MINUTES;
}