package com.duboulder.reflection;

import java.beans.*;
import java.util.*;
import java.math.*;
import java.lang.reflect.*;

/**
 * Utility operations using reflection
 */
public class ReflectionUtils {
	private static final String[] BEAN_PREFIXES = new String[] {
		"get", "is", "has"
	};

	/**
	 * Answer the bean property name for the method name if it follows the bean
	 * naming conventions, null otherwise.
	 * @param methodName the method name to convert (not null, not empty)
	 * @return the corresponding bean name or null
	 */
	public static String GetBeanPropertyName (String methodName) {
		if (methodName == null)
			throw new NullPointerException ("method name is null");
		if (methodName.isEmpty ())
			throw new IllegalArgumentException ("method name is empty");

		// Scan the prefixes for a match, also make sure the method
		// name is longer than the prefix, and that the next character
		// after the prefix is upper case
		for (String prefix : BEAN_PREFIXES) {
			if (!methodName.startsWith (prefix)) continue;

			int pLen = prefix.length ();
			if (methodName.length () < pLen + 1) continue;
			
			if (!Character.isUpperCase (methodName.charAt (pLen))) continue;
			
			if (methodName.length () == pLen + 1)
				return methodName.substring (pLen).toLowerCase ();

			return methodName.substring (pLen, pLen + 1).toLowerCase () +
				   methodName.substring (pLen + 1);
		}

		return null;
	}
	
	/**
	 * Answer a map of the declared methods in the class that match the bean 
	 * naming conventions for property getters.
	 * @param classObj the class to check
	 * @return the map of methods keyed by property name (not method name), may be empty
	 */
	public static Map<String,Method> GetBeanPropertyGetMethods (Class<?> classObj) {
		Map<String,Method> map = new HashMap<String,Method> ();
		Method[] methods = classObj.getDeclaredMethods ();
		for (Method method : methods) {
			// See if we have method conforming to bean naming conventions
			// and that also has no arguments
			String beanName = GetBeanPropertyName (method.getName ());
			if (beanName == null) continue;
			if (method.getParameterTypes().length > 0) continue;

			map.put (beanName, method);
		}

		return map;
	}
	
	/**
	 * Get a method for accessing the named property. Only methods declared
	 * in the class are returned. The property to method name mapping includes
	 * more cases than simple bean properties (more like velocity), the mapping 
	 * is checked in the following order (<Property> is the first-letter 
	 * capitalized form for the property name, <property> is the property name):
	 * 
	 *    get<PropertyName> ()
	 *    is<PropertyName> ()
	 *    has<PropertyName> ()
	 *    <property> ()
	 *    <Property> ()
	 *    get (java.lang.String)
	 *    get (java.lang.Object)
	 *    getProperty (java.lang.String)
	 *    getProperty (java.lang.Object)
	 *    getAttribute (java.lang.String)
	 *    getAttribute (java.lang.Object)
	 *
	 * When the returned methods take an argument, the value is the property 
	 * name as a String. 
	 * @param classObj the class information object
	 * @param propertyName the property's name (not null, not empty)
	 * @return a method or null.
	 */
	public static Method FindGetPropertyMethod (Class<?> classObj, String propertyName) {
		if (propertyName == null)
			throw new NullPointerException ("property name is null");
		if (propertyName.isEmpty ())
			throw new IllegalArgumentException ("property name is empty");

		String ucFirstPropName = (propertyName.length() == 1 ? 
				propertyName.toUpperCase() :
				propertyName.substring (0,1).toUpperCase() +
					propertyName.substring (1)
		);

		Method[] methods = classObj.getDeclaredMethods ();
		for (Method method : methods) {
			// See if we have method conforming to bean naming conventions
			// and that also has no arguments
			String methodName = method.getName ();
			if (methodName.equals ("get" + ucFirstPropName) ||
				methodName.equals ("is" + ucFirstPropName) ||
				methodName.equals ("has" + ucFirstPropName) ||
				methodName.equals (propertyName) ||
				methodName.equals (ucFirstPropName)
			) {
				if (method.getParameterTypes().length == 0)
					return method;
				continue;
			}

			// Check for map-like property access
			if (!methodName.equals ("get") && 
				!methodName.equals ("getProperty") &&
				!methodName.equals ("getAttribute"))
				continue;
			
			// Check number of arguments and for Object or String type
			Class<?>[] paramTypes = method.getParameterTypes ();
			if (paramTypes.length != 1) continue;

			String paramClass = paramTypes[0].getName ();
			if (paramClass.equals ("java.lang.Object") || 
				paramClass.equals ("java.lang.String"))
				return method;
		}

		return null;
	}

