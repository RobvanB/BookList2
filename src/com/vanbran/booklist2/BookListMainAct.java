/*
Developed by Rob vanBrandenburg (RobvanB@gmail.com)
More info can be found at the Project page (https://github.com/RobvanB/BookList2)
Check out the Wiki for documentation. 
Please report bugs and feature requests. 
Icons provided by AndroidIcons.
*/

package com.vanbran.booklist2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

public class BookListMainAct extends Activity {
    /** Called when the activity is first created. */
	
	DBAdapter db = new DBAdapter(this);
	EditText titleFld;
	EditText authorFld;
	EditText statusFld;
	TextView counterFld;
	
	public 	static final   File 	newXml 			= new File(Environment.getExternalStorageDirectory() , "dcandroidexport.xml"); 
	final 	static private String 	dbPath 			= "/dcexport.xml";
	final 	static private int 		tstDuration 	= 70000 ; 
	
	boolean							srchPreChecked	= false ;
	boolean							srchPostChecked = false ;
	
	//DropBox
	final static private AccessType ACCESS_TYPE 		= AccessType.APP_FOLDER ;
	final static private String 	ACCOUNT_PREFS_NAME 	= "prefs";
	final static private String 	ACCESS_KEY_NAME 	= "ACCESS_KEY";
	final static private String 	ACCESS_SECRET_NAME 	= "ACCESS_SECRET";
		  static private String 	APPKEY				= "";
		  static private String 	APPSEC				= "";
	
