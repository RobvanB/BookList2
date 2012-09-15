/*
Developed by Rob vanBrandenburg (RobvanB@gmail.com)
More info can be found at the Project page (https://github.com/RobvanB/BookList2)
Check out the Wiki for documentation. 
Please report bugs and feature requests. 
Icons provided by AndroidIcons.

In order to not have my Application Key and Secret in the code I am using
a config file. THe config file is not included in the project.
If you want to use this code, make sure to create an xml file calles \res\config.xml
and put a <AK> and and <AS> field in there for your app key and app secret
*/
package com.vanbran.booklist2;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.widget.Toast;

public class LoadConfig
{
	private static final String APPKEYTAG = "ak";
	private static final String APPSECTAG = "as";
	private static String APPKEY = "" ;
	private static String APPSEC = "" ;

	public void init(Context _context)
	{
		XmlResourceParser parser = _context.getResources().getXml(R.xml.config);

		try 
		{
		    int eventType = parser.getEventType();

		    while (eventType != XmlPullParser.END_DOCUMENT) {
		        String name = null;

		        switch (eventType){
		            case XmlPullParser.START_TAG:
		                name = parser.getName().toLowerCase();

		                if (name.equals(APPKEYTAG)) 
		                {
		                	parser.next();
		                    APPKEY = parser.getText();
		                }else if (name.equals(APPSECTAG))
		        		{
		                	parser.next();
		        			APPSEC = parser.getText();
		        		}
		                break;
		            case XmlPullParser.END_TAG:
		                name = parser.getName();
		                break;
		        }
		        eventType = parser.next();
		    }
		}
		catch (XmlPullParserException ex) 
		{
			CharSequence text = "LoadConfig XML Parser Exception: " + ex.toString() ;
			int duration = 500000; //Toast.LENGTH_LONG ;

			Toast toast = Toast.makeText(_context, text, duration);
			toast.show();
		}
		catch (IOException ex) {
			CharSequence text = "LoadConfig IO Exception: " + ex.toString() ;
			int duration = 500000; //Toast.LENGTH_LONG ;

				Toast toast = Toast.makeText(_context, text, duration);
				toast.show();
			}
			finally 
			{
			    parser.close();
			}
	}

	public String AppKey()
	{
		return APPKEY;
	}

	public String AppSec()
	{
		return APPSEC;
	}
}