package com.adserver.adview.samples.simple;

import com.adserver.adview.AdServerView;
import com.adserver.adview.samples.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class Video extends Activity {
    /** Called when the activity is first created. */
	private LinearLayout linearLayout;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        linearLayout = (LinearLayout) findViewById(R.id.frameAdContent);
         
        AdServerView adserverView = new AdServerView(this,8061,16109);
        adserverView.setUpdateTime(30000);
        adserverView.setMinSizeX(320);
	    adserverView.setMinSizeY(50);
	    adserverView.setMaxSizeX(320);
	    adserverView.setMaxSizeY(50);
        adserverView.setId(1);
        adserverView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 200));
		linearLayout.addView(adserverView);
    }
}