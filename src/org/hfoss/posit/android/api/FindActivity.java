package org.hfoss.posit.android.api;

import org.hfoss.posit.android.plugin.acdivoca.AcdiVocaDbHelper;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

import android.app.Activity;
import android.location.LocationListener;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemClickListener;

public abstract class FindActivity extends OrmLiteBaseActivity<AcdiVocaDbHelper> 
implements OnClickListener, OnItemClickListener, LocationListener {

}
