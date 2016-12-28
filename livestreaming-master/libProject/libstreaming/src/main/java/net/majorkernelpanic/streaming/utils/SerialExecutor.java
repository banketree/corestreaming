package net.majorkernelpanic.streaming.utils;

/**
 * Created by ericshe on 5/1/2016.
 */

import android.os.Looper;
import android.util.Log;

import java.util.ArrayDeque;
import java.util.concurrent.Executor;

/**
 * Schedule and executes events in order. Always wait for previous event to finish.
 */
public class SerialExecutor implements Executor
{
    private String TAG = "SerialExecutor";
    final ArrayDeque<Runnable> tasks = new ArrayDeque<>();
    final Executor executor;
    Runnable active;
    private volatile boolean mIsRunning = true;

    private class SerialRunnable implements Runnable
    {
        Runnable r;

        public SerialRunnable( Runnable r )
        {
            this.r = r;
        }

        @Override
        public void run()
        {
            if( !mIsRunning ){
                Log.d( TAG, "Thread pool shout down stop the next execution." );
                return;
            }
            try {
                Thread t = Thread.currentThread();
                Log.d( TAG, "<" + t.getName() + ">id:" + t.getId() + ", Priority:" + t.getPriority() + ", Group:" + t.getThreadGroup().getName() );
                if ( Looper.myLooper() == Looper.getMainLooper() ) {
                    Log.w( TAG, "warning: on main thread" );
                }
                if ( null != r ) {
                    r.run();
                }
            }
            finally {
              scheduleNext();
            }
        }
    }

    public SerialExecutor( Executor executor )
    {
        this.executor = executor;
    }

    public synchronized void executeFirst( Runnable r )
    {
        tasks.offerFirst( new SerialRunnable( r ) );
        if ( active == null ) {
            scheduleNext();
        }
    }

    public synchronized void executeLast( Runnable r )
    {
        tasks.offerLast( new SerialRunnable( r ) );
        if ( active == null ) {
            scheduleNext();
        }
    }

    @Override
    public void execute( Runnable r )
    {
        executeLast( r );
    }

    protected synchronized void scheduleNext()
    {
        if ( ( active = tasks.poll() ) != null ) {
            executor.execute( active );
        }
    }

    public void shoutDown(){
        Log.d( TAG, "Thread pool shout down." );
        this.mIsRunning = false;
        tasks.clear();
    }


    /**
     * Executes events on a new thread aside from the main thread.
     */
    public static class ThreadExecutor implements Executor
    {
        public void execute( Runnable r )
        {
            new Thread( r ).start();
        }
    }
}