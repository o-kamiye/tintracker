package ng.com.tinweb.www.tintracker.data;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ng.com.tinweb.www.tintracker.R;
import ng.com.tinweb.www.tintracker.appConfig.ContextProvider;

/**
 * Created by kamiye on 11/3/15.
 */
public class AddressLookup extends IntentService {

    protected ResultReceiver mReceiver;
    private Context context = ContextProvider.getContext();

    private static final String TAG = "AddressLookup";

    public final class Constants {
        public static final int SUCCESS_RESULT = 0;
        public static final int FAILURE_RESULT = 1;
        public static final String PACKAGE_NAME =
                "ng.com.tinweb.www.tintracker.data";
        public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
        public static final String RESULT_DATA_KEY = PACKAGE_NAME +
                ".RESULT_DATA_KEY";
        public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
                ".LOCATION_DATA_EXTRA";
    }


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public AddressLookup() {
        super(TAG);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String errorMessage = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        // Get the location passed to this service through an extra.
        Location location = intent.getParcelableExtra(
                Constants.LOCATION_DATA_EXTRA);


        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    // In this sample, get just a single address.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = getString(R.string.service_not_available);
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = getString(R.string.invalid_lat_long_used);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found);
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            deliverResultToReceiver(Constants.SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"),
                            addressFragments));
        }
    }

    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        mReceiver.send(resultCode, bundle);
    }
}
