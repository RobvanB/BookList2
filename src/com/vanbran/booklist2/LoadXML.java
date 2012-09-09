package com.vanbran.booklist2;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import android.content.Context;
import android.database.Cursor;

public class LoadXML 
{
	
	long 		id 				= 0;
	int  		rowsInserted 	= 0;
	int  		rowsUpdated  	= 0;
	int  		rowsSkipped		= 0;
	int	 		result			= 1;
	Context 	context 		= null ; 
	DBAdapter 	db 				= null ;
	String		msg				= null ;
		
	@SuppressWarnings("finally")
	public String run(Context context)
	{
		db = new DBAdapter(context);
		
		db.open();	
		
		try
		{
			parseXML();
		}
		catch(XmlPullParserException ex)
		{
			msg  = "XML Parser Exception: " + ex.toString() ;
		}
		catch(IOException ex)
		{
			msg = "ParseXML IO Exception: " + ex.toString() ;
		}
		catch(Exception ex)
		{
			msg = "ParseXML Exception: " + ex.toString() ;
		}
		finally
		{
			db.close();
			return msg ;
		}
		
	}
	
	private void parseXML()
	throws XmlPullParserException, IOException
	{
		final File newXml = BookListMainAct.newXml ;		
		//final Activity thisActivity = activity ;
		
		//ProgressBar	pBar ;
				
		//setContentView(R.layout.load);
		
		//pBar = (ProgressBar) findViewById(R.id.ProgressBar);
	
		String titleStr  = "" ;
		String authorStr = "" ;
		String statusStr = "" ;
		String dcId      = "" ;
		String tagName   = "" ;
		
		//Parse the xml
		XmlPullParserFactory  xppf = XmlPullParserFactory.newInstance() ;
		XmlPullParser xpp = xppf.newPullParser();
		xpp.setInput(new FileReader(newXml));
		xpp = nextTag(xpp);
		/*
		 * event types:
		 * END_DOCUMENT = 1
		 * START_TAG = 2
		 * END_TAG = 3
		 * TEXT = 4
		 */
		int eventType = xpp.getEventType();
		try
		{
			while (eventType != XmlPullParser.END_DOCUMENT)
			{
				tagName   = xpp.getName()     ;
				eventType = xpp.getEventType();				
				if (eventType == XmlPullParser.START_TAG)
				{
					if (tagName.equals("author"))
					{
						xpp       = nextTag(xpp);	
						if (xpp.getName().equals("name"))
						{
							xpp.next(); //Get to the content
							authorStr = xpp.getText();
						}
					}
					else if (tagName.equals("title"))
					{
						xpp.next();
						titleStr = xpp.getText();
					}
					else if (tagName.equals("user-short-text-field-2"))
					{	
						xpp.next();
						statusStr = xpp.getText();
					}
					else if (tagName.equals("id"))
					{
						xpp.next();
						dcId   = xpp.getText();
					}
				}	
				if (eventType == XmlPullParser.END_TAG && tagName.equals("book"))
				{
					//Write the record
					this.insert(titleStr, authorStr, statusStr, dcId);
					titleStr  = "";
					authorStr = "";
					statusStr = "";
					dcId	  = "";
				}
				xpp = nextTag(xpp);
			}
		}
		catch(Exception ex)
		{
    		msg = "XML Loop: Error when processing tag " + tagName + ex.toString();
    
		}
		finally
		{
			db.close();
		}
		
		msg = "Upload complete. \n" + rowsSkipped + " Rows Skipped,\n" + rowsInserted 
									+ " Rows Inserted, \n" + rowsUpdated + " Rows Updated." ;
	}
	
	private XmlPullParser nextTag(XmlPullParser _xpp)
	{
		try
		{
			_xpp.next();
			int eventType  = _xpp.getEventType();
			String tagName =  ""                ;
			while (eventType != XmlPullParser.START_TAG && eventType != XmlPullParser.END_DOCUMENT) 
			{
				_xpp.next();
				tagName   = _xpp.getName()     ;	
				eventType = _xpp.getEventType();		
				if (eventType == XmlPullParser.END_TAG && 
					tagName.equals("book")				  )
				{
					break;
				}
			}
		}
		catch(Exception ex)
		{
    		msg = "Next Tag : " + ex.toString();
		}
		return _xpp ;
	}
	
	private void insert(String titleFld, String authorFld, String statusFld, String dcId)
	{
		try
		{
			//If the record exists, update it
			Cursor cursor = db.findByDcId(dcId);
			if (cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				int colId = cursor.getColumnIndexOrThrow("_id");
				id = cursor.getInt(colId);
				colId = cursor.getColumnIndexOrThrow("Author");
				String curAuthor = cursor.getString(colId);
				colId = cursor.getColumnIndexOrThrow("Title");
				String curTitle = cursor.getString(colId);
				colId = cursor.getColumnIndexOrThrow("Status");
				String curStatus = cursor.getString(colId);
				
				if (curAuthor.equals(authorFld) &&
					curTitle.equals(titleFld)   &&
					curStatus.equals(statusFld)    )
				{
					rowsSkipped++;
				}else
				{
					db.updateBook(id, authorFld, titleFld, statusFld, dcId);
					rowsUpdated++ ;
				}
			}
			else
			{
				id = db.insertBook(authorFld, titleFld, statusFld, dcId);
				rowsInserted++;
			}
			
			if (cursor != null)
			{
				cursor.close();	
			}
			
			/*
			Context context = getApplicationContext();
    		CharSequence text = "Record with id " + id + " loaded to DB.";
    		int duration = Toast.LENGTH_SHORT;
    		
    		Toast toast = Toast.makeText(context, text, duration);
    		toast.show();
    		*/			
		}
		catch(Exception ex)
		{
    		msg = "Error Inserting Record: " + ex.toString();
    	}
	}
}	