	/**
	 * Filter the property descriptors list to remove properties that
	 * might be expensive in time or resources:<br/>
	 * &nbsp;&nbsp;anything prefixed with java.io. or groovy.lang.MetaClass, 
	 * Readers, Writers, and Streams.
	 * @param properties the property descriptors to process (not null)
	 * @return the original list if no properties were removed, or a new list
	 * with the remaining properties.
	 */
	public static PropertyDescriptor[] FilterProperties (PropertyDescriptor[] properties) {
		if (properties == null)
			throw new NullPointerException ("properties is null");
		List<PropertyDescriptor> filtered = new ArrayList<PropertyDescriptor> (properties.length);
		for (PropertyDescriptor property : properties) {
			if (property == null) continue;
			if (property.getPropertyType() == null) continue;
			String typeName = property.getPropertyType ().getName ();

			if (typeName.equals ("java.lang.Class")) continue;
			if (typeName.startsWith ("java.io.")) continue;
			if (typeName.startsWith ("java.lang.reflect.")) continue;
			if (typeName.startsWith ("org.mortbay.jetty.")) continue;
			if (typeName.startsWith ("org.hibernate.")) continue;
			if (typeName.startsWith ("groovy.lang.MetaClass")) continue;
			if (typeName.endsWith ("Reader")) continue;
			if (typeName.endsWith ("Writer")) continue;
			if (typeName.endsWith ("Stream")) continue;

			filtered.add (property);
		}

		// See if there is a change in size
		if (properties.length == filtered.size ())
			return properties;

		return filtered.toArray (new PropertyDescriptor[filtered.size ()]);
	}

	/**
	 * Like FindGetPropertyMethod except that super classes are also
	 * searched.
	 * @param classObj the class information object
	 * @param propertyName the property's name (not null, not empty)
	 * @return a method or null.
	 */
	public static Method FindGetPropertyMethodSuper (Class<?> classObj, String propertyName) {
		Class<?> currClass = classObj;
		while (currClass != null) {
			Method method = FindGetPropertyMethod (currClass, propertyName);
			if (method != null) return method;
			currClass = currClass.getSuperclass ();
		}
		return null;
	}

	/**
	 * Answer whether the the value is a simple value that has a compact display
	 * for its complete state (e.g. scalars, strings).<br/>
	 * <br/>
	 * The null value and
	 * instances of Boolean, Character, Byte, Short, Integer, Long, Float, Double,
	 * BigInteger, BigDecimal, Date, String, Enum<?>, and Class<?> are considered 
	 * as simple. All others are not.
	 * @param v the value to check
	 * @return true if the value qualifies as simple
	 */
	public static boolean IsSimpleValue (Object v) {
		if (v == null) return true;
		
		if (v instanceof Boolean ||
			v instanceof Character ||
			v instanceof Byte ||
			v instanceof Short ||
			v instanceof Integer ||
			v instanceof Long ||
			v instanceof Float ||
			v instanceof Double ||
			v instanceof BigInteger ||
			v instanceof BigDecimal ||
			v instanceof Date ||
			v instanceof String ||
			v instanceof Enum<?> ||
			v instanceof Class<?>
		)
			return true;

		return false;
	}
	/**
	 * Get a class object by name trying several class loaders. The
	 * class loader search order is:<pre>
	     1 - the class loader associated with the context instance
	     		context.getClass ().getClassLoader ()
	     2 - the current thread's context class loader
	     		Thread.currentThread ().getContextClassLoader ()
	     3 - the system class loader
	     		ClassLoader.getSystemClassLoader ()
	   </pre>
	 * @param context the context object whose class loader will be used (not null)
	 * @param className the binary class name to use (not null, not empty)
	 * @return the class instance or null if no class could be loaded
	 */
	public static Class<?> GetClassByName (Object context, String className) {
		if (context == null)
			throw new NullPointerException ("caller is null");
		if (className == null)
			throw new NullPointerException ("className is null");
		if (className.isEmpty ())
			throw new IllegalArgumentException ("className is empty");

		// Primitive types
		if (className.equals ("boolean")) return Boolean.TYPE;
		if (className.equals ("char"))    return Character.TYPE;
		if (className.equals ("byte"))    return Byte.TYPE;
		if (className.equals ("short"))   return Short.TYPE;
		if (className.equals ("int"))     return Integer.TYPE;
		if (className.equals ("long"))    return Long.TYPE;
		if (className.equals ("float"))   return Float.TYPE;
		if (className.equals ("double"))  return Double.TYPE;

		// Normal object classes
		ClassLoader[] classLoaders = new ClassLoader[3];
		classLoaders[0] = context.getClass ().getClassLoader ();
		classLoaders[1] = Thread.currentThread ().getContextClassLoader ();
		classLoaders[2] = ClassLoader.getSystemClassLoader ();
		
		for (ClassLoader classLoader : classLoaders) {
			try {
				return classLoader.loadClass (className);
			}
			catch (ClassNotFoundException e) {
				// continue the search
			}
		}

		return null;
	}

