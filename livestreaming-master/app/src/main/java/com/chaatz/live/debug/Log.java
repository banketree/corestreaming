package com.chaatz.live.debug;

public class Log
{
	public static final int NONE = 100;
	public static final int VERBOSE = 2;
	public static final int DEBUG = 3;
	public static final int INFO = 4;
	public static final int WARN = 5;
	public static final int ERROR = 6;
	public static final int ALL = 1;

	private static int debugLevel = ALL;

	public static void setDebugLevel( int level )
	{
		debugLevel = level;
	}

	public static void v( String tag, Object a )
	{
		logVerbose( tag, a );
	}

	public static void v( String tag, Object a, Object b )
	{
		logVerbose( tag, a, b );
	}

	public static void v( String tag, Object a, Object b, Object c )
	{
		logVerbose( tag, a, b, c );
	}

	public static void v( String tag, Object a, Object b, Object c, Object d )
	{
		logVerbose( tag, a, b, c, d );
	}

	public static void v( String tag, Object a, Object b, Object c, Object d, Object e )
	{
		logVerbose( tag, a, b, c, d, e );
	}

	public static void v( String tag, Object a, Object b, Object c, Object d, Object e, Object f )
	{
		logVerbose( tag, a, b, c, d, e, f );
	}

	public static void v( String tag, Object a, Object b, Object c, Object d, Object e, Object f, Object g )
	{
		logVerbose( tag, a, b, c, d, e, f, g );
	}

	public static void d( String tag, Object a )
	{
		logDebug( tag, a );
	}

	public static void d( String tag, Object a, Object b )
	{
		logDebug( tag, a, b );
	}

	public static void d( String tag, Object a, Object b, Object c )
	{
		logDebug( tag, a, b, c );
	}

	public static void d( String tag, Object a, Object b, Object c, Object d )
	{
		logDebug( tag, a, b, c, d );
	}

	public static void d( String tag, Object a, Object b, Object c, Object d, Object e )
	{
		logDebug( tag, a, b, c, d, e );
	}

	public static void d( String tag, Object a, Object b, Object c, Object d, Object e, Object f )
	{
		logDebug( tag, a, b, c, d, e, f );
	}

	public static void d( String tag, Object a, Object b, Object c, Object d, Object e, Object f, Object g )
	{
		logDebug( tag, a, b, c, d, e, f, g );
	}

	public static void d( String tag, Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h )
	{
		logDebug( tag, a, b, c, d, e, f, g, h );
	}

	public static void d( String tag, Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h, Object i )
	{
		logDebug( tag, a, b, c, d, e, f, g, h, i );
	}

	public static void d( String tag, Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h, Object i, Object j )
	{
		logDebug( tag, a, b, c, d, e, f, g, h, i, j );
	}

	public static void i( String tag, Object a )
	{
		logInfo( tag, a );
	}

	public static void i( String tag, Object a, Object b )
	{
		logInfo( tag, a, b );
	}

	public static void i( String tag, Object a, Object b, Object c )
	{
		logInfo( tag, a, b, c );
	}

	public static void i( String tag, Object a, Object b, Object c, Object d )
	{
		logInfo( tag, a, b, c, d );
	}

	public static void i( String tag, Object a, Object b, Object c, Object d, Object e )
	{
		logInfo( tag, a, b, c, d, e );
	}

	public static void i( String tag, Object a, Object b, Object c, Object d, Object e, Object f )
	{
		logInfo( tag, a, b, c, d, e, f );
	}

	public static void i( String tag, Object a, Object b, Object c, Object d, Object e, Object f, Object g )
	{
		logInfo( tag, a, b, c, d, e, f, g );
	}

	public static void i( String tag, Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h )
	{
		logInfo( tag, a, b, c, d, e, f, g, h );
	}

	public static void i( String tag, Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h, Object i )
	{
		logInfo( tag, a, b, c, d, e, f, g, h, i );
	}

	public static void i( String tag, Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h, Object i, Object j )
	{
		logInfo( tag, a, b, c, d, e, f, g, h, i, j );
	}

	public static void w( String tag, Object a )
	{
		logWarn( tag, a );
	}

