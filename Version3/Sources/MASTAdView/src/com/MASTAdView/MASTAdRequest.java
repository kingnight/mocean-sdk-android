//
// Copyright (C) 2011, 2012 Mocean Mobile. All Rights Reserved. 
//
package com.MASTAdView;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.MASTAdView.core.ContentManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * Class encapsulating all of the data and parameters associated with an ad server request. The key parameters needed
 * to retrieve ads from the back-end (such as site, zone, ad width, ad height, etc) may be set and retrieved via the
 * getProperty() and setProperty() methods below. The Mocean UI back-end supports
 * <A HREF="http://developer.moceanmobile.com/Mocean_Ad_Request_API">a variety of additional parameters</A>.
 * Any of these additional parameters can be set via the "generic" custom parameters object, below.
 */
public class MASTAdRequest
{	
	// required
	
	/**
	 *  The id of the publisher site, obtained from the Mocean customer account representative.
	 *  THIS IS A REQUIRED VALUE.
	 */
	public final static String parameter_site 			= "site";
	
	/**
	 * Publisher zone for an ad placement in an application. Obtained from the Mocean customer
	 * account representative or created via the Mocean UI.
	 * THIS IS A REQUIRED VALUE.
	 */
	public final static String parameter_zone 			= "zone";
	
	/**
	 * User agent string to identify the device/os on which an application is running. This will be set to
	 * the value typically sent by the device web browser by default. Do not change this unless you have a
	 * good reason to do so.
	 */
	public final static String parameter_userAgent 		= "ua";
	
	/**
	 * Maximum width of ad to be requested from server.
	 */
	public final static String parameter_size_x 		= "size_x";
	
	/**
	 * Maximum height of ad to be requested from server.
	 */
	public final static String parameter_size_y 		= "size_y";
	
	/**
	 * SDK version. This is set automatically by the SDK.
	 * DO NOT CHANGE THIS VALUE.
	 */
	public final static String parameter_version 		= "version";
	
	/**
	 * Number of ads to return in an ad request response. Always set to 1 by the SDK.
	 * DO NOT CHANGE THIS VALUE.
	 */
	public final static String parameter_count			= "count";
	
	/**
	 * URL of ad server this ad view will use when communicating with back end.
	 * The default value is set for the Mocean Mobile ad server.
	 * DO NOT CHANGE THIS VALUE UNLESS YOU KNOW WHAT YOU ARE DOING.
	 */
	public final static String parameter_ad_server_url 	= "ad_server_url";
	
	/**
	 * User location latitude value (given in degrees.decimal degrees). Can be set directly
	 * by the app, or can be set from GPS by enabling location detection.
	 */
	public final static String parameter_latitude 		= "lat";
	
	/**
	 * User location longitude value (given in degrees.decimal degrees). Can be set directly
	 * by the app, or can be set from GPS by enabling location detection.
	 */
	public final static String parameter_longitude 		= "long";
	
	/**
	 * Set the type of ads to be returned from the back-end, using 1 or more of the type values
	 * AD_TYPE_TEXt, AD_TYPE_IMAGE, or AD_TYPE_RICHMEDIA from the MASTAdConstants class. Values
	 * can be combined using a binary OR to include more than one type.
	 */
	public final static String parameter_type 			= "type";
	
	/**
	 * Timeout value for ad request handling on the back-end server side.
	 */
	public final static String parameter_ad_call_timeout = "timeout";
	
	/**
	 * Test mode flag, for requesting test ads from server.
	 */
	public final static String parameter_test 			= "test";
	
