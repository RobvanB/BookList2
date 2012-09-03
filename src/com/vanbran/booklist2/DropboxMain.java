///*Developed by Rob vanBrandenburg. 
//More info can be found at my Project page. 
//Check out the Wiki for documentation. 
//Please report bugs and feature requests. 
//Icons provided by AndroidIcons.
//https://github.com/RobvanB/BookList
//*/
//
//package com.vanbran.booklist2;
//
//import java.io.BufferedInputStream;
//import java.io.BufferedOutputStream;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.content.SharedPreferences.Editor;
//import android.os.Bundle;
//import android.os.Environment;
////import android.util.Config;
//import android.util.Log;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
////import com.dropbox.client.DropboxAPI;
////import com.dropbox.client.DropboxAPI.Config;
////import com.dropbox.client.DropboxAPI.FileDownload;
//
//import com.dropbox.client2.DropboxAPI;
//import com.dropbox.client2.android.AndroidAuthSession;
//import com.dropbox.client2.session.AccessTokenPair;
//import com.dropbox.client2.session.AppKeyPair;
//import com.dropbox.client2.session.Session.AccessType;
//import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
//import com.dropbox.client2.exception.*;
//
//public class DropboxMain extends Activity 
//{
//    private static final String TAG = "BookListDropboxMain";
//
//    static private String CONSUMER_KEY 			= "";
//    static private String CONSUMER_SECRET 		= "";    
//    final static private String dbPath 			= "/dcexport.xml";
//    final static private AccessType ACCESS_TYPE	= AccessType.DROPBOX;
//       
////    final static public String ACCOUNT_PREFS_NAME 	= "prefs";
////    final static public String ACCESS_KEY_NAME 		= "ACCESS_KEY";
////    final static public String ACCESS_SECRET_NAME 	= "ACCESS_SECRET";
//
//    private Button mSubmit;
//    private TextView mText;
//    private DropboxAPI<AndroidAuthSession> mDBApi;
//    
//    /** Called when the activity is first created. */
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.dblogin);                
////        mLoginEmail 		= (EditText)findViewById(R.id.login_email);
////        mLoginPassword 		= (EditText)findViewById(R.id.login_password);
//        mSubmit 			= (Button)findViewById(R.id.login_submit);
//        mText 				= (TextView)findViewById(R.id.text);
//
//        //LoadConfig loadC 	= new LoadConfig();
//        //loadC.init(getApplicationContext());
//        //CONSUMER_KEY		= loadC.ConsKey() ;
//        //CONSUMER_SECRET		= loadC.ConsSec() ;
//
//        //Dropbox API
//        AppKeyPair appKeys = new AppKeyPair(CONSUMER_KEY, CONSUMER_SECRET);
//        AndroidAuthSession session = new AndroidAuthSession(appKeys, ACCESS_TYPE);
//        mDBApi = new DropboxAPI<AndroidAuthSession>(session);
//        mDBApi.getSession().startAuthentication(DropboxMain.this);
//        //
//        
//        mSubmit.setOnClickListener(new OnClickListener() 
//        {
//            public void onClick(View v) 
//            {
//            	// Try to log in
//            	//getAccountInfo();
//            	
//            	//Get the file we need and send the returncode
//                setResult(getDCFile());
//                finish();
//            }
//        });
//        
//        
//        
////        String[] keys = getKeys();
////        if (keys != null) 
////        {
////        	setLoggedIn(true);
////        	Log.i(TAG, "Logged in already");
////        }else 
////        {
////        	setLoggedIn(false);
////        	Log.i(TAG, "Not logged in");
////        }
////        
////        if (authenticate()) 
////        {
////        	// We can query the account info already, since we have stored 
////        	// credentials
////        	getAccountInfo();
////        	setResult(getDCFile());
////            finish();
////        }
//    }
//
//   /**
//     * Convenience function to change UI state based on being logged in
//     */
////    public void setLoggedIn(boolean loggedIn) {
////    	mLoggedIn = loggedIn;
////    	mLoginEmail.setEnabled(!loggedIn);
////    	mLoginPassword.setEnabled(!loggedIn);
////    	if (loggedIn) {
////    		mSubmit.setText("Log Out of Dropbox");
////    	} else {
////    		mSubmit.setText("Log In to Dropbox");
////    	}
////    }
//
//    public void showToast(String msg) {
//        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
//        error.show();
//    }
//    
//    protected void onResume() {
//        super.onResume();
//
//        // ...
//
//        if (mDBApi.getSession().authenticationSuccessful()) {
//            try {
//                // MANDATORY call to complete auth.
//                // Sets the access token on the session
//                mDBApi.getSession().finishAuthentication();
//
//                AccessTokenPair tokens = mDBApi.getSession().getAccessTokenPair();
//
//                // Provide your own storeKeys to persist the access token pair
//                // A typical way to store tokens is using SharedPreferences
//                //storeKeys(tokens.key, tokens.secret);
//            } catch (IllegalStateException e) {
//                Log.i("DbAuthLog", "Error authenticating", e);
//            }
//        }
//
//        // ...
//    }   
//       
//    public int getDCFile()
//    {
//    	//BufferedInputStream br = null;
//		//BufferedOutputStream bw = null;
//		int	returnCode = 0;
//
//		//final File newXml = BookListMainAct.newXml ;
//		
//		File tmpFile = Environment.getExternalStorageDirectory() ;
//		
//		final File newXml = new File(Environment.getExternalStorageDirectory() , "dcandroidexport.xml"); 
//		
//		FileOutputStream outputStream = null;
//		
//		try {
////				if (!newXml.exists()) 
////				{
////					newXml.createNewFile(); //otherwise dropbox client will fail silently
////				}
//
////			    File file = new File(newXml);
////			    outputStream = new FileOutputStream(newXml);
//			    DropboxFileInfo info = mDBApi.getFile("/testing.txt", null, outputStream, null);
//			  //Log.i("DC Import Log", "The file's rev is: " + info.getMetadata().rev);
//			    // /path/to/new/file.txt now has stuff in it.
//				} catch (DropboxException e) {
//				    Log.e("DC IMport Log", "Something went wrong while downloading.");
////				} catch (FileNotFoundException e) {
////				    Log.e("DC Import Log", "File not found.");
////				} catch(IOException e){
////					Log.e("DC Import Log", "IO Exception.");
//				}finally {
//				    if (outputStream != null) {
//				        try {
//				            outputStream.close();
//				        } catch (IOException e) {}
//				    }
//				}
//				
//				
//				
////				FileDownload fd = mDBApi.getFileStream("dropbox", dbPath, null);
////				br = new BufferedInputStream(fd.is);
////				bw = new BufferedOutputStream(new FileOutputStream(newXml));
////				
////				byte[] buffer = new byte[4096];
////				int read;
////				while (true) 
////				{
////					read = br.read(buffer);
////					if (read <= 0) 
////					{
////						break;
////					}
////					bw.write(buffer, 0, read);
////					returnCode = 1;
////				}
////		} 
////		catch (Exception ex)
////    	{
////    		Context context = getApplicationContext();
////    		CharSequence text = "GetDCFile 1 " + ex.toString();
////    		int duration = 50000 ; //Toast.LENGTH_LONG;
////    		
////    		Toast toast = Toast.makeText(context, text, duration);
////    		toast.show();
////    	}
////		finally 
////		{
////			try
////			{
////				if (bw != null) 
////				{
////					bw.close();
////				}
////				if (br != null) 
////				{
////					br.close();
////				}
////			}
////			catch (Exception ex)
////	    	{
////	    		Context context = getApplicationContext();
////	    		CharSequence text = "GetDCFile 2 " + ex.toString();
////	    		int duration = 50000 ; //Toast.LENGTH_LONG;
////	    		
////	    		Toast toast = Toast.makeText(context, text, duration);
////	    		toast.show();
////	    	}
////		}
//	return returnCode;
//	}    
//    
//}
