package com.vanbran.booklist2;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.dropbox.*;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

public class BookListMainAct extends Activity {
    /** Called when the activity is first created. */
        //setContentView(R.layout.main);
    	DBAdapter db = new DBAdapter(this);
    	EditText titleFld;
    	EditText authorFld;
    	EditText statusFld;
    	TextView counterFld;
    	
    	public static final File newXml = new File(Environment.getExternalStorageDirectory() , "dcandroidexport.xml"); 
    	
    	//DropBox
    	final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER ;
    	private DropboxAPI<AndroidAuthSession> mDBApi;		
    	final static private String APP_KEY = "imd5mawa9ttpi7v";
    	final static private String APP_SECRET = "t6u7e4we1vxnupb";
    	final static private String ACCOUNT_PREFS_NAME = "prefs";
    	final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    	final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    	
    	
    	@Override
        public void onCreate(Bundle savedInstanceState) 
        {
        	try
        	{
        		super.onCreate(savedInstanceState);
        		setContentView(R.layout.main);
        		
        		//Capture buttons from Layout
        		Button insertButton = (Button)findViewById(R.id.Insert); 
        		Button searchButton = (Button)findViewById(R.id.Search);
        		//Register the onClick listener
        		insertButton.setOnClickListener(mAddListenerInsert);
        		searchButton.setOnClickListener(mAddListenerSearch);
        		
        		//Set the counter
        		//counterFld = (TextView) findViewById(R.id.counterVal);
        		//counterFld.setText(countBooks().toString());
        		
        		//Authenticate against DropBox
        		DropBoxAuthenticate();
        	}
        	catch (Exception ex)
        	{
        		Context context = getApplicationContext();
        		CharSequence text = ex.toString();
        		int duration = 50000 ; //Toast.LENGTH_LONG;
        		
        		Toast toast = Toast.makeText(context, text, duration);
        		toast.show();
        	}
        }
        
        @Override
        protected void onResume() {
            super.onResume();

            //Set the counter
    		counterFld = (TextView) findViewById(R.id.counterVal);
    		counterFld.setText(countBooks().toString());

            if (mDBApi.getSession().authenticationSuccessful()) {
                try {
                    // MANDATORY call to complete auth.
                    // Sets the access token on the session
                    mDBApi.getSession().finishAuthentication();

                    AccessTokenPair tokens = mDBApi.getSession().getAccessTokenPair();

                    // Provide your own storeKeys to persist the access token pair
                    // A typical way to store tokens is using SharedPreferences
                    storeKeys(tokens.key, tokens.secret);
                } catch (IllegalStateException e) {
                    Log.i("DbAuthLog", "Error authenticating", e);
                }
            }
        }
        