	/**
	 * Custom parameter map which can be used to send arbitrary key/value pairs to the back-end.
	 * The custom parameter value is a Map<String,String> managed by the application developer. 
	 */
	public final static String parameter_custom = "custom_parameters";
	
	
	private Map<String, String> parameters = new HashMap<String, String>();
	private Map<String, String> customParameters;
	
	
	private MASTAdLog AdLog;
	private Context appContext;
	private String adserverURL = MASTAdConstants.adserverURL;
	
	
	/**
	 * Construct an ad server request object
	 * @param AdLog Logging object for diagnostic information
	 * @param appContext Application context
	 */
	public MASTAdRequest(MASTAdLog AdLog, Context appContext)
	{
		this.AdLog = AdLog;
		this.appContext = appContext;
	}

	
	/**
	 * Return the value for the named property.
	 * @param name Name of property to return, using one of the defined parameter values in this class.
	 * @return Object (usually a string, except in the case of custom_parameters) requested.
	 */
	public synchronized Object getProperty(String name)
	{
		if ((name == null) || (name.length() < 1))
		{
			AdLog.log(AdLog.LOG_LEVEL_3, AdLog.LOG_TYPE_ERROR, MASTAdConstants.STR_INVALID_PARAM, "null name");
			return null; // can't work with unnamed parameter
		}
		
		if (name.compareTo(parameter_ad_server_url) == 0)
		{
			return adserverURL;
		}
		else if (name.compareTo(parameter_custom) == 0)
		{
			return customParameters;
		}
		else
		{
			return parameters.get(name);
		}
	}

	
	/**
	 * Get named property as an Integer value.
	 * @param name Name of property to return, using one of the defined parameter values in this class.
	 * @param defaultValue Default value to return if the named property has not been set.
	 * @return Value for named parameter, or the default value if not yet set.
	 */
	public Integer getProperty(String name, Integer defaultValue)
	{
		String value = (String)getProperty(name);
		
		if (value != null)
		{
			return Integer.parseInt(value);
		}
		else
		{
			return defaultValue;
		}
	}
	
	
	/**
	 * Get named property as a Boolean value.
	 * @param name Name of property to return, using one of the defined parameter values in this class.
	 * @param defaultValue Default value to return if the named property has not been set.
	 * @return Value for named parameter, or the default value if not yet set.
	 */
	public Boolean getProperty(String name, Boolean defaultValue)
	{
		String value = (String)getProperty(name);
		
		if (value != null)
		{
			if (value.compareTo(MASTAdConstants.STRING_TRUE) == 0)
			{
				return true;
			}

			return false;
		}
		else
		{
			return defaultValue;
		}
	}
	
	
	private synchronized boolean setPropertyInternal(String name, String value)
	{
		boolean result = false;
		
		System.out.println("setPropertyInternal: name=" + name + ", value=" + value); // XXX
		
		if (name.compareTo(parameter_count) == 0)
		{
			result = false;
		}
		else if (name.compareTo(parameter_ad_server_url) == 0)
		{
			adserverURL = value;
			result = true;	
		}
		else
		{	
			parameters.put(name, value);
			result = true;
		}
		
		return result;
	}
	
	
	/**
	 * Set (string) value for a named property.
	 * @param name Name of property to set, using one of the defined parameter values in this class.
	 * @param value Value for property.
	 * @return True if set successfully, false otherwise.
	 */
	public boolean setProperty(String name, String value)
	{
		if ((name == null) || (name.length() < 1))
		{
			AdLog.log(AdLog.LOG_LEVEL_3, AdLog.LOG_TYPE_ERROR, MASTAdConstants.STR_INVALID_PARAM, "null name");
			return false;
		}
		
		if (!validate(name, value))
		{
			return false;
		}
		
		return setPropertyInternal(name, value);
	}
	
	
	/**
	 * Set named property using Map<String,String> parameter; only used for custom_parameter. 
	 * @param name Name of property to set, using one of the defined parameter values in this class.
	 * @param value Value for property, as a Map<String,String>.
	 * @return True if set successfully, false otherwise.
	 */
	public synchronized boolean setProperty(String name, Map<String, String> value) 
	{
		if ((name == null) || (name.length() < 1))
		{
			AdLog.log(AdLog.LOG_LEVEL_3, AdLog.LOG_TYPE_ERROR, MASTAdConstants.STR_INVALID_PARAM, "null name");
			return false;
		}
		
		if (name.compareTo(parameter_custom) == 0)
		{
			customParameters = value;
			return true;
		}
		
		return false;
	}
	
	
	/**
	 * Set (integer) value for a named property.
	 * @param name Name of property to set, using one of the defined parameter values in this class.
	 * @param value Value for property.
	 * @return True if set successfully, false otherwise.
	 */
	public boolean setProperty(String name, Integer value)
	{
		if ((name == null) || (name.length() < 1))
		{
			AdLog.log(AdLog.LOG_LEVEL_3, AdLog.LOG_TYPE_ERROR, MASTAdConstants.STR_INVALID_PARAM, "null name");
			return false;
		}
		
		if (!validate(name, value))
		{
			return false;
		}
			
		if (value == null)
		{
			return setPropertyInternal(name, null); // remove
		}
		else
		{
			return setPropertyInternal(name, value.toString());
		}
	}
	
	
	/**
	 * Set (boolean) value for a named property.
	 * @param name Name of property to set, using one of the defined parameter values in this class.
	 * @param value Value for property.
	 * @return True if set successfully, false otherwise.
	 */
	public boolean setProperty(String name, Boolean value)
	{
		if ((name == null) || (name.length() < 1))
		{
			AdLog.log(AdLog.LOG_LEVEL_3, AdLog.LOG_TYPE_ERROR, MASTAdConstants.STR_INVALID_PARAM, "null name");
			return false;
		}
		
		if (!validate(name, value))
		{
			return false;
		}
		
		if ((value != null) && value)
		{
			return setPropertyInternal(name, MASTAdConstants.STRING_TRUE);
		}
		else
		{
			if (name.compareTo(parameter_test) == 0)
			{
				return setPropertyInternal(name, null); // remove
			}
		}
		
		return false;
	}
	
	
	
	// XXX move to strings???
	private final static String error_not_null = "must not be null";
	private final static String error_not_empty = "most not be empty";
	private final static String error_over_zero = "must be > 0";
	
	
	
