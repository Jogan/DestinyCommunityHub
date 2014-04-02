package com.opencabinetlabs.destinycommunityhub;

import javax.inject.Inject;

import timber.log.Timber;
import android.app.Application;

import com.opencabinetlabs.destinycommunityhub.api.IPersistenceManager;
import com.opencabinetlabs.destinycommunityhub.modules.ApiModule;
import com.opencabinetlabs.destinycommunityhub.modules.AppModule;
import com.opencabinetlabs.destinycommunityhub.modules.DaoModule;
import com.opencabinetlabs.destinycommunityhub.modules.OttoModule;
import com.opencabinetlabs.destinycommunityhub.util.ReleaseLogger;
import com.opencabinetlabs.destinycommunityhub.BuildConfig;

import dagger.ObjectGraph;

public class CommunityHubApp extends Application {

	private ObjectGraph mObjectGraph;

	@Inject
	IPersistenceManager mPersistenceManager;

	@Override
	public void onCreate() {
		super.onCreate();

		if (BuildConfig.DEBUG) {
			//Default logger
			Timber.plant(new Timber.DebugTree());
		} else {
			Timber.plant(new ReleaseLogger(getClass().getSimpleName()));
		}

		mObjectGraph = ObjectGraph.create(getDiModules());
		mObjectGraph.injectStatics();
	}

	/**
	 * The base set of DI modules to inject app components with
	 */
	private Object[] getDiModules() {
		return new Object[]{
				new AppModule(this),
				new OttoModule(),
				new DaoModule(),
				new ApiModule(),
		};
	}

	public ObjectGraph objectGraph() {
		return mObjectGraph;
	}

	/**
	 * Inject the given object
	 *
	 * @param obj 	       The object to inject
	 * @param extraModules Any additional modules to include in the injection
	 */
	public void inject(Object obj, Object... extraModules) {
		ObjectGraph og = mObjectGraph;
		if (extraModules != null && extraModules.length > 0) {
			og = mObjectGraph.plus(extraModules);
		}
		og.inject(obj);
	}
	
	

}
