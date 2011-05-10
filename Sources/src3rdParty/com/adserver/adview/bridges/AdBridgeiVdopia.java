package com.adserver.adview.bridges;

import com.adserver.adview.Utils;
import com.vdopia.client.android.VDO;
import com.vdopia.client.android.VDOView;

import android.content.Context;
import android.graphics.Color;
import android.opengl.Visibility;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class AdBridgeiVdopia extends AdBridgeAbstract {

	VDOView iVdopiaView;
	
	public AdBridgeiVdopia(Context context, WebView view, String campaignId,
			String externalParams,String trackUrl) {
		super(context, view, campaignId, externalParams, trackUrl);
	}

	public void run() {
		try {
			String applicationKey = Utils.scrape(externalParams, "<param name=\"applicationKey\">", "</param>");
			iVdopiaView = new VDOView(context);
			//iVdopiaView.setLayoutParams(view.getLayoutParams());
	        VDO.initialize(applicationKey, context);
	        VDO.setListener(new VdopiaEventListener());
	        //View a;
	        //a.setV
	        SetOnBeginVisible(iVdopiaView);
	        
	        iVdopiaView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Click();
				}
	        }
			);
	        
	        view.addView(iVdopiaView);	       
			//view.setBackgroundColor(Color.WHITE);
			//view.loadDataWithBaseURL(null, "", "text/html", "UTF-8", null);			
		} catch (Exception e) {
			DownloadError(e.getMessage());
		}
	}
	
	private class VdopiaEventListener implements VDO.AdEventListener {
			
		public void adShown(int type) {			
			DownloadEnd();	
			//iVdopiaView.setLayoutParams(view.getLayoutParams());
			
		}
		
		public void noAdsAvailable(int type, int willCheckAgainAfterSeconds) {
			DownloadError("[ERROR] AdBridgeiVdopia: noAdsAvailable");
		}

		@Override
		public void adStart(int arg0) {
			//DownloadEnd();			
		}	
		
	}

}
