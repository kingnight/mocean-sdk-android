package com.adserver.adview.bridges;

import java.io.IOException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.adserver.adview.AdLog;
import com.adserver.adview.AdServerView;
import com.adserver.adview.AdServerViewCore.OnAdClickListener;
import com.adserver.adview.AdServerViewCore.OnAdDownload;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;

public abstract class AdBridgeAbstract implements Runnable{
		Context context;
		WebView view;
		String campaignId;
		String externalParams;
		String trackUrl;
		AdLog AdLog;
		private OnAdDownload onAdDownload;
		private OnAdClickListener onAdClickListener;
		private boolean IsDownloadEnd = false;
		private boolean IsDownloadError = false;
	
		public AdBridgeAbstract(Context context, WebView view, AdLog AdLog, String campaignId, String externalParams,String trackUrl)
		{
			this.context=context;
			this.view=view;
			this.campaignId=campaignId;
			this.externalParams=externalParams;		
			this.trackUrl = trackUrl;
			this.AdLog=AdLog;
		}	
		
		public void OnAdDownload(OnAdDownload adDownload)
		{
			this.onAdDownload = adDownload;
		}
		
		public void OnAdClickListener(OnAdClickListener adClickListener)
		{
			this.onAdClickListener = adClickListener;
		}
		
		void DownloadEnd()
		{
			if(!IsDownloadEnd)
			{	
				IsDownloadEnd =true;
				if(onAdDownload !=null) onAdDownload.end((AdServerView)view);
			}
		}
		
		void DownloadError(String error)
		{
			if(!IsDownloadError)
			{
				AdLog.log(AdLog.LOG_LEVEL_2, AdLog.LOG_TYPE_ERROR, "DownloadError", error);
				IsDownloadError =true;
				if(onAdDownload !=null) onAdDownload.error((AdServerView)view,error);
			}
		}
		
		void Click()
		{
			AdLog.log(AdLog.LOG_LEVEL_3, AdLog.LOG_TYPE_INFO, "Click", trackUrl);
			if((trackUrl != null) && (trackUrl.length() > 0)) {
				try {
					sendGetRequest(trackUrl);
				} catch (IOException e) {
					AdLog.log(AdLog.LOG_LEVEL_1, AdLog.LOG_TYPE_ERROR, "Click", e.getMessage());
					e.printStackTrace();
				}
			}
			/*if (onAdClickListener != null)
			{
				onAdClickListener.click(trackUrl);
			}*/
		}
		
		private static void sendGetRequest(String url) throws IOException {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(url);
			client.execute(get);
		}
		
		void SetOnBeginVisible(View v)
		{
			v.setLayoutParams(view.getLayoutParams());
			//v.setVisibility(View.GONE);
			
		}
		
		void SetOnEndVisible(View v)
		{
			
		}
		
		public static boolean IsAvailable()
		{
			return  false;
		}
		
		static boolean IsClassExist(String className)
		{
			Class obj;
			try {
				obj = Class.forName(className);
			} catch (ClassNotFoundException e) {
				return false;
			}
			return obj!=null;
		}
}
