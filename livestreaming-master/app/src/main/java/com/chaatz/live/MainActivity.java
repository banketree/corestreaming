package com.chaatz.live;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.chaatz.live.app.base.RequestPermissionActivity;
import com.chaatz.live.debug.Log;
import com.crashlytics.android.Crashlytics;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.fabric.sdk.android.Fabric;

//import com.chaatz.live.app.data.StreamingHostAdapter;

public class MainActivity extends RequestPermissionActivity implements View.OnClickListener {

    private final static String TAG = "MainActivity";
    private String contentUri = "";
    private Button mLiveNow, mDashExplore, mRtspExplore, mHLSExplore;
    private String serverLink = "";
    private String serverStreamName = "";

    private final static String hostUrlTemplate = "rtsp://dev-ap-wowza.chaatz.com:1935/live/";


    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );


        // for production
       Fabric.with( this, new Crashlytics() );

        Toolbar toolbar = ( Toolbar ) findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        mLiveNow = ( Button ) findViewById( R.id.liveNow );
//        mRtspExplore = ( Button ) findViewById( R.id.rstp_play );
//        mDashExplore = ( Button ) findViewById( R.id.dash_play );
        mHLSExplore = ( Button ) findViewById( R.id.hls_play );
        mLiveNow.setOnClickListener( this );
//        mRtspExplore.setOnClickListener( this );
//        mDashExplore.setOnClickListener( this );
        mHLSExplore.setOnClickListener( this );


//        FloatingActionButton fab = ( FloatingActionButton ) findViewById( R.id.fab );
//        fab.setOnClickListener( new View.OnClickListener() {
//            @Override
//            public void onClick( View view ) {
//                Snackbar.make( view, "Replace with your own action", Snackbar.LENGTH_LONG )
//                        .setAction( "Action", null ).show();
//                startExample3Activity();
//            }
//        } );
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.menu_main, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch ( id ) {
        }
        return super.onOptionsItemSelected( item );
    }

    private boolean validateHostUrl( String hostUrlPrefix, String hostUrl ) {
        boolean isValid = false;
        if ( hostUrl != null ) {
            Pattern uri = Pattern.compile( "rtsp://(.+):(\\d*)/(.+)/(.+)" );
            Matcher matcher = uri.matcher( hostUrlPrefix + hostUrl );
            isValid = matcher.matches();
        }
        return isValid;
    }

    private void startExample3Activity() {
        checkPermission( PERMISSION_REQUEST_CAMERA );
    }

    private void startRSTPPlayerActivity( String hostUrl ) {
        Intent intent = new Intent( this, FastVideoPlayerActivity.class );
        intent.putExtra( "host_url", hostUrl );
        startActivity( intent );
    }

//    private void startDASHPlayerActivity( String contentUri ) {
//        Intent intent = new Intent( this, DASHExoPlayerActivity.class );
//        intent.putExtra( "contentUri", contentUri );
//        startActivity( intent );
//    }

    private void startHLSPlayerActivity( String contentUri ) {
        Intent intent = new Intent( this, HLSExoPlayerActivity.class );
        intent.putExtra( "contentUri", contentUri );
        startActivity( intent );
    }

    private void startVodPlayerActivity( String contentUri ) {
        Intent intent = new Intent( this, VodPlayerActivity.class );
        intent.putExtra( "contentUri", contentUri );
        startActivity( intent );
    }

    @Override
    protected void onPermissionGrated( int requestCode ) {
        if ( PERMISSION_REQUEST_CAMERA == requestCode ) {
            checkPermission( PERMISSION_REQUEST_MIC );
        } else if ( PERMISSION_REQUEST_MIC == requestCode ) {
            checkPermission( PERMISSION_REQUEST_PHONE_STATE );
        } else if ( PERMISSION_REQUEST_PHONE_STATE == requestCode ) {
            Intent intent = new Intent( this, Example3Activity.class );
            startActivity( intent );
        }
    }



    @Override
    protected void onRequestPermissionFail( int requestCode ) {
        //TODO any reject permission dialog and show something to alert the user
    }

    @Override
    public void onClick( View v ) {
        switch ( v.getId() ) {
            case R.id.vod_play: {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder( this );
                dialogBuilder.setCancelable( false );
                LayoutInflater inflater = this.getLayoutInflater();
                View dialogView = inflater.inflate( R.layout.settings_host, null );
                dialogBuilder.setView( dialogView );
                final EditText editText = ( EditText ) dialogView.findViewById( R.id.editTextDialogUserInput );
                dialogBuilder.setPositiveButton( "Enter", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick( DialogInterface dialog, int which ) {
                        serverLink = getResources().getString( R.string.vod_default_stream );
                        serverStreamName = editText.getText().toString();
                        Log.i(TAG, "serverStreamName= "+serverStreamName);
                        if ( !serverLink.equals( null ) && !serverStreamName.equals( "" ) ) {
                            contentUri = serverLink + serverStreamName + ".mp4/playlist.m3u8";
                            startVodPlayerActivity( contentUri );
                        }
                        else {
                            dialog.dismiss();
                            showOnErrorDialog();
                        }
                    }
                } );
                dialogBuilder.setNegativeButton( "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick( DialogInterface dialog, int which ) {
                        dialog.dismiss();
                    }
                } );
                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();
            }
            break;
