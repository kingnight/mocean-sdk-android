//
// Copyright (C) 2011, 2012 Mocean Mobile. All Rights Reserved. 
//
package com.MASTAdView.core;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;


public class AdDialogFactory
{
	private Context context;
	private Dialog  dialog;
	
	
	public AdDialogFactory(Context context)
	{
		this.context = context;
	}
	
	
	protected ViewGroup.LayoutParams createContainerLayoutParameters(final DialogOptions options)
	{
		return new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
	}
	
	
	protected ViewGroup.LayoutParams createAdLayoutParameters(final DialogOptions options)
	{
		if ((options != null) && (options.width != null) && (options.height != null))
		{
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(options.width, options.height);
			layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
			layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
			return layoutParams;
		}
		else
		{
			return new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
		}
	}
	
	
	protected RelativeLayout.LayoutParams createCloseLayoutParameters(final DialogOptions options)
	{
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		return layoutParams;
	}
	
	
	public Dialog createDialog(final View ad, final DialogOptions options)
	{
		if ((options != null) && (options.hideTitlebar != null) && (options.hideTitlebar))
		{
			dialog = new Dialog(context, android.R.style.Theme_NoTitleBar_Fullscreen);
		}
		else
		{
			dialog = new Dialog(context, android.R.style.Theme_NoTitleBar);
		}
		
		// remove ad view from any other containers (if needed)
		if (ad.getParent() != null)
		{
			((ViewGroup)ad.getParent()).removeView(ad);
		}
		
		// Create container to hold ad and close button
		final RelativeLayout adContainer = new RelativeLayout(context);
		adContainer.setLayoutParams(createContainerLayoutParameters(options));
		
		// Set background color (if any)
		if ((options != null) && (options.backgroundColor != null))
		{
			adContainer.setBackgroundColor(options.backgroundColor);
		}
		
		// Put ad in new container, and use all available space
		ad.setLayoutParams(createAdLayoutParameters(options));
		adContainer.addView(ad);

		if ((options != null) && (options.noClose != null) && (options.noClose == true))
		{
			// skip close setup (for MRAID open method)
		}
		else
		{
			// Setup close button
			Button closeButton = new Button(context);
			
			// Mraid spec requires min. 50 pixel height and width for close area
			closeButton.setMinHeight(50);
			closeButton.setMinWidth(50);
			
			if ((options != null) && (options.closeLabel != null))
			{
				closeButton.setText(options.closeLabel);
			}
			else
			{
				closeButton.setText("Close"); // XXX string
			}
			
			// If a custom close button is provided by the creative, make our button transparent but still active
			if ((options != null) && (options.customClose != null) && (options.customClose))
			{
				closeButton.setText("");
				closeButton.setBackgroundColor(Color.TRANSPARENT);
			}
			
			closeButton.setVisibility(View.VISIBLE);
			closeButton.setLayoutParams(createCloseLayoutParameters(options));
			
			adContainer.addView(closeButton);
			closeButton.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					System.out.println("dialog close click: dismissing...");
					dialog.dismiss();
					
					AdWebView v;
					if (ad instanceof AdWebView)
					{
						v = (AdWebView)ad;
					}
					else
					{
						v = ((AdViewContainer)ad).getAdWebView();
					}
					v.injectJavaScript("mraid.close();");
					
					if (options != null)
					{
						runRunnable(options.closeRunnable); // XXX not needed???
					}
				}
			});
		}
		
		dialog.setContentView(adContainer);
		dialog.setOnDismissListener(new Dialog.OnDismissListener()
		{
			@Override
			public void onDismiss(DialogInterface dialog)
			{
				System.out.println("dialog ondismiss ...");
				//System.out.println("dialog ondismiss: removing views...");
				//adContainer.removeAllViews();
				if (options != null)
				{
					runRunnable(options.dismissRunnable);
				}
			}
		});		

		return dialog;
	}
	
	
	private void runRunnable(Runnable worker)
	{
		if (worker != null)
		{
			worker.run();
		}
	}
	
	
	public Dialog getDialog()
	{
		return dialog;
	}
	
	
	public static class DialogOptions
	{
		Boolean  hideTitlebar		= null; 
		Runnable closeRunnable 		= null;
		Runnable dismissRunnable	= null;
		Integer  backgroundColor	= null;
		Integer	 height 			= null;
		Integer	 width				= null;
		String   closeLabel			= null;
		Boolean  customClose		= null;
		Boolean  noClose			= null;
		//Integer  closeLocation		= null;
	};
}