	/**
	 * Create an object instance for the class using the no-args constructor. 
	 * The optional ThrowableHolder will have the error if the instance could not be created.
	 * @param classObject the class to create an instance of (not null)
	 * @param th where instance creation exceptions are store (may be null)
	 * @return the newly created instance or null.
	 */
	public static Object CreateInstance (Class<?> classObject, ThrowableHolder th) {
		if (classObject == null)
			throw new NullPointerException ("class object is null");

		try {
			if (th != null)
				th.throwable = null;
			return classObject.newInstance ();
		}
		catch (Exception e) {
			if (th != null)
				th.throwable = e;
		}

		return null;
	}

	/**
	 * Create an object instance for the specified class name.<br/>
	 * GetClassByName loads the class object and then the class instance
	 * is created as if by calling CreateInstance (classObj, th).
	 * <br/> 
	 * The optional ThrowableHolder will have the error if the instance could not be created.
	 * @param context the context object whose class loader will be used (not null)
	 * @param className the binary class name to use (not null, not empty)
	 * @param th where instance creation exceptions are store (may be null)
	 * @return the newly created instance or null.
	 */
	public static Object CreateInstance (Object context, String className, ThrowableHolder th) {
		Class<?> classObject = GetClassByName (context, className);
		if (classObject == null) {
			if (th != null)
				th.throwable = new ClassNotFoundException (
					"Could not load class '" + className + "' using any class loader"
				);
			return null;
		}
		return CreateInstance (classObject, th);
	}

	/**
	 * Create an instance of a class by name. The class is looked up via
	 * GetClassByName.
	 * @param context the context object whose class loader will be used (not null)
	 * @param className the binary class name to use (not null, not empty)
	 * @return the newly created instance
	 * @Throws ReflectionException if the class lookup or instance create fails 
	 * 		(this is an unchecked exception)
	 */
	public static Object CreateInstance (Object context, String className) {
		Class<?> classObject = GetClassByName (context, className);
		if (classObject == null)
			throw new ReflectionException (
				"could not load class for '" + className + "'"
			);

		try {
			return classObject.newInstance();
		}
		catch (Exception e) {
			throw new ReflectionException (
				"error creating instance of '" + className + "'" +
					(e.getMessage () == null ? "" : ":\n    " + e.getMessage ()),
				e
			);
		}
	}
	
	/**
	 * Locate the public method of the object&apos;s class with the given name and accepting
	 * the specified argument type. The exact method returned depends upon the
	 * jvm. If the object instance is a runtime class object, the object instance is used
	 * as the class to accomplish a static method lookup.
	 * @param  the object instance whose class to use or a runtime class (not null)
	 * @param methodName the method name (not null, not empty)
	 * @param argTypes the argument types (may be null, may be empty)
	 * @return the method object
	 * @Throws ReflectionException if the no method is matched or there are
	 * 		access/security issues (this is an unchecked exception)
	 */
	public static Method GetMethod (
		Object objInstance, String methodName, Class<?>... argTypes
	) {
		if (objInstance == null)
			throw new NullPointerException ("objInstance is null");
		if (methodName == null)
			throw new NullPointerException ("methodName is null");
		if (methodName.isEmpty ())
			throw new IllegalArgumentException ("methodName is empty");

		Class<?> objClass = null;
		if (!(objInstance instanceof Class<?>))
			objClass = objInstance.getClass ();
		else
			objClass = (Class<?>) objInstance;

		try {
			return objClass.getMethod (methodName, argTypes);
		}
		catch (NoSuchMethodException e) {
			String types = zzDumpTypes (argTypes);
			String matchMethods = "";
			for (Method curMethod : objClass.getMethods ()) {
				if (!curMethod.getName ().equals (methodName)) continue;
				matchMethods += "\n    " + zzDumpMethod (curMethod);
			}

			throw new ReflectionException (
				"Error locating method " + methodName + " (" + types + ") on class '" +
					objClass.getName () + "' of object " + objInstance.toString () + 
					"\nPublic methods:" + matchMethods,
				e
			);
		}
	}