//            case R.id.dash_play: {
////                Intent intent = new Intent( this, CustomExoPlayerActivity.class );
////                startActivity( intent );
//                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder( this );
//                dialogBuilder.setCancelable( false );
//                LayoutInflater inflater = this.getLayoutInflater();
//                View dialogView = inflater.inflate( R.layout.settings_host, null );
//                dialogBuilder.setView( dialogView );
//                final EditText editText = ( EditText ) dialogView.findViewById( R.id.editTextDialogUserInput );
//                SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences( this );
//                if ( mPrefs.getString( "uri", null ) != null )
//                    Log.d( TAG, "could not find default url" );
////        editText.setText( mPrefs.getString( "uri", getString( R.string.default_stream ) ) );
//                dialogBuilder.setPositiveButton( "Enter", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick( DialogInterface dialog, int which ) {
//                        serverLink = getResources().getString( R.string.dash_default_stream );
//                        serverStreamName = editText.getText().toString();
//                        if ( !serverLink.equals( null ) && !serverStreamName.equals( null ) ) {
////                    hostUrl = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov";
//                            contentUri = serverLink + serverStreamName + "/manifest.mpd";
//                            startDASHPlayerActivity( contentUri );
//                        }
//                    }
//                } );
//                dialogBuilder.setNegativeButton( "Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick( DialogInterface dialog, int which ) {
//                        dialog.dismiss();
//                    }
//                } );
//                AlertDialog alertDialog = dialogBuilder.create();
//                alertDialog.show();
//
//                break;
//            }
            case R.id.liveNow: {
                startExample3Activity();
            }
            break;
            case R.id.hls_play: {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder( this );
                dialogBuilder.setCancelable( false );
                LayoutInflater inflater = this.getLayoutInflater();
                View dialogView = inflater.inflate( R.layout.settings_host, null );
                dialogBuilder.setView( dialogView );
                final EditText editText = ( EditText ) dialogView.findViewById( R.id.editTextDialogUserInput );
                SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences( this );
                if ( mPrefs.getString( "uri", null ) != null )
                    Log.d( TAG, "could not find default url" );
//        editText.setText( mPrefs.getString( "uri", getString( R.string.default_stream ) ) );
                dialogBuilder.setPositiveButton( "Enter", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick( DialogInterface dialog, int which ) {
                        serverLink = getResources().getString( R.string.dash_default_stream );
                        serverStreamName = editText.getText().toString();
                        if ( !serverLink.equals( null ) && !serverStreamName.equals( null ) ) {
//                    hostUrl = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov";
//                                contentUri = serverLink + serverStreamName+"/manifest.mpd" ;
                            contentUri = serverLink + serverStreamName + "/playlist.m3u8";
                            startHLSPlayerActivity( contentUri );
                        }
                    }
                } );
                dialogBuilder.setNegativeButton( "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick( DialogInterface dialog, int which ) {
                        dialog.dismiss();
                    }
                } );
                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();
            }
            break;
            default:
                break;
        }
    }

    private void showOnErrorDialog() {
        AlertDialog.Builder onErrorDialogBuilder = new AlertDialog.Builder( this );
        onErrorDialogBuilder.setCancelable( false );
        LayoutInflater inflater = this.getLayoutInflater();
        View onErrorDialogView = inflater.inflate( R.layout.error_dialog, null );
        onErrorDialogBuilder.setView( onErrorDialogView );
        onErrorDialogBuilder.setMessage( getString( R.string.dialog_error_desc ) );
        onErrorDialogBuilder.setPositiveButton( "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick( DialogInterface dialog, int which ) {
                dialog.dismiss();
            }
        } );
        AlertDialog onErrorDialog = onErrorDialogBuilder.create();
        onErrorDialog.show();
    }
}
