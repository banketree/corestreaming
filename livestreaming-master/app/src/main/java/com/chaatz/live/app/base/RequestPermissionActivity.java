package com.chaatz.live.app.base;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

/**
 * Created by Edmund on 12/22/15.
 */
public abstract class RequestPermissionActivity extends BaseAppCompactActivity {

    private final static String TAG = "CameraActivity";
    protected final int PERMISSION_REQUEST_CAMERA = 100;
    protected final int PERMISSION_REQUEST_MIC = 101;
    protected final int PERMISSION_REQUEST_READ_SMS = 102;
    protected final int PERMISSION_REQUEST_SEND_SMS = 103;
    protected final int PERMISSION_REQUEST_GPS = 104;
    protected final int PERMISSION_REQUEST_PHONE_STATE = 105;

    @Override
    public void onRequestPermissionsResult( int requestCode, String[] permissions, int[] grantResults ) {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults );
        Log.d( TAG, "RequestCode = " + requestCode );
        if ( grantResults[ 0 ] == PackageManager.PERMISSION_GRANTED ) {
            Log.d( TAG, "Access" );
            onPermissionGrated( requestCode );
        } else {
            Log.d( TAG, "Reject" );
            onRequestPermissionFail( requestCode );
        }
    }

    public void checkPermission( final int requestCode ) {
       final String[] permissions =  new String[1];

        switch ( requestCode ){

            case PERMISSION_REQUEST_CAMERA:{
                permissions[0] = Manifest.permission.CAMERA;
                break;
            }

            case PERMISSION_REQUEST_MIC:{
                permissions[0] = Manifest.permission.RECORD_AUDIO;
                break;
            }

            case PERMISSION_REQUEST_READ_SMS:{
                permissions[0] = Manifest.permission.READ_SMS;
                break;
            }


            case PERMISSION_REQUEST_SEND_SMS:{
                permissions[0] = Manifest.permission.SEND_SMS;
                break;
            }

            case PERMISSION_REQUEST_GPS:{
                permissions[0] = Manifest.permission.ACCESS_FINE_LOCATION;
                break;
            }

            case PERMISSION_REQUEST_PHONE_STATE:{
                permissions[0] = Manifest.permission.READ_PHONE_STATE;
                break;
            }

            default:
                throw new RuntimeException( "Please follow the code we defend in header." );

        }

        if ( ActivityCompat.checkSelfPermission( this, permissions[ 0 ] ) == PackageManager.PERMISSION_GRANTED ) {
            onPermissionGrated( requestCode );
        } else {
             ActivityCompat.requestPermissions( this, permissions, requestCode );
        }
    }

    protected abstract void onPermissionGrated( int requestCode );

    protected abstract void onRequestPermissionFail( int requestCode );
}