	private boolean validate(String name, Boolean value)
	{
		if (value != null)
		{
			return true;
		}
		
		return false;
	}
	
	
	private boolean validate(String name, String value)
	{
		if (name.compareTo(parameter_ad_server_url) == 0)
		{
			if ((value == null) || (value.length() < 1))
			{
				AdLog.log(AdLog.LOG_LEVEL_3, AdLog.LOG_TYPE_ERROR, MASTAdConstants.STR_INVALID_PARAM, name + ": " + error_not_empty);
				return false;
			}
		}
		else if (name.compareTo(parameter_userAgent) == 0)
		{
			if (value == null)
			{
				AdLog.log(AdLog.LOG_LEVEL_3, AdLog.LOG_TYPE_ERROR, MASTAdConstants.STR_INVALID_PARAM, name + ": " + error_not_null);
				return false;
			}
		}
		else if ((name.compareTo(parameter_latitude) == 0) ||
				 (name.compareTo(parameter_longitude) == 0))
		{
			if (value != null)
			{
				double lon = -1000;
				try
				{
					lon = Double.parseDouble(value);
				}
				catch(Exception e) { }
				
				if ((lon < -90) || (lon > 90))
				{
					AdLog.log(AdLog.LOG_LEVEL_3, AdLog.LOG_TYPE_ERROR, MASTAdConstants.STR_INVALID_PARAM, name + ": valid: -90<=double<=90");
					return false;
				}
			}
		}
		
		return true;
	}
	
	
	private boolean validate(String name, Integer value)
	{
		if (name.compareTo(parameter_site) == 0)
		{
			if ((value == null) || (value < 1))
			{
				AdLog.log(AdLog.LOG_LEVEL_3, AdLog.LOG_TYPE_ERROR, MASTAdConstants.STR_INVALID_PARAM, name + ": " + error_over_zero);
				return false;
			}
		}
		else if (name.compareTo(parameter_zone) == 0)
		{
			if ((value == null) || (value < 1))
			{
				AdLog.log(AdLog.LOG_LEVEL_3, AdLog.LOG_TYPE_ERROR, MASTAdConstants.STR_INVALID_PARAM, name + ": " + error_over_zero);
				return false;
			}
		}
		else if (name.compareTo(parameter_count) == 0)
		{
			if ((value == null) || (value < 1))
			{
				AdLog.log(AdLog.LOG_LEVEL_3, AdLog.LOG_TYPE_ERROR, MASTAdConstants.STR_INVALID_PARAM, name + ": " + error_over_zero);
				return false;
			}
		}
		else if (name.compareTo(parameter_type) == 0)
		{
			if ((value == null) || (value < 0) || (value > 8))
			{
				AdLog.log(AdLog.LOG_LEVEL_3, AdLog.LOG_TYPE_ERROR, MASTAdConstants.STR_INVALID_PARAM, name + ": valid: 1<=int<=7, 1 - text, 2 - image, 4 - richmedia ad, set combinations as sum of this values");
				return false;
			}
		}
		else if (name.compareTo(parameter_size_x) == 0)
		{
			if ((value != null) && (value < 1))
			{
				AdLog.log(AdLog.LOG_LEVEL_3, AdLog.LOG_TYPE_ERROR, MASTAdConstants.STR_INVALID_PARAM, name + ": " + error_over_zero);
				return false;
			}
		}
		else if (name.compareTo(parameter_size_y) == 0)
		{
			if ((value != null) && (value < 1))
			{
				AdLog.log(AdLog.LOG_LEVEL_3, AdLog.LOG_TYPE_ERROR, MASTAdConstants.STR_INVALID_PARAM, name + ": " + error_over_zero);
				return false;
			}
		}
		
		return true;
	}
	
	
	/**
	 * Creates URL for default (XML) data format using parameters set by caller (and/or default values)
	 * @return URL string
	 * @throws IllegalStateException if all the required parameters are not present.
	 */
	public String toString()
	{
		return toString(MASTAdConstants.AD_REQUEST_TYPE_XML);
	}
	
	
	/**
	 * Creates URL for specified data format using parameters set by caller (and/or default values)
	 * @return URL string
	 * @throws IllegalStateException if all the required parameters are not present.
	 */
	public String toString(int type)
	{
		StringBuilder builderToString = new StringBuilder();
	
		synchronized(this)
		{
			builderToString.append(adserverURL);
		}
		
		// The key parameter tells the back-end what format to use when returning data;
		// see MASTAdConstants.AD_REQUEST_TYPE_zzz
		builderToString.append("?count=1&key=" + type); 
		builderToString.append(ContentManager.getInstance(null).getAutoDetectParameters());
		
		synchronized(this)
		{
			appendParameters(builderToString, parameters);
			appendParameters(builderToString, customParameters);
		}

		return  builderToString.toString();
	}

	
	private void appendParameters(StringBuilder builderToString, Map<String, String> parameters)
	{
		if(parameters != null)
		{
			Set<String> keySet = parameters.keySet();
			
			for (Iterator<String> parameterNames = keySet.iterator(); parameterNames.hasNext();)
			{
				String param = parameterNames.next();
				String value = parameters.get(param);
	
				if (value != null) {
					builderToString.append("&" + URLEncoder.encode(param) + "=");
					builderToString.append(URLEncoder.encode(value));
				}
			}
		}
	}	
}