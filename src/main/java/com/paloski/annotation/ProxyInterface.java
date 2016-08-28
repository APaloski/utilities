package com.paloski.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A marker annotation used to denote that an interface is reflectively proxied by a tool such as <a
 * href="http://square.github.io/retrofit/">Retrofit</a> or <a href="https://github.com/OpenFeign/feign">OpenFeign</a>.
 * <p/>
 *
 * @author Adam
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Documented
public @interface ProxyInterface {
	/**
	 * The class that provides the proxies for this interface. Usually this would be {@code Retrofit} or {@code Feign}
	 */
	Class<?>[] value();
}
