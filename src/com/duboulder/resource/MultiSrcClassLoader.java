package com.duboulder.resource;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

/**
 * A class loader that attempts to find a class from multiple
 * class loaders. This is to help deal with the varied class
 * loader configurations that application containers have.<br/><br/>
 * 
 * The class loaders that are used for loading a class are:<br/>
 * <div style="padding-left: 18pt">
 *    Thread.currentThread ().getContextClassLoader ()
 *    parent
 *    this.getClass ().getClassLoader ()
 *    ClassLoader.getSystemClassLoader ()
 * </div>
 */
public class MultiSrcClassLoader extends ClassLoader {
	private boolean				_verbose;
	private List<ClassLoader>	_loaders;
	private Map<String,Class<?>> _classes;

	public MultiSrcClassLoader (boolean verbose, ClassLoader parent) {
		super (null);
		_verbose = verbose;
		_loaders = new ArrayList<ClassLoader> (4);
		_classes = new ConcurrentHashMap<String,Class<?>> ();

		// The class loaders to try
		ClassLoader[] loaderList = {
			Thread.currentThread ().getContextClassLoader (),
			(parent == null ? null : parent),
			this.getClass ().getClassLoader (),
			ClassLoader.getSystemClassLoader ()
		};

		// Make the set of loaders be unique
		for (ClassLoader cl : loaderList) {
			if (cl == null) continue;
			if (_loaders.contains (cl)) continue;

			_loaders.add (cl);

			if (verbose) {
				PrintStream out = System.err;
				if (_loaders.size() == 1)
					out.println (this.getClass ().getName () + " using class loaders:");
				out.println ("    " + cl.getClass ().getName ());
				out.flush ();
			}
		}
		
		if (_loaders.isEmpty ())
			throw new RuntimeException ("No class loaders -- should not happen");
	}

	/**
	 * Whether to output to System.err during operations.
	 * @return true if output is generated
	 */
	public boolean getVerbose () { return _verbose; }

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
//		if (_verbose) {
//			System.err.println ("MultiSrcClassLoader: findClass (" + name + ")");
//			System.err.flush ();
//		}

		Class<?> cls = _classes.get (name);
		if (cls != null) return cls;

		synchronized (this) {
			// Test again since the test done before is not
			// part of the protected update (not atomic)
			cls = _classes.get (name);
			if (cls != null) return cls;

			cls = zLoadClass (name);
			if (cls != null) _classes.put (name, cls);
			return cls;
		}
	}

	// findClass is protected so we can't delegate from there. This is one
	// method we can delegate but we can't really do anything with the resolve
	// parameter since we can't pass it on.
	@Override
	public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
//		if (_verbose) {
//			System.err.println (
//				"MultiSrcClassLoader: loadClass (" + name + ", " + resolve + ")"
//			);
//			System.err.flush ();
//		}

		return zLoadClass (name);
	}

	@Override
	public InputStream getResourceAsStream(String name) {
//		if (_verbose) {
//			System.err.println (
//				"MultiSrcClassLoader: getResourceAsStream (" + name + ")"
//			);
//			System.err.flush ();
//		}

		InputStream is = null;
		for (ClassLoader loader : _loaders) {
			is = loader.getResourceAsStream (name);
			if (is != null) {
				if (_verbose) {
					System.err.println (
						"MutilSrcClassLoader: resource stream for " + name + " from " +
							loader.getClass ().getName ()
					);
					System.err.flush ();					
				}
				break;
			}
		}
		return is;
	}

	@Override
	protected URL findResource(String name) {
//		if (_verbose) {
//			System.err.println (
//				"MultiSrcClassLoader: findResource (" + name + ")"
//			);
//			System.err.flush ();
//		}

		URL url = null;
		for (ClassLoader loader : _loaders) {
			url = loader.getResource (name);
			if (url != null) {
				if (_verbose) {
					System.err.println (
						"MutilSrcClassLoader: found resource URL " + name + " from " +
							loader.getClass ().getName ()
					);
					System.err.flush ();
				}
				break;
			}
		}
		return url;
	}

	@Override
	protected Enumeration<URL> findResources(String name) throws IOException {
//		if (_verbose) {
//			System.err.println (
//				"MultiSrcClassLoader: findResources (" + name + ")"
//			);
//			System.err.flush ();
//		}

		List<URL> urls = new LinkedList<URL> ();
		for (ClassLoader loader : _loaders) {
			Enumeration<URL> loaderUrls = loader.getResources (name);
			while (loaderUrls.hasMoreElements ()) {
				urls.add (loaderUrls.nextElement ());
			}
		}

		return Collections.enumeration(urls);
	}

	private Class<?> zLoadClass (String name)
		throws ClassNotFoundException
	{
		ClassNotFoundException e = null;
		for (ClassLoader loader : _loaders) {
			try {
				Class<?> cls = loader.loadClass (name);
				if (cls != null) {
					if (_verbose) {
						System.err.println (
							"MutilSrcClassLoader: loaded class: " + name + " from " +
								loader.getClass ().getName ()
						);
						System.err.flush ();
					}
					return cls;
				}
			}
			catch (ClassNotFoundException nf) {
				if (e == null) e = nf;
			}
		}
		
//		if (_verbose) {
//			System.err.println (
//				"MultSrcClassLoader: error loading " + name + 
//					(e == null || e.getMessage () == null ? "" : 
//						":\n    " + e.getMessage ())
//			);
//			System.err.flush ();
//		}

		throw e;
	}
}
