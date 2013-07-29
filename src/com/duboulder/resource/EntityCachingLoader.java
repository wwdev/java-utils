package com.duboulder.resource;

import java.util.*;
import java.io.*;
import java.util.concurrent.*;

/**
 * A thread-safe cache of Entities. The supplied loader
 * is used to load entities when needed. The lifetime property
 * specifies how long an entity can be in the cache before it
 * is reloaded. A value of 0 means forever.
 */
public class EntityCachingLoader<T> implements EntityLoader<T> {
	private static class ZEntityRef<T> {
		long loadTime;
		long expireTime;
		T entity;
		
		ZEntityRef (T entity, long lifetime) {
			this.entity = entity;
			loadTime = System.currentTimeMillis ();
			expireTime = loadTime + lifetime;
		}
	}

	private String 					_name;
	private long					_lifetime; // in milliseconds
	private EntityLoader<T>			_entityLoader;
	private ConcurrentHashMap<String,ZEntityRef<T>> _entities;

	/**
	 * A caching entity loader where cached entities have a specified
	 * lifetime.
	 * @param lifetime the cache residence time in milliseconds
	 * @param entityLoader the loader used for creating entity instances (not null)
	 */
	public EntityCachingLoader (long lifetime, EntityLoader<T> entityLoader) {
		this (null, lifetime, entityLoader);
	}
	/**
	 * A caching entity loader with a specified name. The cached entities have
	 * the specified lifetime.
	 * @param name the cache name (may be null, default is class name)
	 * @param lifetime the cache residence time in milliseconds
	 * @param entityLoader the loader used for creating entity instances (not null)
	 */
	public EntityCachingLoader (String name, long lifetime, EntityLoader<T> entityLoader) {
		if (entityLoader == null)
			throw new NullPointerException ("entity loader is null");
		System.out.flush ();
		System.err.println (
			this.getClass ().getName () + "@" + hashCode () + "\n" + 
				"   lifetime=" + lifetime + "ms\n" +
				"   loader=" + entityLoader.toString ()
		);
		System.err.flush ();

		_name 			= (name == null ? this.getClass ().getName () : name);
		_lifetime 		= lifetime;
		_entityLoader 	= entityLoader;
		_entities 		= new ConcurrentHashMap<String,ZEntityRef<T>> ();
	}

	/**
	 * The maximum amount time in milliseconds an entity can be in the cache 
	 * before being reloaded. Values less than 0 mean forever.
	 * @return the cache lifetime
	 */
	public long getLifetime () { return _lifetime; }
	
	/**
	 * The entity for the populating the cache
	 * @return the entity loader used for populating the cache (never null)
	 */
	public EntityLoader<T> getEntityLoader () { return _entityLoader; }

	@Override
	public String getEffectivePath(String path) {
		return _entityLoader.getEffectivePath (path);
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public Date getLastModified(String resourcePath) {
		return _entityLoader.getLastModified (resourcePath);
	}

	@Override
	public T loadEntity(String path) throws IOException, ResourceNotFoundException {
		if (path == null)
			throw new NullPointerException ("path is null");
		if (path.isEmpty ())
			throw new IllegalArgumentException ("path is empty");

		// Check for an unexpired or unmodified entity
		long reqTime = System.currentTimeMillis ();
		ZEntityRef<T> entityRef = _entities.get (path);
		if (entityRef != null) {
			if (_lifetime <= 0 || reqTime <= entityRef.expireTime)
				return entityRef.entity;

			// Check the load time against the resource's last modified time
			Date lastModified = _entityLoader.getLastModified (path);
			if (lastModified != null) {
				long lmTime = lastModified.getTime(); 
				if (lmTime <= entityRef.loadTime) {
					entityRef.expireTime = reqTime + _lifetime;
					return entityRef.entity;
				}
			}
		}

		// Load/reload the entity - synchronized to prevent races
		// At this point:
		//     entityRef == null ||
		//     (entityRef != null && lifetime > 0 && reqTime > entityRef.expireTime)
		synchronized (this) {
			// Check again since the read/test/update is not being 
			// done atomically - another thread could have done the
			// update in between the read/test and the lock
			entityRef = _entities.get (path);
			if (entityRef != null) {
				if (_lifetime <= 0 || reqTime <= entityRef.expireTime) 
					return entityRef.entity;

				Date lastModified = _entityLoader.getLastModified (path);
				if (lastModified != null) {
					long lmTime = lastModified.getTime(); 
					if (lmTime <= entityRef.loadTime) {
						entityRef.expireTime = reqTime + _lifetime;
						return entityRef.entity;
					}
				}
			}

			T entity = _entityLoader.loadEntity (path);
			_entities.put (path, new ZEntityRef<T> (entity, _lifetime));
			return entity;
		}
	}
}
