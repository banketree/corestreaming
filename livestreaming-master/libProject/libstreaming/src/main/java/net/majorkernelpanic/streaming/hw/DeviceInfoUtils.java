package net.majorkernelpanic.streaming.hw;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ericshe on 19/1/2016.
 */
public class DeviceInfoUtils {

    public static List< String > getCpuInfo() {
        try {
            ArrayList< String > list = new ArrayList();
            FileReader fr = new FileReader( "/proc/cpuinfo" );
            BufferedReader br = new BufferedReader( fr );
            for ( String line = br.readLine(); line != null; line = br.readLine() ) {
                list.add( line );
            }
            br.close();
            return list;
        }
        catch ( FileNotFoundException e ) {
            e.printStackTrace();
        }
        catch ( IOException e ) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getCpuName( List< String > cpuInfo ) {
        if ( null != cpuInfo && !cpuInfo.isEmpty() ) {
            for ( String info : cpuInfo ) {
                if ( !TextUtils.isEmpty( info ) ) {
                    if ( info.contains( "Hardware" ) ) {
                        String[] array = info.split( ":\\s+", 2 );
                        if ( array.length >= 2 ) {
                            return array[ 1 ];
                        }
                    }
                }
            }
        }
        return "";
    }

    public static boolean isQualcommCpu( String name ) {
        if ( !TextUtils.isEmpty( name ) ) {
            // Qualcomm cpu have  3 type naming convention for checking
            Pattern pattern1 = Pattern.compile( "(MSM)(\\d{4})" );
            Pattern pattern2 = Pattern.compile( "(APQ)(\\d{4})" );
            Pattern pattern3 = Pattern.compile( "(MPQ)(\\d{4})" );
            Matcher matcher1 = pattern1.matcher( name );
            Matcher matcher2 = pattern2.matcher( name );
            Matcher matcher3 = pattern3.matcher( name );
            return name.contains( "Qualcomm" ) || name.contains( "QCT" ) || matcher1.find() || matcher2.find() || matcher3.find();
        }
        return false;
    }

    public static boolean isMtkCpu( String name ) {
        if ( !TextUtils.isEmpty( name ) ) {
            Pattern pattern = Pattern.compile( "(MT)(\\d{4})" );
            Matcher m = pattern.matcher( name );
            return name.startsWith( "MT" ) || m.find();
        }
        return false;
    }

    public static boolean isSamsungCpu( String name ) {
        if ( !TextUtils.isEmpty( name ) ) {
            Pattern pattern = Pattern.compile( "(Exynos)(\\d{4})" );
            Matcher m = pattern.matcher( name );
            return name.startsWith( "SAMSUNG" ) || name.contains( "Exynos" ) || m.find();
        }
        return false;
    }

}
