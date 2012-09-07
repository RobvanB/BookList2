package com.vanbran.booklist2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.DropBoxManager.Entry;
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
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
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
	
	protected ProgressDialog pd;
	
	public static final File newXml = new File(Environment.getExternalStorageDirectory() , "dcandroidexport.xml"); 
	final static private String dbPath 	= "/dcexport.xml";
	
	//DropBox
	final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER ;
	static private String APP_KEY = "imd5mawa9ttpi7v";
	static private String APP_SECRET = "t6u7e4we1vxnupb";
	final static private String ACCOUNT_PREFS_NAME = "prefs";
	final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
	final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
	private DropboxAPI<AndroidAuthSession> mDBApi;
    	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	try
    	{
    		super.onCreate(savedInstanceState);
    		setContentView(R.layout.main);
    		
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

        //DropBox
		if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                // MANDATORY call to complete auth.
                // Sets the access token on the session
                mDBApi.getSession().finishAuthentication();

                AccessTokenPair tokens = mDBApi.getSession().getAccessTokenPair();

                // Store the keys
                storeKeys(tokens.key, tokens.secret);
            } catch (IllegalStateException ex) {
                //Log.i("DbAuthLog", "Error authenticating", e);
            	Context context = getApplicationContext();
        		CharSequence text = ex.toString();
        		int duration = 50000 ; //Toast.LENGTH_LONG;
        		
        		Toast toast = Toast.makeText(context, "Authentication Error: " + text, duration);
        		toast.show();
            }
        }
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
//        		case R.id.load_xml: 
//        			try
//            		{
//            			Intent intent = new Intent(BookListMainAct.this, LoadXML.class);
//            			startActivity(intent);
//            		}
//            		catch(Exception ex)
//            		{
//            			Context context = getApplicationContext();
//            			CharSequence text = ex.toString();
//            			int duration = Toast.LENGTH_LONG ;
//            			
//            			Toast toast = Toast.makeText(context, text, duration);
//            			toast.show();
//            		}
//            		break;
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
    
    //Create an anonymous implementation of OnClickListener for getting an updated file
    private OnClickListener mAddListenerGetFile = new OnClickListener() {
	
    	public void onClick(View v){
    		new getFile().execute();
		
    	}
    	};

    
	private class getFile extends AsyncTask<Void, Integer, Boolean> 
	{
		private ProgressDialog dialog = new ProgressDialog(BookListMainAct.this);
		Exception err = null;
		
		protected void onPreExecute()
		{
		   super.onPreExecute();
		        this.dialog.setMessage("Loading...");
		        this.dialog.show();    
		}
		
		protected void onPostExecute(Boolean result)
 		{
			if (dialog.isShowing())
 			{
 				dialog.dismiss();
 			}
			
			if (this.err != null)
			{
				Context context = getApplicationContext();
	     		CharSequence text = err.toString();
	     		int duration = 50000 ; //Toast.LENGTH_LONG;
	     		
	     		Toast toast = Toast.makeText(context, text, duration);
	     		toast.show();
			}
 		}
		
		
//		protected void onProgressUpdate(Integer... progress) 
// 		{
// 			OptionPane.showMessage("BookListMainAct, "ERROR", err.getMessage());
// 		}
		
		protected Boolean doInBackground(Void... params) 
		{
			// Get a new XML file to load in the db
			FileOutputStream outputStream = null;

			try 
	 		{
				String fname = "";
		    	outputStream = new FileOutputStream(newXml);
		    	//Since we are using access_type=folder, the file will be in 
		    	// /Apps/BookList2 (which is considered the root for this application
		    	// dbPath = /dcexport.xml 
	 		    DropboxFileInfo info = mDBApi.getFile(dbPath, null, outputStream, null);
	 		    //Log.i("BookListLog", "The file's rev is: " + info.getMetadata().rev);
	 		    // /path/to/new/file.txt now has stuff in it.
//	 		    com.dropbox.client2.DropboxAPI.Entry myEntry = mDBApi.metadata("/",100, null, true, null);
//		    	for(com.dropbox.client2.DropboxAPI.Entry e : myEntry.contents )
//		    	{
//		    		if (!e.isDeleted)
//		    		{
//		    			fname = e.fileName();
//		    		}
//		    	}
	 		    
		    	publishProgress();
	 		} 
			//catch (DropboxException e) 
	 		catch (Exception ex)	
	 		{
//	 			Context context = getApplicationContext();
//	     		CharSequence text = ex.toString();
//	     		int duration = 50000 ; //Toast.LENGTH_LONG;
//	     		
//	     		Toast toast = Toast.makeText(context, text, duration);
//	     		toast.show();
	 			this.err = ex ;
	 		} finally 
	 		{
	 			if (dialog.isShowing())
	 			{
	 				dialog.dismiss();
	 			}
	 		    if (outputStream != null) 
	 		    {
	 		        try 
	 		        {
	 		            outputStream.close();
	 		        } catch (IOException e) {}
	 		    }
	 		}	
	 		return true;
		}
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
    		int duration = 50000 ; //Toast.LENGTH_LONG;
    		
    		Toast toast = Toast.makeText(context, text, duration);
    		toast.show();
    	}
		return counted;
	}
        
    //DropBox
    private void DropBoxAuthenticate()
    {
    	SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
    	//APP_KEY 	= prefs.getString(ACCESS_KEY_NAME, "");
    	//APP_SECRET 	= prefs.getString(ACCESS_SECRET_NAME, "");
    	
    	AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
    	AndroidAuthSession session = new AndroidAuthSession(appKeys, ACCESS_TYPE);
    	mDBApi = new DropboxAPI<AndroidAuthSession>(session);
    	
    	//check if we are already authenticated
    	if (session.getAccessTokenPair() == null)
    	{
    		//Start the Authentication
    		mDBApi.getSession().startAuthentication(BookListMainAct.this);
    	}
    }
  
    //Store the retrieved keys (code from DBRoulette.jar example) - DropBox
    private void storeKeys(String key, String secret) 
    {
        // Save the access key for later
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.putString(ACCESS_KEY_NAME, key);
        edit.putString(ACCESS_SECRET_NAME, secret);
        edit.commit();
    }
}