	private DropboxAPI<AndroidAuthSession> mDBApi;
    	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	try
    	{
    		super.onCreate(savedInstanceState);
    		setContentView(R.layout.main);
    		
    		//Get the appkey and secret
    		LoadConfig LoadC = new LoadConfig();
    		LoadC.init(getApplicationContext());
    		APPKEY = LoadC.AppKey();
    		APPSEC = LoadC.AppSec();    		
    		
    		//Capture buttons from Layout
    		Button searchButton = (Button)findViewById(R.id.Search);
    		//Register the onClick listener
    		searchButton.setOnClickListener(mAddListenerSearch);
    		
    		Button getFileButton = (Button)findViewById(R.id.GetFile);
    		//Register the onClick listener
    		getFileButton.setOnClickListener(mAddListenerGetFile);
    		
    		//Authenticate against DropBox
    		DropBoxAuthenticate();
    		
    	}
    	catch (Exception ex)
    	{
    		Context context = getApplicationContext();
    		CharSequence text = ex.toString();
    		    		
    		Toast toast = Toast.makeText(context, text, tstDuration);
    		toast.show();
    	}
    }
    
    @Override
    protected void onResume() {
        super.onResume();

        //Set the counter
		counterFld = (TextView) findViewById(R.id.counterVal);
		counterFld.setText(countBooks().toString());

        //DropBox
		if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                // MANDATORY call to complete auth.
                // Sets the access token on the session
                mDBApi.getSession().finishAuthentication();

                AccessTokenPair tokens = mDBApi.getSession().getAccessTokenPair();

                // Store the keys
                storeKeys(tokens.key, tokens.secret);
            } 
            catch (IllegalStateException ex) 
            {
               	Context context = getApplicationContext();
        		CharSequence text = ex.toString();
        	        		
        		Toast toast = Toast.makeText(context, "Authentication Error: " + text, tstDuration);
        		toast.show();
            }
        }
    }
    
    //Inflate the menu
    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
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
	    	case R.id.RemoveKeys: 
				try
	    		{
					removeKeys();
					Toast toast = Toast.makeText(getApplicationContext(), "Access Keys removed", tstDuration);
					toast.show();
						
	    		}
	    		catch(Exception ex)
	    		{
	    			Context context = getApplicationContext();
	    			CharSequence text = ex.toString();
	    			        			
	    			Toast toast = Toast.makeText(context, text, tstDuration);
	    			toast.show();
	    		}	
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
        			        			
        			Toast toast = Toast.makeText(context, text, tstDuration);
        			toast.show();
        		}
        		break;            		
    	}
    	return true;
    }
    
    //Create an anonymous implementation of OnClickListener for the Search
    private OnClickListener mAddListenerSearch = new OnClickListener() {
    	public void onClick(View v){

    		String titleStr;
    		String authorStr;
    		String statusStr;
    		
    		//Do something when the button is clicked
    		db.open();
    		try{
    			titleFld 	= (EditText)findViewById(R.id.TitleVal);
    			authorFld 	= (EditText)findViewById(R.id.AuthorVal);
    			statusFld 	= (EditText)findViewById(R.id.StatusVal);
    		
    			titleStr 	= titleFld.getText().toString() ;
    			authorStr 	= authorFld.getText().toString();
    			statusStr 	= statusFld.getText().toString();
    		
    			if (srchPreChecked)
				{
					titleStr 	= "%" + titleStr  ;
					authorStr 	= "%" + authorStr ;
					statusStr 	= "%" + statusStr ;
				}
    			if (srchPostChecked)
				{
					titleStr 	= titleStr  + "%";
					authorStr 	= authorStr + "%";
					statusStr 	= statusStr + "%";
				}
    			
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
    				authorStr = "'" + authorStr + "'";
    			}
    			if(statusStr.length() == 0)
    			{
    				statusStr = "'%'";
    			}else
    			{
    				statusStr = "'" + statusStr + "'";
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
    			    			
    			Toast toast = Toast.makeText(context, text, tstDuration);
    			toast.show();
    		}
    		db.close();
    	}
    };
    
    //Handle the checkbox settings for the search
    public void onCheckboxClicked(View v)
    {
    	boolean checked = ((CheckBox) v).isChecked() ;
    	switch(v.getId())
    	{
    		case R.id.srchPre:
    			srchPreChecked = checked ;
    			break;
    		case R.id.srchPost:
    			srchPostChecked = checked ;
    			break;
    	}
    }
    
    //Create an anonymous implementation of OnClickListener for getting an updated XML file
    private OnClickListener mAddListenerGetFile = new OnClickListener() {
	
    	public void onClick(View v){
    		new getFile().execute();
    		}
    	};

	//Count the number of books in the DB
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
    		    		
    		Toast toast = Toast.makeText(context, text, tstDuration);
    		toast.show();
    	}
		return counted;
	}
        
    //DropBox
    private void DropBoxAuthenticate()
    {
    	SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
    	String tokenKey 		= prefs.getString(ACCESS_KEY_NAME, "");
    	String tokenSecret 		= prefs.getString(ACCESS_SECRET_NAME, "");
    	
    	//Create a session
    	AppKeyPair appKeys = new AppKeyPair(APPKEY, APPSEC);
    	AndroidAuthSession session = new AndroidAuthSession(appKeys, ACCESS_TYPE);
    	mDBApi = new DropboxAPI<AndroidAuthSession>(session);
    	
    	//See if we have stored the accesstokens from a previous session
    	if (tokenKey != "" && tokenSecret != "")
    	{
    		AccessTokenPair tokens 		= new AccessTokenPair(tokenKey, tokenSecret);
    		mDBApi.getSession().setAccessTokenPair(tokens);
    	}
    	
    	//check if we are already authenticated
    	if (session.getAccessTokenPair() == null)
    	{
    		//Start the Authentication
    		mDBApi.getSession().startAuthentication(BookListMainAct.this);
    	}
    }
  
    //Store the retrieved access keys (code from DBRoulette.jar example) - DropBox
    private void storeKeys(String key, String secret) 
    {
        // Save the access key for later
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.putString(ACCESS_KEY_NAME, key);
        edit.putString(ACCESS_SECRET_NAME, secret);
        edit.commit();
    }
    
    //Remove stored access keys
    private void removeKeys() 
    {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.remove(ACCESS_KEY_NAME);
        edit.remove(ACCESS_SECRET_NAME);
        edit.commit();
        
        Context context = getApplicationContext();
		CharSequence text = "App keys cleared";
		int duration = 60000 ; 
		
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
		
		//Restart the main activity
		Intent i = getApplicationContext().getPackageManager().getLaunchIntentForPackage(getApplicationContext().getPackageName());
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK );
		startActivity(i);
    }
     
    // *** SubClass ***
    //Pull the file from the dropbox server 
  	public class getFile extends AsyncTask<Void, Integer, Boolean> 
  	{
  		private ProgressDialog 	dbxDialog 	= new ProgressDialog(BookListMainAct.this);
  		public  ProgressDialog 	xmlDialog 	= new ProgressDialog(BookListMainAct.this);
  		Exception 			   	err 		= null;
  		DropboxFileInfo 		info 		= null;
  		String					msg			= null;
  		
  		protected void onPreExecute()
  		{
  			super.onPreExecute();
  	        this.dbxDialog.setMessage("Pulling file from DropBox server...");
  	        this.dbxDialog.show();    
  		}
  		
  		protected void onPostExecute(Boolean result)
   		{
  			if (this.err != null)
  			{
	  			CharSequence text = this.err.toString() ;
	  			Toast toast 	  = Toast.makeText(getApplicationContext(), text, BookListMainAct.tstDuration);
				toast.show();
  			}else
  			{
				//Show a status dialog when we are done
  				AlertDialog.Builder builder = new AlertDialog.Builder(BookListMainAct.this);
				builder.setMessage(msg).setCancelable(false);
			    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() 
			    {
			    	public void onClick(DialogInterface dialog, int id) 
		           {
		        	   //BookListMainAct.this.finish(); //Don't need to do anything when the user clicks 'ok'
		           }
			    });
				AlertDialog alert = builder.create();		
				alert.show();
  			}
   		}	
  		
  		protected void onProgressUpdate(Integer... progress) 
   		{
   			//Change the message in the running dialog to show second step is started
  			this.dbxDialog.setMessage("Loading new file in DataBase...");
  		}

  		protected Boolean doInBackground(Void... params) 
  		{
  			// Get a new XML file to load in the db
  			FileOutputStream outputStream = null;

  			try 
  	 		{
  		    	outputStream = new FileOutputStream(newXml);
  		    	//Since we are using access_type=folder, the file will be in 
  		    	// /Apps/BookList2 (which is considered the root for this application)
  	 		    info = mDBApi.getFile(dbPath, null, outputStream, null);
  		    	publishProgress();
  		    	
	   			Context context = getApplicationContext();
	   			Toast toast		= null ;
	   			
	   			if (this.err == null)
	   			{
   					//Now load the file into the DB
		   			try
	   	    		{
	   					LoadXML loadXML = new LoadXML() ;
	   					msg = loadXML.run(getApplicationContext());
	   	    		}
	   	    		catch(Exception ex)
	   	    		{
	   	    			CharSequence errText 	= ex.toString();
	   	    			toast 					= Toast.makeText(context, errText, BookListMainAct.tstDuration);
	   	    			toast.show();
	   	    		}
	   	     		finally
	   	    		{
		   	     		if (dbxDialog.isShowing())
		 				{
		   	 		    	dbxDialog.dismiss();
		 				}
	   	    		}
	   			}
	   			publishProgress();
  	 		} 
  	 		catch (Exception ex)	
  	 		{
  	 			this.err = ex ; //Can't use Toast as we are in a background thread - pull the error when we are back in the main UI thread
  	 		} 
  			finally 
  	 		{
  	 			if (dbxDialog.isShowing())
  	 			{
  	 				dbxDialog.dismiss();
  	 			}
  	 		    if (outputStream != null) 
  	 		    {
  	 		        try 
  	 		        {
  	 		            outputStream.close();
  	 		        } catch (IOException e) {
  	 		        	this.err = e;//Can't use Toast as we are in a background thread - pull the error when we are back in the main UI thread
  	 		        }
  	 		    }
  	 		}	
  	 		return true;
  		}
  	}
}
