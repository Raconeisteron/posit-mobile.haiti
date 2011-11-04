package org.hfoss.posit.android.api;

import java.io.InputStream;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.hfoss.posit.android.R;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

public class FindPluginManager {
	private static FindPluginManager sInstance = null; 
	
	private static final String TAG = "FindPluginManager";
	
	private ArrayList<Plugin> plugins = new ArrayList<Plugin>();
	
	// NOTE: When multiple plugins are implemented these should disappear
	//  from here and possibly move to Plugin.java??
	private Activity mMainActivity = null;
	private FindFactory mFindFactory = null;
	private FindDataManager mFindDataManager = null;
	private Class<FindActivity> mFindActivityClass = null;
	private Class<ListFindsActivity> mListFindsActivityClass = null;
	private Class<Activity> mExtraActivityClass = null;
	private Class<Activity> mExtraActivityClass2 = null;
	private Class<Activity> mLoginActivityClass = null;
	
	public static String mPreferences = null;  // Shared preferences XML for Settings
	public static String mMainIcon = null;
	public static String mAddButtonLabel = null;
	public static String mListButtonLabel = null;
	public static String mExtraButtonLabel = null;
	public static String mExtraButtonLabel2 = null;

	private FindPluginManager(Activity activity){
		mMainActivity = activity;
	}
	
	public static FindPluginManager initInstance(Activity activity){
		sInstance = new FindPluginManager(activity);
		sInstance.initFromResource(activity, R.raw.plugins_preferences);
		return sInstance;
	}
	
	public static FindPluginManager getInstance(){
		assert(sInstance != null);
		
		return sInstance;
	}
	
	@SuppressWarnings("unchecked")
	public void initFromResource(Context context, int plugins_xml){		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try{
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputStream istream = context.getResources().openRawResource(plugins_xml);
			Document document = builder.parse(istream);
			XPath xpath = XPathFactory.newInstance().newXPath();
			NodeList plugin_nodes = (NodeList)xpath.evaluate("PluginsPreferences/FindPlugins/Plugin", document, XPathConstants.NODESET);
			for(int ii = 0; ii < plugin_nodes.getLength(); ++ii){
				if(plugin_nodes.item(ii).getAttributes().getNamedItem("active").getTextContent().compareTo("true") == 0){
					String package_name = plugin_nodes.item(ii).getAttributes().getNamedItem("package").getTextContent();
					String find_factory_name = plugin_nodes.item(ii).getAttributes().getNamedItem("find_factory").getTextContent();
					String find_data_manager_name = plugin_nodes.item(ii).getAttributes().getNamedItem("find_data_manager").getTextContent();
					String findactivity_name = plugin_nodes.item(ii).getAttributes().getNamedItem("find_activity_class").getTextContent();
					String listfindsactivity_name = plugin_nodes.item(ii).getAttributes().getNamedItem("list_finds_activity_class").getTextContent();
					String extra_activity_name = plugin_nodes.item(ii).getAttributes().getNamedItem("extra_activity_class").getTextContent();
					String extra_activity_name2 = plugin_nodes.item(ii).getAttributes().getNamedItem("extra_activity_class2").getTextContent();
					String login_activity_name = plugin_nodes.item(ii).getAttributes().getNamedItem("login_activity_class").getTextContent();

					mMainIcon = plugin_nodes.item(ii).getAttributes().getNamedItem("main_icon").getTextContent();
					mAddButtonLabel = plugin_nodes.item(ii).getAttributes().getNamedItem("main_add_button_label").getTextContent();
					mListButtonLabel = plugin_nodes.item(ii).getAttributes().getNamedItem("main_list_button_label").getTextContent();
					mExtraButtonLabel = plugin_nodes.item(ii).getAttributes().getNamedItem("main_extra_button_label").getTextContent();
					mExtraButtonLabel2 = plugin_nodes.item(ii).getAttributes().getNamedItem("main_extra_button_label2").getTextContent();
					mPreferences = plugin_nodes.item(ii).getAttributes().getNamedItem("preferences_xml").getTextContent();

					@SuppressWarnings({ "rawtypes" })
					Class new_class = Class.forName(package_name + "." + find_factory_name);
					mFindFactory = (FindFactory)new_class.getMethod("getInstance", null).invoke(null, null);
					
					new_class = Class.forName(package_name + "." + find_data_manager_name);
					mFindDataManager = (FindDataManager)new_class.getMethod("getInstance", null).invoke(null, null);

					mFindActivityClass = (Class<FindActivity>)Class.forName(package_name + "." + findactivity_name);
					mListFindsActivityClass = (Class<ListFindsActivity>)Class.forName(package_name + "." + listfindsactivity_name);
					mLoginActivityClass = (Class<Activity>)Class.forName(package_name + "." + login_activity_name);
					mExtraActivityClass = (Class<Activity>)Class.forName(package_name + "." + extra_activity_name);
					mExtraActivityClass2 = (Class<Activity>)Class.forName(package_name + "." + extra_activity_name2);
						
					Log.i(TAG,"Loading preferences for Settings Activity");
					SettingsActivity.loadPluginPreferences(mMainActivity, mPreferences);

					// Remove break to load more than one plugin
					break;
				}
			}
		}catch(Exception ex)
		{
			Log.i(TAG, "Failed to load plugin");
			Log.i(TAG, "reason: " + ex.getMessage());
			
			mMainActivity.finish();
		}
	}
		
	public FindFactory getFindFactory(){
		return mFindFactory;
	}
	
	public FindDataManager getFindDataManager(){
		return mFindDataManager;
	}
	
	public Class<FindActivity> getFindActivityClass(){
		return mFindActivityClass;
	}
	
	public Class<ListFindsActivity> getListFindsActivityClass(){
		return mListFindsActivityClass;
	}
	
	public Class<Activity> getExtraActivityClass() {
		return mExtraActivityClass;
	}

	public void setExtraActivityClass(Class<Activity> extraActivityClass) {
		mExtraActivityClass = extraActivityClass;
	}
	

	public Class<Activity> getExtraActivityClass2() {
		return mExtraActivityClass2;
	}

	public void setExtraActivityClass2(Class<Activity> extraActivityClass2) {
		mExtraActivityClass2 = extraActivityClass2;
	}

	public Class<Activity> getLoginActivityClass() {
		return mLoginActivityClass;
	}

	public void setLoginActivityClass(Class<Activity> loginActivityClass) {
		mLoginActivityClass = loginActivityClass;
	}
	
	
	public ArrayList<Plugin> getPlugins(){
		return plugins;
	}

	
	
	
}