	public static void w( String tag, Object a, Object b )
	{
		logWarn( tag, a, b );
	}

	public static void w( String tag, Object a, Object b, Object c )
	{
		logWarn( tag, a, b, c );
	}

	public static void w( String tag, Object a, Object b, Object c, Object d )
	{
		logWarn( tag, a, b, c, d );
	}

	public static void w( String tag, Object a, Object b, Object c, Object d, Object e )
	{
		logWarn( tag, a, b, c, d, e );
	}

	public static void w( String tag, Object a, Object b, Object c, Object d, Object e, Object f )
	{
		logWarn( tag, a, b, c, d, e, f );
	}

	public static void w( String tag, Object a, Object b, Object c, Object d, Object e, Object f, Object g )
	{
		logWarn( tag, a, b, c, d, e, f, g );
	}

	public static void w( String tag, Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h )
	{
		logWarn( tag, a, b, c, d, e, f, g, h );
	}

	public static void e( String tag, Object a )
	{
		logError( tag, a );
	}

	public static void e( String tag, Object a, Object b )
	{
		logError( tag, a, b );
	}

	public static void e( String tag, Object a, Object b, Object c )
	{
		logError( tag, a, b, c );
	}

	public static void e( String tag, Object a, Object b, Object c, Object d )
	{
		logError( tag, a, b, c, d );
	}

	public static void e( String tag, Object a, Object b, Object c, Object d, Object e )
	{
		logError( tag, a, b, c, d, e );
	}

	public static void e( String tag, Object a, Object b, Object c, Object d, Object e, Object f )
	{
		logError( tag, a, b, c, d, e, f );
	}

	public static void e( String tag, Object a, Object b, Object c, Object d, Object e, Object f, Object g )
	{
		logError( tag, a, b, c, d, e, f, g );
	}

	public static void e( String tag, Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h )
	{
		logError( tag, a, b, c, d, e, f, g, h );
	}

	public static void e( String tag, Object a, Object b, Object c, Object d, Object e, Object f, Object g, Object h, Object i )
	{
		logError( tag, a, b, c, d, e, f, g, h, i );
	}

	public static void eFormat( String tag, String format, Object a, Object b )
	{
		logError( tag, String.format( format, a, b ) );
	}

	public static void eFormat( String tag, String format, Object a, Object b, Object c )
	{
		logError( tag, String.format( format, a, b, c ) );
	}

	public static void eFormat( String tag, String format, Object a, Object b, Object c, Object d )
	{
		logError( tag, String.format( format, a, b, c, d ) );
	}

	private static void logVerbose( String tag, Object... args )
	{
		if ( debugLevel > VERBOSE ) {
			return;
		}
		StringBuilder string = new StringBuilder();
		for ( Object object : args ) {
			string.append( null == object ? "(null)" : object.toString() );
		}
		android.util.Log.v( tag, string.toString() );

	}

	private static void logDebug( String tag, Object... args )
	{
		if ( debugLevel > DEBUG ) {
			return;
		}
		StringBuilder string = new StringBuilder();
		for ( Object object : args ) {
			string.append( null == object ? "(null)" : object.toString() );
		}
		android.util.Log.d( tag, string.toString() );

	}

	private static void logInfo( String tag, Object... args )
	{
		if ( debugLevel > INFO ) {
			return;
		}
		StringBuilder string = new StringBuilder();
		for ( Object object : args ) {
			string.append( null == object ? "(null)" : object.toString() );
		}
		android.util.Log.i( tag, string.toString() );
	}

	private static void logWarn( String tag, Object... args )
	{
		if ( debugLevel > WARN ) {
			return;
		}
		StringBuilder string = new StringBuilder();
		for ( Object object : args ) {
			string.append( null == object ? "(null)" : object.toString() );
		}
		android.util.Log.w( tag, string.toString() );

	}

	private static void logError( String tag, Object... args )
	{
		if ( debugLevel > ERROR ) {
			return;
		}
		StringBuilder string = new StringBuilder();
		for ( Object object : args ) {
			string.append( null == object ? "(null)" : object.toString() );
		}
		android.util.Log.e( tag, string.toString() );

	}

}