        //Store the retrieved keys (code from DBRoulette.jar
        private void storeKeys(String key, String secret) {
            // Save the access key for later
            SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, key);
            edit.putString(ACCESS_SECRET_NAME, secret);
            edit.commit();
        }
        
        @Override
    	public boolean onCreateOptionsMenu(Menu menu)
    	{
    		//Inflate the menu
    		MenuInflater menuInf = getMenuInflater();
    		menuInf.inflate(R.menu.menu, menu);
    		return true;
    	}
        
        
        
        //Do something when a menu option is selected
        @Override
        public boolean onOptionsItemSelected(MenuItem item)
        {
        	switch (item.getItemId())
        	{
        		case R.id.load_xml: 
        			try
            		{
            			Intent intent = new Intent(BookListMainAct.this, LoadXML.class);
            			startActivity(intent);
            		}
            		catch(Exception ex)
            		{
            			Context context = getApplicationContext();
            			CharSequence text = ex.toString();
            			int duration = Toast.LENGTH_LONG ;
            			
            			Toast toast = Toast.makeText(context, text, duration);
            			toast.show();
            		}
            		break;
        		case R.id.about: 
        			try
            		{
            			Intent intent = new Intent(BookListMainAct.this, about.class);
            			startActivity(intent);
            		}
            		catch(Exception ex)
            		{
            			Context context = getApplicationContext();
            			CharSequence text = ex.toString();
            			int duration = Toast.LENGTH_LONG ;
            			
            			Toast toast = Toast.makeText(context, text, duration);
            			toast.show();
            		}
            		break;            		
        	}
        	return true;
        }

        //Create an anonymous implementation of OnClickListener for the insert
        private OnClickListener mAddListenerInsert = new OnClickListener() {
        	public void onClick(View v){
        		long id = 0;
        		//Do something when the button is clicked
        		db.open();
        		try{
        			String dummyId = "dummy";
        			titleFld = (EditText)findViewById(R.id.TitleVal);
        			authorFld = (EditText)findViewById(R.id.AuthorVal);
        			statusFld = (EditText)findViewById(R.id.StatusVal);
        			
        			id = db.insertBook(authorFld.getText().toString(),
        								titleFld.getText().toString(),
        							   statusFld.getText().toString(), dummyId);
        			
        			//Clear the fields
        			titleFld.setText("");
        			authorFld.setText("");
        			statusFld.setText("");
        			titleFld.requestFocus();        			
        		}
        		catch(Exception ex)
        		{
        			Context context = getApplicationContext();
        			CharSequence text = ex.toString() + "ID: " + id;
        			int duration = Toast.LENGTH_LONG ;
        			
        			Toast toast = Toast.makeText(context, text, duration);
        			toast.show();
        		}
        		
        		db.close();
        	
        	}
        };

        //Create an anonymous implementation of OnClickListener for the Search
        private OnClickListener mAddListenerSearch = new OnClickListener() {
        	public void onClick(View v){

        		String titleStr;
        		String authorStr;
        		String statusStr;
        		
        		//Do something when the button is clicked
        		db.open();
        		try{
        			titleFld = (EditText)findViewById(R.id.TitleVal);
        			authorFld = (EditText)findViewById(R.id.AuthorVal);
        			statusFld = (EditText)findViewById(R.id.StatusVal);
        		
        			titleStr = titleFld.getText().toString() ;
        			authorStr = authorFld.getText().toString();
        			statusStr = statusFld.getText().toString();
        		
        			if (titleStr.length() == 0)
        			{
        				titleStr = "'%'";
        			}else
        			{
        				titleStr = "'" + titleStr + "'";
        			}
        			if (authorStr.length() == 0)
        			{
        				authorStr = "'%'";
        			}else
        			{
        				authorStr = "'" + titleStr + "'";
        			}
        			if(statusStr.length() == 0)
        			{
        				statusStr = "'%'";
        			}else
        			{
        				statusStr = "'" + titleStr + "'";
        			}
        				
        			Intent intent = new Intent(BookListMainAct.this, ShowList.class);
        			intent.putExtra("title", titleStr);
        			intent.putExtra("author", authorStr);
        			intent.putExtra("status", statusStr);
        			startActivity(intent);
        		}
        		catch(Exception ex)
        		{
        			Context context = getApplicationContext();
        			CharSequence text = ex.toString() ;
        			int duration = 500000; //Toast.LENGTH_LONG ;
        			
        			Toast toast = Toast.makeText(context, text, duration);
        			toast.show();
        		}
        		db.close();
        	}
        };
        
        private Integer countBooks()
    	{
    		Integer counted = 0;
    		//Count the number of records so we can show the counter in the screen
    		try
    		{
    			db.open();
    			counted = db.countBooks();
    			db.close();
    		}    			
    		catch(Exception ex)
    		{
        		Context context = getApplicationContext();
        		CharSequence text = ex.toString();
        		int duration = 50000 ; //Toast.LENGTH_LONG;
        		
        		Toast toast = Toast.makeText(context, text, duration);
        		toast.show();
        	}
    		return counted;
    	}
        
        private void DropBoxAuthenticate()
        {
        	AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        	AndroidAuthSession session = new AndroidAuthSession(appKeys, ACCESS_TYPE);
        	mDBApi = new DropboxAPI<AndroidAuthSession>(session);

        	//Start the Authentication
        	mDBApi.getSession().startAuthentication(BookListMainAct.this);
        
        }
}
