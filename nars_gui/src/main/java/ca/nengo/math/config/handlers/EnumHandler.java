/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "EnumHandler.java". Description:
"ConfigurationHandler for SimulationMode values"

The Initial Developer of the Original Code is Bryan Tripp & Centre for Theoretical Neuroscience, University of Waterloo. Copyright (C) 2006-2008. All Rights Reserved.

Alternatively, the contents of this file may be used under the terms of the GNU
Public License license (the GPL License), in which case the provisions of GPL
License are applicable  instead of those above. If you wish to allow use of your
version of this file only under the terms of the GPL License and not to allow
others to use your version of this file under the MPL, indicate your decision
by deleting the provisions above and replace  them with the notice and other
provisions required by the GPL License.  If you do not delete the provisions above,
a recipient may use your version of this file under either the MPL or the GPL License.
*/

/*
 * Created on 17-Dec-07
 */
package ca.nengo.math.config.handlers;

import ca.nengo.math.config.ui.ConfigurationChangeListener;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * ConfigurationHandler for SimulationMode values.
 *
 * @author Bryan Tripp
 */
public class EnumHandler extends BaseHandler {

	private final Enum<?> myDefaultValue;

	/**
	 * Defaults to type Enum with null default value.
	 */
	public EnumHandler() {
		this(Enum.class, null);
	}

	/**
	 * @param type Type handled by this handler
	 * @param defaultValue Default value for this handler
	 */
	public EnumHandler(Class<?> type, Enum<?> defaultValue) {
		super(type);
		myDefaultValue = defaultValue;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Component getEditor(Object o, ConfigurationChangeListener listener, JComponent parent) {
		Enum<?> mode = (Enum<?>) o;
		List<? extends Enum<?>> all = new ArrayList<Enum<?>>(EnumSet.allOf(mode.getClass()));
		final JComboBox result = new JComboBox(all.toArray());
		result.setSelectedItem(mode);

		listener.setProxy(new ConfigurationChangeListener.EditorProxy() {
			public Object getValue() {
				return result.getSelectedItem();
			}
		});
		result.addActionListener(listener);

		return result;
	}

	@Override
	public Object fromString(String s) {
		throw new RuntimeException("Can't get Enum instance from String (expected to get values from a combo box)");
	}

	/**
	 * @see ca.nengo.math.config.ConfigurationHandler#getDefaultValue(java.lang.Class)
	 */
	public Object getDefaultValue(Class<?> c) {
		return myDefaultValue;
	}

}
