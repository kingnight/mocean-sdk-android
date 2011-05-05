package com.adserver.adview.bridges;

//import checkpoint.forms.R;

import com.adserver.adview.Utils;
import com.greystripe.android.sdk.BannerListener;
import com.greystripe.android.sdk.BannerView;
import com.greystripe.android.sdk.GSSDK;

import android.content.Context;
import android.graphics.Color;
import android.webkit.WebView;

public class AdBridgeGreyStripe extends AdBridgeAbstract {

	public AdBridgeGreyStripe(Context context, WebView view, String campaignId,
			String externalParams,String trackUrl) {
		super(context, view, campaignId, externalParams, trackUrl);
	}

	public void run() {
		try {
			String applicationId = Utils.scrape(externalParams, "<param name=\"id\">", "</param>");
		    GSSDK.initialize(context, applicationId);
		    BannerView myBanner = new BannerView(context);
		    myBanner.setLayoutParams(view.getLayoutParams());
		    myBanner.addListener(new GreyStripeBannerListener());
	        view.addView(myBanner);
			view.setBackgroundColor(Color.WHITE);
			view.loadDataWithBaseURL(null, "", "text/html", "UTF-8", null);
	        myBanner.refresh();
		} catch (Exception e) {
		}
	}
	
	private class GreyStripeBannerListener implements BannerListener {
		public void onReceivedAd(BannerView bannerView) {
		
		}

		public void onFailedToReceiveAd(BannerView bannerView) {
			//DownloadError(context.getString(R.string.grey_stripe_download_error));
		}
	}

}