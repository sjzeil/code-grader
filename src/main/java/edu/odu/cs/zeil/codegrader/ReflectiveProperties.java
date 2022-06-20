/**
 * 
 */
package edu.odu.cs.zeil.codegrader;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;


/**
 * @author zeil
 *
 */
public class ReflectiveProperties { // NOPMD by zeil on 4/7/20, 10:24 AM
	
	/**
	 * Message for unknown properties.
	 */
	private static final String UNKNOWN_PROP_MSG = "Could not identify a property named ";


	/**
	 * Set a data member value by name
	 * @param propertyName
	 * @param propertyValue
	 */
	public final void setByReflection(final String propertyName, final String propertyValue) {
		final Class<? extends ReflectiveProperties> cls = getClass();

		if (propertyName.contains(".")) {
			setNestedProperty(propertyName, propertyValue, cls);
		} else {
			setSimpleProperty(propertyName, propertyValue, cls);
		}
	}


	private void setSimpleProperty(final String propertyName, final String propertyValue,
			final Class<? extends ReflectiveProperties> cls) {
		try {
			final Field dataMember = cls.getField(propertyName);
			final Class<?> fieldType = dataMember.getType(); 
			setSimplePropertyFromString(propertyName, propertyValue, dataMember, fieldType);
		} catch (NoSuchFieldException | SecurityException e) {
			throw new IllegalArgumentException(UNKNOWN_PROP_MSG + propertyName); // NOPMD by zeil on 4/7/20, 10:46 AM
		}
	}


	private void setSimplePropertyFromString(final String propertyName, final String propertyValue, final Field dataMember,
			final Class<?> fieldType) {
		if (fieldType.equals(String.class)) {
			setStringProperty(propertyName, propertyValue, dataMember);
		} else {
			setNonStringProperty(propertyName, propertyValue, dataMember, fieldType);
		}
	}


	private void setNonStringProperty(final String propertyName, final String propertyValue, 
			final Field dataMember,
			final Class<?> fieldType) {
		try {
			final Constructor<?> constructor = fieldType.getConstructor(String.class);
			final Object value = constructor.newInstance(propertyValue);
			dataMember.set(this, value);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalArgumentException ("Could not assign an appropriate value to property " + propertyName); // NOPMD by zeil on 4/7/20, 10:49 AM
		}
	}


	private void setStringProperty(final String propertyName, final String propertyValue, final Field dataMember) {
		try {
			dataMember.set(this, propertyValue);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new IllegalArgumentException ("Could not assign a string value to property " + propertyName); // NOPMD by zeil on 4/7/20, 10:50 AM
		}
	}


	private void setNestedProperty(final String propertyName, final String propertyValue,
			final Class<? extends ReflectiveProperties> cls) {
		final int dotPos = propertyName.indexOf('.');
		final String outerPropertyName = propertyName.substring(0, dotPos);
		final String innerPropertyName = propertyName.substring(dotPos+1); // NOPMD by zeil on 4/7/20, 10:54 AM
		try {
			final Field dataMember = cls.getField(outerPropertyName);
			final Class<?> fieldType = dataMember.getType();
			if (ReflectiveProperties.class.isAssignableFrom(fieldType)) {
				final ReflectiveProperties innerProperties = (ReflectiveProperties) dataMember.get(this);
				innerProperties.setByReflection(innerPropertyName, propertyValue);
			} else {
				throw new IllegalArgumentException("Cannot initialize " + propertyName + " from a string.");
			}
		} catch (NoSuchFieldException | SecurityException e) {
			throw new IllegalArgumentException(UNKNOWN_PROP_MSG + outerPropertyName); // NOPMD by zeil on 4/7/20, 10:54 AM
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new IllegalArgumentException("Could not access properties at " + outerPropertyName); // NOPMD by zeil on 4/7/20, 10:54 AM
		}
	}


	/**
	 * Fetch a data member value by name
	 * @param propertyName
	 * @returns property value
	 */
	public Object getByReflection(final String propertyName) {
		final Class<? extends ReflectiveProperties> cls = getClass();
		
		Object result = null; // NOPMD by zeil on 4/7/20, 10:57 AM
		if (propertyName.contains(".")) {
			result = getNestedProperty(propertyName, cls);
		} else {
			result = getSimpleProperty(propertyName, cls);
		}
		return result;
	}


	private Object getSimpleProperty(final String propertyName, 
			final Class<? extends ReflectiveProperties> cls) {
		Field dataMember;
		try {
			dataMember = cls.getField(propertyName);
			return dataMember.get(this);
		} catch (NoSuchFieldException | SecurityException e) {
			throw new IllegalArgumentException(UNKNOWN_PROP_MSG + propertyName); // NOPMD by zeil on 4/7/20, 10:57 AM
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new IllegalArgumentException("Could not fetch the property " + propertyName); // NOPMD by zeil on 4/7/20, 10:57 AM
		}
	}


	private Object getNestedProperty(final String propertyName, final Class<? extends ReflectiveProperties> cls) {
		final int firstDotPos = propertyName.indexOf('.');
		final String outerPropertyName = propertyName.substring(0, firstDotPos);
		final String innerPropertyName = propertyName.substring(firstDotPos+1); // NOPMD by zeil on 4/7/20, 11:00 AM
		Field dataMember;
		try {
			dataMember = cls.getField(outerPropertyName);
			final Class<?> fieldType = dataMember.getType();
			if (ReflectiveProperties.class.isAssignableFrom(fieldType)) {
				final ReflectiveProperties innerProperties = (ReflectiveProperties) dataMember.get(this);
				return innerProperties.getByReflection(innerPropertyName);
			} else {
				throw new IllegalArgumentException(outerPropertyName + " is not a nested property set.");
			}
		} catch (NoSuchFieldException | SecurityException e) {
			throw new IllegalArgumentException(UNKNOWN_PROP_MSG + outerPropertyName); // NOPMD by zeil on 4/7/20, 11:00 AM
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new IllegalArgumentException("Could not access properties at " + outerPropertyName); // NOPMD by zeil on 4/7/20, 11:00 AM
		}
	}


}