	/**
	 * Invoke the method represented by the method argument. The object argument
	 * is used as the object instance used with the Method.Invoke mehtod.
	 * @param obj the object instance on which to invoke the method (not null)
	 * @param method the method runtime representation (not null)
	 * @param args the method arguments (may be null, may be empty)
	 * @return the return value of the invoked method or null if there is return value
	 * @throws ReflectionException if there is an error invoking the method, or if the
	 * 		method execution throws an exception (this is an unchecked exception)
	 */
	public static Object InvokeMethod (Object obj, Method method, Object...args) {
		if (obj == null)
			throw new NullPointerException ("obj is null");
		if (method == null)
			throw new NullPointerException ("method is null");
		try {
			return method.invoke(obj, args);
		}
		catch (IllegalAccessException e1){
			throw new ReflectionException (
				"method access error for method '" + method.getDeclaringClass ().getName () + 
					"." + method.getName () + "' of object '" + obj.toString (),
				e1
			);
		}
		catch (IllegalArgumentException e2) {
			throw new ReflectionException (
				"illegal argument error for method '" + method.getDeclaringClass ().getName () + 
					"." + method.getName () + " (" + zzDumpArgs (args) + ")" + 
					" of object " + obj.toString (),
				e2
			);
		}
		catch (InvocationTargetException e3) {
			throw new ReflectionException (
				"illegal argument error for method '" + method.getDeclaringClass ().getName () + 
					"." + method.getName () + " (" + zzDumpArgs (args) + ")" + 
					" of object " + obj.toString (),
				e3
			);
		}
	}

	/**
	 * Invoke the method represented by the method argument. The object argument
	 * is used as the object instance used with the Method.Invoke mehtod.
	 * @param obj the object instance on which to invoke the method (not null)
	 * @param method the method runtime representation (not null)
	 * @param args the method arguments (may be null, may be empty)
	 * @return the return value of the invoked method or null if there is return value
	 * @throws ReflectionException if there is an error invoking the method, or if the
	 * 		method execution throws an exception (this is an unchecked exception)
	 */
	public static Object InvokeClassMethod (Method method, Object...args) {
		if (method == null)
			throw new NullPointerException ("method is null");
		try {
			return method.invoke (null, args);
		}
		catch (IllegalAccessException e1){
			throw new ReflectionException (
				"method access error for method '" + method.getDeclaringClass ().getName () + 
					"." + method.getName (),
				e1
			);
		}
		catch (IllegalArgumentException e2) {
			throw new ReflectionException (
				"illegal argument error for method '" + method.getDeclaringClass ().getName () + 
					"." + method.getName () + "' of object '",
				e2
			);
		}
		catch (InvocationTargetException e3) {
			throw new ReflectionException (
				"illegal argument error for method '" + method.getDeclaringClass ().getName () + 
					"." + method.getName () + "' of object '",
				e3
			);
		}
	}

	/**
	 * Find the constructor for the given class name that accepts the given argument types.
	 * @param context the object to use as a class loader source (not null)
	 * @param className the name of the class (not null, not empty)
	 * @param argTypes the constructor argument types (may be null, may be empty)
	 * @return the runtime constructor instance
	 * @throws ReflectionException if there is a problem loading the class or finding
	 * 		a constructor (this is an unchecked exception)
	 */
	public static Constructor<?> GetConstructor (Object context, String className, Class<?>... argTypes) {
		if (className == null)
			throw new NullPointerException ("className is null");
		if (className.isEmpty ())
			throw new IllegalArgumentException ("className is empty");

		Class<?> objClass = GetClassByName (context, className);
		if (objClass == null)
			throw new ReflectionException (
				"class not found for name '" + className + "'"
			);

		try {
			return objClass.getConstructor (argTypes);
		}
		catch (NoSuchMethodException e) {
			String types = zzDumpTypes (argTypes);
			String ctors = "";
			for (Constructor<?> ctor : objClass.getConstructors ()) {
				ctors += "\n    " + zzDumpCtor (ctor);
			}

			throw new ReflectionException (
				"Error locating constructor for class '" + objClass.getName () +
				    " (" + types + ")" + "\nPublic constructors:" + ctors,
				e
			);
		}
	}

