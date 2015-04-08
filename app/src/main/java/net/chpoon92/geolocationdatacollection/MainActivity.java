package net.chpoon92.geolocationdatacollection;

        import android.content.Context;
        import android.content.Intent;
        import android.location.Criteria;
        import android.location.Location;
        import android.location.LocationListener;
        import android.location.LocationManager;
        import android.os.Environment;
        import android.provider.Settings;
        import android.support.v7.app.ActionBarActivity;
        import android.os.Bundle;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.widget.Button;
        import android.widget.CheckBox;
        import android.widget.EditText;
        import android.widget.TextView;
        import android.widget.Toast;

        import java.io.BufferedWriter;
        import java.io.File;
        import java.io.FileNotFoundException;
        import java.io.FileWriter;
        import java.io.IOException;

public class MainActivity extends ActionBarActivity {

  private double mLatitude;
  private double mLongitude;
  private double mAltitude;

  private Button choose;
  private Button save;
  private CheckBox fineAcc;
  private Criteria mCriteria;
  private LocationManager mLocationManager;
  private MyLocationListener mMyLocationListener;
  private String mProvider;
  private EditText locationText;
  private TextView choice;
  private TextView provText;
  private TextView latitude;
  private TextView longitude;
  private TextView altitude;
  private File file;
  private BufferedWriter writer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    locationText = (EditText) findViewById(R.id.locationName);
    latitude = (TextView) findViewById(R.id.lat);
    longitude = (TextView) findViewById(R.id.lon);
    altitude = (TextView) findViewById(R.id.alt);
    provText = (TextView) findViewById(R.id.prov);
    choice = (TextView) findViewById(R.id.choice);
    fineAcc = (CheckBox) findViewById(R.id.fineAccuracy);
    choose = (Button) findViewById(R.id.chooseRadio);
    save = (Button) findViewById(R.id.savebtn);
    file = new File(((Context)this).getExternalFilesDir(null), "data.txt");

    try {
      if (!file.exists())
        file.createNewFile();
      writer = new BufferedWriter(new FileWriter(file, true /*append*/));
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Acquire a reference to the system Location Manager
    mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

    // Define the criteria how to select the location provider
    mCriteria = new Criteria();
    mCriteria.setAccuracy(Criteria.ACCURACY_FINE); // Default value

    // User defines the criteria
    choose.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if(fineAcc.isChecked()){
          mCriteria.setAccuracy(Criteria.ACCURACY_FINE);
          choice.setText("fine accuracy selected");
        }else {
          mCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
          choice.setText("coarse accuracy selected");
        }
      }
    });

    // Get the best provider depending on the criteria
    mProvider = mLocationManager.getBestProvider(mCriteria, false);

    // The last known location of this provider
    Location location = mLocationManager.getLastKnownLocation(mProvider);

    mMyLocationListener = new MyLocationListener();

    if (location != null) {
      mMyLocationListener.onLocationChanged(location);
    } else {
      // leads to the settings because there is no last known location
      Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
      startActivity(intent);
    }

    // Register the listener with the Location Manager to receive location updates
    mLocationManager.requestLocationUpdates(mProvider, 0, 0,
            mMyLocationListener);

    // Save data
    save.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        try {
          writer.write(locationText.getText() + "\t\t\t" + String.valueOf(mLatitude) + "\t\t\t"
                  + Double.valueOf(mLongitude) + "\t\t\t" + String.valueOf(mAltitude) + '\n');
          writer.flush();
          Toast.makeText(MainActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  // Define a listener that responds to location updates
  private class MyLocationListener implements LocationListener {
    @Override
    public void onLocationChanged(Location location) {
      mLatitude = location.getLatitude();
      mLongitude = location.getLongitude();
      mAltitude = location.getAltitude();
      latitude.setText("Latitude: " + String.valueOf(location.getLatitude()));
      longitude.setText("Longitude: " + String.valueOf(location.getLongitude()));
      if(location.hasAltitude())
        altitude.setText("Altitude: " + String.valueOf(location.getAltitude()));
      else
        altitude.setText("Altitude is not supported");

      provText.setText(mProvider + " provider has been selected.");
      Toast.makeText(MainActivity.this, "Location changed!",
              Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
      Toast.makeText(MainActivity.this, provider + "'s status changed to " + status + "!",
              Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
      Toast.makeText(MainActivity.this, "Provider " + provider + " enabled!",
              Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
      Toast.makeText(MainActivity.this, "Provider " + provider + " disabled!",
              Toast.LENGTH_SHORT).show();
    }
  }
}
