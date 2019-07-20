package org.util.npci.coreconnect.util;

public final class Locker<T> {

	public final T request;
	public T       response;

	public Locker(T request) {
		this.request = request;
	}

}
