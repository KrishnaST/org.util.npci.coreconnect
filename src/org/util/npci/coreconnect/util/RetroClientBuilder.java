package org.util.npci.coreconnect.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.util.nanolog.RetroLoggingInterceptor;
import org.util.nanolog.RetroLoggingInterceptor.Level;

import com.fasterxml.jackson.annotation.JsonIgnore;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public final class RetroClientBuilder {

	private String                        baseURL;
	private long                          readTimeout    = 20;
	private long                          writeTimeout   = 5;
	private long                          connectTimeout = 5;
	private Level                         level;
	
	@JsonIgnore private Converter.Factory factory        = JacksonConverterFactory.create();
	@JsonIgnore private List<Interceptor> interceptors   = new ArrayList<Interceptor>();

	private RetroClientBuilder() {
	}

	public static final RetroClientBuilder newBuilder() {
		return new RetroClientBuilder();
	}

	public final RetroClientBuilder baseURL(final String baseURL) {
		this.baseURL = baseURL;
		return this;
	}

	public final RetroClientBuilder readTimeout(int readTimeout, TimeUnit timeUnit) {
		if (readTimeout != 0) this.readTimeout = timeUnit.toSeconds(readTimeout);
		return this;
	}

	public final RetroClientBuilder writeTimeout(int writeTimeout, TimeUnit timeUnit) {
		if (writeTimeout != 0) this.writeTimeout = timeUnit.toSeconds(writeTimeout);
		return this;
	}

	public final RetroClientBuilder connectTimeout(int connectTimeout, TimeUnit timeUnit) {
		if (connectTimeout != 0) this.connectTimeout = timeUnit.toSeconds(connectTimeout);
		return this;
	}

	public final RetroClientBuilder addInterceptor(Interceptor interceptor) {
		if (interceptor != null) this.interceptors.add(interceptor);
		return this;
	}

	public final RetroClientBuilder withLogging(final Level level) {
		this.level = level;
		return this;
	}

	public final RetroClientBuilder withLogging(final String level) {
		try {
			this.level = Level.valueOf(level);
		} catch (Exception e) {
			this.level = Level.BASIC;
		}
		return this;
	}

	public final Retrofit build() {
		System.out.println(this);
		final OkHttpClient.Builder okhttpBuilder = new OkHttpClient.Builder();
		if (this.level != null) {
			final RetroLoggingInterceptor logging = new RetroLoggingInterceptor();
			logging.setLevel(this.level);
			okhttpBuilder.addInterceptor(logging);
		}
		for (Interceptor interceptor : interceptors) okhttpBuilder.addInterceptor(interceptor);
		okhttpBuilder.connectTimeout(connectTimeout, TimeUnit.SECONDS).readTimeout(readTimeout, TimeUnit.SECONDS).writeTimeout(writeTimeout, TimeUnit.SECONDS);
		return new Retrofit.Builder().client(okhttpBuilder.build()).baseUrl(baseURL).addConverterFactory(factory).build();

	}

	@Override
	public String toString() {
		return "RetroClientBuilder [baseURL=" + baseURL + ", readTimeout=" + readTimeout + ", writeTimeout=" + writeTimeout + ", connectTimeout=" + connectTimeout + ", level="
				+ level + ", factory=" + factory + ", interceptors=" + interceptors + "]";
	}

}
