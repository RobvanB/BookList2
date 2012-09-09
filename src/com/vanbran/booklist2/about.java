/*
Developed by Rob vanBrandenburg (RobvanB@gmail.com)
More info can be found at the Project page (https://github.com/RobvanB/BookList2)
Check out the Wiki for documentation. 
Please report bugs and feature requests. 
Icons provided by AndroidIcons.
*/
package com.vanbran.booklist2;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class about extends Activity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		WebView webView = new WebView(this);
		setContentView(webView);
		
		String url = "file:///android_asset/about.html";
		webView.loadUrl(url) ;
	}	
}	

