package com.duboulder.groovy;

import java.io.IOException;

import com.duboulder.resource.*;

public class GroovyInstanceCache<T>
	extends EntityCachingLoader<T>
	implements GroovyInstanceLoader<T>
{
	public GroovyInstanceCache (long lifetime, GroovyInstanceLoader<T> baseLoader) {
		super (lifetime, baseLoader);
	}

	@Override
	public T loadInstance (String path) 
		throws IOException, ResourceNotFoundException 
	{
		return loadEntity (path);
	}
}