	public static Object InvokeConstructor (Constructor<?> constructor, Object... argValues) {
		if (constructor == null)
			throw new NullPointerException ("constructor is null");

		try {
			return constructor.newInstance (argValues);
		}
		catch (IllegalAccessException e1){
			throw new ReflectionException (
				"constructor access error for class '" + 
					constructor.getDeclaringClass ().getName () + "' " +
					zzDumpCtor (constructor),
				e1
			);
		}
		catch (InvocationTargetException e2) {
			throw new ReflectionException (
				"constructor execution error for class " + 
					constructor.getDeclaringClass ().getName () + 
						" (" + zzDumpArgs (argValues) + ")",
				e2
			);
		}
		catch (InstantiationException e3) {
			throw new ReflectionException (
				"class instantiation error for class '" + 
					constructor.getDeclaringClass ().getName () + 
					" (" + zzDumpArgs (argValues) + ")",
				e3
			);
		}
		catch (IllegalArgumentException e4){
			throw new ReflectionException (
				"illegal argument error for class '" + 
					constructor.getDeclaringClass ().getName () + "' " +
					zzDumpCtor (constructor) + " from (" + zzDumpArgs (argValues) + ")",
				e4
			);
		}
	}

	/**
	 * Answer a readable representation for the constructor
	 * @param ctor the constructor to format (npot null)
	 * @return the formatted string
	 */
	public static String zzDumpCtor (Constructor<?> ctor) {
		if (ctor == null)
			throw new NullPointerException ("ctor is null");

		return ctor.getName () + 
			" (" + zzDumpTypes (ctor.getParameterTypes ()) + ")";
	}

	/**
	 * Answer a readable representation for the method
	 * @param method the method to format (not null)
	 * @return the formatted string
	 */
	public static String zzDumpMethod (Method method) {
		if (method == null)
			throw new NullPointerException ("method is null");
		return method.getName () + 
			" (" + zzDumpTypes (method.getParameterTypes ()) + ")";
	}

	/**
	 * Answer a readable representation for the array of argument types. 
	 * @param argTypes the argument types (may null, may be empty)
	 * @return the empty string if the types are null or the formatted string
	 */
	public static String zzDumpTypes (Class<?>[] argTypes) {
		if (argTypes == null) return "";
		String answer = "";
		for (Class<?> argType : argTypes) {
			if (!answer.isEmpty ()) answer += ", ";
			if (argType == null)
				answer += "null";
			else
				answer += argType.getName ();
		}

		return answer;
	}
	
	public static String zzDumpArgs (Object[] argValues) {
		if (argValues == null) return "";

		String answer = "";
		for (Object arg : argValues) {
			if (!answer.isEmpty ()) answer += ", ";
			if (arg == null) {
				answer += "null";
				continue;
			}
			if (arg instanceof String) {
				String str = (String) arg;
				int l = str.length ();
				if (l >= 30)
					answer += "\"" + str.substring (0, 15) +
						"..." + str.substring (l - 15) + "\"";
				else
					answer += "\"" + str + "\"";
				continue;
			}
			Class<?> objClass = arg.getClass ();
			if (objClass.isArray ()) {
				answer += "ARRAY: " + objClass.getComponentType ().getName () +
					"[" + Array.getLength (arg) + "] {";
				for (int i=0; i<Array.getLength (arg); i++) {
					Object v = Array.get (arg, i);
					if (v == null) {
						answer += " null";
						continue;
					}
					if (v instanceof String) {
						answer += " \"" + ((String) v) + "\"";
						continue;
					}
					answer += " " + v.toString () + " (" +
						v.getClass ().getName () + "@" + 
						Integer.toBinaryString (v.hashCode ()) + ")";
				}
				answer += " }";
				continue;
			}
			answer += arg.toString () + "(" + arg.getClass ().getName () + ")";
		}

		return answer;
	}
}
