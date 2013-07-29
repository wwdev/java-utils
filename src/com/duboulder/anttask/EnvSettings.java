package com.duboulder.anttask;

import java.util.*;
import org.apache.tools.ant.*;

/**
 * Environment settings collection.
 */
public class EnvSettings extends EnvSettingBase implements EnvSetting {
	private List<EnvSetting>	_settings;
	
	public EnvSettings () {
		_settings = new ArrayList<EnvSetting> ();
		setSpaceAfter (1);
	}

	public void addConfigured (EnvSetting setting) {
		_settings.add (setting);
	}

	@Override
	public void show(Project project) {
		if (!isEnabled ()) return;
		project.log ("============================================================");
		project.log (getName ());
		project.log ("============================================================");

		int i = 0;
		for (EnvSetting setting : _settings) {
			setting.show (project);
			
			// Add the spacing lines a
			if (setting.getSpaceAfter () > 0 && ++i < _settings.size ()) {
				for (int j=0; j<setting.getSpaceAfter (); j++)
					project.log ("");
			}
		}
	}

	@Override
	public String check(Project project) {
		if (!isEnabled () || !isChecked ()) return null;
		String errMsg = null;

		for (EnvSetting setting : _settings) {
			String msg = setting.check (project);
			if (msg == null) continue;

			if (errMsg == null)
				errMsg = msg;
			else
				errMsg += "\n" + msg;
		}

		return errMsg;
	}	
}
